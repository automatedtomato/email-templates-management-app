package com.automatedtomato.bmtm.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.automatedtomato.bmtm.config.AbstractTestContainers;
import com.automatedtomato.bmtm.entity.User;
import com.automatedtomato.bmtm.enums.UserStatus;

@DataJpaTest  // Spring Data JPAのテスト用アノテーション
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 実際のDBを使用
@ActiveProfiles("test")  // テスト用のプロファイル
class UserRepositoryTest extends AbstractTestContainers {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();  // 各テスト前にDBをクリーンナップ
    }

    @Test
    void shouldSaveUser() {
        // Arrange (準備)
        User user = User.builder()  // IDは自動生成される
            .email("test@example.com")
            .passwordHash("hashed_password")
            .name("Test User")
            .status(UserStatus.ACTIVE)
            .build();

        // Act (実行)
        User savedUser = userRepository.save(user);

        // Assert (検証)
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByEmail() {
        // Arrange (準備)
        User user = User.builder()  // IDは自動生成される
            .email("test@example.com")
            .passwordHash("hashed_password")
            .name("Test User")
            .status(UserStatus.ACTIVE)
            .build();

        User savedUser = userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Assert (検証)
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(savedUser.getEmail());
    }

    @Test
    void shouldNotFindUserByNonExistentEmail() {
        // Act (実行)
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example");

        // Assert (検証)
        assertThat(foundUser).isEmpty();
    }

    @Test
void shouldUpdateUserStatus() {
    // Arrange (準備)
    User user = User.builder()
        .email("test@example.com")
        .passwordHash("hashed_password")
        .name("Test User")
        .status(UserStatus.ACTIVE)
        .build();
    
    User savedUser = userRepository.save(user);
    
    // 少し待機して時間差を作る
    try {
        Thread.sleep(10);  // 10ミリ秒待機
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    
    // Act (実行)
    savedUser.setStatus(UserStatus.INACTIVE);
    User updatedUser = userRepository.save(savedUser);
    
    // Assert (検証)
    assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
    assertThat(updatedUser.getUpdatedAt()).isAfter(updatedUser.getCreatedAt());
}

    @Test
    void shouldNotSaveDuplicateEmail() {
        // Arrange (準備)
        String duplicateEmail = "test@example.com";
        User firstUser = User.builder()
            .email(duplicateEmail)
            .passwordHash("hashed_password")
            .name("First User")
            .status(UserStatus.ACTIVE)
            .build();

        User secondUser = User.builder()
            .email(duplicateEmail)
            .passwordHash("different_password")
            .name("Second User")
            .status(UserStatus.ACTIVE)
            .build();

        // Act (実行)
        userRepository.save(firstUser);

        assertThatThrownBy(() -> userRepository.save(secondUser))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("constraint");
    }

    @Test
    void shouldNotSaveUserWithoutRequiredFields() {
        // Arrange (準備)
        User userWithoutEmail = User.builder()
            .passwordHash("hashed_password")
            .name("Test User")
            .status(UserStatus.ACTIVE)
            .build();

        User userWithoutName = User.builder()
            .email("test@example.com")
            .passwordHash("hashed_password")
            .status(UserStatus.ACTIVE)
            .build();

        User userWithoutPasswordHash = User.builder()
            .email("test@example.com")
            .name("Test User")
            .status(UserStatus.ACTIVE)
            .build();

        // Act & Assert
        assertThatThrownBy(() -> userRepository.save(userWithoutEmail))
            .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> userRepository.save(userWithoutName))
            .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> userRepository.save(userWithoutPasswordHash))
            .isInstanceOf(Exception.class);
    }
}