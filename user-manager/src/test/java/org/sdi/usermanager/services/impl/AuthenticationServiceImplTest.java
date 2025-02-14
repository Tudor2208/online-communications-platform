package org.sdi.usermanager.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sdi.usermanager.config.JwtService;
import org.sdi.usermanager.dtos.AuthenticationRequest;
import org.sdi.usermanager.dtos.AuthenticationResponse;
import org.sdi.usermanager.dtos.RegisterRequest;
import org.sdi.usermanager.entities.Role;
import org.sdi.usermanager.entities.User;
import org.sdi.usermanager.exceptions.EmailAlreadyExistsException;
import org.sdi.usermanager.repositories.UserRepository;
import org.sdi.usermanager.services.KafkaProducerService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_whenEmailAlreadyExists_shouldThrowException() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () ->
                authenticationService.register(registerRequest)
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_whenValidRequest_shouldSaveUserAndReturnResponse() {
        // Arrange
        String email = "test@example.com";
        String encodedPassword = "encodedPassword";
        String jwtToken = "jwtToken";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPassword("password");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(encodedPassword);
        when(jwtService.generateToken(any(User.class))).thenReturn(jwtToken);

        // Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Assert
        verify(userRepository).save(any(User.class));
        verify(kafkaProducerService).sendMessage(eq("create.user"), any(String.class));
        assertNotNull(response);
        assertEquals(email, response.getEmail());
        assertEquals(jwtToken, response.getToken());
    }

    @Test
    void authenticate_whenValidCredentials_shouldReturnResponse() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        String jwtToken = "jwtToken";

        User user = new User();
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setFirstName("John");

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail(email);
        authenticationRequest.setPassword(password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(jwtToken);

        // Act
        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        // Assert
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertNotNull(response);
        assertEquals(email, response.getEmail());
        assertEquals(jwtToken, response.getToken());
    }

    @Test
    void authenticate_whenInvalidCredentials_shouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String password = "password";

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail(email);
        authenticationRequest.setPassword(password);

        doThrow(new RuntimeException("Authentication failed"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                authenticationService.authenticate(authenticationRequest)
        );
        verify(userRepository, never()).findByEmail(email);
    }
}
