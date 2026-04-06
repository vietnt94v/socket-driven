package com.socketdriven.chat.config;

import com.socketdriven.chat.domain.User;
import com.socketdriven.chat.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeedConfig {

  @Bean
  ApplicationRunner seedDemoUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      if (userRepository.count() == 0) {
        User u = new User();
        u.setUsername("demo");
        u.setEmail("demo@local.test");
        u.setPasswordHash(passwordEncoder.encode("demo"));
        u.setDisplayName("Demo User");
        userRepository.save(u);
      }
    };
  }
}
