INSERT INTO students (first_name, last_name, matriculation_number, course, email) VALUES ('tester', 'tester', 00000000, 'QQ2', 'tester.tester@testmail.com');
SELECT * FROM students;

CREATE TABLE students (
  id int Not NULL PRIMARY KEY AUTO_INCREMENT,
  first_name varchar(255) NOT NULL,
  last_name varchar(255) NOT NULL,
  matriculation_number int NOT NULL,
  course varchar(255) NOT NULL ,
  semester int NOT NULL ,
  email varchar(255) NOT NULL
);