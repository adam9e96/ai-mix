-- 1. 모든 테이블 소유권을 aimixuser로 변경
DO $$
    DECLARE
        r RECORD;
    BEGIN
        FOR r IN
            SELECT tablename
            FROM pg_tables
            WHERE schemaname = 'public'
            LOOP
                EXECUTE 'ALTER TABLE public.' || quote_ident(r.tablename) || ' OWNER TO aimixuser';
            END LOOP;
    END $$;

-- 2. 모든 시퀀스 소유권을 aimixuser로 변경
DO $$
    DECLARE
        r RECORD;
    BEGIN
        FOR r IN
            SELECT sequence_name
            FROM information_schema.sequences
            WHERE sequence_schema = 'public'
            LOOP
                EXECUTE 'ALTER SEQUENCE public.' || quote_ident(r.sequence_name) || ' OWNER TO aimixuser';
            END LOOP;
    END $$;

-- 3. 모든 테이블에 대한 모든 권한 부여
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO aimixuser;

-- 4. 모든 시퀀스에 대한 모든 권한 부여
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO aimixuser;

-- 5. 향후 생성될 테이블에 대한 기본 권한 설정
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO aimixuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO aimixuser;

-- 6. 스키마 권한 부여
GRANT USAGE, CREATE ON SCHEMA public TO aimixuser;