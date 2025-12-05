package com.training.demo.utils.enums;

public enum RoleType {

    USER(1),
    STAFF(2),
    THEATER_MANAGER(3),
    ADMIN(4);

    private final int level;

    RoleType(int level) { this.level = level; }
}
