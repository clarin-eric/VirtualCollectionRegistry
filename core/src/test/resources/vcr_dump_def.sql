-- MySQL dump 10.16  Distrib 10.3.10-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: vcr
-- ------------------------------------------------------
-- Server version	10.3.10-MariaDB-1:10.3.10+maria~bionic












--
-- Table structure for table api_key
--

DROP TABLE IF EXISTS api_key;


CREATE TABLE api_key (
  id bigint NOT NULL IDENTITY,
  user_id bigint NOT NULL,
  value varchar(255) NOT NULL,
  created_at datetime NOT NULL,
  last_used_at datetime DEFAULT NULL,
  revoked_at datetime DEFAULT NULL,
  PRIMARY KEY (id)
);


--
-- Table structure for table config
--

DROP TABLE IF EXISTS config;


CREATE TABLE config (
  key varchar(255) NOT NULL,
  value varchar(255) NOT NULL,
  PRIMARY KEY (key)
);


--
-- Table structure for table creator
--

DROP TABLE IF EXISTS creator;


CREATE TABLE creator (
  id bigint NOT NULL IDENTITY,
  address varchar(255) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  organisation varchar(255) DEFAULT NULL,
  family_name varchar(255) DEFAULT NULL,
  role varchar(255) DEFAULT NULL,
  telephone varchar(255) DEFAULT NULL,
  website varchar(255) DEFAULT NULL,
  vc_id bigint NOT NULL,
  display_order bigint DEFAULT NULL,
  given_name varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),

  FOREIGN KEY (vc_id) REFERENCES virtualcollection (id)
);


--
-- Table structure for table keyword
--

DROP TABLE IF EXISTS keyword;


CREATE TABLE keyword (
  vc_id bigint NOT NULL,
  keywords varchar(255) DEFAULT NULL,

  FOREIGN KEY (vc_id) REFERENCES virtualcollection (id)
);


--
-- Table structure for table pid
--

DROP TABLE IF EXISTS pid;


CREATE TABLE pid (
  id bigint NOT NULL IDENTITY,
  identifier varchar(255) NOT NULL,
  type bigint DEFAULT NULL,
  is_primary boolean(1) DEFAULT NULL,
  vc_id bigint NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT identifier UNIQUE,

  FOREIGN KEY (vc_id) REFERENCES virtualcollection (id)
);


--
-- Table structure for table resource
--

DROP TABLE IF EXISTS resource;


CREATE TABLE resource (
  id bigint NOT NULL IDENTITY,
  description LONGVARCHAR DEFAULT NULL,
  label varchar(255) DEFAULT NULL,
  ref varchar(255) NOT NULL,
  type bigint NOT NULL,
  vc_id bigint NOT NULL,
  resources_ORDER bigint NOT NULL,
  checked varchar(255) DEFAULT NULL,
  mimetype varchar(255) DEFAULT NULL,
  display_order bigint DEFAULT NULL,
  origin varchar(255) DEFAULT NULL,
  original_query varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),

  FOREIGN KEY (vc_id) REFERENCES virtualcollection (id)
);


--
-- Table structure for table vcr_user
--

DROP TABLE IF EXISTS vcr_user;


CREATE TABLE vcr_user (
  id bigint NOT NULL IDENTITY,
  display_name varchar(255) DEFAULT NULL,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT name UNIQUE
);


--
-- Table structure for table virtualcollection
--

DROP TABLE IF EXISTS virtualcollection;


CREATE TABLE virtualcollection (
  id bigint NOT NULL IDENTITY,
  creation_date date DEFAULT NULL,
  created datetime NOT NULL,
  modified datetime NOT NULL,
  description LONGVARCHAR DEFAULT NULL,
  generatedby_description LONGVARCHAR DEFAULT NULL,
  generatedby_query_profile varchar(255) DEFAULT NULL,
  generatedby_query_value LONGVARCHAR DEFAULT NULL,
  generatedby_uri varchar(255) DEFAULT NULL,
  name varchar(255) NOT NULL,
  purpose bigint DEFAULT NULL,
  reproducibility bigint DEFAULT NULL,
  reproducibility_notice LONGVARCHAR DEFAULT NULL,
  state bigint NOT NULL,
  type bigint NOT NULL,
  owner_id bigint NOT NULL,
  problem bigint DEFAULT NULL,
  problem_details LONGVARCHAR DEFAULT NULL,
  origin varchar(255) DEFAULT NULL,
  published datetime DEFAULT NULL,
  parent bigint DEFAULT NULL,
  child bigint DEFAULT NULL,
  latest bigint DEFAULT NULL,
  forked_from bigint DEFAULT NULL,
  PRIMARY KEY (id),

  FOREIGN KEY (owner_id) REFERENCES vcr_user (id)
);


--
-- Dumping events for database 'vcr'
--

--
-- Dumping routines for database 'vcr'
--










-- Dump completed on 2022-01-14  9:57:43
