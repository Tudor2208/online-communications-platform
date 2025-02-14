package org.sdi.chatmanager.services;

import org.sdi.chatmanager.dtos.CreateGroupMessageRequest;
import org.sdi.chatmanager.dtos.GroupMessageResponse;
import org.sdi.chatmanager.dtos.PatchGroupMessageRequest;
import org.sdi.chatmanager.entities.GroupMessage;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public interface GroupMessageService {

    GroupMessageResponse createGroupMessage(CreateGroupMessageRequest request);
    List<GroupMessageResponse> getGroupMessages(Long groupId);
    GroupMessage deleteGroupMessage(Long messageId);
    GroupMessageResponse patchGroupMessage(Long messageId, PatchGroupMessageRequest request);
    void uploadVocalMessage(MultipartFile file, Long senderId, Long groupId);
    Resource streamAudio(Long messageId);
}
