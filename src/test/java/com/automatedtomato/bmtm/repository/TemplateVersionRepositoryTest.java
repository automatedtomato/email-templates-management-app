package com.automatedtomato.bmtm.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
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
import com.automatedtomato.bmtm.entity.Template;
import com.automatedtomato.bmtm.entity.TemplateVersion;
import com.automatedtomato.bmtm.entity.User;
import com.automatedtomato.bmtm.enums.TemplateStatus;
import com.automatedtomato.bmtm.enums.UserStatus;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test4")
class TemplateVersionRepositoryTest extends AbstractTestContainers{

    @Autowired
    private TemplateVersionRepository templateVersionRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Template testTemplate;

    @BeforeEach
    void setUp() {
        templateVersionRepository.deleteAll();
        templateRepository.deleteAll();
        userRepository.deleteAll();

        // 基本テストデータ
        testUser = userRepository.save(User.builder()
            .email("test@example.com")
            .passwordHash("password_hash")
            .name("Test User")
            .status(UserStatus.ACTIVE)
            .build());
        
        testTemplate = templateRepository.save(Template.builder()
            .title("Test Template")
            .description("This is test template")
            .createdBy(testUser)
            .status(TemplateStatus.PUBLISHED)
            .build());
    }

    @Test
    void shouldSaveTemplateVersion() {
        // Arrange
        TemplateVersion templateVersion = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .branchName("name")
            .content("Test Content")
            .commitMessage("Initial commit")
            .createdBy(testUser)
            .build();

        // Act
        TemplateVersion savedTemplateVersion = templateVersionRepository.save(templateVersion);

        // Assert
        assertThat(savedTemplateVersion.getId()).isNotNull();
        assertThat(savedTemplateVersion.getContent()).isEqualTo("Test Content");
        assertThat(savedTemplateVersion.getVersionNumber()).isEqualTo(1L);
        assertThat(savedTemplateVersion.getCreatedBy().getId()).isEqualTo(testUser.getId());
        assertThat(savedTemplateVersion.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldNotSaveTemplateVersionWithoutRequiredFields() {
        // Arrange
        TemplateVersion templateVersion = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .branchName("main")
            .commitMessage("Initial commit")
            .build();

        TemplateVersion templateVersion2 = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .content("Test Content")
            .commitMessage("Initial commit")
            .build();

        // Act & Assert
        assertThatThrownBy(() -> templateVersionRepository.save(templateVersion))
            .isInstanceOf(Exception.class);
        assertThatThrownBy(() -> templateVersionRepository.save(templateVersion2))
            .isInstanceOf(Exception.class);
    }

    @Test
    void shouldFindById() {
        // Arrange
        TemplateVersion templateVersion = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .branchName("main")
            .content("Test Content")
            .commitMessage("Initial commit")
            .createdBy(testUser)
            .build();

        TemplateVersion savedTemplateVersion = templateVersionRepository.save(templateVersion);

        // Act
        TemplateVersion foundTemplateVersion = templateVersionRepository.findById(savedTemplateVersion.getId()).orElse(null);

        // Assert
        assertThat(foundTemplateVersion.getId()).isEqualTo(savedTemplateVersion.getId());
        assertThat(foundTemplateVersion.getTemplate().getId()).isEqualTo(testTemplate.getId());
        assertThat(foundTemplateVersion.getContent()).isEqualTo("Test Content");
        assertThat(foundTemplateVersion.getVersionNumber()).isEqualTo(1L);
        assertThat(foundTemplateVersion.getBranchName()).isEqualTo("main");
        assertThat(foundTemplateVersion.getCreatedBy().getId()).isEqualTo(testUser.getId());
        assertThat(foundTemplateVersion.getCommitMessage()).isEqualTo("Initial commit");
    }

    @Test
    void shouldFindByTemplateId() {
        // Arrange
        TemplateVersion templateVersion = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .content("Test Content")
            .branchName("main")
            .commitMessage("Initial commit")
            .createdBy(testUser)
            .build();

        TemplateVersion templateVersion2 = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(2L)
            .branchName("main")
            .content("Test Content 2")
            .commitMessage("Initial commit")
            .createdBy(testUser)
            .build();

        templateVersionRepository.save(templateVersion);
        templateVersionRepository.save(templateVersion2);

        // Act
        List<TemplateVersion> templateVersions = templateVersionRepository.findByTemplateIdOrderByVersionNumberDesc(testTemplate.getId());

        // Assert
        assertThat(templateVersions).hasSize(2);
        assertThat(templateVersions.get(0).getVersionNumber()).isEqualTo(2L);
        assertThat(templateVersions.get(1).getVersionNumber()).isEqualTo(1L);
    }

    @Test
    void shouldNotFindTemplateWithNonExistentId() {
        // Act & Assert
        assertThat(templateVersionRepository.findById(999L)).isEmpty();
    }

    @Test
    void shouldDistinguishVersionsByBranchName() {
        // Arrange
        TemplateVersion mainVersion = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .branchName("main")
            .content("Test Content")
            .commitMessage("Main commit 1")
            .createdBy(testUser)
            .build();

        TemplateVersion mainVersion2 = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(2L)
            .branchName("main")
            .content("Test Content 2")
            .commitMessage("Main commit 2")
            .createdBy(testUser)
            .build();
        
        TemplateVersion devVersion = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .branchName("dev")
            .content("Test Content")
            .commitMessage("Dev commit 1")
            .createdBy(testUser)
            .build();

        templateVersionRepository.saveAll(Arrays.asList(mainVersion, mainVersion2, devVersion));

        // Act
        List<TemplateVersion> mainVersions = templateVersionRepository
            .findByTemplateIdAndBranchNameOrderByVersionNumberDesc(testTemplate.getId(), "main");
        List<TemplateVersion> devVersions = templateVersionRepository
            .findByTemplateIdAndBranchNameOrderByVersionNumberDesc(testTemplate.getId(), "dev");

        // Assert
        assertThat(mainVersions).hasSize(2);
        assertThat(mainVersions.get(0).getVersionNumber()).isEqualTo(2L);
        assertThat(mainVersions.get(1).getVersionNumber()).isEqualTo(1L);
        assertThat(devVersions).hasSize(1);
        assertThat(devVersions.get(0).getVersionNumber()).isEqualTo(1L);
        assertThat(devVersions.get(0).getBranchName()).isEqualTo("dev");
    }
    @Test
    void shouldGetNextVersionNumber() {
        // Arrange
        TemplateVersion mainVersion = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .branchName("main")
            .content("Test Content")
            .commitMessage("Main commit 1")
            .createdBy(testUser)
            .build();

        TemplateVersion mainVersion2 = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(2L)
            .branchName("main")
            .content("Test Content 2")
            .commitMessage("Main commit 2")
            .createdBy(testUser)
            .build();
        
        templateVersionRepository.saveAll(Arrays.asList(mainVersion, mainVersion2));

        // Act
        Long nextVersionNumber = templateVersionRepository
            .getNextVersionNumber(testTemplate.getId(), "main");
        Long nextVersionNumber2 = templateVersionRepository
            .getNextVersionNumber(testTemplate.getId(), "dev");

        // Assert
        assertThat(nextVersionNumber).isEqualTo(3L);
        assertThat(nextVersionNumber2).isEqualTo(1L);
    }

    @Test
    void shouldCreateNewVersion() {
        // Arrange
        TemplateVersion initialVersion = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .branchName("main")
            .content("Initial Content")
            .commitMessage("Initial commit")
            .createdBy(testUser)
            .build();

        templateVersionRepository.save(initialVersion);

        Long nextVersionNumber = templateVersionRepository
            .getNextVersionNumber(testTemplate.getId(), "main");

        TemplateVersion newVersion = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(nextVersionNumber)
            .branchName("main")
            .content("New Content")
            .commitMessage("New commit")
            .createdBy(testUser)
            .build();

        // Act
        TemplateVersion savedNewVersion = templateVersionRepository.save(newVersion);

        // Assert
        assertThat(savedNewVersion.getId()).isEqualTo(2L);
        assertThat(savedNewVersion.getContent()).isEqualTo("New Content");
        assertThat(savedNewVersion.getCreatedBy().getId()).isEqualTo(testUser.getId());
        assertThat(savedNewVersion.getCommitMessage()).isEqualTo("New commit");
    }

    @Test
    void shouldFindLatestVersionByTemplateId() {
        // Arrange
        TemplateVersion version1 = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .branchName("main")
            .content("Content v1")
            .commitMessage("Commit 1")
            .createdBy(testUser)
            .build();

        TemplateVersion version2 = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(2L)
            .branchName("main")
            .content("Content v2")
            .commitMessage("Commit 2")
            .createdBy(testUser)
            .build();

        templateVersionRepository.saveAll(Arrays.asList(version1, version2));

        // Act
        TemplateVersion latestVersion = templateVersionRepository
            .findFirstByTemplateIdAndBranchNameOrderByVersionNumberDesc(
                testTemplate.getId(),
                "main"
                ).orElse(null);

        // Assert
        assertThat(latestVersion).isNotNull();
        assertThat(latestVersion.getVersionNumber()).isEqualTo(2L);
        assertThat(latestVersion.getContent()).isEqualTo("Content v2");
    }

    @Test
    void shouldMaintainVersionOrder() {
        // Arrange
        List<TemplateVersion> versions = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            versions.add(TemplateVersion.builder()
                .template(testTemplate)
                .versionNumber((long) i)
                .branchName("main")
                .content("Content v" + i)
                .commitMessage("Commit " + i)
                .createdBy(testUser)
                .build());
        }
        templateVersionRepository.saveAll(versions);

        // Act
        List<TemplateVersion> foundVersions = templateVersionRepository
            .findByTemplateIdAndBranchNameOrderByVersionNumberDesc(testTemplate.getId(), "main");

        // Assert
        assertThat(foundVersions).hasSize(5);
        assertThat(foundVersions).extracting(TemplateVersion::getVersionNumber)
            .containsExactly(5L, 4L, 3L, 2L, 1L);
        assertThat(foundVersions).extracting(TemplateVersion::getContent)
            .containsExactly("Content v5",
                                       "Content v4",
                                       "Content v3",
                                       "Content v2",
                                       "Content v1"
            );
    }

    @Test
    void shouldCreateNewBranch() {
    // Arrange
    // メインブランチにバージョンを作成
    TemplateVersion mainVersion = TemplateVersion.builder()
        .template(testTemplate)
        .versionNumber(1L)
        .branchName("main")
        .content("Main branch content")
        .commitMessage("Initial commit")
        .createdBy(testUser)
        .build();
    templateVersionRepository.save(mainVersion);

    // 新しいブランチの最初のバージョン
    TemplateVersion featureVersion = TemplateVersion.builder()
        .template(testTemplate)
        .versionNumber(1L)  // 新しいブランチは1から開始
        .branchName("feature/new-design")
        .content("Feature branch content")
        .commitMessage("Feature branch initial commit")
        .createdBy(testUser)
        .build();

    // Act
    TemplateVersion savedFeatureVersion = templateVersionRepository.save(featureVersion);

    // Assert
    assertThat(savedFeatureVersion.getId()).isNotNull();
    assertThat(savedFeatureVersion.getBranchName()).isEqualTo("feature/new-design");
    assertThat(savedFeatureVersion.getVersionNumber()).isEqualTo(1L);
    }

    @Test
    void shouldFindAllVersionsByBranch() {
    // Arrange
    // メインブランチのバージョン
    TemplateVersion mainVersion1 = TemplateVersion.builder()
        .template(testTemplate)
        .versionNumber(1L)
        .branchName("main")
        .content("Main content 1")
        .commitMessage("Main commit 1")
        .createdBy(testUser)
        .build();

    TemplateVersion mainVersion2 = TemplateVersion.builder()
        .template(testTemplate)
        .versionNumber(2L)
        .branchName("main")
        .content("Main content 2")
        .commitMessage("Main commit 2")
        .createdBy(testUser)
        .build();

    // フィーチャーブランチのバージョン
    TemplateVersion featureVersion = TemplateVersion.builder()
        .template(testTemplate)
        .versionNumber(1L)
        .branchName("feature/new-design")
        .content("Feature content")
        .commitMessage("Feature commit")
        .createdBy(testUser)
        .build();

    templateVersionRepository.saveAll(Arrays.asList(mainVersion1, mainVersion2, featureVersion));

    // Act
    List<TemplateVersion> mainVersions = templateVersionRepository
        .findByTemplateIdAndBranchNameOrderByVersionNumberDesc(testTemplate.getId(), "main");
    List<TemplateVersion> featureVersions = templateVersionRepository
        .findByTemplateIdAndBranchNameOrderByVersionNumberDesc(testTemplate.getId(), "feature/new-design");

    // Assert
    assertThat(mainVersions).hasSize(2);
    assertThat(featureVersions).hasSize(1);
    assertThat(mainVersions).extracting(TemplateVersion::getBranchName)
        .containsOnly("main");
    assertThat(featureVersions).extracting(TemplateVersion::getBranchName)
            .containsOnly("feature/new-design");
    }

    @Test
    void shouldFindLatestVersionInBranch() {
        // Arrange
        TemplateVersion mainVersion1 = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .branchName("main")
            .content("Main content 1")
            .commitMessage("Main commit 1")
            .createdBy(testUser)
            .build();

        TemplateVersion mainVersion2 = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(2L)
            .branchName("main")
            .content("Main content 2")
            .commitMessage("Main commit 2")
            .createdBy(testUser)
            .build();

        templateVersionRepository.saveAll(Arrays.asList(mainVersion1, mainVersion2));

        // Act
        Optional<TemplateVersion> latestVersion = templateVersionRepository
            .findFirstByTemplateIdAndBranchNameOrderByVersionNumberDesc(
                testTemplate.getId(), "main");

        // Assert
        assertThat(latestVersion).isPresent();
        assertThat(latestVersion.get().getVersionNumber()).isEqualTo(2L);
        assertThat(latestVersion.get().getContent()).isEqualTo("Main content 2");
    }

    @Test
    void shouldMaintainVersionOrderPerBranch() {
        // Arrange
        // メインブランチのバージョン
        List<TemplateVersion> mainVersions = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            mainVersions.add(TemplateVersion.builder()
                .template(testTemplate)
                .versionNumber((long) i)
                .branchName("main")
                .content("Main content " + i)
                .commitMessage("Main commit " + i)
                .createdBy(testUser)
                .build());
        }

        // フィーチャーブランチのバージョン
        List<TemplateVersion> featureVersions = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            featureVersions.add(TemplateVersion.builder()
                .template(testTemplate)
                .versionNumber((long) i)
                .branchName("feature/new-design")
                .content("Feature content " + i)
                .commitMessage("Feature commit " + i)
                .createdBy(testUser)
                .build());
        }

        templateVersionRepository.saveAll(mainVersions);
        templateVersionRepository.saveAll(featureVersions);

        // Act
        List<TemplateVersion> foundMainVersions = templateVersionRepository
            .findByTemplateIdAndBranchNameOrderByVersionNumberDesc(
                testTemplate.getId(), "main");
        List<TemplateVersion> foundFeatureVersions = templateVersionRepository
            .findByTemplateIdAndBranchNameOrderByVersionNumberDesc(
                testTemplate.getId(), "feature/new-design");

        // Assert
        assertThat(foundMainVersions).hasSize(3);
        assertThat(foundMainVersions).extracting(TemplateVersion::getVersionNumber)
            .containsExactly(3L, 2L, 1L);

        assertThat(foundFeatureVersions).hasSize(2);
        assertThat(foundFeatureVersions).extracting(TemplateVersion::getVersionNumber)
            .containsExactly(2L, 1L);

            
        }

    @Test
    void shouldSaveVersionWithTemplate() {
        // Arrange
        TemplateVersion version = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .branchName("main")
            .content("Test content")
            .commitMessage("Initial commit")
            .createdBy(testUser)
            .build();

        // Act
        TemplateVersion savedVersion = templateVersionRepository.save(version);
        
        // データベースから再取得して関連を確認
        TemplateVersion fetchedVersion = templateVersionRepository
            .findById(savedVersion.getId())
            .orElse(null);

        // Assert
        assertThat(fetchedVersion).isNotNull();
        assertThat(fetchedVersion.getTemplate().getId()).isEqualTo(testTemplate.getId());
        assertThat(fetchedVersion.getTemplate().getTitle()).isEqualTo(testTemplate.getTitle());
        assertThat(fetchedVersion.getTemplate().getDescription()).isEqualTo(testTemplate.getDescription());
    }

    @Test
    void shouldSaveVersionWithCreatedBy() {
        // Arrange
        TemplateVersion version = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .branchName("main")
            .content("Test content")
            .commitMessage("Initial commit")
            .createdBy(testUser)
            .build();

        // Act
        TemplateVersion savedVersion = templateVersionRepository.save(version);
        
        // データベースから再取得して関連を確認
        TemplateVersion fetchedVersion = templateVersionRepository
            .findById(savedVersion.getId())
            .orElse(null);

        // Assert
        assertThat(fetchedVersion).isNotNull();
        assertThat(fetchedVersion.getCreatedBy().getId()).isEqualTo(testUser.getId());
        assertThat(fetchedVersion.getCreatedBy().getName()).isEqualTo(testUser.getName());
        assertThat(fetchedVersion.getCreatedBy().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void shouldIncludeCommitMessage() {
        // Arrange
        TemplateVersion version = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(1L)
            .branchName("main")
            .content("Test content")
            .commitMessage("Fix typo in header")
            .createdBy(testUser)
            .build();

        // commitMessageなしのバージョン
        TemplateVersion versionWithoutMessage = TemplateVersion.builder()
            .template(testTemplate)
            .versionNumber(2L)
            .branchName("main")
            .content("Updated content")
            .createdBy(testUser)
            .build();

        // Act
        TemplateVersion savedVersion = templateVersionRepository.save(version);
        TemplateVersion savedVersionWithoutMessage = templateVersionRepository.save(versionWithoutMessage);
        
        // データベースから再取得
        TemplateVersion fetchedVersion = templateVersionRepository
            .findById(savedVersion.getId())
            .orElse(null);
        TemplateVersion fetchedVersionWithoutMessage = templateVersionRepository
            .findById(savedVersionWithoutMessage.getId())
            .orElse(null);

        // Assert
        assertThat(fetchedVersion).isNotNull();
        assertThat(fetchedVersion.getCommitMessage()).isEqualTo("Fix typo in header");
        
        assertThat(fetchedVersionWithoutMessage).isNotNull();
        assertThat(fetchedVersionWithoutMessage.getCommitMessage()).isNull();
    }
}

