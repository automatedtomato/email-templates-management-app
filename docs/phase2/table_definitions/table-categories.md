# 分類管理関連テーブル定義

## categories テーブル

### テーブル概要
テンプレートの分類を管理するカテゴリマスタテーブル。階層構造を持たせることで柔軟な分類を可能にする。

### カラム定義
| カラム名 | データ型 | NULL | デフォルト | キー | 説明 |
|---------|----------|------|------------|------|------|
| id | BIGINT | NO | | PK | カテゴリID |
| name | VARCHAR(100) | NO | | | カテゴリ名 |
| description | TEXT | YES | | | カテゴリの説明 |
| parent_id | BIGINT | YES | | FK | 親カテゴリID |
| display_order | INT | NO | 0 | | 表示順序 |
| created_by | BIGINT | NO | | FK | 作成者のユーザーID |
| created_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 作成日時 |
| updated_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 更新日時 |

### インデックス
1. PRIMARY KEY (id)
2. INDEX idx_categories_parent (parent_id)
3. INDEX idx_categories_display_order (display_order)
4. INDEX idx_categories_created_by (created_by)

### JPA Entity 例
```java
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    // その他のフィールド...
}
```

## category_templates テーブル

### テーブル概要
テンプレートとカテゴリの関連付けを管理する中間テーブル。1つのテンプレートが複数のカテゴリに属することを可能にする。

### カラム定義
| カラム名 | データ型 | NULL | デフォルト | キー | 説明 |
|---------|----------|------|------------|------|------|
| id | BIGINT | NO | | PK | 関連ID |
| template_id | BIGINT | NO | | FK | テンプレートID |
| category_id | BIGINT | NO | | FK | カテゴリID |
| created_by | BIGINT | NO | | FK | 作成者のユーザーID |
| created_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 作成日時 |

### インデックス
1. PRIMARY KEY (id)
2. UNIQUE INDEX idx_category_templates_relation (template_id, category_id)
3. INDEX idx_category_templates_category (category_id)
4. INDEX idx_category_templates_created_by (created_by)