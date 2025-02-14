package org.sdi.usermanager.services.impl;

import org.sdi.usermanager.dtos.UserResponse;
import org.sdi.usermanager.entities.User;
import org.sdi.usermanager.exceptions.NotFoundException;
import org.sdi.usermanager.repositories.UserRepository;
import org.sdi.usermanager.services.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)));

        UserResponse userResponse = new UserResponse();
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        return userResponse;
    }
}
