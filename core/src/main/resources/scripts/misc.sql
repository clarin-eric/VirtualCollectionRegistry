-- Update schema
-- ALTER TABLE resource_scan ADD COLUMN `processor` varchar(255) NULL;
-- ALTER TABLE resource_scan ADD COLUMN `pid_suggestion` text NULL;

-- ALTER TABLE `resource_kv` DROP FOREIGN KEY `FK1B18B8E3FCDE`;
-- ALTER TABLE resource_kv
--  ADD CONSTRAINT `FK1B18B8E3FCDE` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`) ON DELETE CASCADE;

-- ALTER TABLE `resource_scan_log` DROP FOREIGN KEY `FK1B18B8E3FCDC`;
-- ALTER TABLE resource_scan_log
--  ADD CONSTRAINT `FK1B18B8E3FCDC` FOREIGN KEY (`scan_id`) REFERENCES `resource_scan` (`id`) ON DELETE CASCADE;


-- ALTER TABLE `resource_kv` CHANGE COLUMN `key` `k` varchar(255);
-- ALTER TABLE `resource_kv` CHANGE COLUMN `value` `v` text;
-- ALTER TABLE `resource_scan_log_kv` CHANGE COLUMN `key` `k` varchar(255);
-- ALTER TABLE `resource_scan_log_kv` CHANGE COLUMN `value` `v` text;
