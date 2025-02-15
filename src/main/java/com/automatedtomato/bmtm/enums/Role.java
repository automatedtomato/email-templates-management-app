package com.automatedtomato.bmtm.enums;

public enum Role {
    ADMIN,  // 管理者: グループの設定変更を含む全ての操作が可能
    EDITOR, // 編集者: テンプレートの作成・編集が可能 
    VIEWER  // 閲覧者: テンプレートの閲覧のみ可能
}
