package com.automatedtomato.bmtm.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
@ActiveProfiles("test2")
class GroupRepositoryTest extends AbstractTestContainers {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupUserRepository groupUserRepository;

    @Autowired
    private UserRepository userRepository;

    private Group testGroup;

    @BeforeEach
    void setUp() {
        groupRepository.deleteAll();  // 各テスト前にDBをクリーンナップ
        groupUserRepository.deleteAll();
        userRepository.deleteAll();

        // 基本テストデータ
        testGroup = groupRepository.save(Group.builder()
            .name("Test Group")
            .description("This is a test group")
            .status(GroupStatus.ACTIVE)
            .build());
    }

    @Test
    void shouldSaveGroup() {    
        // Act (実行)
        Group savedGroup = groupRepository.save(testGroup);

        // Assert (検証)
        assertThat(savedGroup.getId()).isNotNull();
        assertThat(savedGroup.getName()).isEqualTo("Test Group");
        assertThat(savedGroup.getDescription()).isEqualTo("This is a test group");
        assertThat(savedGroup.getStatus()).isEqualTo(GroupStatus.ACTIVE);
        assertThat(savedGroup.getCreatedAt()).isNotNull();
        assertThat(savedGroup.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindByName() {
        Optional<Group> foundGroup = groupRepository.findByName(testGroup.getName());

        assertThat(foundGroup).isNotNull();
        assertThat(foundGroup.get().getName()).isEqualTo(testGroup.getName());
    }

    @Test
    void shouldNotFindGroupByNonExistentName() {
        Optional<Group> foundGroup = groupRepository.findByName("Nonexistent Group");

        assertThat(foundGroup).isEmpty();
    }

    @Test
    void shouldFindAllByStatus() {
        Group testGroup2 = Group.builder()
            .name("Test Group 2")
            .description("This is a test group 2")
            .status(GroupStatus.ACTIVE)
            .build();

        Group testGroup3 = Group.builder()
            .name("Inactive Group ")
            .description("This group should not be found")
            .status(GroupStatus.INACTIVE)
            .build();

        groupRepository.save(testGroup2);
        groupRepository.save(testGroup3);

        List<Group> foundGroups = groupRepository.findAllByStatus(GroupStatus.ACTIVE);

        assertThat(foundGroups).hasSize(2);
        assertThat(foundGroups).extracting(Group::getName)
            .containsExactlyInAnyOrder("Test Group", "Test Group 2");   
    }

    @Test
    void shouldNotSaveDuplicateGroup() {
        // Arrange
        Group duplicateGroup = Group.builder()
            .name("Test Group")
            .description("This group should not be saved")
            .status(GroupStatus.ACTIVE)
            .build();

        // Act & Assert
        assertThatThrownBy(() -> groupRepository.save(duplicateGroup))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("constraint");
    }

    @Test
    void shouldNotSaveGroupWithoutRequiredFields() {
        // Arrange
        Group groupWithoutName = Group.builder()
            .description("This group should not be saved")
            .status(GroupStatus.ACTIVE)
            .build();

        Group groupWithoutStatus = Group.builder()
            .name("Non-Status Group")
            .description("This group should not be saved")
            .build();

        // Act & Assert
        assertThatThrownBy(() -> groupRepository.save(groupWithoutName))
            .isInstanceOf(Exception.class);
        
        assertThatThrownBy(() -> groupRepository.save(groupWithoutStatus))
            .isInstanceOf(Exception.class);
    }

    @Test
    void shouldUpdateGroup() {
        // Arrange
        Group updatedGroup = Group.builder()
            .id(testGroup.getId())
            .name("Updated Group")
            .description("This group should be updated")
            .status(GroupStatus.ACTIVE)
            .build();

        // Act
        Group savedGroup = groupRepository.save(updatedGroup);

        // Assert
        assertThat(savedGroup.getName()).isEqualTo("Updated Group");
        assertThat(savedGroup.getDescription()).isEqualTo("This group should be updated");
        assertThat(savedGroup.getStatus()).isEqualTo(GroupStatus.ACTIVE);
    }

    @Test
    void shouldDeleteGroup() {
        // Arrange
        Group groupToDelete = Group.builder()
            .id(testGroup.getId())
            .name("Group to Delete")
            .description("This group should be deleted")
            .status(GroupStatus.DELETED)
            .build();

        // Act
        Group deletedGroup = groupRepository.save(groupToDelete);

        // Assert
        assertThat(deletedGroup.getStatus()).isEqualTo(GroupStatus.DELETED);
    }

    @Test
    void shouldFindGroupById() {
        Optional<Group> foundGroup = groupRepository.findById(testGroup.getId());

        assertThat(foundGroup).isPresent();
        assertThat(foundGroup.get().getId()).isEqualTo(testGroup.getId());
    }

    @Test
    void shouldUpdateTimestamps() {
        // Arrange
        Group groupToSave = Group.builder()
            .name("Saved Group")
            .description("This group should be updated")
            .status(GroupStatus.ACTIVE)
            .build();

        Group savedGroup = groupRepository.save(groupToSave);
        
        try {
            Thread.sleep(10);  // 10ミリ秒待機
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        
        // Act
        savedGroup.setName("Updated Group");
        Group updatedGroup = groupRepository.save(savedGroup);

        // Assert
        assertThat(updatedGroup.getCreatedAt()).isEqualTo(savedGroup.getCreatedAt());
        assertThat(updatedGroup.getUpdatedAt()).isAfter(savedGroup.getCreatedAt());
    }

    @Test
    void shouldFindAllByUserId() {

        // Arrange
        User testUser = userRepository.save(User.builder()
            .email("test@example.com")
            .passwordHash("password_hash")
            .name("Test User")
            .status(UserStatus.ACTIVE)
            .build());


        Group testGroup2 = Group.builder()
            .name("Test Group 2")
            .description("This is a test group 2")
            .status(GroupStatus.ACTIVE)
            .build();

        Group testGroup3 = Group.builder()
            .name("Test Group 3")
            .description("This group should not be found.")
            .status(GroupStatus.ACTIVE)
            .build();

        groupRepository.save(testGroup2);
        groupRepository.save(testGroup3);

        GroupUser testGroupUser = GroupUser.builder()
            .user(testUser)
            .group(testGroup)    
            .role(Role.VIEWER)
            .build();
        
        GroupUser testGroupUser2 = GroupUser.builder()
            .user(testUser)
            .group(testGroup2)    
            .role(Role.VIEWER)
            .build();

        groupUserRepository.saveAll(Arrays.asList(testGroupUser, testGroupUser2));

        // Act
        List<Group> foundGroups = groupRepository.findAllByUserId(testUser.getId());

        // Assert
        assertThat(foundGroups).hasSize(2);
        assertThat(foundGroups).extracting(Group::getName)
            .containsExactlyInAnyOrder("Test Group", "Test Group 2");       
    }

    @Test
    void shouldNotFindInactiveGroupsByUserId() {

        // Arrange
        User testUser = userRepository.save(User.builder()
            .email("test@example.com")
            .passwordHash("password_hash")
            .name("Test User")
            .status(UserStatus.ACTIVE)
            .build());

        Group testGroup2 = Group.builder()
            .name("Test Group 2")
            .description("This group should not be found")
            .status(GroupStatus.INACTIVE)
            .build();

        groupRepository.save(testGroup2);

        GroupUser testGroupUser = GroupUser.builder()
            .user(testUser)
            .group(testGroup)    
            .role(Role.VIEWER)
            .build();
        
        GroupUser testGroupUser2 = GroupUser.builder()
            .user(testUser)
            .group(testGroup2)    
            .role(Role.VIEWER)
            .build();

        groupUserRepository.saveAll(Arrays.asList(testGroupUser, testGroupUser2));

        // Act
        List<Group> foundGroups = groupRepository.findAllByUserIdAndStatus(testUser.getId(), GroupStatus.ACTIVE);

        // Assert
        assertThat(foundGroups).hasSize(1);
        assertThat(foundGroups).extracting(Group::getName)
            .containsExactlyInAnyOrder("Test Group");
    }
}
