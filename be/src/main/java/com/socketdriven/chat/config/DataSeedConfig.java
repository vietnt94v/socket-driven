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
  ApplicationRunner seedDefaultUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      seedIfAbsent(
          userRepository, passwordEncoder, "viet", "viet@mail.com", "A@123", "Viet Nguyen");
      seedIfAbsent(
          userRepository, passwordEncoder, "vananh", "vananh@mail.com", "A@123", "Van Anh");
      seedIfAbsent(
          userRepository, passwordEncoder, "smarty", "smarty@mail.com", "A@123", "Smarty");
    };
  }

  private static void seedIfAbsent(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      String username,
      String email,
      String rawPassword,
      String displayName) {
    if (userRepository.existsByUsername(username)) {
      return;
    }
    User u = new User();
    u.setUsername(username);
    u.setEmail(email);
    u.setPasswordHash(passwordEncoder.encode(rawPassword));
    u.setDisplayName(displayName);
    userRepository.save(u);
  }
}
