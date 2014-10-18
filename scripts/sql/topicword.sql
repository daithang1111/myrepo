PRAGMA encoding = "UTF-8";
DROP TABLE IF EXISTS TOPIC_WORD;
CREATE TABLE TOPIC_WORD (
    rowID       INTEGER PRIMARY KEY AUTOINCREMENT,
    TOPICID      varchar(100),
    WORDID		varchar(100),
    WORDPROP 	text
);
CREATE INDEX XXX_TOPIC_ID ON TOPIC_WORD (TOPICID);
CREATE INDEX X_WORD_ID ON TOPIC_WORD (WORDID);

