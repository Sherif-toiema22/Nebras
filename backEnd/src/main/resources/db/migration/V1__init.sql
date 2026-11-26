-- نبراس: مخطط قاعدة البيانات (PostgreSQL 15+)
-- الأدوار: STUDENT, TEACHER, PARENT, ADMIN

CREATE TABLE users (
  id            BIGSERIAL PRIMARY KEY,
  role          VARCHAR(10) NOT NULL CHECK (role IN ('STUDENT','TEACHER','PARENT','ADMIN')),
  full_name     VARCHAR(120) NOT NULL,
  email         VARCHAR(160) UNIQUE NOT NULL,
  phone         VARCHAR(20),
  password_hash VARCHAR(100) NOT NULL,
  active        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE classrooms (
  id         BIGSERIAL PRIMARY KEY,
  name       VARCHAR(80) NOT NULL,          -- الصف الثالث الثانوي - ٣/٢
  teacher_id BIGINT NOT NULL REFERENCES users(id)
);

CREATE TABLE teams (
  id           BIGSERIAL PRIMARY KEY,
  classroom_id BIGINT NOT NULL REFERENCES classrooms(id),
  name         VARCHAR(60) NOT NULL
);

CREATE TABLE students (
  user_id      BIGINT PRIMARY KEY REFERENCES users(id),
  classroom_id BIGINT NOT NULL REFERENCES classrooms(id),
  team_id      BIGINT REFERENCES teams(id)
);

-- ولي الأمر يرتبط بابن أو أكثر
CREATE TABLE parent_student (
  parent_id  BIGINT REFERENCES users(id),
  student_id BIGINT REFERENCES students(user_id),
  PRIMARY KEY (parent_id, student_id)
);

-- الحضور والغياب
CREATE TABLE attendance (
  id         BIGSERIAL PRIMARY KEY,
  student_id BIGINT NOT NULL REFERENCES students(user_id),
  day        DATE NOT NULL,
  status     VARCHAR(8) NOT NULL CHECK (status IN ('PRESENT','ABSENT','LATE')),
  note       VARCHAR(200),
  UNIQUE (student_id, day)
);

-- تقييم المعلم (١ إلى ٥ لكل معيار)
CREATE TABLE ratings (
  id            BIGSERIAL PRIMARY KEY,
  student_id    BIGINT NOT NULL REFERENCES students(user_id),
  teacher_id    BIGINT NOT NULL REFERENCES users(id),
  month         DATE NOT NULL,              -- أول يوم في الشهر
  commitment    SMALLINT CHECK (commitment BETWEEN 1 AND 5),
  participation SMALLINT CHECK (participation BETWEEN 1 AND 5),
  homework      SMALLINT CHECK (homework BETWEEN 1 AND 5),
  behavior      SMALLINT CHECK (behavior BETWEEN 1 AND 5),
  note          VARCHAR(300),
  UNIQUE (student_id, month)
);

-- بنك الأسئلة (موسوم لتسهيل التوليد بالذكاء الاصطناعي لاحقًا)
CREATE TABLE questions (
  id           BIGSERIAL PRIMARY KEY,
  body         TEXT NOT NULL,
  type         VARCHAR(10) NOT NULL CHECK (type IN ('MCQ','ESSAY')),
  unit         VARCHAR(80),
  difficulty   SMALLINT CHECK (difficulty BETWEEN 1 AND 5),
  skill        VARCHAR(60),                 -- فهم، تحليل، تطبيق ...
  source       VARCHAR(10) NOT NULL DEFAULT 'MANUAL' CHECK (source IN ('MANUAL','AI')),
  approved     BOOLEAN NOT NULL DEFAULT TRUE, -- أسئلة الذكاء الاصطناعي تحتاج اعتماد المعلم
  max_score    NUMERIC(5,2) NOT NULL DEFAULT 1
);
CREATE TABLE question_options (
  id          BIGSERIAL PRIMARY KEY,
  question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
  text        TEXT NOT NULL,
  correct     BOOLEAN NOT NULL DEFAULT FALSE
);

-- الاختبارات الدورية المفتوحة لفترة محددة
CREATE TABLE exams (
  id           BIGSERIAL PRIMARY KEY,
  classroom_id BIGINT NOT NULL REFERENCES classrooms(id),
  title        VARCHAR(120) NOT NULL,
  opens_at     TIMESTAMPTZ NOT NULL,
  closes_at    TIMESTAMPTZ NOT NULL,
  duration_min INT NOT NULL,
  CHECK (closes_at > opens_at)
);
CREATE TABLE exam_questions (
  exam_id     BIGINT REFERENCES exams(id) ON DELETE CASCADE,
  question_id BIGINT REFERENCES questions(id),
  position    INT NOT NULL,
  PRIMARY KEY (exam_id, question_id)
);
CREATE TABLE exam_attempts (
  id           BIGSERIAL PRIMARY KEY,
  exam_id      BIGINT NOT NULL REFERENCES exams(id),
  student_id   BIGINT NOT NULL REFERENCES students(user_id),
  started_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  submitted_at TIMESTAMPTZ,
  score        NUMERIC(6,2),
  UNIQUE (exam_id, student_id)              -- محاولة واحدة
);
CREATE TABLE attempt_answers (
  id          BIGSERIAL PRIMARY KEY,
  attempt_id  BIGINT NOT NULL REFERENCES exam_attempts(id) ON DELETE CASCADE,
  question_id BIGINT NOT NULL REFERENCES questions(id),
  option_id   BIGINT REFERENCES question_options(id),
  text_answer TEXT,
  score       NUMERIC(5,2),
  ai_score    NUMERIC(5,2),                 -- درجة مقترحة (مستقبلًا)
  graded_by   BIGINT REFERENCES users(id)   -- المعلم صاحب القرار النهائي
);

-- الاشتراكات
CREATE TABLE subscriptions (
  id         BIGSERIAL PRIMARY KEY,
  student_id BIGINT NOT NULL REFERENCES students(user_id),
  month      DATE NOT NULL,
  amount     NUMERIC(8,2) NOT NULL,
  paid       BOOLEAN NOT NULL DEFAULT FALSE,
  paid_at    TIMESTAMPTZ,
  UNIQUE (student_id, month)
);

-- الواجبات
CREATE TABLE homework (
  id           BIGSERIAL PRIMARY KEY,
  classroom_id BIGINT NOT NULL REFERENCES classrooms(id),
  title        VARCHAR(150) NOT NULL,
  details      TEXT,
  due_at       TIMESTAMPTZ NOT NULL
);
CREATE TABLE homework_submissions (
  id          BIGSERIAL PRIMARY KEY,
  homework_id BIGINT NOT NULL REFERENCES homework(id),
  student_id  BIGINT NOT NULL REFERENCES students(user_id),
  file_path   VARCHAR(300) NOT NULL,
  submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (homework_id, student_id)
);

-- المحادثات: غرفة الفصل (GROUP) أو معلم/طالب (DIRECT)
CREATE TABLE chat_rooms (
  id           BIGSERIAL PRIMARY KEY,
  kind         VARCHAR(6) NOT NULL CHECK (kind IN ('GROUP','DIRECT')),
  classroom_id BIGINT REFERENCES classrooms(id),
  muted        BOOLEAN NOT NULL DEFAULT FALSE   -- المعلم يقفل الشات وقت الحصة
);
CREATE TABLE chat_members (
  room_id BIGINT REFERENCES chat_rooms(id) ON DELETE CASCADE,
  user_id BIGINT REFERENCES users(id),
  PRIMARY KEY (room_id, user_id)
);
CREATE TABLE messages (
  id         BIGSERIAL PRIMARY KEY,
  room_id    BIGINT NOT NULL REFERENCES chat_rooms(id),
  sender_id  BIGINT NOT NULL REFERENCES users(id),
  body       TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- صندوق الأسئلة المجهول:
-- author_id يُحفظ للإشراف فقط (الإدارة)، ولا يُرجَع أبدًا في واجهة المعلم
CREATE TABLE anonymous_questions (
  id           BIGSERIAL PRIMARY KEY,
  classroom_id BIGINT NOT NULL REFERENCES classrooms(id),
  author_id    BIGINT NOT NULL REFERENCES users(id),
  body         TEXT NOT NULL,
  answer       TEXT,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- سؤال الأسبوع
CREATE TABLE weekly_challenges (
  id           BIGSERIAL PRIMARY KEY,
  classroom_id BIGINT NOT NULL REFERENCES classrooms(id),
  question     TEXT NOT NULL,
  week_start   DATE NOT NULL
);
CREATE TABLE challenge_answers (
  id           BIGSERIAL PRIMARY KEY,
  challenge_id BIGINT NOT NULL REFERENCES weekly_challenges(id),
  student_id   BIGINT NOT NULL REFERENCES students(user_id),
  body         TEXT NOT NULL,
  best         BOOLEAN NOT NULL DEFAULT FALSE,
  bonus_points SMALLINT NOT NULL DEFAULT 0
);

-- كتاب الشهر
CREATE TABLE books_of_month (
  id         BIGSERIAL PRIMARY KEY,
  month      DATE NOT NULL UNIQUE,
  title      VARCHAR(160) NOT NULL,
  author     VARCHAR(120),
  summary    TEXT,
  discussion TEXT
);

-- إشعارات (غياب، تقرير، نتيجة)
CREATE TABLE notifications (
  id         BIGSERIAL PRIMARY KEY,
  user_id    BIGINT NOT NULL REFERENCES users(id),
  body       VARCHAR(300) NOT NULL,
  read       BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- نقاط الشهر = اختبار ٤٠ + تقييم ٤٠ + حضور ٢٠ + نقاط المشاركة العلمية
CREATE VIEW monthly_scores AS
SELECT s.user_id AS student_id,
       date_trunc('month', CURRENT_DATE)::date AS month,
       ROUND(
         COALESCE((SELECT AVG(a.score) FROM exam_attempts a
                   JOIN exams e ON e.id=a.exam_id
                   WHERE a.student_id=s.user_id
                     AND date_trunc('month', e.opens_at)=date_trunc('month', CURRENT_DATE)),0)*0.4
       + COALESCE((SELECT (commitment+participation+homework+behavior)/4.0*8 FROM ratings r
                   WHERE r.student_id=s.user_id
                     AND r.month=date_trunc('month', CURRENT_DATE)::date),0)
       + COALESCE((SELECT 100.0*COUNT(*) FILTER (WHERE status<>'ABSENT')/NULLIF(COUNT(*),0)*0.2
                   FROM attendance t WHERE t.student_id=s.user_id
                     AND date_trunc('month', t.day)=date_trunc('month', CURRENT_DATE)),0)
       + COALESCE((SELECT SUM(bonus_points) FROM challenge_answers c
                   WHERE c.student_id=s.user_id),0)
       ) AS total
FROM students s;
