package org.sdi.chatmanager.services;

import org.sdi.chatmanager.dtos.*;
import org.sdi.chatmanager.entities.Message;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public interface MessageService {
    Message createMessage(CreateMessageRequest createMessageRequest);
    Message uploadVocalMessage(MultipartFile file, Long senderId, Long recipientId);
    Resource streamAudio(Long messageId);
    List<MessageResponse> getConversation(Long userId1, Long recipientId);
    Message deleteMessage(Long messageId);
    MessageResponse patchMessage(Long messageId, PatchMessageRequest patchMessageRequest);
    List<ConversationResponse> getConversations(Long userId);
}
