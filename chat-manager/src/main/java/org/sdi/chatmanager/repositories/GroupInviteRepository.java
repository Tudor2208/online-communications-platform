package org.sdi.chatmanager.repositories;

import org.sdi.chatmanager.entities.Group;
import org.sdi.chatmanager.entities.GroupInvite;
import org.sdi.chatmanager.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupInviteRepository extends JpaRepository<GroupInvite, Long> {

    List<GroupInvite> findAllByUser(User user);
    List<GroupInvite> findAllByGroup(Group group);
}
