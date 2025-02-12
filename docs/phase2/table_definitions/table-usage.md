# 使用統計関連テーブル定義

## template_usages テーブル

### テーブル概要
テンプレートの使用履歴を記録するテーブル。使用統計やアナリティクスのためのデータを提供する。

### カラム定義
| カラム名 | データ型 | NULL | デフォルト | キー | 説明 |
|---------|----------|------|------------|------|------|
| id | BIGINT | NO | | PK | 使用履歴ID |
| template_id | BIGINT | NO | | FK | テンプレートID |
| user_id | BIGINT | NO | | FK | 使用者のユーザーID |
| group_id | BIGINT | NO | | FK | 使用時のグループID |
| version_id | BIGINT | NO | | FK | 使用時のバージョンID |
| action_type | VARCHAR(20) | NO | | | アクション種別 |
| used_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | | 使用日時 |
| client_info | JSON | YES | | | クライアント情報 |

### インデックス
1. PRIMARY KEY (id)
2. INDEX idx_template_usages_template (template_id)
3. INDEX idx_template_usages_user (user_id)
4. INDEX idx_template_usages_group (group_id)
5. INDEX idx_template_usages_version (version_id)
6. INDEX idx_template_usages_date (used_at)
7. INDEX idx_template_usages_action (action_type)

### パフォーマンス最適化
1. 複合インデックス
```sql
CREATE INDEX idx_template_usages_analysis 
ON template_usages (template_id, group_id, used_at);

### パフォーマンス最適化

#### 1. パーティショニング戦略
```sql
-- 月次パーティショニングの例
CREATE TABLE template_usages (
    id BIGINT,
    -- 他のカラム
    used_at TIMESTAMP NOT NULL
) PARTITION BY RANGE (date_trunc('month', used_at));

-- 各月のパーティション作成
CREATE TABLE template_usages_y2025m01 
    PARTITION OF template_usages
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
```

#### 2. バキューム設定
```sql
-- テーブル固有の設定
ALTER TABLE template_usages SET (
    autovacuum_vacuum_scale_factor = 0.1,
    autovacuum_analyze_scale_factor = 0.05
);
```

### JPA Entity 例
```java
@Entity
@Table(name = "template_usages")
public class TemplateUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private TemplateVersion version;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private UsageActionType actionType;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    @Column(name = "client_info", columnDefinition = "jsonb")
    private String clientInfo;
}

public enum UsageActionType {
    VIEW, COPY, EXPORT
}
```

### 運用考慮事項

1. アーカイブ戦略
   - 1年以上古いデータは集計テーブルに移行
   - 2年以上古いデータはアーカイブストレージに移動

2. パフォーマンスモニタリング
   - クエリ実行時間の監視
   - インデックス使用状況の追跡
   - パーティション使用効率の確認

3. バックアップ考慮事項
   - パーティションごとのバックアップ戦略
   - リストア手順の整備