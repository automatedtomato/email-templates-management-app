package com.automatedtomato.email_templates_management_app.repository;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.userdetails.User;

import com.automatedtomato.email_templates_management_app.config.AbstractTestContainers;

@DataJpaTest  // Spring Data JPAのテスト用アノテーション
class UserRepositoryTest extends AbstractTestContainers {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {
        // given
        User user = new User(
            null, // IDはDBが生成
            "test@example.com",
            "password_hash",
            "Test USer"
            UserStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("Test USer");
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}
