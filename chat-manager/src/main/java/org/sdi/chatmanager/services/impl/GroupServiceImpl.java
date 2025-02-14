package org.sdi.chatmanager.services.impl;

import org.sdi.chatmanager.dtos.CreateGroupRequest;
import org.sdi.chatmanager.dtos.GroupResponse;
import org.sdi.chatmanager.dtos.PatchGroupRequest;
import org.sdi.chatmanager.entities.Group;
import org.sdi.chatmanager.entities.GroupInvite;
import org.sdi.chatmanager.entities.GroupMessage;
import org.sdi.chatmanager.entities.User;
import org.sdi.chatmanager.exceptions.ExistingMemberException;
import org.sdi.chatmanager.exceptions.NotFoundException;
import org.sdi.chatmanager.repositories.GroupInviteRepository;
import org.sdi.chatmanager.repositories.GroupMessageRepository;
import org.sdi.chatmanager.repositories.GroupRepository;
import org.sdi.chatmanager.repositories.UserRepository;
import org.sdi.chatmanager.services.GroupService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final GroupMessageRepository groupMessageRepository;

    public GroupServiceImpl(UserRepository userRepository, GroupRepository groupRepository, GroupInviteRepository groupInviteRepository, GroupMessageRepository groupMessageRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupInviteRepository = groupInviteRepository;
        this.groupMessageRepository = groupMessageRepository;
    }

    @Override
    public Group createGroup(CreateGroupRequest createGroupRequest) {
        User user = userRepository.findById(createGroupRequest.getOwnerId())
                .orElseThrow(() -> new NotFoundException("User with ID " + createGroupRequest.getOwnerId() + " not found"));
        Group group = new Group();
        group.setName(createGroupRequest.getName());
        group.setOwner(user);
        return groupRepository.save(group);
    }

    @Override
    public GroupResponse getGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group with ID " + groupId + " not found"));

        GroupResponse groupResponse = new GroupResponse();
        groupResponse.setId(group.getId());
        groupResponse.setName(group.getName());
        groupResponse.setOwnerId(group.getOwner().getId());

        List<Long> ids = group.getMembers().stream().map(User::getId).toList();
        groupResponse.setMembersIds(ids);
        return groupResponse;
    }

    @Override
    public GroupResponse addMember(Long groupId, Long memberId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group with ID " + groupId + " not found"));

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("User with ID " + memberId + " not found"));

        if (group.getMembers().contains(member)) {
            throw new ExistingMemberException("User with ID " + member.getId() + " is already in this group");
        }

        if (group.getOwner().equals(member)) {
            throw new ExistingMemberException("User with ID " + member.getId() + " is the group's owner");
        }

        group.getMembers().add(member);
        Group savedGroup = groupRepository.save(group);
        return buildGroupResponse(savedGroup);
    }

    @Override
    public void deleteGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group with ID " + groupId + " not found"));
        List<GroupInvite> allGroupInvites = groupInviteRepository.findAllByGroup(group);
        List<GroupMessage> allGroupMessages = groupMessageRepository.findAllByGroup(group);

        groupMessageRepository.deleteAll(allGroupMessages);
        groupInviteRepository.deleteAll(allGroupInvites);
        groupRepository.delete(group);
    }

    @Override
    public void removeMember(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group with ID " + groupId + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));

        group.getMembers().remove(user);
        groupRepository.save(group);
    }

    @Override
    public GroupResponse patchGroup(Long groupId, PatchGroupRequest patchGroupRequest) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group with ID " + groupId + " not found"));
        group.setName(patchGroupRequest.getName());
        groupRepository.save(group);
        return buildGroupResponse(group);
    }

    @Override
    public List<GroupResponse> getUsersGroups(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));
        List<Group> allGroups = groupRepository.findAll();
        return allGroups.stream()
                .filter(group -> group.getMembers().contains(user) || group.getOwner().equals(user))
                .map(this::buildGroupResponse)
                .toList();
    }

    private GroupResponse buildGroupResponse(Group group) {
        GroupResponse groupResponse = new GroupResponse();
        groupResponse.setId(group.getId());
        groupResponse.setName(group.getName());
        groupResponse.setOwnerId(group.getOwner().getId());
        groupResponse.setMembersIds(group.getMembers().stream().map(User::getId).toList());
        return groupResponse;
    }
}
