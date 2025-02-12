# 認証・認可API仕様書

## 1. 概要
ユーザー認証、認可に関するAPIエンドポイントの定義。JWT（JSON Web Token）を使用した認証を実装する。

## 2. 認証フロー
1. ユーザーがメールアドレスとパスワードで認証
2. 認証成功時、Access TokenとRefresh Tokenを発行
3. 以降のリクエストではAccess Tokenをヘッダーに付与
4. Access Token失効時はRefresh Tokenで再取得

## 3. API エンドポイント

### 3.1 ユーザー登録
```yaml
POST /api/v1/auth/register
概要: 新規ユーザーを登録する
リクエストボディ:
  content-type: application/json
  {
    "email": "string",
    "password": "string",
    "name": "string"
  }
レスポンス:
  201:
    description: 登録成功
    content:
      application/json:
        {
          "id": "number",
          "email": "string",
          "name": "string"
        }
  400:
    description: バリデーションエラー
    content:
      application/json:
        {
          "message": "string",
          "errors": ["string"]
        }
```

### 3.2 ログイン
```yaml
POST /api/v1/auth/login
概要: ユーザー認証を行い、トークンを発行する
リクエストボディ:
  content-type: application/json
  {
    "email": "string",
    "password": "string"
  }
レスポンス:
  200:
    description: 認証成功
    content:
      application/json:
        {
          "accessToken": "string",
          "refreshToken": "string",
          "expiresIn": "number"  # 有効期限（秒）
        }
  401:
    description: 認証失敗
    content:
      application/json:
        {
          "message": "Invalid credentials"
        }
```

### 3.3 トークンリフレッシュ
```yaml
POST /api/v1/auth/refresh
概要: Access Tokenを再発行する
リクエストヘッダー:
  Authorization: "Bearer {refresh_token}"
レスポンス:
  200:
    description: トークン再発行成功
    content:
      application/json:
        {
          "accessToken": "string",
          "expiresIn": "number"
        }
  401:
    description: 無効なリフレッシュトークン
```

### 3.4 ログアウト
```yaml
POST /api/v1/auth/logout
概要: ログアウト処理（リフレッシュトークンの無効化）
リクエストヘッダー:
  Authorization: "Bearer {access_token}"
レスポンス:
  200:
    description: ログアウト成功
  401:
    description: 無効なトークン
```

## 4. 認証ヘッダー形式
```
Authorization: Bearer {token}
```

## 5. トークン仕様

### Access Token
- 有効期限: 12時間
- 含む情報:
  - ユーザーID
  - メールアドレス
  - ロール
  - 発行時刻
  - 有効期限

### Refresh Token
- 有効期限: 30日
- データベースで管理
- 一度使用したらローテーション（新しいトークンを発行）

## 6. セキュリティ要件
1. パスワードのバリデーション
   - 最小8文字
   - 大文字小文字を含む
   - 数字を含む
   - 特殊文字を含む

2. レート制限
   - ログイン試行: 5回/分
   - トークンリフレッシュ: 10回/分

3. CORS設定
   - 許可オリジン: フロントエンドドメインのみ
   - 許可メソッド: POST, GET, PUT, DELETE
   - 許可ヘッダー: Content-Type, Authorization

## 7. エラーレスポンス共通フォーマット
```json
{
  "timestamp": "string (ISO8601)",
  "status": "number",
  "error": "string",
  "message": "string",
  "path": "string"
}
```
