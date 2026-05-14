-- ============================================
-- Seed data (admin user)
-- ============================================
INSERT INTO users (email, password, nickname, is_agreed, role, created_at)
VALUES ('root@admin.com', 'qwer3033!@', 'root', true, 'ADMIN'::user_role, NOW());
