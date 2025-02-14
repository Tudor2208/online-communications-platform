package org.sdi.usermanager.services.impl;

import org.sdi.usermanager.dtos.FriendResponse;
import org.sdi.usermanager.entities.Friendship;
import org.sdi.usermanager.dtos.FriendshipId;
import org.sdi.usermanager.entities.User;
import org.sdi.usermanager.exceptions.NotFoundException;
import org.sdi.usermanager.repositories.FriendshipRepository;
import org.sdi.usermanager.repositories.UserRepository;
import org.sdi.usermanager.services.FriendshipService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendshipServiceImpl implements FriendshipService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public FriendshipServiceImpl(UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Override
    public void createFriendship(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId1)));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId2)));

        if (friendshipRepository.findByUserIdAndFriendId(userId1, userId2).isPresent() ||
                friendshipRepository.findByUserIdAndFriendId(userId2, userId1).isPresent()) {
            throw new RuntimeException("Friendship already exists");
        }

        Friendship newFriendship = new Friendship();
        newFriendship.setId(new FriendshipId(userId1, userId2));
        newFriendship.setUser(user1);
        newFriendship.setFriend(user2);
        newFriendship.setConfirmed(false);

        friendshipRepository.save(newFriendship);
    }

    public void deleteFriendship(Long userId1, Long userId2) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId1, userId2)
                .orElseGet(() -> friendshipRepository.findByUserIdAndFriendId(userId2, userId1)
                        .orElseThrow(() -> new NotFoundException("Friendship not found between user " + userId1 + " and user " + userId2)));

        friendshipRepository.delete(friendship);
    }

    @Override
    public List<FriendResponse> getFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findConfirmedFriendships(userId);

        return friendships.stream()
                .map(friendship -> {
                    User friend = friendship.getUser().getId().equals(userId)
                            ? friendship.getFriend()
                            : friendship.getUser();
                    return new FriendResponse(friend.getId(), friend.getFirstName(), friend.getLastName());
                })
                .toList();
    }

    public List<FriendResponse> getPendingFriendRequests(Long userId) {
        List<Friendship> pendingFriendships = friendshipRepository.findByFriendIdAndConfirmed(userId, false);

        return pendingFriendships.stream()
                .map(friendship -> {
                    User user = friendship.getUser();  // The user who sent the friend request
                    return new FriendResponse(user.getId(), user.getFirstName(), user.getLastName());
                })
                .toList();
    }

    @Override
    public void acceptFriendRequest(Long userId1, Long userId2) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId1, userId2)
                .orElseGet(() -> friendshipRepository.findByUserIdAndFriendId(userId2, userId1)
                        .orElseThrow(() -> new NotFoundException("Friendship not found between user " + userId1 + " and user " + userId2)));
        friendship.setConfirmed(true);
        friendshipRepository.save(friendship);
    }
}
