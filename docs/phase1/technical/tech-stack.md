# 技術スタック概要書

## 1. バックエンド

### 主要フレームワーク
- Spring Boot 3.4.2
  - Spring Security（認証・認可）
  - Spring Data JPA（データアクセス）
  - Spring Validation（入力検証）

### データベース
- PostgreSQL 15
  - データの整合性とトランザクション管理が重要なため
  - テンプレートの検索性能を考慮

### APIドキュメント
- OpenAPI (Swagger)
  - API仕様の自動生成と管理
  - クライアントとの連携効率化

## 2. フロントエンド

### フレームワーク・ライブラリ
- React 18
  - TypeScript採用
  - React Router（画面遷移管理）
- Tanstack Query（データフェッチング）
- Zustand（状態管理）
  - ReduxよりもシンプルでMVP向き

### UIコンポーネント
- TailwindCSS（スタイリング）
  - 学習コストが低く、高速な開発が可能
- HeadlessUI（アクセシビリティ対応コンポーネント）

## 3. インフラストラクチャ

### クラウドプラットフォーム
- Google Cloud Platform (GCP)
  - Cloud Run（コンテナ実行環境）
  - Cloud SQL（PostgreSQL）
  - Cloud Storage（テンプレートストレージ）
  - Identity-Aware Proxy（認証）

### CI/CD
- GitHub Actions
  - テスト自動化
  - デプロイ自動化
  - 品質チェック

### モニタリング
- Cloud Logging
- Cloud Monitoring
- Cloud Trace

## 4. 開発環境

### バージョン管理
- Git/GitHub
  - GitHub Flow採用（シンプルなブランチ戦略）

### コンテナ化
- Docker
  - docker-compose（ローカル開発環境）

### 品質管理
- JUnit 5（単体テスト）
- Testcontainers（インテグレーションテスト）
- SonarQube（コード品質分析）

## 5. セキュリティ

### 認証・認可
- JWT（トークンベース認証）
- Spring Security（ロールベースアクセス制御）

### データ保護
- データの暗号化（保存時）
- HTTPS通信の強制
- CSRFトークンの実装

## 6. 選定理由と懸念点

### 採用の主な理由
1. Spring Boot
   - Javaの経験を活かせる
   - 豊富なドキュメントと事例
   - エンタープライズでの実績

2. React + TypeScript
   - 型安全性の確保
   - 保守性の向上
   - 学習リソースの豊富さ

3. GCP
   - マネージドサービスの活用
   - スケーラビリティの確保
   - 運用負荷の軽減

### 懸念点と対策
1. 学習コスト
   - ペアプログラミングの活用
   - 段階的な機能実装
   - ドキュメントの充実

2. 運用コスト
   - 自動化の徹底
   - モニタリングの強化
   - 障害対応フローの整備