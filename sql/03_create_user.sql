-- 새로운 사용자 생성
-- CREATE USER aimixuser WITH PASSWORD '303326';

-- 데이터베이스에 대한 권한 부여
GRANT ALL PRIVILEGES ON DATABASE ai_mix TO aimixuser;

-- aimixuser에게 필요한 권한 부여

-- 1. 데이터베이스에 연결 권한
GRANT CONNECT ON DATABASE ai_mix TO aimixuser;

-- 2. public 스키마 사용 권한
GRANT USAGE ON SCHEMA public TO aimixuser;

-- 3. public 스키마에서 테이블 생성 권한
GRANT CREATE ON SCHEMA public TO aimixuser;

-- 4. 기존 테이블에 대한 모든 권한 부여
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO aimixuser;

-- 5. 향후 생성될 테이블에 대한 기본 권한 설정
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO aimixuser;

-- 6. 시퀀스 권한 (SERIAL/IDENTITY 사용 시 필요)
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO aimixuser;

-- 7. 향후 생성될 시퀀스에 대한 기본 권한 설정
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO aimixuser;

-- 8. 함수 권한 (필요시)
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO aimixuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO aimixuser;
