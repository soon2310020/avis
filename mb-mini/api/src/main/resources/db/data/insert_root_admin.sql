INSERT INTO "user"
(id, first_name, last_name, email, non_locked, enabled, "role", creation_time, last_updated_timestamp)
VALUES(1, 'Super', 'Admin', 'dunghtt3@mbbank.com.vn', true, true, 'SUPER_ADMIN', current_timestamp, current_timestamp);

INSERT INTO credential
(id, credential_type, user_id, non_expired, creation_time)
VALUES(1, 'email_and_password', 1, true, current_timestamp);

INSERT INTO email_and_password_credential
(id, email, "password")
VALUES(1, 'dunghtt3@mbbank.com.vn', '$2a$10$kYpbarBDWdsVaCw5LGA7f.6ucmfTc0lzqaoJ8tIVE09qOLLzA6rSy');
