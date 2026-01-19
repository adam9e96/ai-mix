INSERT INTO users (
    email,
    password,
    nickname,
    is_agreed,
    role,
    created_at
) VALUES (
             'root@admin.com',
             'qwer3033!@',  -- 임시 해시 (실제로는 생성 필요)
             'root',
             true,
             'ADMIN'::user_role,
             NOW()
         );