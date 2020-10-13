-- Update database v1.0.0 to database v1.0.1

-- Check the current version of the database. If it is incorrect a null value is
-- inserted into the 'db_version_check` field. This will fail, since the field is
-- defined as non null, and thus also fail this script
SELECT null INTO @current_value;
DELETE FROM `config` WHERE `key` = 'db_version_check';
SELECT `value` INTO @current_value FROM `config` WHERE ( `key` = 'db_version' AND `value` = '1.0.0' );
INSERT INTO `config` (`key`, `value`) VALUES ('db_version_check', @current_value);

-- Update current database config value
UPDATE `config` SET `value` = '1.0.1' WHERE `key` = 'db_version';
