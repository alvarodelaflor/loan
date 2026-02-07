CREATE TABLE LOAN_APPLICATIONS (
                                   ID RAW(16) PRIMARY KEY,
                                   APPLICANT_NAME VARCHAR2(255) NOT NULL,
                                   APPLICANT_IDENTITY VARCHAR2(20) NOT NULL,
                                   AMOUNT NUMBER(19, 2) NOT NULL,
                                   CURRENCY VARCHAR2(3) NOT NULL,
                                   STATUS VARCHAR2(20) NOT NULL,
                                   CREATED_AT TIMESTAMP NOT NULL,
                                   MODIFIED_AT TIMESTAMP
);

CREATE TABLE REVINFO (
                         REV NUMBER(10) PRIMARY KEY,
                         REVTSTMP NUMBER(19)
);

CREATE SEQUENCE REVINFO_SEQ START WITH 1 INCREMENT BY 50 NOCACHE;

CREATE TABLE LOAN_APPLICATIONS_AUD (
                                       ID RAW(16) NOT NULL,
                                       REV NUMBER(10) NOT NULL,
                                       REVTYPE NUMBER(3) NOT NULL,
                                       APPLICANT_NAME VARCHAR2(255),
                                       APPLICANT_IDENTITY VARCHAR2(20),
                                       AMOUNT NUMBER(19, 2),
                                       CURRENCY VARCHAR2(3),
                                       STATUS VARCHAR2(20),
                                       CREATED_AT TIMESTAMP,
                                       MODIFIED_AT TIMESTAMP,
                                       PRIMARY KEY (ID, REV)
);

ALTER TABLE LOAN_APPLICATIONS_AUD ADD CONSTRAINT FK_REVINFO FOREIGN KEY (REV) REFERENCES REVINFO (REV);