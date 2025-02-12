# ユーザー管理API仕様書

## 1. 概要
ユーザー情報の管理、グループメンバーシップの管理を行うAPIエンドポイント群。

## 2. 共通仕様
- ベースURL: `/api/v1/users`
- 認証: Bearer認証（JWTトークン）
- レスポンス形式: application/json

## 3. エンドポイント一覧

### 3.1 ユーザー一覧取得
```yaml
GET /api/v1/users
概要: システム内のユーザー一覧を取得する
権限: ADMIN
クエリパラメータ:
  page: integer (デフォルト: 0)
  size: integer (デフォルト: 20)
  sort: string (例: "name,asc", "email,desc")
  status: string (オプション、ACTIVE/INACTIVE/DELETED)
レスポンス:
  200:
    description: 取得成功
    content:
      application/json:
        {
          "content": [
            {
              "id": "number",
              "email": "string",
              "name": "string",
              "status": "string",
              "createdAt": "string (ISO8601)"
            }
          ],
          "totalElements": "number",
          "totalPages": "number",
          "number": "number",
          "size": "number"
        }
  403:
    description: 権限エラー
```

### 3.2 ユーザー詳細取得
```yaml
GET /api/v1/users/{userId}
概要: 特定のユーザー情報を取得する
権限: ADMIN または 自身の情報
パスパラメータ:
  userId: number (必須)
レスポンス:
  200:
    description: 取得成功
    content:
      application/json:
        {
          "id": "number",
          "email": "string",
          "name": "string",
          "status": "string",
          "createdAt": "string",
          "groups": [
            {
              "id": "number",
              "name": "string",
              "role": "string"
            }
          ]
        }
  404:
    description: ユーザーが存在しない
```

### 3.3 ユーザー情報更新
```yaml
PUT /api/v1/users/{userId}
概要: ユーザー情報を更新する
権限: ADMIN または 自身の情報
パスパラメータ:
  userId: number (必須)
リクエストボディ:
  content-type: application/json
  {
    "name": "string",
    "email": "string"
  }
レスポンス:
  200:
    description: 更新成功
    content:
      application/json:
        {
          "id": "number",
          "email": "string",
          "name": "string"
        }
  400:
    description: バリデーションエラー
  404:
    description: ユーザーが存在しない
```

### 3.4 パスワード変更
```yaml
PUT /api/v1/users/{userId}/password
概要: ユーザーのパスワードを変更する
権限: ADMIN または 自身の情報
パスパラメータ:
  userId: number (必須)
リクエストボディ:
  content-type: application/json
  {
    "currentPassword": "string",
    "newPassword": "string"
  }
レスポンス:
  200:
    description: パスワード変更成功
  400:
    description: バリデーションエラー
  401:
    description: 現在のパスワードが不正
```

### 3.5 ユーザーの無効化/有効化
```yaml
PUT /api/v1/users/{userId}/status
概要: ユーザーのステータスを変更する
権限: ADMIN のみ
パスパラメータ:
  userId: number (必須)
リクエストボディ:
  content-type: application/json
  {
    "status": "string (ACTIVE/INACTIVE)"
  }
レスポンス:
  200:
    description: ステータス変更成功
  400:
    description: 不正なステータス値
  403:
    description: 権限エラー
```

## 4. エラーレスポンス
```json
{
  "timestamp": "string (ISO8601)",
  "status": "number",
  "error": "string",
  "message": "string",
  "path": "string"
}
```

## 5. バリデーションルール
1. name
   - 1文字以上100文字以下
   - 空白文字のみは不可

2. email
   - 有効なメールアドレス形式
   - 255文字以下
   - システム内で一意

3. password
   - 8文字以上
   - 大文字小文字を含む
   - 数字を含む
   - 特殊文字を含む
