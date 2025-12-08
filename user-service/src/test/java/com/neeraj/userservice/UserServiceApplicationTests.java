package com.neeraj.userservice;

import com.neeraj.userservice.entity.User;
import com.neeraj.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class UserServiceApplicationTests {

    @Autowired
    private UserRepository userRepository;
    public static final int NO_OF_USERS = 10;

    @Test
    void contextLoads() {
    }

    @Disabled
    @Test
    void createUsers() {
        for (int i = 1; i <= NO_OF_USERS; i++) {
            User user = User.builder()
                    .firstName("User " + i)
                    .lastName("Lastname " + i)
                    .email("user" + i + "@example.com")
                    .address("Address Example Street " + i)
                    .alertEnabled(i % 2 == 0)
                    .energyAlertThreshold(1000.0 + i)
                    .build();
            userRepository.save(user);
        }

        log.info("Created {} users in the database", NO_OF_USERS);
    }

}
