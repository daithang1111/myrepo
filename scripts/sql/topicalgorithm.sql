PRAGMA encoding = "UTF-8";
DROP TABLE IF EXISTS TOPIC_ALGORITHM;
CREATE TABLE TOPIC_ALGORITHM (
    rowID       INTEGER PRIMARY KEY AUTOINCREMENT,
    ALGORITHM	varchar(100),
    TOPICID      varchar(1000),
);
CREATE INDEX X_ALGORITHM ON TOPIC_ALGORITHM (ALGORITHM);