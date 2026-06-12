package com.skyball.volley.user.domain;

public enum Role {
    COACH,
    PLAYER;

    public String asAuthority() {
        return "ROLE_" + this.name();
    }
}

