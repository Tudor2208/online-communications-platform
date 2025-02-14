package org.sdi.usermanager.repositories;

import org.sdi.usermanager.entities.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE (f.id.userId = :userId OR f.id.friendId = :userId) AND f.confirmed = true")
    List<Friendship> findConfirmedFriendships(@Param("userId") Long userId);
    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);
    List<Friendship> findByFriendIdAndConfirmed(Long friendId, boolean confirmed);
}
