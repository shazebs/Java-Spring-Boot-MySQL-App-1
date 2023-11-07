CREATE DATABASE istatus;

CREATE TABLE statuses ( 
	Id INT IDENTITY(1,1) PRIMARY KEY,
	Author VARCHAR(255) NOT NULL,
	Message VARCHAR(1000) NOT NULL,
	PhotoUrl VARCHAR(1000) NULL,
	Datetime VARCHAR(100) NOT NULL 
);