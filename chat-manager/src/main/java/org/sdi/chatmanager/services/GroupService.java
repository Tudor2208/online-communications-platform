package org.sdi.chatmanager.services;

import org.sdi.chatmanager.dtos.CreateGroupRequest;
import org.sdi.chatmanager.dtos.GroupResponse;
import org.sdi.chatmanager.dtos.PatchGroupRequest;
import org.sdi.chatmanager.entities.Group;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface GroupService {

    Group createGroup(CreateGroupRequest createGroupRequest);
    GroupResponse getGroup(Long groupId);
    GroupResponse addMember(Long groupId, Long userId);
    void deleteGroup(Long groupId);
    void removeMember(Long groupId, Long userId);
    GroupResponse patchGroup(Long groupId, PatchGroupRequest patchGroupRequest);
    List<GroupResponse> getUsersGroups(Long userId);
}
