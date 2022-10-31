package com.example.reactclone.service;

import com.example.reactclone.dto.AuthenticationResponse;
import com.example.reactclone.dto.LoginRequest;
import com.example.reactclone.dto.RegisterRequest;
import com.example.reactclone.exception.RedditException;
import com.example.reactclone.model.NotificationEmail;
import com.example.reactclone.model.User;
import com.example.reactclone.model.VerificationToken;
import com.example.reactclone.repository.UserRepository;
import com.example.reactclone.repository.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    private final MailService mailService;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private static final String ACTIVATION_URL =
            "http://localhost:3000/api/auth/accountVerification/";

    private final static String ACTIVATION_EMAIL_BODY = """
            Thank you for signing up to the Reddit clone.
            Please click on the below URL to activate your account - 
                %s
            """;

    @Transactional
    public void register(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.username());
        user.setEmail(registerRequest.email());
        user.setPassword(
                this.passwordEncoder.encode(registerRequest.password())
        );
        user.setCreated(Instant.now());
        user.setEnabled(Boolean.FALSE);

        this.userRepository.save(user);

        String token = generateVerificationToken(user);
        mailService.sendMail(new NotificationEmail(
            "Please Activate your Account",
            user.getEmail(),
                String.format(ACTIVATION_EMAIL_BODY, ACTIVATION_URL + token)
        ));
    }

    private String generateVerificationToken(final User user) {
        // if user already exists, and trying to create new token for registration, don't allow
        Optional<User> existingUser = this.userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent())
            throw new RedditException(String.format("User with username [%s] already exists", user.getUsername()));

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);

        this.verificationTokenRepository.save(verificationToken);

        return token;
    }

    public void verifyAccount(final String token) {
        VerificationToken verificationToken =
                this.verificationTokenRepository.findByToken(token)
                        .orElseThrow(
                                () -> new RedditException(
                                        String.format("Verification token [%s] does not exist", token)
                        ));

        fetchUserByTokenAndEnable(verificationToken);
    }

    @Transactional
    private void fetchUserByTokenAndEnable(final VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();

        User user = this.userRepository.findByUsername(username)
                            .orElseThrow(
                                    () -> new RedditException(
                                            String.format("User with username [%s] not found", username)
                            ));
        user.setEnabled(Boolean.TRUE);
        this.userRepository.save(user);
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authenticate = this.authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.username(),
                    loginRequest.password()
                ));

        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String authToken = this.jwtService.generateToken(authenticate);
        return new AuthenticationResponse(authToken, loginRequest.username());
    }
}
