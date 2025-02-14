package org.sdi.chatmanager.services;

import org.sdi.chatmanager.dtos.CreateGroupInviteRequest;
import org.sdi.chatmanager.dtos.InviteResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface GroupInviteService {

    void createGroupInvite(CreateGroupInviteRequest request);
    void acceptGroupInvite(Long inviteId);
    void denyGroupInvite(Long inviteId);
    List<InviteResponse> getUsersInvites(Long userId);
}
