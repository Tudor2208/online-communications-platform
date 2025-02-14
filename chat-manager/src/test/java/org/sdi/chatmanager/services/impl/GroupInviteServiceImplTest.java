package org.sdi.chatmanager.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sdi.chatmanager.dtos.CreateGroupInviteRequest;
import org.sdi.chatmanager.entities.Group;
import org.sdi.chatmanager.entities.GroupInvite;
import org.sdi.chatmanager.entities.User;
import org.sdi.chatmanager.exceptions.NotFoundException;
import org.sdi.chatmanager.repositories.GroupInviteRepository;
import org.sdi.chatmanager.repositories.GroupRepository;
import org.sdi.chatmanager.repositories.UserRepository;
import org.sdi.chatmanager.services.GroupService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GroupInviteServiceImplTest {

    @InjectMocks
    private GroupInviteServiceImpl groupInviteService;

    @Mock
    private GroupInviteRepository groupInviteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateGroupInvite_Success() {
        // Arrange
        Long userId = 1L;
        Long groupId = 2L;
        CreateGroupInviteRequest request = new CreateGroupInviteRequest();
        request.setUserId(userId);
        request.setGroupId(groupId);

        User user = new User();
        user.setId(userId);

        Group group = new Group();
        group.setId(groupId);
        group.setOwner(new User());
        group.setMembers(List.of());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupInviteRepository.findAllByUser(user)).thenReturn(List.of());

        // Act
        groupInviteService.createGroupInvite(request);

        // Assert
        verify(groupInviteRepository, times(1)).save(any(GroupInvite.class));
    }

    @Test
    void testCreateGroupInvite_InvalidRequest_UserIdNull() {
        // Arrange
        CreateGroupInviteRequest request = new CreateGroupInviteRequest();
        request.setUserId(null); // Invalid userId
        request.setGroupId(2L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validateRequest(request));
    }

    @Test
    void testCreateGroupInvite_InvalidRequest_GroupIdNull() {
        // Arrange
        CreateGroupInviteRequest request = new CreateGroupInviteRequest();
        request.setUserId(1L);
        request.setGroupId(null); // Invalid groupId

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validateRequest(request));
    }

    @Test
    void testCreateGroupInvite_InvalidRequest_UserIdNegative() {
        // Arrange
        CreateGroupInviteRequest request = new CreateGroupInviteRequest();
        request.setUserId(-1L); // Invalid userId
        request.setGroupId(2L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validateRequest(request));
    }

    @Test
    void testCreateGroupInvite_InvalidRequest_GroupIdNegative() {
        // Arrange
        CreateGroupInviteRequest request = new CreateGroupInviteRequest();
        request.setUserId(1L);
        request.setGroupId(-2L); // Invalid groupId

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> validateRequest(request));
    }

    @Test
    void testCreateGroupInvite_UserNotFound() {
        // Arrange
        Long userId = 1L;
        CreateGroupInviteRequest request = new CreateGroupInviteRequest();
        request.setUserId(userId);
        request.setGroupId(2L);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> groupInviteService.createGroupInvite(request));
    }

    @Test
    void testCreateGroupInvite_GroupNotFound() {
        // Arrange
        Long userId = 1L;
        Long groupId = 2L;
        CreateGroupInviteRequest request = new CreateGroupInviteRequest();
        request.setUserId(userId);
        request.setGroupId(groupId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> groupInviteService.createGroupInvite(request));
    }

    private void validateRequest(CreateGroupInviteRequest request) {
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new IllegalArgumentException("User ID must be positive and not null");
        }
        if (request.getGroupId() == null || request.getGroupId() <= 0) {
            throw new IllegalArgumentException("Group ID must be positive and not null");
        }
    }
}
