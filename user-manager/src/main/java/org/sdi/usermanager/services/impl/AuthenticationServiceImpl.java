package org.sdi.usermanager.services.impl;

import org.sdi.usermanager.config.JwtService;
import org.sdi.usermanager.dtos.AuthenticationRequest;
import org.sdi.usermanager.dtos.AuthenticationResponse;
import org.sdi.usermanager.dtos.RegisterRequest;
import org.sdi.usermanager.entities.Role;
import org.sdi.usermanager.entities.User;
import org.sdi.usermanager.exceptions.EmailAlreadyExistsException;
import org.sdi.usermanager.repositories.UserRepository;
import org.sdi.usermanager.services.AuthenticationService;
import org.sdi.usermanager.services.KafkaProducerService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final KafkaProducerService kafkaProducerService;

    private final String CREATE_USER_TOPIC = "create.user";

    public AuthenticationServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, JwtService jwtService, AuthenticationManager authenticationManager, KafkaProducerService kafkaProducerService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public AuthenticationResponse register(RegisterRequest registerRequest) {
        userRepository.findByEmail(registerRequest.getEmail()).ifPresent(user -> {
            throw new EmailAlreadyExistsException(String.format("User with email %s already exists", user.getEmail()));
        });

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        kafkaProducerService.sendMessage(CREATE_USER_TOPIC, user.toString());
        return new AuthenticationResponse(user.getId(), jwtToken, user.getEmail(), user.getRole().name(), user.getFirstName());
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword())
        );

        var user = userRepository.findByEmail(authenticationRequest.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(user.getId(), jwtToken, user.getEmail(), user.getRole().name(), user.getFirstName());
    }
}
