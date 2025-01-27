-- Update database v1.5.0 to database v1.6.0

-- Check the current version of the database. If it is incorrect a null value is
-- inserted into the 'db_version_check` field. This will fail, since the field is
-- defined as non null, and thus also fail this script
SELECT null INTO @current_value;
DELETE FROM `config` WHERE `key` = 'db_version_check';
SELECT `value` INTO @current_value FROM `config` WHERE ( `key` = 'db_version' AND `value` = '1.7.0' );
INSERT INTO `config` (`key`, `value`) VALUES ('db_version_check', @current_value);

ALTER TABLE resource ADD COLUMN `ref_resolved` VARCHAR(2048) NULL;
ALTER TABLE resource MODIFY ref VARCHAR(2048);

ALTER TABLE resource_scan ADD COLUMN `ref_resolved` VARCHAR(2048) NULL;
ALTER TABLE resource_scan MODIFY ref VARCHAR(2048);

-- Update current database config value
UPDATE `config` SET `value` = '1.8.0' WHERE `key` = 'db_version';