package org.sdi.usermanager.controllers;

import org.sdi.usermanager.dtos.FriendResponse;
import org.sdi.usermanager.services.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/friendships")
@CrossOrigin
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping("/{userId1}/{userId2}")
    public ResponseEntity<Void> createFriendship(@PathVariable Long userId1, @PathVariable Long userId2) {
        friendshipService.createFriendship(userId1, userId2);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId1}/{userId2}")
    public ResponseEntity<Void> deleteFriendship(@PathVariable Long userId1, @PathVariable Long userId2) {
        friendshipService.deleteFriendship(userId1, userId2);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getUsersFriends(@RequestParam("userId") Long userId) {
        return ok(friendshipService.getFriends(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FriendResponse>> getPendingFriendRequests(@RequestParam("userId") Long userId) {
        return ok(friendshipService.getPendingFriendRequests(userId));
    }

    @PostMapping("/accept/{userId1}/{userId2}")
    public ResponseEntity<Void> acceptFriendRequest(@PathVariable Long userId1, @PathVariable Long userId2) {
        friendshipService.acceptFriendRequest(userId1, userId2);
        return ResponseEntity.noContent().build();
    }
}
