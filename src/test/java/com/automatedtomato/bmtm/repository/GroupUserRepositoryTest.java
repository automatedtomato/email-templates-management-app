package com.automatedtomato.bmtm.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.automatedtomato.bmtm.config.AbstractTestContainers;
import com.automatedtomato.bmtm.entity.Group;
import com.automatedtomato.bmtm.entity.GroupUser;
import com.automatedtomato.bmtm.entity.User;
import com.automatedtomato.bmtm.enums.GroupStatus;
import com.automatedtomato.bmtm.enums.Role;
import com.automatedtomato.bmtm.enums.UserStatus;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class GroupUserRepositoryTest extends AbstractTestContainers {

    @Autowired
    private GroupUserRepository groupUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    private User testUser;
    private Group testGroup;
    private User editorUser;
    private User adminUser;
    private Group testGroup2;

    @BeforeEach
    void setUp() {
        // クリーンアップ
        groupUserRepository.deleteAll();
        userRepository.deleteAll();
        groupRepository.deleteAll();

        // 基本テストユーザーの作成
        testUser = userRepository.save(User.builder()
            .email("test@example.com")
            .passwordHash("password_hash")
            .name("Test User")
            .status(UserStatus.ACTIVE)
            .build());

        // 基本テストグループの作成
        testGroup = groupRepository.save(Group.builder()
            .name("Test Group")
            .description("This is test group")
            .status(GroupStatus.ACTIVE)
            .build());

        // 追加のテストデータ
        testGroup2 = groupRepository.save(Group.builder()
            .name("Test Group 2")
            .description("This is test group 2")
            .status(GroupStatus.ACTIVE)
            .build());

        editorUser = userRepository.save(User.builder()
            .email("editor@example.com")
            .passwordHash("password_hash2")
            .name("Editor User")
            .status(UserStatus.ACTIVE)
            .build());

        adminUser = userRepository.save(User.builder()
            .email("admin@example.com")
            .passwordHash("password_hash3")
            .name("Admin User")
            .status(UserStatus.ACTIVE)
            .build());
    }

    @Test
    void shouldSaveGroupUser() {
        // Arrange
        GroupUser groupUser = GroupUser.builder()
            .user(testUser)
            .group(testGroup)
            .role(Role.VIEWER)
            .build();

        // Act
        GroupUser savedGroupUser = groupUserRepository.save(groupUser);

        // Assert
        // IDが自動生成されていることを確認
        assertThat(savedGroupUser.getId()).isNotNull();
        
        // ユーザー関連付けが正しいことを確認
        assertThat(savedGroupUser.getUser().getId()).isEqualTo(testUser.getId());
        
        // グループ関連付けが正しいことを確認
        assertThat(savedGroupUser.getGroup().getId()).isEqualTo(testGroup.getId());
        
        // ロールが正しく設定されていることを確認
        assertThat(savedGroupUser.getRole()).isEqualTo(Role.VIEWER);
        
        // タイムスタンプが自動設定されていることを確認
        assertThat(savedGroupUser.getCreatedAt()).isNotNull();
        assertThat(savedGroupUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindAllByUser() {
        // Arrange
        GroupUser membership1 = groupUserRepository.save(GroupUser.builder()
            .user(testUser)
            .group(testGroup)
            .role(Role.VIEWER)
            .build());

        GroupUser membership2 = groupUserRepository.save(GroupUser.builder()
            .user(testUser)
            .group(testGroup2)
            .role(Role.EDITOR)
            .build());

        // 別ユーザーの所属（分離確認用）
        groupUserRepository.save(GroupUser.builder()
            .user(editorUser)
            .group(testGroup)
            .role(Role.VIEWER)
            .build());

        // Act
        List<GroupUser> foundGroupUsers = groupUserRepository.findAllByUser(testUser);
        
        // Assert
        // 期待される数のメンバーシップが取得できていることを確認
        assertThat(foundGroupUsers).hasSize(2);
        
        // 所属グループ名が期待通りであることを確認（順序は問わない）
        assertThat(foundGroupUsers)
            .extracting(groupUser -> groupUser.getGroup().getName())
            .containsExactlyInAnyOrder("Test Group", "Test Group 2");
        
        // 各グループでのロールが期待通りであることを確認（順序は問わない）
        assertThat(foundGroupUsers)
            .extracting(GroupUser::getRole)
            .containsExactlyInAnyOrder(Role.VIEWER, Role.EDITOR);
    }

    @Test
    void shouldFindAllByGroupId() {
        // Arrange
        groupUserRepository.save(GroupUser.builder()
            .user(testUser)
            .group(testGroup)
            .role(Role.VIEWER)
            .build());

        groupUserRepository.save(GroupUser.builder()
            .user(editorUser)
            .group(testGroup)
            .role(Role.EDITOR)
            .build());

        // 別グループの所属（分離確認用）
        groupUserRepository.save(GroupUser.builder()
            .user(adminUser)
            .group(testGroup2)
            .role(Role.ADMIN)
            .build());

        // Act
        List<GroupUser> foundGroupUsers = groupUserRepository.findAllByGroupId(testGroup.getId());
        
        // Assert
        // 期待される数のメンバーが取得できていることを確認
        assertThat(foundGroupUsers).hasSize(2);
        
        // メンバーのユーザー名が期待通りであることを確認（順序は問わない）
        assertThat(foundGroupUsers)
            .extracting(GroupUser::getUser)
            .extracting(User::getName)
            .containsExactlyInAnyOrder("Test User", "Editor User");
        
        // メンバーのロールが期待通りであることを確認（順序は問わない）
        assertThat(foundGroupUsers)
            .extracting(GroupUser::getRole)
            .containsExactlyInAnyOrder(Role.VIEWER, Role.EDITOR);
        
        // すべてのメンバーシップが共通の条件を満たすことを確認
        assertThat(foundGroupUsers)
            .allSatisfy(groupUser -> {
                // タイムスタンプが設定されていることを確認
                assertThat(groupUser.getCreatedAt()).isNotNull();
                assertThat(groupUser.getUpdatedAt()).isNotNull();
                // 正しいグループに所属していることを確認
                assertThat(groupUser.getGroup().getId()).isEqualTo(testGroup.getId());
                // ユーザーが有効であることを確認
                assertThat(groupUser.getUser().getStatus()).isEqualTo(UserStatus.ACTIVE);
            });
    }

    @Test
    void shouldFindByGroupIdAndRole() {
        // Arrange
        groupUserRepository.save(GroupUser.builder()
            .user(testUser)
            .group(testGroup)
            .role(Role.VIEWER)
            .build());

        groupUserRepository.save(GroupUser.builder()
            .user(editorUser)
            .group(testGroup)
            .role(Role.EDITOR)
            .build());

        groupUserRepository.save(GroupUser.builder()
            .user(adminUser)
            .group(testGroup)
            .role(Role.VIEWER)
            .build());

        // Act
        List<GroupUser> foundGroupUsers = groupUserRepository.findByGroupIdAndRole(
            testGroup.getId(), 
            Role.VIEWER
        );
        
        // Assert
        // 指定されたロールを持つメンバーの数を確認
        assertThat(foundGroupUsers).hasSize(2);
        
        // グループID、ロール、ユーザー名の組み合わせが期待通りであることを確認
        assertThat(foundGroupUsers)
            .extracting(
                groupUser -> groupUser.getGroup().getId(),
                GroupUser::getRole,
                groupUser -> groupUser.getUser().getName()
            )
            .containsExactlyInAnyOrder(
                tuple(testGroup.getId(), Role.VIEWER, "Test User"),
                tuple(testGroup.getId(), Role.VIEWER, "Admin User")
            );
        
        // すべてのメンバーシップが共通の条件を満たすことを確認
        assertThat(foundGroupUsers)
            .allSatisfy(groupUser -> {
                // タイムスタンプが設定されていることを確認
                assertThat(groupUser.getCreatedAt()).isNotNull();
                assertThat(groupUser.getUpdatedAt()).isNotNull();
                // ユーザーが有効であることを確認
                assertThat(groupUser.getUser().getStatus()).isEqualTo(UserStatus.ACTIVE);
            });
    }

    @Test
    void shouldUpdateGroupUserRole() {
        // Arrange
        GroupUser groupUser = groupUserRepository.save(GroupUser.builder()
            .user(testUser)
            .group(testGroup)
            .role(Role.VIEWER)
            .build());

        // Act
        groupUser.setRole(Role.EDITOR);
        GroupUser updatedGroupUser = groupUserRepository.save(groupUser);

        // Assert
        // ロールが更新されていることを確認
        assertThat(updatedGroupUser.getRole()).isEqualTo(Role.EDITOR);
        // 更新日時が作成日時より後であることを確認
        assertThat(updatedGroupUser.getUpdatedAt()).isAfter(updatedGroupUser.getCreatedAt());
    }

    @Test
    void shouldNotSaveDuplicateGroupUser() {
        // Arrange
        groupUserRepository.save(GroupUser.builder()
            .user(testUser)
            .group(testGroup)
            .role(Role.VIEWER)
            .build());

        GroupUser duplicateGroupUser = GroupUser.builder()
            .user(testUser)
            .group(testGroup)
            .role(Role.EDITOR)
            .build();

        // Act & Assert
        // 同じユーザー・グループの組み合わせで2回目の保存が失敗することを確認
        assertThatThrownBy(() -> groupUserRepository.save(duplicateGroupUser))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldNotSaveGroupUserWithoutRequiredFields() {
        // Without User
        GroupUser groupUserWithoutUser = GroupUser.builder()
            .group(testGroup)
            .role(Role.VIEWER)
            .build();

        // Without Group
        GroupUser groupUserWithoutGroup = GroupUser.builder()
            .user(testUser)
            .role(Role.VIEWER)
            .build();

        // Without Role
        GroupUser groupUserWithoutRole = GroupUser.builder()
            .user(testUser)
            .group(testGroup)
            .build();

        // Assert
        // ユーザーなしでの保存が失敗することを確認
        assertThatThrownBy(() -> groupUserRepository.save(groupUserWithoutUser))
            .isInstanceOf(Exception.class);
        
        // グループなしでの保存が失敗することを確認
        assertThatThrownBy(() -> groupUserRepository.save(groupUserWithoutGroup))
            .isInstanceOf(Exception.class);
        
        // ロールなしでの保存が失敗することを確認
        assertThatThrownBy(() -> groupUserRepository.save(groupUserWithoutRole))
            .isInstanceOf(Exception.class);
    }
}