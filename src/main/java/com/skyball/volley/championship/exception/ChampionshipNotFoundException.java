package com.skyball.volley.championship.exception;

public class ChampionshipNotFoundException extends RuntimeException {
    public ChampionshipNotFoundException(Long id) {
        super("Championship not found with id: " + id);
    }
}
