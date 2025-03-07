INSERT INTO users (id, username, password, email, first_name, last_name, is_deleted)
VALUES
    (3,
     'admin',
     '$2a$12$jtvBQ6G0m1k4x5otVTpuauyYBGNa.0j3zulE4oi90VslTB82ZGqTy', --qwerty
     'admin@example.com',
     'John',
     'Smith',
     false),
    (4,
     'john.doe',
     '$2y$10$o8VZTEVPro5VwHreAnJKWeAje1UmfK6h9fCsFFys0Snezd8Or8myO', --password123
     'john.doe@example.com',
     'John',
     'Doe',
     false),
    (5,
     'jane.smith',
     '$2y$10$CYCviojQUsC2KFkH0SP27O7oVoYZ5MshlZQPV3TLgd5cNdnxRGIPG', --password456
     'jane.smith@example.com',
     'Jane',
     'Smith',
     false);
INSERT INTO users_roles VALUES (3, 1);
INSERT INTO users_roles VALUES (4, 2);
INSERT INTO users_roles VALUES (5, 2);
