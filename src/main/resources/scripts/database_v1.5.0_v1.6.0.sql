-- Update database v1.5.0 to database v1.6.0

-- Check the current version of the database. If it is incorrect a null value is
-- inserted into the 'db_version_check` field. This will fail, since the field is
-- defined as non null, and thus also fail this script
SELECT null INTO @current_value;
DELETE FROM `config` WHERE `key` = 'db_version_check';
SELECT `value` INTO @current_value FROM `config` WHERE ( `key` = 'db_version' AND `value` = '1.5.0' );
INSERT INTO `config` (`key`, `value`) VALUES ('db_version_check', @current_value);

---
--- Update schema
---

ALTER TABLE virtualcollection ADD COLUMN `parent` bigint(20) NULL;
ALTER TABLE virtualcollection ADD COLUMN `child` bigint(20) NULL;
ALTER TABLE virtualcollection ADD COLUMN `latest` bigint(20) NULL;
ALTER TABLE virtualcollection ADD COLUMN `original` bigint(20) NULL;
ALTER TABLE virtualcollection ADD COLUMN `forked_from` bigint(20) NULL;
ALTER TABLE virtualcollection ADD COLUMN `public_leaf` bool default false;
ALTER TABLE pid ADD COLUMN `is_latest` bool default false;
ALTER TABLE pid ADD COLUMN `modified` datetime;
ALTER TABLE pid ADD COLUMN `modificationError` bool default false;
ALTER TABLE pid ADD COLUMN `modificationMsg` text NULL;

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
  `name_suggestion` varchar(255),
  `description_suggestion` text,
  PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--ALTER TABLE resource_scan ADD COLUMN `created` datetime;
--ALTER TABLE resource_scan ADD COLUMN `exception` varchar(255) NULL;
--ALTER TABLE resource_scan ADD COLUMN `name_suggestion` varchar(255) NULL;
--ALTER TABLE resource_scan ADD COLUMN `description_suggestion` text NULL;

---
--- Update data
---

-- Update all public collections to leaf
--update virtualcollection set public_leaf = 1 where state = 2;

-- Revert update to leaf for those public collections that have public children
--update virtualcollection set public_leaf = 0 where id in (select parent from virtualcollection where id IN (select child from virtualcollection where public_leaf = 1 and child is not null) and state = 2);

-- Revert update to leaf for those public collections that have public frozen children
--update virtualcollection set public_leaf = 0 where id in (select parent from virtualcollection where id IN (select child from virtualcollection where public_leaf = 1 and child is not null) and state = 4);

-- Update current database config value
UPDATE `config` SET `value` = '1.6.0' WHERE `key` = 'db_version';
