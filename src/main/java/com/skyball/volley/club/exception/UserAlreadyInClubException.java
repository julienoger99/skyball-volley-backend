package com.skyball.volley.club.exception;

public class UserAlreadyInClubException extends RuntimeException {
    public UserAlreadyInClubException(Long userId) {
        super("User " + userId + " already belongs to a club");
    }
}
