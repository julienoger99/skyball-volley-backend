package com.skyball.volley.team.exception;

public class TeamNotFoundException extends RuntimeException {
    public TeamNotFoundException(Long id) {
        super("Team not found with id: " + id);
    }
}
