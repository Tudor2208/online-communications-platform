package org.sdi.chatmanager.repositories;

import org.sdi.chatmanager.entities.Message;
import org.sdi.chatmanager.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllBySenderAndRecipient(User sender, User recipient);
    List<Message> findAllBySender(User sender);
    List<Message> findAllByRecipient(User recipient);
}
