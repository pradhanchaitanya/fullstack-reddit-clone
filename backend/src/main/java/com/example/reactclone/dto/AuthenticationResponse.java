package com.example.reactclone.dto;

public record AuthenticationResponse(
        String authToken,
        String username
) { }
