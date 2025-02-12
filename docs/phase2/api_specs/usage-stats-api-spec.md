# 使用統計API仕様書

## 1. 概要
テンプレートの使用状況や分析データを提供するAPIエンドポイント群。

## 2. 共通仕様
- ベースURL: `/api/v1/statistics`
- 認証: Bearer認証（JWTトークン）
- レスポンス形式: application/json

## 3. エンドポイント一覧

### 3.1 グループ別使用統計
```yaml
GET /api/v1/statistics/groups/{groupId}
概要: グループ単位の使用統計を取得
権限: グループのADMIN/EDITOR
パスパラメータ:
  groupId: number (必須)
クエリパラメータ:
  from: string (ISO8601)
  to: string (ISO8601)
  interval: string (day/week/month)
レスポンス:
  200:
    description: 取得成功
    content:
      application/json:
        {
          "summary": {
            "totalTemplates": "number",
            "totalUsage": "number",
            "activeUsers": "number"
          },
          "timeSeriesData": [
            {
              "date": "string (ISO8601)",
              "views": "number",
              "copies": "number",
              "exports": "number"
            }
          ],
          "topTemplates": [
            {
              "templateId": "number",
              "title": "string",
              "usageCount": "number",
              "lastUsed": "string (ISO8601)"
            }
          ],
          "userActivities": [
            {
              "userId": "number",
              "name": "string",
              "activityCount": "number"
            }
          ]
        }
```

### 3.2 テンプレート別使用統計
```yaml
GET /api/v1/statistics/templates/{templateId}
概要: 特定テンプレートの使用統計を取得
権限: グループメンバー
パスパラメータ:
  templateId: number (必須)
クエリパラメータ:
  from: string (ISO8601)
  to: string (ISO8601)
レスポンス:
  200:
    description: 取得成功
    content:
      application/json:
        {
          "summary": {
            "totalViews": "number",
            "totalCopies": "number",
            "totalExports": "number",
            "uniqueUsers": "number"
          },
          "versionUsage": [
            {
              "version": "string",
              "usageCount": "number"
            }
          ],
          "recentActivities": [
            {
              "userId": "number",
              "userName": "string",
              "actionType": "string",
              "timestamp": "string (ISO8601)"
            }
          ]
        }
```

### 3.3 ユーザー別使用統計
```yaml
GET /api/v1/statistics/users/{userId}
概要: ユーザーの使用統計を取得
権限: 対象ユーザー自身またはADMIN
パスパラメータ:
  userId: number (必須)
クエリパラメータ:
  from: string (ISO8601)
  to: string (ISO8601)
レスポンス:
  200:
    description: 取得成功
    content:
      application/json:
        {
          "summary": {
            "totalActions": "number",
            "viewCount": "number",
            "copyCount": "number",
            "exportCount": "number"
          },
          "frequentTemplates": [
            {
              "templateId": "number",
              "title": "string",
              "usageCount": "number"
            }
          ],
          "activityHistory": [
            {
              "date": "string (ISO8601)",
              "actionCount": "number"
            }
          ]
        }
```

### 3.4 カテゴリ別使用統計
```yaml
GET /api/v1/statistics/categories/{categoryId}
概要: カテゴリ単位の使用統計を取得
権限: グループメンバー
パスパラメータ:
  categoryId: number (必須)
クエリパラメータ:
  from: string (ISO8601)
  to: string (ISO8601)
  includeSubCategories: boolean (デフォルト: true)
レスポンス:
  200:
    description: 取得成功
    content:
      application/json:
        {
          "summary": {
            "templateCount": "number",
            "totalUsage": "number",
            "activeTemplates": "number"
          },
          "subCategoryStats": [
            {
              "categoryId": "number",
              "name": "string",
              "usageCount": "number"
            }
          ],
          "topTemplates": [
            {
              "templateId": "number",
              "title": "string",
              "usageCount": "number"
            }
          ]
        }
```

### 3.5 一括エクスポート
```yaml
POST /api/v1/statistics/export
概要: 指定期間の統計データをCSVでエクスポート
権限: ADMIN
リクエストボディ:
  content-type: application/json
  {
    "from": "string (ISO8601)",
    "to": "string (ISO8601)",
    "groupId": "number",
    "includeUserDetails": "boolean",
    "types": ["template_usage", "user_activity", "category_stats"]
  }
レスポンス:
  200:
    description: エクスポート成功
    content:
      application/csv
```

## 4. 集計ルール
1. 期間指定
   - デフォルトは過去30日
   - 最大1年まで

2. アクションの種類
   - VIEW: テンプレートの閲覧
   - COPY: テンプレートのコピー
   - EXPORT: テンプレートのエクスポート

3. アクティブユーザーの定義
   - 期間内に1回以上のアクションを実行したユーザー

4. アクティブテンプレートの定義
   - 期間内に1回以上使用されたテンプレート
