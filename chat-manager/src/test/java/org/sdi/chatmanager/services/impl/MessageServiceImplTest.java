package org.sdi.chatmanager.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sdi.chatmanager.dtos.ConversationResponse;
import org.sdi.chatmanager.dtos.CreateMessageRequest;
import org.sdi.chatmanager.dtos.MessageResponse;
import org.sdi.chatmanager.dtos.PatchMessageRequest;
import org.sdi.chatmanager.entities.Message;
import org.sdi.chatmanager.entities.User;
import org.sdi.chatmanager.exceptions.NotFoundException;
import org.sdi.chatmanager.repositories.GroupMessageRepository;
import org.sdi.chatmanager.repositories.GroupRepository;
import org.sdi.chatmanager.repositories.MessageRepository;
import org.sdi.chatmanager.repositories.UserRepository;
import org.sdi.chatmanager.services.GroupMessageService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMessageService groupMessageService;
    @Mock
    private GroupMessageRepository groupMessageRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateMessage_Success() {
        Long senderId = 1L;
        Long recipientId = 2L;
        CreateMessageRequest createMessageRequest = new CreateMessageRequest();
        createMessageRequest.setSenderId(senderId);
        createMessageRequest.setRecipientId(recipientId);
        createMessageRequest.setText("Hello!");

        User sender = new User();
        sender.setId(senderId);
        User recipient = new User();
        recipient.setId(recipientId);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));

        messageService.createMessage(createMessageRequest);

        verify(messageRepository).save(any(Message.class)); // Verifying that save was called on the repository
    }

    @Test
    public void testCreateMessage_UserNotFound() {
        Long senderId = 1L;
        Long recipientId = 2L;
        CreateMessageRequest createMessageRequest = new CreateMessageRequest();
        createMessageRequest.setSenderId(senderId);
        createMessageRequest.setRecipientId(recipientId);
        createMessageRequest.setText("Hello!");

        when(userRepository.findById(senderId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> messageService.createMessage(createMessageRequest));
        assertEquals("User with ID 1 not found", exception.getMessage());
    }

    @Test
    public void testGetConversation_Success() {
        Long userId1 = 1L;
        Long userId2 = 2L;

        User sender = new User();
        sender.setId(userId1);
        User recipient = new User();
        recipient.setId(userId2);

        Message message1 = new Message();
        message1.setSender(sender);
        message1.setRecipient(recipient);
        message1.setText("Hi");
        message1.setTimestamp(new Date());

        Message message2 = new Message();
        message2.setSender(recipient);
        message2.setRecipient(sender);
        message2.setText("Hello");
        message2.setTimestamp(new Date());

        when(userRepository.findById(userId1)).thenReturn(Optional.of(sender));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(recipient));
        when(messageRepository.findAllBySenderAndRecipient(sender, recipient)).thenReturn(List.of(message1));
        when(messageRepository.findAllBySenderAndRecipient(recipient, sender)).thenReturn(List.of(message2));

        List<MessageResponse> responses = messageService.getConversation(userId1, userId2);

        assertEquals(2, responses.size());
        assertEquals("Hi", responses.get(0).getText());
        assertEquals("Hello", responses.get(1).getText());
    }

    @Test
    public void testGetConversation_UserNotFound() {
        Long userId1 = 1L;
        Long userId2 = 2L;

        when(userRepository.findById(userId1)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> messageService.getConversation(userId1, userId2));
        assertEquals("User with ID 1 not found", exception.getMessage());
    }

    @Test
    public void testDeleteMessage_Success() {
        Long messageId = 1L;
        Message message = new Message();
        message.setId(messageId);

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        messageService.deleteMessage(messageId);

        verify(messageRepository).delete(message); // Verifying delete is called on the repository
    }

    @Test
    public void testDeleteMessage_NotFound() {
        Long messageId = 1L;

        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> messageService.deleteMessage(messageId));
        assertEquals("Message with ID 1 not found", exception.getMessage());
    }

    @Test
    public void testPatchMessage_NotFound() {
        Long messageId = 1L;
        PatchMessageRequest patchMessageRequest = new PatchMessageRequest();
        patchMessageRequest.setText("Updated Text");

        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> messageService.patchMessage(messageId, patchMessageRequest));
        assertEquals("Message with ID 1 not found", exception.getMessage());
    }

    @Test
    public void testGetConversations_Success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Message message1 = new Message();
        message1.setSender(user);
        message1.setRecipient(new User());
        message1.setText("Hello");
        message1.setTimestamp(new Date());

        List<Message> messages = Collections.singletonList(message1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(messageRepository.findAllBySender(user)).thenReturn(messages);
        when(messageRepository.findAllByRecipient(user)).thenReturn(Collections.emptyList());

        List<ConversationResponse> conversations = messageService.getConversations(userId);

        assertEquals(1, conversations.size());
        assertEquals("Hello", conversations.get(0).getLastMessage());
    }

    @Test
    public void testGetConversations_UserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> messageService.getConversations(userId));
        assertEquals("User with ID 1 not found", exception.getMessage());
    }
}
