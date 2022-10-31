package com.example.reactclone.dto;

public record RegisterRequest(
        String email,
        String username,
        String password
) { }
