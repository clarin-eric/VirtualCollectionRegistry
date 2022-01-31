-- MySQL dump 10.16  Distrib 10.3.10-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: vcr
-- ------------------------------------------------------
-- Server version	10.3.10-MariaDB-1:10.3.10+maria~bionic


--
-- Dumping data for table config
--
INSERT INTO config VALUES ('db_version','1.5.0'),('db_version_check','1.3.0');

--
-- Dumping data for table vcr_user
--
INSERT INTO vcr_user VALUES (1,NULL,'schonefeld@ids-mannheim.de'),(2,NULL,'dietuyt_mpi.nl@clarin.eu'),(3,NULL,'r.vansluijs_let.ru.nl@clarin.eu'),(4,NULL,'Odijk101@soliscom.uu.nl'),(5,NULL,'willem_clarin.eu@clarin.eu'),(6,NULL,'nnstt01@uni-tuebingen.de'),(7,NULL,'nalida_sfs.uni-tuebingen.de@clarin.eu'),(8,NULL,'27364@ut.ee'),(9,NULL,'44130@ut.ee'),(10,NULL,'BAC1286@uni-hamburg.de'),(11,NULL,'misutka_ufal.mff.cuni.cz@clarin.eu'),(12,NULL,'thorsten.trippel_uni-tuebingen.de@clarin.eu'),(13,NULL,'teckart_informatik.uni-leipzig.de@clarin.eu'),(14,NULL,'thomas.schmidt@ids-mannheim.de'),(15,NULL,'dieter_clarin.eu@clarin.eu'),(17,NULL,'emanuel.dima_uni-tuebingen.de@clarin.eu'),(18,NULL,'twan_clarin.eu@clarin.eu'),(19,NULL,'admin'),(26,NULL,'user1'),(27,NULL,'admin1');

--
-- Dumping data for table api_key
--
INSERT INTO api_key (id, user_id, value, created_at, last_used_at, revoked_at) VALUES (1,26,'xI5ft8eljFVU0Oh6o5k88wTqXBpL2CuXymDrQkv3NCdPJguDpyPEF7ADJ1BGi9RQ','2021-05-31 11:14:42',NULL,NULL),(2,26,'Y0zVV9vkWyyykIzuD013WJuejTQo8ZMZSaTm2BXohM9cwk3s0HO4wHx4EEb8fGE3','2021-05-31 11:14:45',NULL,NULL),(3,26,'spc7qrmplo0VgQGZ9T0rKaWYTaDIxNnqSXz3mednqj32KDIw8tisbL3V2jrUTehj','2021-05-31 11:14:46',NULL,NULL),(4,26,'zeBaNMYqkUGslaluEdGF19Pbpj0JieO8vM3vxWQyNiUp8w6NLKRtuqKVWgVLdNHB','2021-05-31 11:14:47',NULL,NULL),(5,26,'NOm8LQQZ57nsLSAHGeZzhHrVpoEpYRIKg9RhiOmW12N7NMkYrmZQtHoa118Nv5aA','2021-05-31 11:14:48',NULL,NULL);




--
-- Dumping data for table creator
--
INSERT INTO creator (id, address, email, organisation, family_name, role, telephone, website, vc_id, display_order, given_name) VALUES (5,NULL,'Gunter.Senft@mpi.nl','Max Planck Institute for Psycholinguistics','Gunter Senft','Researcher',NULL,'http://www.mpi.nl/people/senft-gunter',1000,1,NULL),
(7,NULL,NULL,'Max Planck Institute for Psycholinguistics','Connie de Vos','Researcher',NULL,'http://www.mpi.nl/people/vos-connie-de',1001,1,NULL),
(8,NULL,'dieter@clarin.eu','CLARIN','Dieter Van Uytvanck',NULL,NULL,NULL,1002,1,NULL),
(9,NULL,'r.vansluijs@let.ru.nl','Radboud University','Robbert van Sluijs',NULL,NULL,NULL,1003,1,NULL),
(10,NULL,'M.C.vandenBerg1@uu.nl','Utrecht University','Margot van den Berg',NULL,NULL,NULL,1003,1,NULL),
(11,NULL,'p.muysken@let.ru.nl','Radboud University','Pieter Muysken',NULL,NULL,NULL,1003,1,NULL),
(12,NULL,'j.odijk@uu.nl','uu.nl','J.E.J.M. Odijk',NULL,NULL,NULL,1004,1,NULL),
(13,NULL,'willem@clarin.eu','CLARIN','Willem Elbers',NULL,NULL,NULL,1005,1,NULL),
(14,NULL,'thorsten.trippel@uni-tuebingen.de','Universität Tübingen','Trippel, Thorsten',NULL,NULL,NULL,1006,1,NULL),
(16,NULL,'kadri.vare@ut.ee',NULL,'Kadri Vare',NULL,NULL,NULL,1008,1,NULL),
(17,NULL,'krista.liin@ut.ee',NULL,'Krista Liin',NULL,NULL,NULL,1009,1,NULL),
(18,NULL,'misutka@ufal.mff.cuni.cz','CLARIN','misutka_ufal.mff.cuni.cz@clarin.eu',NULL,NULL,NULL,1010,1,NULL),
(19,NULL,'willem@clarin.eu','CLARIN','willem_clarin.eu@clarin.eu',NULL,NULL,NULL,1011,1,NULL),
(20,NULL,'thorsten.trippel@uni-tuebingen.de','Universität Tübingen','Thorsten Trippel',NULL,NULL,NULL,1012,1,NULL),
(21,NULL,'thorsten.trippel@uni-tuebingen.de','Universität Tübingen','Thorsten Trippel',NULL,NULL,NULL,1013,1,NULL),
(22,NULL,'thomas.schmidt@ids-mannheim.de','Institut fÃ¼r Deutsche Sprache','Thomas Schmidt',NULL,NULL,NULL,1015,1,NULL),
(25,NULL,NULL,NULL,'test',NULL,NULL,NULL,1017,1,NULL),(26,NULL,'someperson@gmail.com','Some Organisation','Some Person',NULL,NULL,NULL,1017,1,NULL),
(27,NULL,NULL,NULL,'willem_clarin.eu@clarin.eu',NULL,NULL,NULL,1018,1,NULL),(28,NULL,NULL,NULL,'anonymous',NULL,NULL,NULL,1019,1,NULL),
(29,NULL,NULL,NULL,'anonymous',NULL,NULL,NULL,1020,1,NULL),(30,NULL,NULL,NULL,'anonymous',NULL,NULL,NULL,1021,1,NULL),
(31,NULL,NULL,NULL,'willem_clarin.eu@clarin.eu',NULL,NULL,NULL,1022,1,NULL),
(32,NULL,NULL,NULL,'willem_clarin.eu@clarin.eu',NULL,NULL,NULL,1023,1,NULL),
(33,NULL,'willem@clarin.eu','CLARIN ERIC','Willem Elbers',NULL,NULL,NULL,1024,1,NULL),
(34,NULL,'dieter@clarin.eu','CLARIN ERIC','Dieter Van Uytvanck',NULL,NULL,NULL,1025,1,NULL),
(35,NULL,NULL,NULL,'willem_clarin.eu@clarin.eu',NULL,NULL,NULL,1026,1,NULL),
(38,NULL,NULL,NULL,'willem_clarin.eu@clarin.eu',NULL,NULL,NULL,1029,1,NULL),
(39,NULL,NULL,NULL,'admin',NULL,NULL,NULL,1030,1,NULL),
(49,NULL,'dieter@clarin.eu',NULL,'Dieter van Uytvank',NULL,NULL,NULL,1040,0,NULL),
(50,NULL,'dieter@clarin.eu',NULL,'Dieter van Uytvank',NULL,NULL,NULL,1041,0,NULL),
(57,NULL,'willem@clarin.eu','CLARIN ERIC','Willem Elbers',NULL,NULL,NULL,1048,0,NULL),
(58,NULL,'dieter@clarin.eu','CLARIN ERIC','Dieter van Uytvank',NULL,NULL,NULL,1048,0,NULL),
(59,NULL,'dieter@clarin.eu','CLARIN ERIC','Dieter van Uytvank',NULL,NULL,NULL,1049,0,NULL),
(60,NULL,'willem@clarin.eu',NULL,'Willem Elbers',NULL,NULL,NULL,1049,0,NULL),
(62,NULL,'dieter@clarin.eu',NULL,'Dieter van Uytvank',NULL,NULL,NULL,1052,0,NULL),
(63,NULL,'dieter@clarin.eu',NULL,'Dieter van Uytvank',NULL,NULL,NULL,1053,0,NULL),
(64,NULL,'dieter@clarin.eu',NULL,'Dieter van Uytvank',NULL,NULL,NULL,1054,0,NULL),
(65,NULL,'willem@clarin.eu','CLARIN ERIC','Willem Elbers',NULL,NULL,NULL,1055,0,NULL),
(66,NULL,'willem@clarin.eu','CLARIN ERIC','Willem Elbers',NULL,NULL,NULL,1056,0,NULL),
(67,NULL,'willem@clarin.eu','CLARIN ERIC','Willem Elbers',NULL,NULL,NULL,1057,0,NULL),
(68,NULL,'willem@clarin.eu','CLARIN ERIC','Willem Elbers',NULL,NULL,NULL,1058,0,NULL),
(69,NULL,'willem@clarin.eu','CLARIN ERIC','Willem Elbers',NULL,NULL,NULL,1059,0,NULL),
(70,NULL,'willem@clarin.eu','CLARIN ERIC','Willem Elbers',NULL,NULL,NULL,1060,0,NULL),
(71,NULL,'dieter@clarin.eu','CLARIN ERIC','Dieter van Uytvank',NULL,NULL,NULL,1061,0,NULL),
(72,NULL,'willem@clarin.eu','CLARIN ERIC','Willem Elbers',NULL,NULL,NULL,1061,0,NULL),
(73,NULL,'willem@clarin.eu','willem@clarin.eu','Elbers',NULL,NULL,NULL,1062,0,'Willem'),
(74,NULL,'willem@clarin.eu','CLARIN ERIC','Elbers',NULL,NULL,NULL,1063,0,'Willem'),
(75,NULL,'willem@clarin.eu','CLARIN ERIC','Willem',NULL,NULL,NULL,1064,0,'Elbers'),
(76,NULL,'willem@clarin.eu','CLARIN ERIC','Willem',NULL,NULL,NULL,1065,0,'Elbers'),
(77,NULL,'1@2.3','123','1',NULL,NULL,NULL,1066,0,'2'),
(78,NULL,NULL,NULL,'user1',NULL,NULL,NULL,1067,0,''),
(79,NULL,NULL,NULL,'user1',NULL,NULL,NULL,1068,0,''),
(80,NULL,NULL,NULL,'user1',NULL,NULL,NULL,1069,0,''),
(81,NULL,NULL,NULL,'user1',NULL,NULL,NULL,1070,0,''),
(82,NULL,NULL,NULL,'user1',NULL,NULL,NULL,1071,0,''),
(83,NULL,NULL,NULL,'user1',NULL,NULL,NULL,1072,0,''),
(84,NULL,NULL,NULL,'user1',NULL,NULL,NULL,1073,0,''),
(85,NULL,NULL,NULL,'user1',NULL,NULL,NULL,1074,0,''),
(86,NULL,NULL,NULL,'user1',NULL,NULL,NULL,1075,0,''),
(87,NULL,NULL,NULL,'user1',NULL,NULL,NULL,1076,0,''),
(88,NULL,'willem@clarin.eu',NULL,'Elbers',NULL,NULL,NULL,1077,0,'Willem'),
(89,NULL,NULL,NULL,'user1',NULL,NULL,NULL,1078,0,''),
(90,NULL,NULL,NULL,'admin1',NULL,NULL,NULL,1079,0,''),
(110,NULL,'willem@clarin.eu',NULL,'Elbers',NULL,NULL,NULL,1099,0,'Willem'),
(111,NULL,'willem@clarin.eu',NULL,'Elbers',NULL,NULL,NULL,1100,0,'Willem'),
(112,NULL,'willem@clarin.eu',NULL,'Elbers',NULL,NULL,NULL,1101,0,'Willem'),
(117,NULL,'willem@clarin.eu',NULL,'Elbers',NULL,NULL,NULL,1106,0,'Willem'),
(118,NULL,'willem@clarin.eu',NULL,'Elbers',NULL,NULL,NULL,1107,0,'Willem'),
(123,NULL,'willem@clarin.eu',NULL,'Elbers',NULL,NULL,NULL,1112,0,'Willem'),
(124,NULL,'willem@clarin.eu',NULL,'Elbers',NULL,NULL,NULL,1113,0,'Willem'),
(125,NULL,'x@y.z',NULL,'x',NULL,NULL,NULL,1114,0,'y'),
(126,NULL,'a@b.c',NULL,'a',NULL,NULL,NULL,1114,1,'b'),
(127,NULL,'dieter@clarin.eu',NULL,'Dieter Van Uytvanck',NULL,NULL,NULL,1118,1,'Unkown'),
(128,NULL,'dieter@clarin.eu',NULL,'Dieter Van Uytvanck',NULL,NULL,NULL,1119,1,'Unkown'),
(129,NULL,'willem@clarin.eu',NULL,'Elbers',NULL,NULL,NULL,1120,1,'Willem'),
(130,NULL,'willem@clarin.eu',NULL,'Willem Elbers',NULL,NULL,NULL,1121,0,'Unkown');

--
-- Dumping data for table keyword
--
INSERT INTO keyword VALUES (1000,'Endangered Languages'),(1000,'Textlinguistics'),(1000,'Sociolinguistics'),(1000,'Anthropology'),(1001,'sign language'),(1001,'Kata Kolok'),(1002,'Henrik Ibsen'),(1002,'plays'),(1003,'property concept, Moravian, gospel harmony, dialogue, narrative, Anansi, Surinam, Virgin Islands, Negerhollands, Sranan, Sranantongo, Saramaccan'),(1012,'BAS'),(1012,'code:deu'),(1013,'bas'),(1013,'code:deu'),(1014,'code:deu'),(1018,'Nijmegen AND spoken AND soundbites'),(1019,'keyword1'),(1020,'kleve nijmegen'),(1021,'kleef nijmegen'),(1022,'limburg.sp.nl'),(1023,'limburg.sp.nl'),(1024,'test'),(1029,'pubmed covid19 influenza'),(1030,'pubmed covid19 influenza'),(1040,'test1'),(1040,'test2'),(1040,'test3'),(1040,'test4'),(1041,'test1'),(1041,'test2'),(1041,'test3'),(1041,'test4'),(1048,'test1'),(1048,'test2'),(1048,'test3'),(1048,'test4'),(1049,'test1'),(1049,'test2'),(1049,'test3'),(1049,'test4'),(1052,'test1'),(1052,'test2'),(1052,'test3'),(1052,'test4'),(1053,'test1'),(1053,'test2'),(1053,'test3'),(1053,'test4'),(1054,'test1'),(1054,'test2'),(1054,'test3'),(1054,'test4'),(1055,'test1'),(1055,'test2'),(1055,'test3'),(1055,'test4'),(1056,'doi'),(1056,'test'),(1056,'collection'),(1057,'test1'),(1057,'test2'),(1057,'test3'),(1057,'test4'),(1058,'test1'),(1058,'test2'),(1058,'test3'),(1058,'test4'),(1059,'test1'),(1059,'test2'),(1059,'test3'),(1059,'test4'),(1060,'test1'),(1060,'test2'),(1060,'test3'),(1060,'test4'),(1061,'test1'),(1061,'test2'),(1061,'test3'),(1061,'test4'),(1062,'test1'),(1062,'test2'),(1062,'test3'),(1062,'test4'),(1063,'test1'),(1063,'test2'),(1063,'test3'),(1063,'test4'),(1065,'test1'),(1065,'test2'),(1065,'test3'),(1065,'test4'),(1066,'123'),(1066,'123'),(1066,'123'),(1066,'123'),(1066,'123'),(1066,'123'),(1066,'123'),(1066,'123'),(1066,'123'),(1066,'123'),(1064,'test'),(1064,'test'),(1064,'test'),(1064,'test'),(1064,'test'),(1064,'test'),(1064,'test'),(1064,'test'),(1064,'test'),(1064,'test'),(1067,'keyword1'),(1068,'keyword1'),(1069,'keyword1'),(1070,'keyword1'),(1071,'keyword1'),(1072,'keyword1'),(1073,'keyword1'),(1074,'keyword1'),(1075,'keyword1'),(1076,'keyword1'),(1078,'keyword1'),(1078,'keyword1'),(1078,'keyword1'),(1078,'keyword1'),(1078,'keyword1'),(1078,'keyword1'),(1078,'keyword1'),(1078,'keyword1'),(1078,'keyword1'),(1078,'keyword1'),(1077,'test1'),(1077,'test1'),(1077,'test1'),(1077,'test1'),(1077,'test1'),(1077,'test1'),(1077,'test1'),(1077,'test1'),(1077,'test1'),(1077,'test1'),(1079,'keyword1'),(1079,'keyword1'),(1099,'test'),(1100,'test'),(1100,'2'),(1101,'test'),(1101,'3'),(1106,'test'),(1107,'test'),(1112,'test'),(1113,'test'),(1114,'c'),(1118,'Henrik'),(1118,'Ibsen'),(1118,'plays'),(1119,'Henrik'),(1119,'Ibsen'),(1119,'plays'),(1120,'kleve'),(1120,'nijmegen'),(1121,'doi'),(1121,'test'),(1121,'collection'),(1121,'doi'),(1121,'test'),(1121,'collection'),(1121,'doi'),(1121,'test'),(1121,'collection'),(1121,'doi'),(1121,'test'),(1121,'collection'),(1121,'doi'),(1121,'test'),(1121,'collection'),(1121,'doi'),(1121,'test'),(1121,'collection'),(1121,'doi'),(1121,'test'),(1121,'collection'),(1121,'doi'),(1121,'test'),(1121,'collection'),(1121,'doi'),(1121,'test'),(1121,'collection'),(1121,'doi'),(1121,'test'),(1121,'collection');



--
-- Dumping data for table pid
--



INSERT INTO pid VALUES (5,'11372/VC-1000',1,1,1000),(6,'11372/VC-1002',1,1,1002),(7,'11372/VC-1001',1,1,1001),(8,'11372/VC-1003',1,1,1003),(9,'11372/VC-1006',1,1,1006),(10,'11372/VC-1012',1,1,1012),(11,'dummy-1022',0,1,1022),(12,'dummy-1020',0,1,1020),(13,'11372/VCR-BETA-1018',1,1,1018),(14,'11372/VCR-BETA-1029',1,1,1029),(15,'dummy-1041',0,1,1041),(16,'dummy-1040',0,1,1040),(17,'dummy-1052',0,1,1052),(19,'dummy-1054',0,1,1054),(24,'10.17907/e59d-8t51',1,0,1053),(25,'dummy-1053',0,1,1053),(26,'10.17907/v58g-8a25',2,0,1055),(27,'dummy-1055',0,1,1055),(28,'dummy-1056',0,1,1056),(29,'10.17907/nemp-8r43',2,0,1056),(30,'10.17907/5sc6-as65',2,0,1057),(31,'dummy-1057',0,1,1057),(32,'10.17907/yht5-k682',2,0,1058),(33,'dummy-1058',0,1,1058),(34,'dummy-1059',0,1,1059),(35,'10.17907/cqje-6z22',2,0,1059),(36,'dummy-1060',0,1,1060),(37,'10.17907/k73h-2f70',2,0,1060),(38,'dummy-1061',0,1,1061),(39,'10.17907/w4qr-r288',2,0,1061),(40,'10.17907/hp5e-5r67',2,0,1062),(41,'dummy-1062',0,1,1062),(42,'dummy-1063',0,1,1063),(43,'10.17907/hjht-nz02',2,0,1063),(44,'10.17907/test1079',2,0,1079),(45,'dummy-1079',0,1,1079);



--
-- Dumping data for table resource
--



INSERT INTO resource VALUES (5,'‘Biga baloma / Biga tommwaya’ and ‘Wosi milamala’ – ‘Speech of the spirits of the dead / Old peoples’ speech’ and ‘songs of the harvest festival’','Chapter 4','http://dx.doi.org/10.1515/9783110227994.26',1,1000,0,NULL,NULL,1,NULL,NULL),(6,NULL,'Sound recording Tauwema_1983_T1_sideA','http://hdl.handle.net/1839/00-0000-0000-0005-6798-1',1,1000,1,NULL,NULL,1,NULL,NULL),(7,NULL,'Sound recording Tauwema_1983_T1_sideB','http://hdl.handle.net/1839/00-0000-0000-0005-6797-3',1,1000,2,NULL,NULL,1,NULL,NULL),(8,'‘Biga megwa’ and ‘megwa’ – ‘Magic speech’ and ‘magical formulae’','Chapter 5','http://dx.doi.org/10.1515/9783110227994.40',1,1000,3,NULL,NULL,1,NULL,NULL),(9,NULL,'Sound recording Magie_1989_sideA','http://hdl.handle.net/1839/00-0000-0000-0004-9221-F',1,1000,4,NULL,NULL,1,NULL,NULL),(10,NULL,'Sound recording Magie_1989_sideB','http://hdl.handle.net/1839/00-0000-0000-0004-9222-1',1,1000,5,NULL,NULL,1,NULL,NULL),(11,'This film presents (1);






--
-- Dumping data for table virtualcollection
--



INSERT INTO virtualcollection VALUES (1000,'2014-09-22','2014-09-22 21:10:03','2021-09-14 10:40:00','Digital references for the book \"The Trobriand Islanders\' Ways of Speaking\" by Gunter Senft (De Gruyter Mouton, 2010)',NULL,NULL,NULL,NULL,'The Trobriand Islanders\' Ways of Speaking',1,0,NULL,2,0,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(1001,'2014-09-26','2014-09-26 16:46:36','2021-09-14 10:40:00','Digital references for De Vos, C. (2014). Absolute spatial deixis and proto-toponyms in Kata Kolok. NUSA: Linguistic studies of languages in and around Indonesia, 56, 3-26.',NULL,NULL,NULL,NULL,'Absolute spatial deixis and proto-toponyms in Kata Kolok',0,0,NULL,2,0,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(1002,'2014-10-20','2014-10-20 10:57:25','2021-09-14 10:40:00','A collection of works by and secondary sources about Henrik Ibsen. Inspired by the Hathi Trust collection (http://babel.hathitrust.org/cgi/mb?a=listis;c=1024421342);











-- Dump completed on 2022-01-14  9:57:43
