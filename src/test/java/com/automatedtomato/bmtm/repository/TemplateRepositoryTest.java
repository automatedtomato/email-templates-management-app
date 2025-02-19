package com.automatedtomato.bmtm.repository;

import static org.assertj.core.api.Assertions.*;

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
import com.automatedtomato.bmtm.entity.User;
import com.automatedtomato.bmtm.enums.TemplateStatus;
import com.automatedtomato.bmtm.enums.UserStatus;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test3")
public class TemplateRepositoryTest extends AbstractTestContainers{

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        templateRepository.deleteAll();

        // 基本テストデータ

        testUser = userRepository.save(User.builder()
            .email("test@example.com")
            .passwordHash("password_hash")
            .name("Test User")
            .status(UserStatus.ACTIVE)
            .build());
    }

    @Test
    void shouldSaveTemplate() {

        Template tesTemplate = Template.builder()
            .title("Test Template") 
            .description("This is a test template")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build();

        Template savedTemplate = templateRepository.save(tesTemplate);

        // IDの自動生成
        assertThat(savedTemplate.getId()).isNotNull();

        // 各フィールドの検証
        assertThat(savedTemplate.getTitle()).isEqualTo("Test Template");
        assertThat(savedTemplate.getDescription()).isEqualTo("This is a test template");
        assertThat(savedTemplate.getStatus()).isEqualTo(TemplateStatus.DRAFT);
        assertThat(savedTemplate.getCreatedBy().getId()).isEqualTo(testUser.getId());

        // タイムスタンプ検証
        assertThat(savedTemplate.getCreatedAt()).isNotNull();
        assertThat(savedTemplate.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldNotSaveTemplateWithoutRequiredFields() {
        Template templateWithoutTitle = Template.builder()
            .description("This template should not be saved")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build();

        assertThatThrownBy(() -> templateRepository.save(templateWithoutTitle))
            .isInstanceOf(Exception.class);
    }

    @Test
    void shouldFindAllByTitle() {

        Template template1 = Template.builder()
            .title("Found Template 1") 
            .description("This is a test template 1")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build();

        Template template2 = Template.builder()
            .title("FOUND TEMPLATE 2") 
            .description("This is a test template 2")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build();

        Template template3 = Template.builder()
            .title("Dummy Template 3") 
            .description("This template should not be found")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build();

        templateRepository.save(template1);
        templateRepository.save(template2);
        templateRepository.save(template3);

        // Act
        List<Template> foundTemplates = templateRepository.findAllByTitleContainingIgnoreCase("Found Template");
    
        // Assert
        assertThat(foundTemplates).hasSize(2);
        assertThat(foundTemplates).extracting(Template::getTitle)
            .contains("Found Template 1", "FOUND TEMPLATE 2");
    }

    @Test
    void shouldNotFindTemplateWithNonexistentTitle() {

        Template template1 = Template.builder()
            .title("Found Template 1") 
            .description("This is a test template 1")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build();

        templateRepository.save(template1);
        // Act & Assert
        assertThat(templateRepository.findAllByTitleContainingIgnoreCase("Nonexistent Template"))
            .isEmpty();
    }

    @Test
    void shouldFindById() {
        Template template1 = Template.builder()
            .title("Test Template")
            .description("This is a test template")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build();

        Template savedTemplate = templateRepository.save(template1);

        // Act
        Optional<Template> foundTemplate = templateRepository.findById(savedTemplate.getId());
    
        // Assert
        assertThat(foundTemplate).isPresent();
        assertThat(foundTemplate.get().getTitle()).isEqualTo("Test Template");
        assertThat(foundTemplate.get().getDescription()).isEqualTo("This is a test template");
        assertThat(foundTemplate.get().getCreatedBy().getId()).isEqualTo(testUser.getId());
        assertThat(foundTemplate.get().getStatus()).isEqualTo(TemplateStatus.DRAFT);
        assertThat(foundTemplate.get().getId()).isEqualTo(savedTemplate.getId());
    }

    @Test
    void shouldNotFindTemplateWithNonExistentId() {
        Template template1 = Template.builder()
            .title("Test Template")
            .description("This is a test template")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build();

        templateRepository.save(template1);
        // Act & Assert
        assertThat(templateRepository.findById(999L)).isEmpty();
    }

    @Test
    void shouldUpdateStatus() {

        Template template1 = Template.builder()
            .title("Test Template")
            .description("This is a test template")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build();

        Template savedTemplate = templateRepository.save(template1);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        savedTemplate.setStatus(TemplateStatus.PUBLISHED);
        Template updatedTemplate = templateRepository.save(savedTemplate);

        // 更新のついでにタイムスタンプの挙動も確認
        assertThat(updatedTemplate.getCreatedAt()).isEqualTo(savedTemplate.getCreatedAt());
        assertThat(updatedTemplate.getUpdatedAt()).isAfter(updatedTemplate.getCreatedAt());
        assertThat(updatedTemplate.getStatus()).isEqualTo(TemplateStatus.PUBLISHED);
    }

    @Test
    void shouldDeleteTemplate() {

        Template template1 = Template.builder()
            .title("Test Template")
            .description("This is a test template")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build();

        Template savedTemplate = templateRepository.save(template1);

        // Act
        savedTemplate.setStatus(TemplateStatus.ARCHIVED);
        Template deletedTemplate = templateRepository.save(savedTemplate);

        // Assert
        assertThat(deletedTemplate.getStatus()).isEqualTo(TemplateStatus.ARCHIVED);
    }

    @Test
    void shouldFindByStatus() {
        Template template1 = templateRepository.save(Template.builder()
            .title("Test Template")
            .description("This is a test template")
            .createdBy(testUser)
            .status(TemplateStatus.PUBLISHED)
            .build());

        Template template2 = templateRepository.save(Template.builder()
            .title("Test Template 2")
            .description("This template should no be found")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build());

        Template template3 = templateRepository.save(Template.builder()
            .title("Test Template 3")
            .description("This template should no be found")
            .createdBy(testUser)
            .status(TemplateStatus.ARCHIVED)
            .build());

        // Act
        List<Template> foundTemplates1 = templateRepository.findAllByStatus(TemplateStatus.PUBLISHED);
        List<Template> foundTemplates2 = templateRepository.findAllByStatus(TemplateStatus.DRAFT);
        List<Template> foundTemplates3 = templateRepository.findAllByStatus(TemplateStatus.ARCHIVED);
        // Assert
        assertThat(foundTemplates1).hasSize(1);
        assertThat(foundTemplates1).extracting(Template::getTitle)
            .containsExactly("Test Template");

        assertThat(foundTemplates2).hasSize(1);
        assertThat(foundTemplates2).extracting(Template::getTitle)
            .containsExactly("Test Template 2");

        assertThat(foundTemplates3).hasSize(1);
        assertThat(foundTemplates3).extracting(Template::getTitle)
            .containsExactly("Test Template 3");
    }

    @Test
    void shouldFindAllPublishedTemplates() {
        Template template1 = templateRepository.save(Template.builder()
            .title("Published Template")
            .description("This is a test template")
            .createdBy(testUser)
            .status(TemplateStatus.PUBLISHED)
            .build());

        Template template2 = templateRepository.save(Template.builder()
            .title("Draft Template")
            .description("This template should no be found")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build());

        Template template3 = templateRepository.save(Template.builder()
            .title("Archived Template")
            .description("This template should no be found")
            .createdBy(testUser)
            .status(TemplateStatus.ARCHIVED)
            .build());

        // Act
        List<Template> foundTemplates = templateRepository.findAllPublishedTemplates();
        // Assert
        assertThat(foundTemplates).hasSize(1);
        assertThat(foundTemplates).extracting(Template::getTitle)
            .containsExactly("Published Template");
    }

    @Test
    void shouldFindAllPublishedTemplatesByKeyword() {
        Template template1 = templateRepository.save(Template.builder()
            .title("Published Template")
            .description("This is a test template with keyword found")
            .createdBy(testUser)
            .status(TemplateStatus.PUBLISHED)
            .build());

        Template template2 = templateRepository.save(Template.builder()
            .title("Published Template 2")
            .description("This is a test template without keyword")
            .createdBy(testUser)
            .status(TemplateStatus.PUBLISHED)
            .build());

        Template template3 = templateRepository.save(Template.builder()
            .title("Draft Template")
            .description("This template should no be found")
            .createdBy(testUser)
            .status(TemplateStatus.DRAFT)
            .build());

        Template template4 = templateRepository.save(Template.builder()
            .title("Archived Template")
            .description("This template should no be found")
            .createdBy(testUser)
            .status(TemplateStatus.ARCHIVED)
            .build());

        // Act
        List<Template> foundTemplates = templateRepository.findAllPublishedTemplatesByKeyword(TemplateStatus.PUBLISHED,"found");
        // Assert
        assertThat(foundTemplates).hasSize(1);
        assertThat(foundTemplates).extracting(Template::getTitle)
            .containsExactly("Published Template");
    }


    @Test
void shouldFindAllPublishedTemplatesByKeywordInTitleAndDescription() {
    // タイトルのみマッチ
    Template template1 = templateRepository.save(Template.builder()
        .title("Published Template with KEYWORD")
        .description("This is a test template")
        .createdBy(testUser)
        .status(TemplateStatus.PUBLISHED)
        .build());

    // 説明文のみマッチ
    Template template2 = templateRepository.save(Template.builder()
        .title("Another Published Template")
        .description("This description contains keyword")
        .createdBy(testUser)
        .status(TemplateStatus.PUBLISHED)
        .build());

    // 両方マッチ
    Template template3 = templateRepository.save(Template.builder()
        .title("Keyword in Title")
        .description("Also keyword in description")
        .createdBy(testUser)
        .status(TemplateStatus.PUBLISHED)
        .build());

    // マッチしない
    Template template4 = templateRepository.save(Template.builder()
        .title("No match here")
        .description("Nothing to find")
        .createdBy(testUser)
        .status(TemplateStatus.PUBLISHED)
        .build());

    // Act
    List<Template> foundTemplates = templateRepository.findAllPublishedTemplatesByKeyword(TemplateStatus.PUBLISHED, "keyword");

    // Assert
    assertThat(foundTemplates).hasSize(3);
    assertThat(foundTemplates).extracting(Template::getTitle)
        .containsExactlyInAnyOrder(
            "Published Template with KEYWORD",
            "Another Published Template",
            "Keyword in Title"
        );
    }
}