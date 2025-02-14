package org.sdi.usermanager.services;

import org.sdi.usermanager.dtos.FriendResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface FriendshipService {

    void createFriendship(Long userId1, Long userId2);
    void deleteFriendship(Long userId1, Long userId2);
    List<FriendResponse> getFriends(Long userId);
    List<FriendResponse> getPendingFriendRequests(Long userId);
    void acceptFriendRequest(Long userId1, Long userId2);
}
