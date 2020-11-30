-- Update database v1.1.0 to database v1.2.0

-- Check the current version of the database. If it is incorrect a null value is
-- inserted into the 'db_version_check` field. This will fail, since the field is
-- defined as non null, and thus also fail this script
SELECT null INTO @current_value;
DELETE FROM `config` WHERE `key` = 'db_version_check';
SELECT `value` INTO @current_value FROM `config` WHERE ( `key` = 'db_version' AND `value` = '1.1.0' );
INSERT INTO `config` (`key`, `value`) VALUES ('db_version_check', @current_value);

ALTER TABLE resource ADD COLUMN `checked` VARCHAR(255) NULL;
ALTER TABLE resource ADD COLUMN `mimetype` VARCHAR(255) NULL;
ALTER TABLE resource ADD COLUMN `display_order` BIGINT NULL;
UPDATE resource SET display_order = 1;

ALTER TABLE creator ADD COLUMN `display_order` BIGINT NULL;
UPDATE creator SET display_order = 1;

ALTER TABLE virtualcollection ADD COLUMN `problem_details` TEXT NULL;

-- Update current database config value
UPDATE `config` SET `value` = '1.2.0' WHERE `key` = 'db_version';
