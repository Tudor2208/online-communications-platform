package org.sdi.chatmanager.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.sdi.chatmanager.entities.Role;
import org.sdi.chatmanager.entities.User;
import org.sdi.chatmanager.repositories.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    public void testCreateUser_Success() {
        // Prepare a valid message that matches the regex
        String validMessage = "User{id=1, email='test@example.com', firstName='John', lastName='Doe', password='password123', role=USER}";

        userService.createUser(validMessage);

        // Capture the argument passed to save()
        verify(userRepository, times(1)).save(userCaptor.capture());

        // Get the captured user and check its fields
        User capturedUser = userCaptor.getValue();
        assertEquals(1L, capturedUser.getId());
        assertEquals("test@example.com", capturedUser.getEmail());
        assertEquals("John", capturedUser.getFirstName());
        assertEquals("Doe", capturedUser.getLastName());
        assertEquals("password123", capturedUser.getPassword());
        assertEquals(Role.USER, capturedUser.getRole());
    }

    @Test
    public void testCreateUser_InvalidMessage() {
        // Prepare an invalid message that does not match the regex pattern
        String invalidMessage = "Invalid user data without proper format";

        userService.createUser(invalidMessage);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testCreateUser_IncompleteMessage() {
        // Prepare an incomplete message where some fields are missing or the format is incorrect
        String incompleteMessage = "User{id=1, email='test@example.com', firstName='John', lastName='Doe', password='password123'}"; // Missing role

        userService.createUser(incompleteMessage);

        verify(userRepository, never()).save(any(User.class));
    }
}
