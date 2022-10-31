package com.example.reactclone.model;

public record NotificationEmail(
        String subject,
        String recipient,
        String body
) { }
