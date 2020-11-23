CREATE TABLE BANK_ACCOUNT (
    IBAN VARCHAR(35) PRIMARY KEY,
    STATUS VARCHAR(25) NOT NULL,
    DAILY_LIMIT NUMERIC(13, 2) NOT NULL,  -- DECIMAL_AMOUNT
    MONTHLY_LIMIT NUMERIC(13, 2) NOT NULL,-- DECIMAL_AMOUNT
    FIRST_NAME VARCHAR NOT NULL,
    LAST_NAME VARCHAR NOT NULL,
--    VERSION BIGINT NOT NULL,
    VERSION BIGINT,
    PERSONAL_ID VARCHAR NOT NULL,
    EMAIL VARCHAR NOT NULL
);

CREATE TABLE BANK_ACCOUNT_TX (
    BANK_ACCOUNT_IBAN VARCHAR(35),
    INDEX INTEGER,
--    INDEX INTEGER NOT NULL,
    UID CHAR(26) PRIMARY KEY,
    AMOUNT NUMERIC(13, 2) NOT NULL, -- DECIMAL_AMOUNT
    BOOKING_TIME TIMESTAMP NOT NULL,
    TYPE VARCHAR(25) NOT NULL,
    CONSTRAINT FK_IBAN
        FOREIGN KEY (BANK_ACCOUNT_IBAN)
            REFERENCES BANK_ACCOUNT(IBAN)
);

CREATE TABLE CUSTOMER (
    PERSONAL_ID VARCHAR PRIMARY KEY,
    FIRST_NAME VARCHAR NOT NULL,
    LAST_NAME VARCHAR NOT NULL,
    EMAIL VARCHAR NOT NULL UNIQUE
);

CREATE TABLE SCHEDULED_COMMAND (
    ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    CREATION_DATE TIMESTAMP NOT NULL,
    COMMAND VARCHAR NOT NULL
);