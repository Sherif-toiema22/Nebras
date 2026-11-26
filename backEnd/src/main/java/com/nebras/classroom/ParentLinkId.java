package com.nebras.classroom;

import java.io.Serializable;

public record ParentLinkId(Long parentId, Long studentId) implements Serializable {}
