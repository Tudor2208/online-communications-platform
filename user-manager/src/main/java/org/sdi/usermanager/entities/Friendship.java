package org.sdi.usermanager.entities;

import jakarta.persistence.*;
import org.sdi.usermanager.dtos.FriendshipId;

import java.io.Serializable;

@Entity
@Table(name = "friendships")
public class Friendship implements Serializable {

    @EmbeddedId
    private FriendshipId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("friendId")
    @JoinColumn(name = "user_id_2")
    private User friend;

    @Column(name = "confirmed", nullable = false)
    private boolean confirmed;

    // Getters și Setters
    public FriendshipId getId() {
        return id;
    }

    public void setId(FriendshipId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
