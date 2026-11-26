# نبراس: الخلفية (Backend)

Spring Boot 3 · Java 21 · PostgreSQL · JWT · Flyway · JPA

> **الحالة:** كتبنا المرحلة 1 لكنها **لم تُجرَّب على جهاز حقيقي بعد**. أول تشغيل قد يكشف أخطاء بسيطة. حدّث هذه الجملة بعد نجاح سيناريو التحقق أدناه.

## ما يوفره الآن
دخول بـ JWT، إدارة المستخدمين والفصول (للمدير)، تسجيل الحضور والغياب (للمعلم)، ربط ولي الأمر بابنه، وإشعار تلقائي لولي الأمر عند تسجيل غياب جديد.

## المتطلبات
Java 21، Maven 3.9+، Docker (يعمل)، وأي طرفية فيها `curl`.
على ويندوز: استخدم **Git Bash** (يأتي مع Git) لتعمل أوامر `export` و`curl` كما هي في هذا الملف.

## التشغيل
```bash
docker compose up -d                              # قاعدة البيانات
export JWT_SECRET="$(openssl rand -base64 48)"    # إجباري، 32 بايت فأكثر
export ADMIN_PASSWORD="كلمة-قوية-8-أحرف-فأكثر"
mvn spring-boot:run
```
- المتغيرات تبقى في **نفس نافذة الطرفية** فقط.
- في PowerShell بدل `export`: `$env:JWT_SECRET = "..."` و`$env:ADMIN_PASSWORD = "..."`.
- أول تشغيل ينشئ الجداول (Flyway) وحساب `admin@nebras.local`.
- **النجاح:** يظهر `تم إنشاء حساب المدير` ثم `Started NebrasApplication`.

## سيناريو التحقق (انسخه كما هو)
```bash
B=localhost:8080/api; J='Content-Type: application/json'
tok() { curl -s $B/auth/login -H "$J" -d "{\"email\":\"$1\",\"password\":\"$2\"}" | sed 's/.*"token":"\([^"]*\)".*/\1/'; }

# 1) المدير ينشئ المستخدمين (الأرقام: معلم=2، طالب=3، ولي أمر=4، معلم آخر=5)
A="Authorization: Bearer $(tok admin@nebras.local "$ADMIN_PASSWORD")"
for u in '"TEACHER","fullName":"أ. محمد","email":"t@x.com"' \
         '"STUDENT","fullName":"أحمد سمير","email":"s@x.com"' \
         '"PARENT","fullName":"والد أحمد","email":"p@x.com"' \
         '"TEACHER","fullName":"أ. علي","email":"t2@x.com"'; do
  curl -s $B/admin/users -H "$A" -H "$J" -d "{\"role\":$u,\"password\":\"12345678\"}"; echo
done

# 2) فصل للمعلم 2، وإضافة الطالب، وربط ولي الأمر
curl -s $B/admin/classrooms -H "$A" -H "$J" -d '{"name":"3/2","teacherId":2}'; echo
curl -s $B/admin/classrooms/1/students -H "$A" -H "$J" -d '{"studentId":3}'
curl -s $B/admin/parent-links -H "$A" -H "$J" -d '{"parentId":4,"studentId":3}'

# 3) المعلم يسجل غياب الطالب
T="Authorization: Bearer $(tok t@x.com 12345678)"
curl -s $B/attendance -H "$T" -H "$J" \
  -d '{"classroomId":1,"day":"2026-11-01","records":[{"studentId":3,"status":"ABSENT"}]}'; echo
# المتوقع: {"saved":1,"parentsNotified":1}

# 4) ولي الأمر يرى الإشعار
P="Authorization: Bearer $(tok p@x.com 12345678)"
curl -s $B/notifications -H "$P"; echo
# المتوقع: رسالة "غاب أحمد سمير بتاريخ 2026-11-01"

# 5) معلم آخر لا يرى بيانات الطالب
T2="Authorization: Bearer $(tok t2@x.com 12345678)"
curl -s -o /dev/null -w "%{http_code}\n" $B/students/3/attendance -H "$T2"
# المتوقع: 403
```
إن نجحت الخطوات الخمس بالنتائج المتوقعة فالمرحلة 1 في الخلفية تعمل.

## إن لم يشتغل
| العَرَض | السبب المرجّح |
|---|---|
| `JWT_SECRET` ... Could not resolve placeholder | لم تضبط المتغير في نفس النافذة |
| `JWT_SECRET must be at least 32 bytes` | السر قصير |
| `Connection refused` على 5432 | Docker لا يعمل أو الحاوية لم تبدأ (`docker ps`) |
| خطأ Flyway أو SQL عند الإقلاع | أرسل أول سطرين فيهما `ERROR` و`Caused by` |
| `ADMIN_PASSWORD` قصيرة: يظهر تحذير ولا يُنشأ مدير | اجعلها 8 أحرف فأكثر وأعد التشغيل (إن بدأت القاعدة قبلها بدون مدير) |
| دخول المدير يفشل بعد تغيير كلمة السر | المدير يُنشأ مرة واحدة فقط. للبدء من جديد: `docker compose down -v` ثم أعد التشغيل (يمسح كل البيانات) |

## هيكل المشروع
```text
src/main/java/com/nebras/
  user/          User, Role, UserRepository
  classroom/     Classroom, StudentProfile, ParentLink + مستودعاتها
  attendance/    Attendance, Repository, DTOs, Service, Controller
  notification/  Notification, Repository, Service, Controller
  admin/         AdminService, AdminController, DTOs
  auth/          AuthService, AuthController, AdminSeeder
  security/      JwtService, JwtFilter, SecurityConfig, Access
src/main/resources/
  application.yml
  db/migration/V1__init.sql     (مخطط قاعدة البيانات الكامل)
```
**الطبقات:** Controller ← Service ← Repository ← Entity. منطق العمل في الـ Service فقط، والـ Controller لا يُرجِع Entity أبدًا (DTO فقط). المخطط يغيّره **Flyway فقط** (`ddl-auto: none`): لا تعدّل `V1` بعد تشغيله، أضف `V2__...sql`.

## نقاط النهاية
| المسار | من يستخدمه |
|---|---|
| `POST /api/auth/login` | الجميع |
| `POST /api/admin/users` · `/classrooms` · `/classrooms/{id}/students` · `/parent-links` | المدير |
| `POST /api/attendance` | المعلم (لفصله) أو المدير |
| `GET /api/classrooms/{id}/attendance?day=YYYY-MM-DD` | معلم الفصل أو المدير |
| `GET /api/students/{id}/attendance` | الطالب نفسه، ولي أمره، معلمه، المدير |
| `GET /api/notifications` · `POST /api/notifications/{id}/read` | صاحب الإشعار |

## المتغيرات
| المتغير | الوصف | افتراضي |
|---|---|---|
| `JWT_SECRET` | سر توقيع التوكن | لا يوجد (إجباري) |
| `ADMIN_PASSWORD` | كلمة سر المدير الأول | فارغ |
| `ADMIN_EMAIL` | بريد المدير | `admin@nebras.local` |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | اتصال PostgreSQL | محلي: `nebras` |
| `CORS_ORIGIN` | عنوان الواجهة المسموح | `http://localhost:4200` |

## أمان
- لا أسرار في الكود أو Git (`.env` في `.gitignore`).
- في الإنتاج: HTTPS، وكلمة سر قوية لقاعدة البيانات، وإبقاؤها غير مكشوفة للإنترنت.
- **غير موجود بعد:** حد لمحاولات الدخول الفاشلة، وRefresh token. انظر `docs/adr/0003-jwt-stateless-auth.md`.

## التوثيق
القرارات والمعمارية ويوميات التنفيذ في المجلد `docs/` على مستوى المستودع.
