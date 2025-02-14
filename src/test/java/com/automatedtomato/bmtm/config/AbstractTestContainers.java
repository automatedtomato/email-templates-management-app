package com.automatedtomato.bmtm.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractTestContainers {

    @Container  // このフィールドがコンテナであることを示す
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            "postgres:14-alpine") // PostgreSQLのバージョン指定
            .withDatabaseName("email_template_test_db") // テスト用のDB名
            .withUsername("test") // テスト用ユーザー名
            .withPassword("test"); // テスト用パスワード
    
    // Spring Bootとの連携設定
    @DynamicPropertySource  // テスト実行時にSpringの設定を動的に追加するためのアノテーション
    private static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        // register~ : データベース接続情報を登録するメソッド
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);  // Spring Bootの設定値を追加
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
