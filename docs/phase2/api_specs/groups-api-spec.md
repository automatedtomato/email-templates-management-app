# グループ管理API仕様書

## 1. 概要
グループの作成、メンバー管理、権限管理を行うAPIエンドポイント群。

## 2. 共通仕様
- ベースURL: `/api/v1/groups`
- 認証: Bearer認証（JWTトークン）
- レスポンス形式: application/json

## 3. エンドポイント一覧

### 3.1 グループ一覧取得
```yaml
GET /api/v1/groups
概要: アクセス可能なグループ一覧を取得する
権限: 全てのユーザー（自分が所属するグループのみ表示）
クエリパラメータ:
  page: integer (デフォルト: 0)
  size: integer (デフォルト: 20)
  sort: string (例: "name,asc")
  status: string (オプション、ACTIVE/INACTIVE)
レスポンス:
  200:
    description: 取得成功
    content:
      application/json:
        {
          "content": [
            {
              "id": "number",
              "name": "string",
              "description": "string",
              "status": "string",
              "memberCount": "number",
              "userRole": "string",
              "createdAt": "string (ISO8601)"
            }
          ],
          "totalElements": "number",
          "totalPages": "number",
          "number": "number",
          "size": "number"
        }
```

### 3.2 グループ作成
```yaml
POST /api/v1/groups
概要: 新規グループを作成する
権限: ADMIN
リクエストボディ:
  content-type: application/json
  {
    "name": "string",
    "description": "string"
  }
レスポンス:
  201:
    description: 作成成功
    content:
      application/json:
        {
          "id": "number",
          "name": "string",
          "description": "string",
          "status": "string",
          "createdAt": "string"
        }
  400:
    description: バリデーションエラー
```

### 3.3 グループ詳細取得
```yaml
GET /api/v1/groups/{groupId}
概要: グループの詳細情報を取得する
権限: グループメンバー
パスパラメータ:
  groupId: number (必須)
レスポンス:
  200:
    description: 取得成功
    content:
      application/json:
        {
          "id": "number",
          "name": "string",
          "description": "string",
          "status": "string",
          "members": [
            {
              "id": "number",
              "name": "string",
              "email": "string",
              "role": "string"
            }
          ],
          "createdAt": "string",
          "templateCount": "number"
        }
  404:
    description: グループが存在しない
```

### 3.4 グループ情報更新
```yaml
PUT /api/v1/groups/{groupId}
概要: グループ情報を更新する
権限: グループのADMIN
パスパラメータ:
  groupId: number (必須)
リクエストボディ:
  content-type: application/json
  {
    "name": "string",
    "description": "string"
  }
レスポンス:
  200:
    description: 更新成功
    content:
      application/json:
        {
          "id": "number",
          "name": "string",
          "description": "string"
        }
```

### 3.5 メンバー追加
```yaml
POST /api/v1/groups/{groupId}/members
概要: グループにメンバーを追加する
権限: グループのADMIN
パスパラメータ:
  groupId: number (必須)
リクエストボディ:
  content-type: application/json
  {
    "userId": "number",
    "role": "string (ADMIN/EDITOR/VIEWER)"
  }
レスポンス:
  201:
    description: メンバー追加成功
  400:
    description: バリデーションエラー
  404:
    description: ユーザーまたはグループが存在しない
```

### 3.6 メンバー権限更新
```yaml
PUT /api/v1/groups/{groupId}/members/{userId}
概要: グループメンバーの権限を更新する
権限: グループのADMIN
パスパラメータ:
  groupId: number (必須)
  userId: number (必須)
リクエストボディ:
  content-type: application/json
  {
    "role": "string (ADMIN/EDITOR/VIEWER)"
  }
レスポンス:
  200:
    description: 権限更新成功
```

### 3.7 メンバー削除
```yaml
DELETE /api/v1/groups/{groupId}/members/{userId}
概要: グループからメンバーを削除する
権限: グループのADMIN
パスパラメータ:
  groupId: number (必須)
  userId: number (必須)
レスポンス:
  204:
    description: 削除成功
```

### 3.8 使用統計取得
```yaml
GET /api/v1/groups/{groupId}/statistics
概要: グループの使用統計を取得する
権限: グループメンバー
パスパラメータ:
  groupId: number (必須)
クエリパラメータ:
  from: string (ISO8601)
  to: string (ISO8601)
レスポンス:
  200:
    description: 統計取得成功
    content:
      application/json:
        {
          "totalTemplates": "number",
          "totalUsage": "number",
          "activeUsers": "number",
          "templateUsage": [
            {
              "templateId": "number",
              "templateName": "string",
              "useCount": "number"
            }
          ]
        }
```

## 4. バリデーションルール
1. グループ名
   - 1文字以上100文字以下
   - 空白文字のみは不可
   - システム内で一意であること

2. 説明
   - 1000文字以下

3. ロール
   - ADMIN, EDITOR, VIEWER のいずれか
   - グループには最低1名のADMINが必要

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
