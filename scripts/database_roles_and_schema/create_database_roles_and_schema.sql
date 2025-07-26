-- Create new database role for client application.

\c postgres

SELECT
EXISTS (SELECT rolname FROM pg_roles WHERE rolname=:'client_app_role') 
AS exists \gset

\if :exists
    \echo 'Role' :'client_app_role' 'already exists, skipping'
\else
    \echo 'Creating role' :'client_app_role'
    CREATE ROLE :"client_app_role"
	WITH LOGIN PASSWORD :'client_app_password';
\endif




/* Set priveleges in the template database.
   Any newly created database based on the template database
   will inherit these priveleges.

   Both client app and PUBLIC roles are specified explicitly,
   because any role's privelege is a sum of
   - privelege for specific role
   - privelege for group
   - privelege for PUBLIC. */
\c template1

START TRANSACTION;

-- Revoke priveleges for current objects.
REVOKE ALL ON DATABASE template1
FROM PUBLIC, :"client_app_role" CASCADE;

REVOKE ALL ON SCHEMA public
FROM PUBLIC, :"client_app_role" CASCADE;

REVOKE ALL ON ALL TABLES IN SCHEMA public
FROM PUBLIC, :"client_app_role" CASCADE;

REVOKE ALL ON ALL SEQUENCES IN SCHEMA public
FROM PUBLIC, :"client_app_role" CASCADE;

REVOKE ALL ON ALL ROUTINES IN SCHEMA public
FROM PUBLIC, :"client_app_role" CASCADE;

-- Revoke priveleges for future objects.
ALTER DEFAULT PRIVILEGES FOR ROLE postgres
REVOKE ALL ON SCHEMAS
FROM PUBLIC, :"client_app_role" CASCADE;

ALTER DEFAULT PRIVILEGES FOR ROLE postgres
REVOKE ALL ON TABLES
FROM PUBLIC, :"client_app_role" CASCADE;

ALTER DEFAULT PRIVILEGES FOR ROLE postgres
REVOKE ALL ON SEQUENCES
FROM PUBLIC, :"client_app_role" CASCADE;

ALTER DEFAULT PRIVILEGES FOR ROLE postgres
REVOKE ALL ON ROUTINES
FROM PUBLIC, :"client_app_role" CASCADE;

COMMIT;




-- Create new database for client application.
\c postgres

SELECT
EXISTS (SELECT datname FROM pg_database WHERE datname=:'database_name')
AS exists \gset

\if :exists
    \echo 'Database' :'database_name' 'already exists, skipping'
\else
    \echo 'Creating database' :'database_name'
    CREATE DATABASE :"database_name"
	OWNER postgres
	TEMPLATE template1
	ALLOW_CONNECTIONS true
	CONNECTION LIMIT 32;
\endif

-- Create schema.
\c :"database_name"

START TRANSACTION;

CREATE TABLE IF NOT EXISTS dialogue_state(
	dialogue_state_id TEXT PRIMARY KEY
);

INSERT INTO dialogue_state (dialogue_state_id)
VALUES ('start'), ('choose'), ('record'), ('check')
ON CONFLICT (dialogue_state_id) DO NOTHING;

CREATE TABLE IF NOT EXISTS audio_class(
	audio_class_id TEXT PRIMARY KEY
);

INSERT INTO audio_class (audio_class_id)
VALUES ('doctor'), ('samehada')
ON CONFLICT (audio_class_id) DO NOTHING;

CREATE TABLE IF NOT EXISTS telegram_user(
	user_id
		BIGINT
		PRIMARY KEY,
	username TEXT,
	first_name
		TEXT
		NOT NULL,
	last_name TEXT,
	dialogue_state_id
		TEXT
		NOT NULL
		REFERENCES dialogue_state(dialogue_state_id)
		ON DELETE RESTRICT
		ON UPDATE CASCADE,
	audio_class_id
		TEXT
		REFERENCES audio_class(audio_class_id)
		ON DELETE SET NULL
		ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS index_telegram_user_dialogue_state_id
ON telegram_user(dialogue_state_id);

CREATE INDEX IF NOT EXISTS index_telegram_user_audio_class_id
ON telegram_user(audio_class_id);

CREATE TABLE IF NOT EXISTS voice(
	file_unique_id
		TEXT
		PRIMARY KEY,
	file_id
		TEXT
		NOT NULL,
	duration
		INTEGER
		NOT NULL,
	timestamp
		TIMESTAMP WITH TIME ZONE
		NOT NULL
		DEFAULT CURRENT_TIMESTAMP,
    audio_class_id
        TEXT
        NOT NULL
        REFERENCES audio_class(audio_class_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS index_voice_audio_class_id
ON voice(audio_class_id);

ALTER TABLE IF EXISTS telegram_user
ADD COLUMN IF NOT EXISTS
    most_recent_voice_id
        TEXT
        REFERENCES voice(file_unique_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE;

CREATE INDEX IF NOT EXISTS index_telegram_user_most_recent_voice_id
ON telegram_user(most_recent_voice_id);

ALTER TABLE IF EXISTS voice
ADD COLUMN IF NOT EXISTS
	user_id
		BIGINT
		NOT NULL
		REFERENCES telegram_user(user_id)
		ON DELETE CASCADE
		ON UPDATE CASCADE;

CREATE INDEX IF NOT EXISTS index_voice_user_id
ON voice(user_id);

-- Set priveleges in the new database.
-- Newly created database always has Tc default PUBLIC priveleges.
REVOKE ALL ON DATABASE :"database_name"
FROM PUBLIC, :"client_app_role" CASCADE;

GRANT CONNECT ON DATABASE :"database_name"
TO :"client_app_role";

GRANT USAGE ON SCHEMA public
TO :"client_app_role";

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public
TO :"client_app_role";

COMMIT;