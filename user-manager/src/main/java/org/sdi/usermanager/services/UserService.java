package org.sdi.usermanager.services;

import org.sdi.usermanager.dtos.UserResponse;
import org.springframework.stereotype.Component;

@Component
public interface UserService {

    UserResponse getUser(Long userId);
}
