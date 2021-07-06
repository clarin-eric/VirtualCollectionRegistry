-- Update database v1.2.0 to database v1.3.0

-- Check the current version of the database. If it is incorrect a null value is
-- inserted into the 'db_version_check` field. This will fail, since the field is
-- defined as non null, and thus also fail this script
SELECT null INTO @current_value;
DELETE FROM `config` WHERE `key` = 'db_version_check';
SELECT `value` INTO @current_value FROM `config` WHERE ( `key` = 'db_version' AND `value` = '1.2.0' );
INSERT INTO `config` (`key`, `value`) VALUES ('db_version_check', @current_value);

-- Apply updates

CREATE TABLE `api_key` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `value` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL,
  `last_used_at` datetime,
  `revoked_at` datetime,
  PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Update current database config value
UPDATE `config` SET `value` = '1.3.0' WHERE `key` = 'db_version';

ALTER TABLE api_keys CHANGE COLUMN `revokedAt` `revoked_at` datetime;