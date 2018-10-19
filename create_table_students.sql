CREATE TABLE students (
  id int Not NULL PRIMARY KEY AUTO_INCREMENT,
  first_name varchar(255) NOT NULL,
  last_name varchar(255) NOT NULL,
  matriculation_number int NOT NULL,
  course varchar(255) NOT NULL ,
  semester int NOT NULL ,
  email varchar(255) NOT NULL
);