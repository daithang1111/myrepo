PRAGMA encoding = "UTF-8";
DROP TABLE IF EXISTS DATA_GROUP;
CREATE TABLE DATA_GROUP (
    rowID       INTEGER PRIMARY KEY AUTOINCREMENT,
    GROUPID      varchar(100),
    DESCRIPTION	 text
);
CREATE INDEX X_GROUP_ID ON DATA_GROUP (GROUPID);
