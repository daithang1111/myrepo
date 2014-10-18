PRAGMA encoding = "UTF-8";
DROP TABLE IF EXISTS VOCABULARY;
CREATE TABLE VOCABULARY (
    rowID       INTEGER PRIMARY KEY AUTOINCREMENT,
    WORDID      varchar(100),
    WORD	 varchar(100)
);
CREATE INDEX XX_WORD_ID ON VOCABULARY (WORDID);
