package org.sdi.chatmanager.controllers;

import jakarta.validation.Valid;
import org.sdi.chatmanager.dtos.CreateGroupMessageRequest;
import org.sdi.chatmanager.dtos.GroupMessageResponse;
import org.sdi.chatmanager.dtos.PatchGroupMessageRequest;
import org.sdi.chatmanager.entities.GroupMessage;
import org.sdi.chatmanager.repositories.GroupRepository;
import org.sdi.chatmanager.services.GroupMessageService;
import org.sdi.chatmanager.websocket.ChatWebSocketHandler;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/group-messages")
public class GroupMessageController {

    private final GroupMessageService groupMessageService;
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final GroupRepository groupRepository;

    public GroupMessageController(GroupMessageService groupMessageService, ChatWebSocketHandler chatWebSocketHandler, GroupRepository groupRepository) {
        this.groupMessageService = groupMessageService;
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.groupRepository = groupRepository;
    }

    @PostMapping
    public ResponseEntity<Void> createMessage(@RequestBody @Valid CreateGroupMessageRequest request) {
        GroupMessageResponse groupMessage = groupMessageService.createGroupMessage(request);
        groupRepository.findById(request.getGroupId()).ifPresent(group -> {
            group.getMembers().forEach(member -> chatWebSocketHandler.sendMessage(member.getId(), ChatWebSocketHandler.MessageType.GROUP_MESSAGE_CREATE, groupMessage));
            chatWebSocketHandler.sendMessage(group.getOwner().getId(), ChatWebSocketHandler.MessageType.GROUP_MESSAGE_CREATE, groupMessage);
        });
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<List<GroupMessageResponse>> getGroupMessages(@PathVariable("groupId") Long groupId) {
        return ResponseEntity.ok(groupMessageService.getGroupMessages(groupId));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteGroupMessage(@PathVariable("messageId") Long messageId) {
        GroupMessage groupMessage = groupMessageService.deleteGroupMessage(messageId);
        groupRepository.findById(groupMessage.getGroup().getId()).ifPresent(group -> {
            group.getMembers().forEach(member -> chatWebSocketHandler.sendMessage(member.getId(), ChatWebSocketHandler.MessageType.GROUP_MESSAGE_DELETE, messageId));
            chatWebSocketHandler.sendMessage(group.getOwner().getId(), ChatWebSocketHandler.MessageType.GROUP_MESSAGE_DELETE, messageId);
        });
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<GroupMessageResponse> patchGroupMessage(@PathVariable("messageId") Long messageId,
                                                                  @RequestBody @Valid PatchGroupMessageRequest request) {
        var message = groupMessageService.patchGroupMessage(messageId, request);
        GroupMessageResponse messageResponse = new GroupMessageResponse();
        messageResponse.setId(message.getId());
        messageResponse.setGroupId(message.getGroupId());
        messageResponse.setSenderId(message.getSenderId());
        messageResponse.setText(message.getText());
        messageResponse.setTimestamp(message.getTimestamp());
        messageResponse.setEdited(message.isEdited());
        groupRepository.findById(message.getGroupId()).ifPresent(group -> {
            group.getMembers().forEach(member -> chatWebSocketHandler.sendMessage(member.getId(), ChatWebSocketHandler.MessageType.GROUP_MESSAGE_PATCH, messageResponse));
            chatWebSocketHandler.sendMessage(group.getOwner().getId(), ChatWebSocketHandler.MessageType.GROUP_MESSAGE_PATCH, messageResponse);
        });
        return ResponseEntity.ok(messageResponse);
    }

    @PostMapping("/audio")
    public ResponseEntity<Void> uploadVocalMessage(
            @RequestPart("file") MultipartFile file,
            @RequestParam("senderId") Long senderId,
            @RequestParam("groupId") Long groupId) {

        groupMessageService.uploadVocalMessage(file, senderId, groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audio/{messageId}")
    public ResponseEntity<Resource> streamAudio(@PathVariable Long messageId) {
        Resource resource = groupMessageService.streamAudio(messageId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
