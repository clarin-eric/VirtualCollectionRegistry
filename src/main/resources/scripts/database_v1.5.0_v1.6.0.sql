-- Update database v1.5.0 to database v1.6.0

-- Check the current version of the database. If it is incorrect a null value is
-- inserted into the 'db_version_check` field. This will fail, since the field is
-- defined as non null, and thus also fail this script
SELECT null INTO @current_value;
DELETE FROM `config` WHERE `key` = 'db_version_check';
SELECT `value` INTO @current_value FROM `config` WHERE ( `key` = 'db_version' AND `value` = '1.5.0' );
INSERT INTO `config` (`key`, `value`) VALUES ('db_version_check', @current_value);

-- Apply updates
ALTER TABLE virtualcollection ADD COLUMN `parent` bigint(20) NULL;
ALTER TABLE virtualcollection ADD COLUMN `child` bigint(20) NULL;
ALTER TABLE virtualcollection ADD COLUMN `latest` bigint(20) NULL;
ALTER TABLE virtualcollection ADD COLUMN `original` bigint(20) NULL;
ALTER TABLE virtualcollection ADD COLUMN `forked_from` bigint(20) NULL;
ALTER TABLE virtualcollection ADD COLUMN `original` bigint(20) NULL;
ALTER TABLE virtualcollection ADD COLUMN `public_leaf` bool default false;
ALTER TABLE pid ADD COLUMN `is_latest` bool default false;

--ALTER TABLE resource_scan ADD COLUMN `created` datetime;

CREATE TABLE `resource_scan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ref` varchar(255) NOT NULL,
  `session_id` varchar(255) NOT NULL,
  `created` datetime,
  `last_scan_start` datetime,
  `last_scan_end` datetime,
  `http_code` bigint(20),
  `http_message` varchar(255),
  `exception` varchar(255),
  `mimetype` varchar(255),
  PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--ALTER TABLE resource_scan ADD COLUMN `exception` varchar(255) NULL;
-- Update current database config value
UPDATE `config` SET `value` = '1.6.0' WHERE `key` = 'db_version';