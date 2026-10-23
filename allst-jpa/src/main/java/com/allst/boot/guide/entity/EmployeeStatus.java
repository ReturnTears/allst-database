package com.allst.boot.guide.entity;

/**
 * 员工状态。持久化使用 {@link javax.persistence.EnumType#STRING}，避免枚举顺序调整后旧数据错位。
 */
public enum EmployeeStatus {
    ACTIVE,
    LEAVE,
    DISABLED
}
