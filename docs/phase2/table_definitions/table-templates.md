# テンプレート管理関連テーブル定義

## templates テーブル

### テーブル概要
メールテンプレートのメタ情報を管理するテーブル。実際のテンプレート内容はtemplate_versionsテーブルで管理する。

### カラム定義
| カラム名 | データ型 | NULL | デフォルト | キー | 説明 |
|---------|----------|------|------------|------|------|
| id | BIGINT | NO | | PK | テンプレートID |
| title | VARCHAR(200) | NO | | | テンプレートタイトル |
| description | TEXT | YES | | | テンプレートの説明 |
| created_by | BIGINT | NO | | FK | 作成者のユーザーID |
| is_archived | BOOLEAN | NO | false | | アーカイブフラグ |
| status | VARCHAR(20) | NO | 'DRAFT' | | ステータス |
| last_version_id | BIGINT | YES | | FK | 最新バージョンのID |
| created_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 作成日時 |
| updated_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 更新日時 |

### インデックス
1. PRIMARY KEY (id)
2. INDEX idx_templates_created_by (created_by)
3. INDEX idx_templates_status (status)
4. INDEX idx_templates_title (title)
5. INDEX idx_templates_is_archived (is_archived)

### JPA Entity 例
```java
@Entity
@Table(name = "templates")
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateStatus status = TemplateStatus.DRAFT;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_version_id")
    private TemplateVersion lastVersion;

    // タイムスタンプフィールド...
}
```

## template_versions テーブル

### テーブル概要
メールテンプレートの各バージョンの内容を管理するテーブル。履歴管理を可能にする。

### カラム定義
| カラム名 | データ型 | NULL | デフォルト | キー | 説明 |
|---------|----------|------|------------|------|------|
| id | BIGINT | NO | | PK | バージョンID |
| template_id | BIGINT | NO | | FK | テンプレートID |
| content | TEXT | NO | | | テンプレート内容 |
| version | VARCHAR(50) | NO | | | バージョン番号 |
| created_by | BIGINT | NO | | FK | 作成者のユーザーID |
| commit_message | TEXT | YES | | | 変更内容の説明 |
| created_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 作成日時 |

### インデックス
1. PRIMARY KEY (id)
2. INDEX idx_template_versions_template (template_id)
3. INDEX idx_template_versions_created_by (created_by)
4. UNIQUE INDEX idx_template_versions_version (template_id, version)