# カテゴリ管理API仕様書

## 1. 概要
テンプレートのカテゴリ分類を管理するAPIエンドポイント群。階層構造のカテゴリツリーを実現する。

## 2. 共通仕様
- ベースURL: `/api/v1/categories`
- 認証: Bearer認証（JWTトークン）
- レスポンス形式: application/json

## 3. エンドポイント一覧

### 3.1 カテゴリツリー取得
```yaml
GET /api/v1/categories/tree
概要: カテゴリの階層構造を取得する
権限: 認証済みユーザー
レスポンス:
  200:
    description: 取得成功
    content:
      application/json:
        {
          "categories": [
            {
              "id": "number",
              "name": "string",
              "description": "string",
              "children": [
                {
                  "id": "number",
                  "name": "string",
                  "description": "string",
                  "children": []
                }
              ]
            }
          ]
        }
```

### 3.2 カテゴリ作成
```yaml
POST /api/v1/categories
概要: 新規カテゴリを作成する
権限: EDITOR以上
リクエストボディ:
  content-type: application/json
  {
    "name": "string",
    "description": "string",
    "parentId": "number",
    "displayOrder": "number"
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
          "parentId": "number",
          "displayOrder": "number"
        }
```

### 3.3 カテゴリ更新
```yaml
PUT /api/v1/categories/{categoryId}
概要: カテゴリ情報を更新する
権限: EDITOR以上
パスパラメータ:
  categoryId: number (必須)
リクエストボディ:
  content-type: application/json
  {
    "name": "string",
    "description": "string",
    "parentId": "number",
    "displayOrder": "number"
  }
レスポンス:
  200:
    description: 更新成功
```

### 3.4 カテゴリ削除
```yaml
DELETE /api/v1/categories/{categoryId}
概要: カテゴリを削除する（子カテゴリがある場合は削除不可）
権限: EDITOR以上
パスパラメータ:
  categoryId: number (必須)
レスポンス:
  204:
    description: 削除成功
  400:
    description: 子カテゴリが存在する場合
```

### 3.5 表示順序一括更新
```yaml
PUT /api/v1/categories/order
概要: 同一階層内のカテゴリの表示順序を一括更新
権限: EDITOR以上
リクエストボディ:
  content-type: application/json
  {
    "orders": [
      {
        "categoryId": "number",
        "displayOrder": "number"
      }
    ]
  }
レスポンス:
  200:
    description: 更新成功
```

### 3.6 カテゴリ配下のテンプレート取得
```yaml
GET /api/v1/categories/{categoryId}/templates
概要: 指定カテゴリに属するテンプレート一覧を取得
権限: 認証済みユーザー
パスパラメータ:
  categoryId: number (必須)
クエリパラメータ:
  page: integer (デフォルト: 0)
  size: integer (デフォルト: 20)
  includeSubCategories: boolean (デフォルト: false)
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
              "status": "string"
            }
          ],
          "totalElements": "number",
          "totalPages": "number"
        }
```

## 4. バリデーションルール
1. カテゴリ名
   - 1文字以上100文字以下
   - 同一階層内で一意

2. 表示順序
   - 0以上の整数
   - 同一階層内で一意である必要はない

3. 階層の制限
   - 最大3階層まで
   - 循環参照は不可
