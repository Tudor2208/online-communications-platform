package org.sdi.chatmanager.services.impl;

import org.sdi.chatmanager.dtos.CreateGroupInviteRequest;
import org.sdi.chatmanager.dtos.InviteResponse;
import org.sdi.chatmanager.entities.Group;
import org.sdi.chatmanager.entities.GroupInvite;
import org.sdi.chatmanager.entities.User;
import org.sdi.chatmanager.exceptions.ExistingMemberException;
import org.sdi.chatmanager.exceptions.NotFoundException;
import org.sdi.chatmanager.exceptions.UserAlreadyInvitedException;
import org.sdi.chatmanager.repositories.GroupInviteRepository;
import org.sdi.chatmanager.repositories.GroupRepository;
import org.sdi.chatmanager.repositories.UserRepository;
import org.sdi.chatmanager.services.GroupInviteService;
import org.sdi.chatmanager.services.GroupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupInviteServiceImpl implements GroupInviteService {

    private final GroupInviteRepository groupInviteRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupService groupService;

    public GroupInviteServiceImpl(GroupInviteRepository groupInviteRepository, UserRepository userRepository, GroupRepository groupRepository, GroupService groupService) {
        this.groupInviteRepository = groupInviteRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupService = groupService;
    }

    @Override
    public void createGroupInvite(CreateGroupInviteRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User with ID " + request.getUserId() + " not found"));

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group with ID " + request.getGroupId() + " not found"));

        if (group.getMembers().contains(user)) {
            throw new ExistingMemberException("User with ID " + request.getUserId() + " is already a member of the group: " + request.getGroupId());
        }

        if (group.getOwner().equals(user)) {
            throw new ExistingMemberException("You cannot invite yourself to the group.");
        }

        List<GroupInvite> allUsersInvites = groupInviteRepository.findAllByUser(user);
        Optional<GroupInvite> opt = allUsersInvites.stream()
                .filter(invite -> invite.getGroup().equals(group))
                .findAny();

        opt.ifPresentOrElse(x -> {
            throw new UserAlreadyInvitedException("User with ID " + request.getUserId() + " is already invited in group: " + request.getGroupId());
        }, () -> {
            GroupInvite groupInvite = new GroupInvite();
            groupInvite.setGroup(group);
            groupInvite.setUser(user);
            groupInviteRepository.save(groupInvite);
        });
    }

    @Override
    public void acceptGroupInvite(Long inviteId) {
        GroupInvite groupInvite = groupInviteRepository.findById(inviteId)
                .orElseThrow(() -> new NotFoundException("Group Invite with ID " + inviteId + " not found"));

        groupService.addMember(groupInvite.getGroup().getId(), groupInvite.getUser().getId());
        groupInviteRepository.delete(groupInvite);
    }

    @Override
    public void denyGroupInvite(Long inviteId) {
        GroupInvite groupInvite = groupInviteRepository.findById(inviteId)
                .orElseThrow(() -> new NotFoundException("Group Invite with ID " + inviteId + " not found"));

        groupInviteRepository.delete(groupInvite);
    }

    @Override
    public List<InviteResponse> getUsersInvites(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));

        List<GroupInvite> allByUser = groupInviteRepository.findAllByUser(user);
        return allByUser.stream()
                .map(groupInvite -> {
                    InviteResponse inviteResponse = new InviteResponse();
                    inviteResponse.setInviteId(groupInvite.getId());
                    inviteResponse.setGroupName(groupInvite.getGroup().getName());
                    inviteResponse.setOwnerId(groupInvite.getGroup().getOwner().getId());
                    return inviteResponse;
                })
                .toList();
    }
}
