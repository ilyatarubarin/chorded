package com.chorded.app.recommendations;

public enum SongStatus {
    SEARCH,        // результат поиска (когда введены аккорды)
    IN_PROGRESS,   // часть аккордов выучена
    LEARNED,       // все аккорды выучены
    NOT_STARTED    // ни одного аккорда не выучено
}
