package com.godare.app;

import com.godare.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void testUserRepositoryExistEmail() {
        boolean exists = userRepository.existsByEmail("maria@example.com");
        System.out.println("Existe usuario: " + exists);
    }

    @Test
    void testUserRepositoryNoExistEmail() {
        boolean exists = userRepository.existsByEmail("example@example.com");
        System.out.println("Existe usuario: " + exists);
    }
}