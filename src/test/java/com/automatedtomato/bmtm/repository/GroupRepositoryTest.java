package com.automatedtomato.bmtm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.automatedtomato.bmtm.config.AbstractTestContainers;
import com.automatedtomato.bmtm.entity.Group;
import com.automatedtomato.bmtm.enums.GroupStatus;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test2")
class GroupRepositoryTest extends AbstractTestContainers {

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        groupRepository.deleteAll();  // 各テスト前にDBをクリーンナップ
    }

    @Test
    void shouldSaveGroup() {
        // Arrange (準備)
        Group group = Group.builder()
            .name("Test Group")
            .description("This is a test group")
            .status(GroupStatus.ACTIVE)
            .build();

        // Act (実行)
        Group savedGroup = groupRepository.save(group);

        // Assert (検証)
        assertThat(savedGroup.getId()).isNotNull();
        assertThat(savedGroup.getName()).isEqualTo("Test Group");
        assertThat(savedGroup.getDescription()).isEqualTo("This is a test group");
        assertThat(savedGroup.getStatus()).isEqualTo(GroupStatus.ACTIVE);
        assertThat(savedGroup.getCreatedAt()).isNotNull();
        assertThat(savedGroup.getUpdatedAt()).isNotNull();
    }
}
