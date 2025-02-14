package org.sdi.chatmanager.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sdi.chatmanager.dtos.CreateGroupRequest;
import org.sdi.chatmanager.dtos.GroupResponse;
import org.sdi.chatmanager.dtos.PatchGroupRequest;
import org.sdi.chatmanager.entities.Group;
import org.sdi.chatmanager.entities.GroupInvite;
import org.sdi.chatmanager.entities.User;
import org.sdi.chatmanager.exceptions.ExistingMemberException;
import org.sdi.chatmanager.exceptions.NotFoundException;
import org.sdi.chatmanager.repositories.GroupInviteRepository;
import org.sdi.chatmanager.repositories.GroupRepository;
import org.sdi.chatmanager.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupServiceImplTest {

    @InjectMocks
    private GroupServiceImpl groupService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupInviteRepository groupInviteRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateGroup_Success() {
        // Arrange
        Long ownerId = 1L;
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(ownerId);
        request.setName("New Group");

        User owner = new User();
        owner.setId(ownerId);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        // Act
        groupService.createGroup(request);

        // Assert
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void testCreateGroup_UserNotFound() {
        // Arrange
        Long ownerId = 1L;
        CreateGroupRequest request = new CreateGroupRequest();
        request.setOwnerId(ownerId);
        request.setName("New Group");

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> groupService.createGroup(request));
    }

    @Test
    void testGetGroup_Success() {
        // Arrange
        Long groupId = 1L;

        Group group = new Group();
        group.setId(groupId);
        group.setName("Test Group");
        group.setOwner(new User());
        group.setMembers(new ArrayList<>());

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        // Act
        GroupResponse response = groupService.getGroup(groupId);

        // Assert
        assertEquals(groupId, response.getId());
        assertEquals("Test Group", response.getName());
    }

    @Test
    void testGetGroup_NotFound() {
        // Arrange
        Long groupId = 1L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> groupService.getGroup(groupId));
    }

    @Test
    void testAddMember_AlreadyMember() {
        // Arrange
        Long groupId = 1L;
        Long memberId = 2L;

        Group group = new Group();
        group.setId(groupId);

        User member = new User();
        member.setId(memberId);

        group.setMembers(List.of(member));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(memberId)).thenReturn(Optional.of(member));

        // Act & Assert
        assertThrows(ExistingMemberException.class, () -> groupService.addMember(groupId, memberId));
    }

    @Test
    void testDeleteGroup_NotFound() {
        // Arrange
        Long groupId = 1L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> groupService.deleteGroup(groupId));
    }

    @Test
    void testRemoveMember_Success() {
        // Arrange
        Long groupId = 1L;
        Long userId = 2L;

        Group group = new Group();
        group.setId(groupId);

        User user = new User();
        user.setId(userId);

        group.setMembers(new ArrayList<>(List.of(user)));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        groupService.removeMember(groupId, userId);

        // Assert
        assertFalse(group.getMembers().contains(user));
        verify(groupRepository, times(1)).save(group);
    }

    @Test
    void testPatchGroup_NotFound() {
        // Arrange
        Long groupId = 1L;

        PatchGroupRequest request = new PatchGroupRequest();
        request.setName("New Name");

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> groupService.patchGroup(groupId, request));
    }

    @Test
    void testGetUsersGroups_Success() {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Group group = new Group();
        group.setId(2L);
        group.setMembers(List.of(user));
        group.setOwner(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupRepository.findAll()).thenReturn(List.of(group));

        // Act
        List<GroupResponse> responses = groupService.getUsersGroups(userId);

        // Assert
        assertEquals(1, responses.size());
        assertEquals(2L, responses.get(0).getId());
    }

    @Test
    void testGetUsersGroups_UserNotFound() {
        // Arrange
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> groupService.getUsersGroups(userId));
    }
}
