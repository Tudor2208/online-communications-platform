package org.sdi.chatmanager.services.impl;

import org.sdi.chatmanager.dtos.*;
import org.sdi.chatmanager.entities.Group;
import org.sdi.chatmanager.entities.GroupMessage;
import org.sdi.chatmanager.entities.Message;
import org.sdi.chatmanager.entities.User;
import org.sdi.chatmanager.exceptions.NotFoundException;
import org.sdi.chatmanager.repositories.GroupMessageRepository;
import org.sdi.chatmanager.repositories.GroupRepository;
import org.sdi.chatmanager.repositories.MessageRepository;
import org.sdi.chatmanager.repositories.UserRepository;
import org.sdi.chatmanager.services.MessageService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMessageRepository groupMessageRepository;

    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository, GroupRepository groupRepository, GroupMessageRepository groupMessageRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMessageRepository = groupMessageRepository;
    }

    @Override
    public Message createMessage(CreateMessageRequest createMessageRequest) {
        User sender = userRepository.findById(createMessageRequest.getSenderId())
                .orElseThrow(() -> new NotFoundException("User with ID " + createMessageRequest.getSenderId() + " not found"));

        User recipient = userRepository.findById(createMessageRequest.getRecipientId())
                .orElseThrow(() -> new NotFoundException("User with ID " + createMessageRequest.getRecipientId() + " not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setText(createMessageRequest.getText());
        message.setTimestamp(new Date());
        message.setEdited(false);
        return messageRepository.save(message);
    }

    @Override
    public Message uploadVocalMessage(MultipartFile file, Long senderId, Long recipientId) {
        try{
            User sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new NotFoundException("User with ID " + senderId + " not found"));

            User recipient = userRepository.findById(recipientId)
                    .orElseThrow(() -> new NotFoundException("User with ID " + recipientId + " not found"));

            Message message = new Message();
            message.setSender(sender);
            message.setRecipient(recipient);
            message.setAudioData(file.getBytes());
            message.setTimestamp(new Date());
            message.setEdited(false);
            return messageRepository.save(message);
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file");
        }
    }

    @Override
    public Resource streamAudio(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message with ID " + messageId + " not found"));

        byte[] audioData = message.getAudioData();
        if (audioData != null) {
            ByteArrayResource resource = new ByteArrayResource(audioData);
            return resource;
        } else {
            throw new RuntimeException("Audio data not found");
        }
    }

    @Override
    public List<MessageResponse> getConversation(Long userId1, Long userId2) {
        User sender = userRepository.findById(userId1)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId1 + " not found"));

        User recipient = userRepository.findById(userId2)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId2 + " not found"));

        List<Message> allBySenderAndRecipient = messageRepository.findAllBySenderAndRecipient(sender, recipient);
        List<Message> allByRecipientAndSender = messageRepository.findAllBySenderAndRecipient(recipient, sender);

        List<Message> combinedMessages = new ArrayList<>();
        combinedMessages.addAll(allBySenderAndRecipient);
        combinedMessages.addAll(allByRecipientAndSender);

        return combinedMessages.stream()
                .sorted(Comparator.comparing(Message::getTimestamp))
                .map(message -> new MessageResponse(
                        message.getId(),
                        message.getSender().getId(),
                        message.getRecipient().getId(),
                        message.getText(),
                        message.getTimestamp(),
                        message.isEdited(),
                        message.getAudioData()
                ))
                .toList();
    }

    @Override
    public Message deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message with ID " + messageId + " not found"));
        messageRepository.delete(message);
        return message;
    }

    @Override
    public MessageResponse patchMessage(Long messageId, PatchMessageRequest patchMessageRequest) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message with ID " + messageId + " not found"));

        message.setText(patchMessageRequest.getText());
        message.setEdited(true);
        messageRepository.save(message);

        return new MessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getRecipient().getId(),
                message.getText(),
                message.getTimestamp(),
                message.isEdited(),
                message.getAudioData()
        );
    }

    @Override
    public List<ConversationResponse> getConversations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));

        List<Message> allBySender = messageRepository.findAllBySender(user);
        List<Message> allByRecipient = messageRepository.findAllByRecipient(user);

        List<Message> combinedMessages = new ArrayList<>();
        combinedMessages.addAll(allBySender);
        combinedMessages.addAll(allByRecipient);

        Map<String, List<Message>> conversations = new HashMap<>();
        for (Message message : combinedMessages) {
            String conversationKey = getConversationKey(message, user);
            conversations.computeIfAbsent(conversationKey, k -> new ArrayList<>()).add(message);
        }

        List<ConversationResponse> conversationResponses = new ArrayList<>();
        for (Map.Entry<String, List<Message>> entry : conversations.entrySet()) {
            List<Message> conversationMessages = entry.getValue();

            Message lastMessage = conversationMessages.stream()
                    .max(Comparator.comparing(Message::getTimestamp))
                    .orElseThrow(() -> new RuntimeException("No message found in the conversation"));

            ConversationResponse conversationResponse = getConversationResponse(userId, lastMessage);
            conversationResponses.add(conversationResponse);
        }

        List<Group> userGroups = groupRepository.findAll().stream()
                .filter(group -> group.getMembers().contains(user) || group.getOwner().equals(user))
                .toList();

        for (Group group : userGroups) {
            List<GroupMessage> groupMessages = groupMessageRepository.findAllByGroup(group);

            GroupMessage lastMessage = groupMessages.stream()
                    .max(Comparator.comparing(GroupMessage::getTimestamp))
                    .orElse(null);

            if (lastMessage != null) {
                ConversationResponse groupConversation = new ConversationResponse();
                groupConversation.setLastMessage(lastMessage.getText());
                groupConversation.setLastMessageTimestamp(lastMessage.getTimestamp());
                groupConversation.setPrivateConversation(false);
                groupConversation.setGroupName(group.getName());
                groupConversation.setGroupId(group.getId());
                conversationResponses.add(groupConversation);
            }
        }

        return conversationResponses;
    }

    private static ConversationResponse getConversationResponse(Long userId, Message lastMessage) {
        ConversationResponse conversationResponse = new ConversationResponse();
        conversationResponse.setLastMessage(lastMessage.getText());
        conversationResponse.setLastMessageTimestamp(lastMessage.getTimestamp());
        conversationResponse.setSent(Objects.equals(lastMessage.getSender().getId(), userId));
        conversationResponse.setPrivateConversation(true);

        if (Objects.equals(lastMessage.getSender().getId(), userId)) {
            conversationResponse.setFirstName(lastMessage.getRecipient().getFirstName());
            conversationResponse.setLastName(lastMessage.getRecipient().getLastName());
            conversationResponse.setFriendId(lastMessage.getRecipient().getId());
        } else {
            conversationResponse.setFirstName(lastMessage.getSender().getFirstName());
            conversationResponse.setLastName(lastMessage.getSender().getLastName());
            conversationResponse.setFriendId(lastMessage.getSender().getId());
        }
        return conversationResponse;
    }

    private String getConversationKey(Message message, User user) {
        if (message.getSender().equals(user)) {
            return message.getRecipient().getId() + "-" + message.getSender().getId();
        } else {
            return message.getSender().getId() + "-" + message.getRecipient().getId();
        }
    }
}
