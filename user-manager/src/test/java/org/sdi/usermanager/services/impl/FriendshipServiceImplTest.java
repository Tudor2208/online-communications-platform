package org.sdi.usermanager.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sdi.usermanager.dtos.FriendResponse;
import org.sdi.usermanager.entities.Friendship;
import org.sdi.usermanager.dtos.FriendshipId;
import org.sdi.usermanager.entities.User;
import org.sdi.usermanager.exceptions.NotFoundException;
import org.sdi.usermanager.repositories.FriendshipRepository;
import org.sdi.usermanager.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FriendshipServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private FriendshipServiceImpl friendshipService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createFriendship_whenUsersExist_shouldCreateFriendship() {
        // Arrange
        Long userId1 = 1L;
        Long userId2 = 2L;

        User user1 = new User();
        user1.setId(userId1);

        User user2 = new User();
        user2.setId(userId2);

        when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));
        when(friendshipRepository.findByUserIdAndFriendId(userId1, userId2)).thenReturn(Optional.empty());

        // Act
        friendshipService.createFriendship(userId1, userId2);

        // Assert
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    void createFriendship_whenFriendshipAlreadyExists_shouldThrowException() {
        // Arrange
        Long userId1 = 1L;
        Long userId2 = 2L;

        Friendship existingFriendship = new Friendship();
        when(friendshipRepository.findByUserIdAndFriendId(userId1, userId2)).thenReturn(Optional.of(existingFriendship));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> friendshipService.createFriendship(userId1, userId2));
    }

    @Test
    void deleteFriendship_whenFriendshipExists_shouldDeleteFriendship() {
        // Arrange
        Long userId1 = 1L;
        Long userId2 = 2L;

        Friendship friendship = new Friendship();
        when(friendshipRepository.findByUserIdAndFriendId(userId1, userId2)).thenReturn(Optional.of(friendship));

        // Act
        friendshipService.deleteFriendship(userId1, userId2);

        // Assert
        verify(friendshipRepository).delete(friendship);
    }

    @Test
    void deleteFriendship_whenFriendshipDoesNotExist_shouldThrowException() {
        // Arrange
        Long userId1 = 1L;
        Long userId2 = 2L;

        when(friendshipRepository.findByUserIdAndFriendId(userId1, userId2)).thenReturn(Optional.empty());
        when(friendshipRepository.findByUserIdAndFriendId(userId2, userId1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> friendshipService.deleteFriendship(userId1, userId2));
    }

    @Test
    void getPendingFriendRequests_whenPendingRequestsExist_shouldReturnFriendResponses() {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setId(2L);
        user.setFirstName("Jane");
        user.setLastName("Doe");

        Friendship friendship = new Friendship();
        friendship.setUser(user);

        when(friendshipRepository.findByFriendIdAndConfirmed(userId, false)).thenReturn(List.of(friendship));

        // Act
        List<FriendResponse> pendingRequests = friendshipService.getPendingFriendRequests(userId);

        // Assert
        assertEquals(1, pendingRequests.size());
        assertEquals(user.getId(), pendingRequests.get(0).getId());
    }

    @Test
    void acceptFriendRequest_whenFriendshipExists_shouldConfirmFriendship() {
        // Arrange
        Long userId1 = 1L;
        Long userId2 = 2L;

        Friendship friendship = new Friendship();
        friendship.setConfirmed(false);

        when(friendshipRepository.findByUserIdAndFriendId(userId1, userId2)).thenReturn(Optional.of(friendship));

        // Act
        friendshipService.acceptFriendRequest(userId1, userId2);

        // Assert
        assertTrue(friendship.isConfirmed());
        verify(friendshipRepository).save(friendship);
    }

    @Test
    void acceptFriendRequest_whenFriendshipDoesNotExist_shouldThrowException() {
        // Arrange
        Long userId1 = 1L;
        Long userId2 = 2L;

        when(friendshipRepository.findByUserIdAndFriendId(userId1, userId2)).thenReturn(Optional.empty());
        when(friendshipRepository.findByUserIdAndFriendId(userId2, userId1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> friendshipService.acceptFriendRequest(userId1, userId2));
    }
}
