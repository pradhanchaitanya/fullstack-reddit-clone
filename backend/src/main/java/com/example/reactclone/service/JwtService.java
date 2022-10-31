package com.example.reactclone.service;

import com.example.reactclone.exception.RedditException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

@Service
public class JwtService {

    private static final String STR_KEYSTORE_JKS = "JKS";
    private static final String STR_SECRET = "secret";
    private static final String STR_SECRET_ALIAS = "spring-reddit-clone";
    private static final String KEYSTORE_PATH = "/spring-reddit-clone.jks";
    private KeyStore keyStore;

    @PostConstruct
    public void init() {
        try {
            keyStore = KeyStore.getInstance(STR_KEYSTORE_JKS);
            InputStream resourceStream = getClass().getResourceAsStream(KEYSTORE_PATH);
            keyStore.load(resourceStream, STR_SECRET.toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RedditException("Exception occurred while loading keystore", e);
        }
    }

    private PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) keyStore.getKey(STR_SECRET_ALIAS, STR_SECRET.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new RedditException("Exception occurred while loading keystore", e);
        }
    }

    private PublicKey getPublicKey() {
        try {
            return keyStore.getCertificate(STR_SECRET_ALIAS).getPublicKey();
        } catch (KeyStoreException e) {
            throw new RedditException("Exception occurred while retrieving public key", e);
        }
    }

    public String generateToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .signWith(getPrivateKey())
                .compact();
    }

    public boolean validateToken(String jwt) {
        Jwts.parser()
                .setSigningKey(getPublicKey())
                .parseClaimsJws(jwt);
        return Boolean.TRUE;
    }

    public String getUsernameFromJwt(String jwt) {
        Claims claims = Jwts.parser()
                .setSigningKey(getPublicKey())
                .parseClaimsJws(jwt)
                .getBody();
        return claims.getSubject();
    }
}
