package org.sdi.chatmanager.controllers;

import jakarta.validation.Valid;
import org.sdi.chatmanager.dtos.CreateGroupInviteRequest;
import org.sdi.chatmanager.dtos.InviteResponse;
import org.sdi.chatmanager.services.GroupInviteService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invites")
@Validated
public class GroupInviteController {

    private final GroupInviteService groupInviteService;

    public GroupInviteController(GroupInviteService groupInviteService) {
        this.groupInviteService = groupInviteService;
    }

    @PostMapping
    public ResponseEntity<Void> createGroupInvite(@RequestBody @Valid CreateGroupInviteRequest createGroupInviteRequest) {
        groupInviteService.createGroupInvite(createGroupInviteRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{inviteId}/accept-invite")
    public ResponseEntity<Void> acceptGroupInvite(@PathVariable Long inviteId) {
        groupInviteService.acceptGroupInvite(inviteId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{inviteId}/deny-invite")
    public ResponseEntity<Void> denyGroupInvite(@PathVariable Long inviteId) {
        groupInviteService.denyGroupInvite(inviteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<InviteResponse>> getUsersInvites(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(groupInviteService.getUsersInvites(userId));
    }
}
