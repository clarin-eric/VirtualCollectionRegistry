-- MySQL dump 10.16  Distrib 10.3.10-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: vcr
-- ------------------------------------------------------
-- Server version	10.3.10-MariaDB-1:10.3.10+maria~bionic

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `api_key`
--

DROP TABLE IF EXISTS `api_key`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `api_key` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `value` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL,
  `last_used_at` datetime DEFAULT NULL,
  `revoked_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `config`
--

DROP TABLE IF EXISTS `config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `config` (
  `key` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `creator`
--

DROP TABLE IF EXISTS `creator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `creator` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `organisation` varchar(255) DEFAULT NULL,
  `family_name` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `telephone` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `vc_id` bigint(20) NOT NULL,
  `display_order` bigint(20) DEFAULT NULL,
  `given_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3D4E802C8E3FCDB` (`vc_id`),
  CONSTRAINT `FK3D4E802C8E3FCDB` FOREIGN KEY (`vc_id`) REFERENCES `virtualcollection` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=131 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `keyword`
--

DROP TABLE IF EXISTS `keyword`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `keyword` (
  `vc_id` bigint(20) NOT NULL,
  `keywords` varchar(255) DEFAULT NULL,
  KEY `FKCF751DE98E3FCDB` (`vc_id`),
  CONSTRAINT `FKCF751DE98E3FCDB` FOREIGN KEY (`vc_id`) REFERENCES `virtualcollection` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pid`
--

DROP TABLE IF EXISTS `pid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pid` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `identifier` varchar(255) NOT NULL,
  `type` int(11) DEFAULT NULL,
  `is_primary` tinyint(1) DEFAULT NULL,
  `vc_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `identifier` (`identifier`),
  KEY `FK1B18B8E3FCDB` (`vc_id`),
  CONSTRAINT `FK1B18B8E3FCDB` FOREIGN KEY (`vc_id`) REFERENCES `virtualcollection` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource`
--

DROP TABLE IF EXISTS `resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` longtext DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `ref` varchar(255) NOT NULL,
  `type` int(11) NOT NULL,
  `vc_id` bigint(20) NOT NULL,
  `resources_ORDER` int(11) NOT NULL,
  `checked` varchar(255) DEFAULT NULL,
  `mimetype` varchar(255) DEFAULT NULL,
  `display_order` bigint(20) DEFAULT NULL,
  `origin` varchar(255) DEFAULT NULL,
  `original_query` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKEBABC40E8E3FCDB` (`vc_id`),
  CONSTRAINT `FKEBABC40E8E3FCDB` FOREIGN KEY (`vc_id`) REFERENCES `virtualcollection` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=488 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vcr_user`
--

DROP TABLE IF EXISTS `vcr_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vcr_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `display_name` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `virtualcollection`
--

DROP TABLE IF EXISTS `virtualcollection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `virtualcollection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` date DEFAULT NULL,
  `created` datetime NOT NULL,
  `modified` datetime NOT NULL,
  `description` longtext DEFAULT NULL,
  `generatedby_description` longtext DEFAULT NULL,
  `generatedby_query_profile` varchar(255) DEFAULT NULL,
  `generatedby_query_value` longtext DEFAULT NULL,
  `generatedby_uri` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `purpose` int(11) DEFAULT NULL,
  `reproducibility` int(11) DEFAULT NULL,
  `reproducibility_notice` longtext DEFAULT NULL,
  `state` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `owner_id` bigint(20) NOT NULL,
  `problem` int(11) DEFAULT NULL,
  `problem_details` text DEFAULT NULL,
  `origin` varchar(255) DEFAULT NULL,
  `published` datetime DEFAULT NULL,
  `parent` bigint(20) DEFAULT NULL,
  `child` bigint(20) DEFAULT NULL,
  `latest` bigint(20) DEFAULT NULL,
  `forked_from` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6437BBE9C7855CED` (`owner_id`),
  CONSTRAINT `FK6437BBE9C7855CED` FOREIGN KEY (`owner_id`) REFERENCES `vcr_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1122 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'vcr'
--

--
-- Dumping routines for database 'vcr'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-01-13 13:44:57
