-- 사용자가 연결되어 있는 세션 종료
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE usename = 'aimixuser';

-- ai_mix 데이터베이스에 대한 권한 철회
REVOKE ALL PRIVILEGES ON DATABASE ai_mix FROM aimixuser;

-- public 스키마에 대한 권한 철회
REVOKE ALL PRIVILEGES ON SCHEMA public FROM aimixuser;

-- public 스키마 내의 모든 테이블에 대한 권한 철회
REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM aimixuser;

-- public 스키마 내의 모든 시퀀스에 대한 권한 철회
REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public FROM aimixuser;

-- public 스키마 내의 모든 함수에 대한 권한 철회
REVOKE ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public FROM aimixuser;

-- 향후 생성될 테이블, 시퀀스, 함수에 대한 권한 철회
ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON TABLES FROM aimixuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON SEQUENCES FROM aimixuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON FUNCTIONS FROM aimixuser;




-- aimixuser 삭제
DROP USER IF EXISTS aimixuser;
-- `ai_mix` 데이터베이스 삭제
DROP DATABASE IF EXISTS ai_mix;

-- `postgres` 데이터베이스는 기본 시스템 DB이므로, 삭제할 수는 없습니다.
-- 그러나 연결된 다른 데이터베이스들은 삭제가 가능합니다.
-- 모든 테이블 삭제
DROP OWNED BY aimixuser;