-- Update unversioned database (v0.0.0) to database v1.0.0

-- No version restrictions configured, since this is the first update to apply
-- a database version.

-- Update database encoding
--   Reference: https://www.a2hosting.com/kb/developer-corner/mysql/convert-mysql-database-utf-8
ALTER DATABASE `vcr` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- Update table encoding
--   Reference: https://stackoverflow.com/a/8906937
ALTER TABLE `virtualcollection` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `pid` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `creator` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `keyword` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `resource` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `user` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- Create new table to store administrative key/value pairs
CREATE TABLE `config` (
  `key` varchar(255) NOT NULL PRIMARY KEY,
  `value` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert initial data
LOCK TABLES `config` WRITE;
ALTER TABLE `config` DISABLE KEYS;
INSERT INTO `config` VALUES ('db_version','1.0.0');
ALTER TABLE `config` ENABLE KEYS;
UNLOCK TABLES;