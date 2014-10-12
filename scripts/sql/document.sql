PRAGMA encoding = "UTF-8";
DROP TABLE IF EXISTS DOCUMENT_DATA;
CREATE TABLE DOCUMENT_DATA (
    rowID       INTEGER PRIMARY KEY AUTOINCREMENT,
    DOCID      varchar(1000),
    DOCCONTENT      text

);
CREATE INDEX X_DOC_ID ON DOCUMENT_DATA (DOCID);