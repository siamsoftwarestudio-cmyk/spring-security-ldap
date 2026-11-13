package com.example.security.ldap.config;

import com.example.security.ldap.entity.Role;
import com.example.security.ldap.entity.User;
import com.example.security.ldap.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new User("admin", Role.ADMINS));
            }
            if (!userRepository.existsByUsername("ivan")) {
                userRepository.save(new User("ivan", Role.DEVELOPERS));
            }
        };
    }
}
