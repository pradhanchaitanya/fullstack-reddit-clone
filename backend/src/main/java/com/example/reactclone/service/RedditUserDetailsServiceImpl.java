package com.example.reactclone.service;

import com.example.reactclone.config.Role;
import com.example.reactclone.exception.RedditException;
import com.example.reactclone.model.User;
import com.example.reactclone.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RedditUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = this.userRepository.findByUsername(username);

        User user = userOptional.orElseThrow(
                () -> new RedditException(String.format("User with username [%s] does not exist", username))
        );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                getAuthorities(Role.USER)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(final Role role) {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }
}
