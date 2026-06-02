INSERT INTO users (username,
                   email,
                   password_hash,
                   full_name,
                   is_active)
VALUES ('${admin_username}',
        '${admin_email}',
        '${admin_password_hash}',
        'System Administrator',
        TRUE);

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id,
       r.role_id
FROM users u
         JOIN roles r
              ON r.role_name = 'ADMIN'
WHERE u.username = 'admin';