package org.sdi.chatmanager.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sdi.chatmanager.dtos.CreateGroupMessageRequest;
import org.sdi.chatmanager.dtos.GroupMessageResponse;
import org.sdi.chatmanager.dtos.PatchGroupMessageRequest;
import org.sdi.chatmanager.entities.Group;
import org.sdi.chatmanager.entities.GroupMessage;
import org.sdi.chatmanager.entities.User;
import org.sdi.chatmanager.exceptions.NotFoundException;
import org.sdi.chatmanager.repositories.GroupMessageRepository;
import org.sdi.chatmanager.repositories.GroupRepository;
import org.sdi.chatmanager.repositories.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupMessageServiceImplTest {

    @InjectMocks
    private GroupMessageServiceImpl groupMessageService;

    @Mock
    private GroupMessageRepository groupMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateGroupMessage_GroupNotFound() {
        // Arrange
        Long groupId = 1L;
        Long senderId = 2L;
        CreateGroupMessageRequest request = new CreateGroupMessageRequest();
        request.setGroupId(groupId);
        request.setSenderId(senderId);
        request.setText("Hello Group!");

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> groupMessageService.createGroupMessage(request));
    }

    @Test
    void testCreateGroupMessage_SenderNotFound() {
        // Arrange
        Long groupId = 1L;
        Long senderId = 2L;
        CreateGroupMessageRequest request = new CreateGroupMessageRequest();
        request.setGroupId(groupId);
        request.setSenderId(senderId);
        request.setText("Hello Group!");

        Group group = new Group();
        group.setId(groupId);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(senderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> groupMessageService.createGroupMessage(request));
    }

    @Test
    void testGetGroupMessages_Success() {
        // Arrange
        Long groupId = 1L;

        Group group = new Group();
        group.setId(groupId);

        GroupMessage message1 = new GroupMessage();
        message1.setId(1L);
        message1.setText("Message 1");
        message1.setTimestamp(new Date());
        message1.setEdited(false);
        message1.setGroup(group);
        message1.setSender(new User());

        GroupMessage message2 = new GroupMessage();
        message2.setId(2L);
        message2.setText("Message 2");
        message2.setTimestamp(new Date());
        message2.setEdited(true);
        message2.setGroup(group);
        message2.setSender(new User());

        List<GroupMessage> messages = List.of(message1, message2);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMessageRepository.findAllByGroup(group)).thenReturn(messages);

        // Act
        List<GroupMessageResponse> responses = groupMessageService.getGroupMessages(groupId);

        // Assert
        assertEquals(2, responses.size());
        assertEquals("Message 1", responses.get(0).getText());
        assertEquals("Message 2", responses.get(1).getText());
    }

    @Test
    void testGetGroupMessages_GroupNotFound() {
        // Arrange
        Long groupId = 1L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> groupMessageService.getGroupMessages(groupId));
    }

    @Test
    void testDeleteGroupMessage_Success() {
        // Arrange
        Long messageId = 1L;

        GroupMessage message = new GroupMessage();
        message.setId(messageId);

        when(groupMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        // Act
        groupMessageService.deleteGroupMessage(messageId);

        // Assert
        verify(groupMessageRepository, times(1)).delete(message);
    }

    @Test
    void testDeleteGroupMessage_NotFound() {
        // Arrange
        Long messageId = 1L;

        when(groupMessageRepository.findById(messageId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> groupMessageService.deleteGroupMessage(messageId));
    }

    @Test
    void testPatchGroupMessage_NotFound() {
        // Arrange
        Long messageId = 1L;
        PatchGroupMessageRequest request = new PatchGroupMessageRequest();
        request.setText("New Text");

        when(groupMessageRepository.findById(messageId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> groupMessageService.patchGroupMessage(messageId, request));
    }
}
