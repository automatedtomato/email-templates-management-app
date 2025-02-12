# テンプレート管理API仕様書

## 1. 概要
メールテンプレートの作成、編集、バージョン管理、カテゴリ管理を行うAPIエンドポイント群。

## 2. 共通仕様
- ベースURL: `/api/v1/templates`
- 認証: Bearer認証（JWTトークン）
- レスポンス形式: application/json

## 3. エンドポイント一覧

### 3.1 テンプレート一覧取得
```yaml
GET /api/v1/templates
概要: アクセス可能なテンプレート一覧を取得する
権限: 認証済みユーザー（グループ権限に基づく）
クエリパラメータ:
  groupId: number (必須)
  categoryId: number (オプション)
  page: integer (デフォルト: 0)
  size: integer (デフォルト: 20)
  sort: string (例: "title,asc", "createdAt,desc")
  status: string (オプション、DRAFT/PUBLISHED/ARCHIVED)
  search: string (オプション、タイトルと説明の全文検索)
レスポンス:
  200:
    description: 取得成功
    content:
      application/json:
        {
          "content": [
            {
              "id": "number",
              "title": "string",
              "description": "string",
              "status": "string",
              "createdBy": {
                "id": "number",
                "name": "string"
              },
              "categories": [
                {
                  "id": "number",
                  "name": "string"
                }
              ],
              "lastModified": "string (ISO8601)",
              "version": "string"
            }
          ],
          "totalElements": "number",
          "totalPages": "number"
        }
```

### 3.2 テンプレート作成
```yaml
POST /api/v1/templates
概要: 新規テンプレートを作成する
権限: EDITOR以上
リクエストボディ:
  content-type: application/json
  {
    "groupId": "number",
    "title": "string",
    "description": "string",
    "content": "string",
    "categoryIds": ["number"],
    "status": "string (DRAFT/PUBLISHED)"
  }
レスポンス:
  201:
    description: 作成成功
    content:
      application/json:
        {
          "id": "number",
          "title": "string",
          "version": "string",
          "status": "string"
        }
```

### 3.3 テンプレート詳細取得
```yaml
GET /api/v1/templates/{templateId}
概要: テンプレートの詳細情報を取得する
権限: グループメンバー
パスパラメータ:
  templateId: number (必須)
クエリパラメータ:
  version: string (オプション、指定バージョンを取得)
レスポンス:
  200:
    description: 取得成功
    content:
      application/json:
        {
          "id": "number",
          "title": "string",
          "description": "string",
          "content": "string",
          "version": "string",
          "status": "string",
          "categories": [
            {
              "id": "number",
              "name": "string"
            }
          ],
          "createdBy": {
            "id": "number",
            "name": "string"
          },
          "createdAt": "string",
          "lastModified": "string"
        }
```

### 3.4 テンプレート更新
```yaml
PUT /api/v1/templates/{templateId}
概要: テンプレートを更新し、新しいバージョンを作成
権限: EDITOR以上
パスパラメータ:
  templateId: number (必須)
リクエストボディ:
  content-type: application/json
  {
    "title": "string",
    "description": "string",
    "content": "string",
    "categoryIds": ["number"],
    "commitMessage": "string",
    "status": "string"
  }
レスポンス:
  200:
    description: 更新成功
    content:
      application/json:
        {
          "id": "number",
          "version": "string",
          "status": "string"
        }
```

### 3.5 バージョン履歴取得
```yaml
GET /api/v1/templates/{templateId}/versions
概要: テンプレートのバージョン履歴を取得
権限: グループメンバー
パスパラメータ:
  templateId: number (必須)
レスポンス:
  200:
    description: 取得成功
    content:
      application/json:
        {
          "versions": [
            {
              "version": "string",
              "commitMessage": "string",
              "createdBy": {
                "id": "number",
                "name": "string"
              },
              "createdAt": "string"
            }
          ]
        }
```

### 3.6 バージョン比較
```yaml
GET /api/v1/templates/{templateId}/diff
概要: 2つのバージョン間の差分を取得
権限: グループメンバー
パスパラメータ:
  templateId: number (必須)
クエリパラメータ:
  fromVersion: string (必須)
  toVersion: string (必須)
レスポンス:
  200:
    description: 差分取得成功
    content:
      application/json:
        {
          "diff": "string (差分情報)",
          "fromVersion": "string",
          "toVersion": "string"
        }
```

### 3.7 テンプレートのアーカイブ/復元
```yaml
PUT /api/v1/templates/{templateId}/archive
概要: テンプレートのアーカイブ状態を変更
権限: EDITOR以上
パスパラメータ:
  templateId: number (必須)
リクエストボディ:
  content-type: application/json
  {
    "archived": "boolean"
  }
レスポンス:
  200:
    description: 状態変更成功
```

### 3.8 テンプレートの使用記録
```yaml
POST /api/v1/templates/{templateId}/usage
概要: テンプレートの使用を記録
権限: グループメンバー
パスパラメータ:
  templateId: number (必須)
リクエストボディ:
  content-type: application/json
  {
    "actionType": "string (VIEW/COPY/EXPORT)",
    "clientInfo": "object (任意)"
  }
レスポンス:
  201:
    description: 記録成功
```

## 4. バリデーションルール
1. タイトル
   - 1文字以上200文字以下
   - グループ内で一意

2. 説明
   - 1000文字以下

3. コンテンツ
   - 必須
   - 10MB以下

4. バージョン番号
   - セマンティックバージョニング（x.y.z）

## 5. エラーレスポンス
```json
{
  "timestamp": "string (ISO8601)",
  "status": "number",
  "error": "string",
  "message": "string",
  "path": "string"
}
```
