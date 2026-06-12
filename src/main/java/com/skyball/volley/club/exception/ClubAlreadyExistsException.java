package com.skyball.volley.club.exception;

public class ClubAlreadyExistsException extends RuntimeException {
    public ClubAlreadyExistsException(String name) {
        super("Club already exists with name: " + name);
    }
}
