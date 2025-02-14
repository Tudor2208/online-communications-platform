package org.sdi.usermanager.dtos;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FriendshipId implements Serializable {

    private Long userId;
    private Long friendId; // This should remain logical but maps to user_id_2

    // Constructori
    public FriendshipId() {}

    public FriendshipId(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    // Getters și Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    // hashCode și equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendshipId that = (FriendshipId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(friendId, that.friendId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, friendId);
    }
}

