CREATE TABLE IF NOT EXISTS payment_request
(
    id                      UUID PRIMARY KEY,
    sender_account_number   varchar(20) NOT NULL,
    receiver_account_number varchar(20) NOT NULL,
    amount                  DECIMAL     NOT NULL,
    currency                VARCHAR(3)  NOT NULL,
    operation_type          VARCHAR(64) NOT NULL,
    operation_status        VARCHAR(16) NOT NULL,
    clear_scheduled_at      TIMESTAMPTZ NOT NULL,
    created_at              TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);