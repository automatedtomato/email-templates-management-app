# 認証・認可関連テーブル定義

## users テーブル

### テーブル概要
ユーザー情報を管理するテーブル。認証・認可の基本となる。

### カラム定義
| カラム名 | データ型 | NULL | デフォルト | キー | 説明 |
|---------|----------|------|------------|------|------|
| id | BIGINT | NO | | PK | ユーザーID |
| email | VARCHAR(255) | NO | | UQ | メールアドレス |
| password_hash | VARCHAR(255) | NO | | | パスワードハッシュ（BCrypt） |
| name | VARCHAR(100) | NO | | | ユーザー名 |
| status | VARCHAR(20) | NO | 'ACTIVE' | | ユーザーステータス（ACTIVE/INACTIVE/DELETED） |
| created_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 作成日時 |
| updated_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 更新日時 |

### インデックス
1. PRIMARY KEY (id)
2. UNIQUE INDEX idx_users_email (email)
3. INDEX idx_users_status (status)

### JPA Entity 例
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Email
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

## groups テーブル

### テーブル概要
テンプレートを管理するグループ情報を格納するテーブル。企業や部署単位での管理を可能にする。

### カラム定義
| カラム名 | データ型 | NULL | デフォルト | キー | 説明 |
|---------|----------|------|------------|------|------|
| id | BIGINT | NO | | PK | グループID |
| name | VARCHAR(100) | NO | | | グループ名 |
| description | TEXT | YES | | | グループの説明 |
| status | VARCHAR(20) | NO | 'ACTIVE' | | グループステータス |
| created_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 作成日時 |
| updated_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 更新日時 |

### インデックス
1. PRIMARY KEY (id)
2. INDEX idx_groups_status (status)

## group_users テーブル

### テーブル概要
ユーザーとグループの関連付けを管理するテーブル。ユーザーの権限も管理する。

### カラム定義
| カラム名 | データ型 | NULL | デフォルト | キー | 説明 |
|---------|----------|------|------------|------|------|
| id | BIGINT | NO | | PK | グループユーザーID |
| group_id | BIGINT | NO | | FK | グループID |
| user_id | BIGINT | NO | | FK | ユーザーID |
| role | VARCHAR(20) | NO | 'VIEWER' | | ユーザー権限 |
| created_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 作成日時 |
| updated_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 更新日時 |

### インデックス
1. PRIMARY KEY (id)
2. UNIQUE INDEX idx_group_users_user_group (group_id, user_id)
3. INDEX idx_group_users_user (user_id)
4. INDEX idx_group_users_role (role)