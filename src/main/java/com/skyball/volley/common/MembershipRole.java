package com.skyball.volley.common;

public enum MembershipRole {
    MEMBER(0),
    MANAGER(1),
    ADMIN(2);

    private final int level;

    MembershipRole(int level) {
        this.level = level;
    }

    public boolean atLeast(MembershipRole minimum) {
        return this.level >= minimum.level;
    }
}
