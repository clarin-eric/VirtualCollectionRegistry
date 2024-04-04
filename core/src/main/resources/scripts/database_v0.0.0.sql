---CREATE DATABASE `vcr`;

---DROP TABLE IF EXISTS `creator`;
CREATE TABLE `creator` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `organisation` varchar(255) DEFAULT NULL,
  `person` varchar(255) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  `telephone` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `vc_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3D4E802C8E3FCDB` (`vc_id`),
  CONSTRAINT `FK3D4E802C8E3FCDB` FOREIGN KEY (`vc_id`) REFERENCES `virtualcollection` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=latin1;

--
-- Table structure for table `keyword`
--

--- DROP TABLE IF EXISTS `keyword`;
CREATE TABLE `keyword` (
  `vc_id` bigint(20) NOT NULL,
  `keywords` varchar(255) DEFAULT NULL,
  KEY `FKCF751DE98E3FCDB` (`vc_id`),
  CONSTRAINT `FKCF751DE98E3FCDB` FOREIGN KEY (`vc_id`) REFERENCES `virtualcollection` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `pid`
--

--- DROP TABLE IF EXISTS `pid`;
CREATE TABLE `pid` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `identifier` varchar(255) NOT NULL,
  `type` int(11) DEFAULT NULL,
  `vc_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `identifier` (`identifier`),
  UNIQUE KEY `vc_id` (`vc_id`),
  KEY `FK1B18B8E3FCDB` (`vc_id`),
  CONSTRAINT `FK1B18B8E3FCDB` FOREIGN KEY (`vc_id`) REFERENCES `virtualcollection` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;

--
-- Table structure for table `resource`
--

--- DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `label` varchar(255) DEFAULT NULL,
  `ref` varchar(255) NOT NULL,
  `type` int(11) NOT NULL,
  `vc_id` bigint(20) NOT NULL,
  `resources_ORDER` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKEBABC40E8E3FCDB` (`vc_id`),
  CONSTRAINT `FKEBABC40E8E3FCDB` FOREIGN KEY (`vc_id`) REFERENCES `virtualcollection` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=183 DEFAULT CHARSET=latin1;

--
-- Table structure for table `user`
--

--- DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `display_name` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;

--
-- Table structure for table `virtualcollection`
--

--- DROP TABLE IF EXISTS `virtualcollection`;
CREATE TABLE `virtualcollection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` date DEFAULT NULL,
  `created` datetime NOT NULL,
  `modified` datetime NOT NULL,
  `description` longtext,
  `generatedby_description` longtext,
  `generatedby_query_profile` varchar(255) DEFAULT NULL,
  `generatedby_query_value` longtext,
  `generatedby_uri` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `purpose` int(11) DEFAULT NULL,
  `reproducibility` int(11) DEFAULT NULL,
  `reproducibility_notice` longtext,
  `state` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `owner_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6437BBE9C7855CED` (`owner_id`),
  CONSTRAINT `FK6437BBE9C7855CED` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1020 DEFAULT CHARSET=latin1;