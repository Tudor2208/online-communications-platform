package org.sdi.chatmanager.services.impl;

import org.sdi.chatmanager.entities.Role;
import org.sdi.chatmanager.entities.User;
import org.sdi.chatmanager.repositories.UserRepository;
import org.sdi.chatmanager.services.UserService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private static final String REGEX = "User\\{id=(\\d+), email='([^']+)', firstName='([^']+)', lastName='([^']+)', password='([^']+)', role=([^}]+)}";

    private final Pattern pattern = Pattern.compile(REGEX);

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @KafkaListener(topics = "create.user", groupId = "my-group")
    public void createUser(String message) {
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            Long id = Long.valueOf(matcher.group(1));
            String email = matcher.group(2);
            String firstName = matcher.group(3);
            String lastName = matcher.group(4);
            String password = matcher.group(5);
            Role role = Role.valueOf(matcher.group(6));

            User user = new User();
            user.setId(id);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(password);
            user.setRole(role);

            userRepository.save(user);
        } else {
            System.out.println("No match found in message: " + message);
        }
    }
}
