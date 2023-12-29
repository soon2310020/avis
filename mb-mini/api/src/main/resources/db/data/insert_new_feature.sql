delete from feature where name = 'Danh sách nghề nghiệp' or name = 'Danh sách câu hỏi sức khoẻ' or name = 'Cài đặt phân quyền';

INSERT INTO feature
(id, name, enabled)
VALUES(9, 'Danh sách nghề nghiệp', true);

INSERT INTO feature
(id, name, enabled)
VALUES(10, 'Danh sách câu hỏi sức khoẻ', true);