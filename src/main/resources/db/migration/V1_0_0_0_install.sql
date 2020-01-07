CREATE TABLE user (
    uuid UUID PRIMARY KEY NOT NULL,
    login TEXT NOT NULL UNIQUE,
    email TEXT UNIQUE,
    first_name TEXT,
    last_name TEXT,
    locked BOOLEAN NOT NULL DEFAULT false,
    failed_login_attempts SMALLINT NOT NULL DEFAULT 0,
    password_hash TEXT,
    password_expiration_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    password_reset_hash TEXT,
    password_reset_hash_expires TIMESTAMP WITH TIME ZONE,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE password_history (
  password_id BIGSERIAL PRIMARY KEY NOT NULL,
  uuid UUID NOT NULL,
  date_added TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  password_hash TEXT NOT NULL,
  CONSTRAINT fk_uuid FOREIGN KEY (uuid) REFERENCES user (uuid) ON DELETE CASCADE
);
