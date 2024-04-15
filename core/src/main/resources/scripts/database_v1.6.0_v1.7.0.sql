-- Update database v1.5.0 to database v1.6.0

-- Check the current version of the database. If it is incorrect a null value is
-- inserted into the 'db_version_check` field. This will fail, since the field is
-- defined as non null, and thus also fail this script
SELECT null INTO @current_value;
DELETE FROM `config` WHERE `key` = 'db_version_check';
SELECT `value` INTO @current_value FROM `config` WHERE ( `key` = 'db_version' AND `value` = '1.6.0' );
INSERT INTO `config` (`key`, `value`) VALUES ('db_version_check', @current_value);

--
-- Table structure for table `resource_kv`
--

--- DROP TABLE IF EXISTS `resource_kv`;
CREATE TABLE `resource_kv` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `k` varchar(255) NOT NULL,
  `v` text NOT NULL,
  `resource_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK1B18B8E3FCDE` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;

--
-- Table structure for table `resource_scan_log`
--

--- DROP TABLE IF EXISTS `resource_scan_log`;
CREATE TABLE `resource_scan_log` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `processor` varchar(255) NOT NULL,
  `scan_id` bigint(20) NOT NULL,
  `start` datetime NULL,
  `end` datetime NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK1B18B8E3FCDC` FOREIGN KEY (`scan_id`) REFERENCES `resource_scan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;


--- ALTER TABLE resource_scan_log ADD COLUMN `start` datetime NULL;
--- ALTER TABLE resource_scan_log ADD COLUMN `end` datetime NULL;

--
-- Table structure for table `resource_scan_log_kv`
--

--- DROP TABLE IF EXISTS `resource_scan_log_kv`;
CREATE TABLE `resource_scan_log_kv` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `k` varchar(255) NOT NULL,
  `v` text NOT NULL,
  `scan_log_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK1B18B8E3FCDD` FOREIGN KEY (`scan_log_id`) REFERENCES `resource_scan_log` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;


-- Update current database config value
UPDATE `config` SET `value` = '1.7.0' WHERE `key` = 'db_version';