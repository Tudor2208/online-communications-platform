package org.sdi.chatmanager.services;

import org.springframework.stereotype.Component;

@Component
public interface UserService {

    void createUser(String message);
}
