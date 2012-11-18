
-- DELETE FROM dms.APPLICATION;
-- DELETE FROM dms.PRODUCT_VERSION;
-- DELETE FROM dms.PRODUCT;
-- DELETE FROM dms.ALCATEL_LANGUAGE_CODE;
-- DELETE FROM dms.LANGUAGE;
-- DELETE FROM dms.CHARSET;

-- for PostgreSQL and Oracle

-- INSERT INTO dms.PRODUCT_BASE (ID,NAME) VALUES (nextval('ID_PRODUCT_BASE'),'ProductA');
-- INSERT INTO dms.PRODUCT (ID,VERSION,PRODUCT_BASE_ID) VALUES (nextval('ID_PRODUCT'),'1.0' ,1 );
-- INSERT INTO dms.PRODUCT (ID,VERSION,PRODUCT_BASE_ID) VALUES (nextval('ID_PRODUCT'),'2.0' ,1 );
-- INSERT INTO dms.PRODUCT (ID,VERSION,PRODUCT_BASE_ID) VALUES (nextval('ID_PRODUCT'),'3.0' ,1 );
--
-- INSERT INTO dms.APPLICATION_BASE (ID,NAME,PRODUCT_BASE_ID) VALUES(nextval('ID_APPLICATION_BASE'),'App_A1',1);
-- INSERT INTO dms.APPLICATION_BASE (ID,NAME,PRODUCT_BASE_ID) VALUES(nextval('ID_APPLICATION_BASE'),'App_A2',1);
--
-- INSERT INTO dms.APPLICATION (ID,VERSION,APPLICATION_BASE_ID) VALUES(nextval('ID_APPLICATION'),'1.0',1);
-- INSERT INTO dms.PRODUCT_APPLICATION (PRODUCT_ID, APPLICATION_ID) VALUES(1,1);
--
-- INSERT INTO dms.DICTIONARY_BASE(ID,NAME, FORMAT,ENCODING,PATH,APPLICATION_BASE_ID ) VALUES (nextval('ID_DICTIONARY_BASE'),'testdict','DCT', 'UTF-8','/root/testpath',1)
-- INSERT INTO dms.DICTIONARY_BASE(ID,NAME, FORMAT,ENCODING,PATH,APPLICATION_BASE_ID ) VALUES (nextval('ID_DICTIONARY_BASE'),'testdict1','DCT', 'ISO8859-1','/root/testpath1',1)
--
-- INSERT INTO dms.DICTIONARY (ID,VERSION, DICTIONARY_BASE_ID, LOCKED )VALUES (nextval('ID_DICTIONARY'),'v1.0',1, FALSE)
-- INSERT INTO dms.DICTIONARY (ID,VERSION, DICTIONARY_BASE_ID, LOCKED )VALUES (nextval('ID_DICTIONARY'),'v1.1',1, FALSE)
--
-- INSERT INTO dms.APPLICATION_DICTIONARY (APPLICATION_ID, DICTIONARY_ID) VALUES (1,1)
-- INSERT INTO dms.APPLICATION_DICTIONARY (APPLICATION_ID, DICTIONARY_ID) VALUES (1,2)
--
-- INSERT INTO dms.APPLICATION (ID,VERSION,APPLICATION_BASE_ID) VALUES(nextval('ID_APPLICATION'),'1.0',2);
-- INSERT INTO dms.PRODUCT_APPLICATION (PRODUCT_ID, APPLICATION_ID) VALUES(1,2);
--
-- INSERT INTO dms.APPLICATION (ID,VERSION,APPLICATION_BASE_ID) VALUES(nextval('ID_APPLICATION'),'1.1',1);
-- INSERT INTO dms.PRODUCT_APPLICATION (PRODUCT_ID, APPLICATION_ID) VALUES(2,3);
--
-- INSERT INTO dms.PRODUCT_BASE (ID,NAME) VALUES (nextval('ID_PRODUCT_BASE'),'ProductB');
-- INSERT INTO dms.PRODUCT (ID,VERSION,PRODUCT_BASE_ID) VALUES (nextval('ID_PRODUCT'),'1.0' ,2 );
-- INSERT INTO dms.APPLICATION_BASE (ID,NAME,PRODUCT_BASE_ID) VALUES(nextval('ID_APPLICATION_BASE'),'App_B1',2);

-- for MySQL and H2

-- INSERT INTO dms.PRODUCT_BASE (NAME) VALUES ('ProductA');
-- INSERT INTO dms.PRODUCT (VERSION,PRODUCT_BASE_ID) VALUES ('1.0' ,1 );
-- INSERT INTO dms.PRODUCT (VERSION,PRODUCT_BASE_ID) VALUES ('2.0' ,1 );
-- INSERT INTO dms.PRODUCT (VERSION,PRODUCT_BASE_ID) VALUES ('3.0' ,1 );
--
-- INSERT INTO dms.APPLICATION_BASE (NAME,PRODUCT_BASE_ID) VALUES('App_A1',1);
-- INSERT INTO dms.APPLICATION_BASE (NAME,PRODUCT_BASE_ID) VALUES('App_A2',1);
--
-- INSERT INTO dms.APPLICATION (VERSION,APPLICATION_BASE_ID) VALUES('1.0',1);
-- INSERT INTO dms.PRODUCT_APPLICATION (PRODUCT_ID, APPLICATION_ID) VALUES(1,1);
--
-- INSERT INTO dms.DICTIONARY_BASE(NAME, FORMAT,ENCODING,PATH,APPLICATION_BASE_ID ) VALUES ('testdict','DCT', 'UTF-8','/root/testpath',1)
-- INSERT INTO dms.DICTIONARY_BASE(NAME, FORMAT,ENCODING,PATH,APPLICATION_BASE_ID ) VALUES ('testdict1','DCT', 'ISO8859-1','/root/testpath1',1)
--
-- INSERT INTO dms.DICTIONARY (VERSION, DICTIONARY_BASE_ID, LOCKED )VALUES ('v1.0',1, FALSE)
-- INSERT INTO dms.DICTIONARY (VERSION, DICTIONARY_BASE_ID, LOCKED )VALUES ('v1.1',1, FALSE)
--
-- INSERT INTO dms.APPLICATION_DICTIONARY (APPLICATION_ID, DICTIONARY_ID) VALUES (1,1)
-- INSERT INTO dms.APPLICATION_DICTIONARY (APPLICATION_ID, DICTIONARY_ID) VALUES (1,2)
--
-- INSERT INTO dms.APPLICATION (VERSION,APPLICATION_BASE_ID) VALUES('1.0',2);
-- INSERT INTO dms.PRODUCT_APPLICATION (PRODUCT_ID, APPLICATION_ID) VALUES(1,2);
--
-- INSERT INTO dms.APPLICATION (VERSION,APPLICATION_BASE_ID) VALUES('1.1',1);
-- INSERT INTO dms.PRODUCT_APPLICATION (PRODUCT_ID, APPLICATION_ID) VALUES(2,3);
--
-- INSERT INTO dms.PRODUCT_BASE (NAME) VALUES ('ProductB');
-- INSERT INTO dms.PRODUCT (VERSION,PRODUCT_BASE_ID) VALUES ('1.0' ,2 );
-- INSERT INTO dms.APPLICATION_BASE (NAME,PRODUCT_BASE_ID) VALUES('App_B1',2);


INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (1,'English','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (2,'Arabic','windows-1256');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (3,'Catalan','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (4,'Catalan (Spain)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (5,'Chinese','GBK');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (6,'Chinese (China)','GBK');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (7,'Chinese (Hong Kong)','Big5-HKSCS');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (8,'Chinese (Taiwan)','Big5');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (9,'Croatia','windows-1250');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (10,'Czech','windows-1250');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (11,'Danish','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (12,'Dutch (Belgium)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (13,'Dutch (Netherlands)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (14,'English (Australia)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (15,'English (United Kingdom)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (16,'English (United States)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (17,'Estonia','ISO-8859-15');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (18,'Finnish','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (19,'French (Belgium)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (20,'French (Canada)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (21,'French (France)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (22,'French (Switzerland)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (23,'German (Austria)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (24,'German (Germany)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (25,'German (Switzerland)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (26,'Greek','ISO-8859-7');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (27,'Hebrew','ISO-8859-8');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (28,'Hungarian','windows-1250');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (29,'Italian (Italy)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (30,'Italian (Switzerland)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (31,'Japanese','EUC-JP');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (32,'Korean','x-windows-949');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (33,'Latvian','windows-1257');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (34,'Lithuania','windows-1257');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (35,'Norvegian','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (36,'Polish','windows-1250');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (37,'Portuguese (Brazil)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (38,'Portuguese (Portugal)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (39,'Rumania','windows-1250');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (40,'Russian','windows-1251');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (41,'Serbian','windows-1250');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (42,'Slovakian','windows-1250');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (43,'Slovenian','windows-1250');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (44,'Spanish (Mexico)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (45,'Spanish (Spain)','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (46,'Swedish','windows-1252');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (47,'Thai','x-windows-874');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (48,'Turkish','windows-1254');
-- extra for dictionary.conf file
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (49,'en-CN','UTF-8');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (50,'en-GR','UTF-8');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (51,'en-CA','UTF-8');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (52,'en-TW','UTF-8');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (53,'en-MA','UTF-8');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (54,'en-RU','UTF-8');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (55,'es-AR','UTF-8');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (56,'fr-MA','UTF-8');
INSERT INTO dms.LANGUAGE(ID,NAME,DEFAULT_CHARSET) VALUES (57,'sr-CS','UTF-8');




INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('GAE',1, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('AR0',2, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('ES1',4, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('CH0',6, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('ZH0',6, FALSE );
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('HK0',7, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('CH1',8, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('TW0',8, FALSE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('HR0',9, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('CS0',10, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('DA0',11, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('NL1',12, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('NL0',13, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('AS0',14, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('EN0',15, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('US0',16, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('EE0',17, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('FI0',18, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('FR2',19, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('FR3',20, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('FR0',21, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('FR1',22, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('DE1',23, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('DE0',24, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('DE2',25, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('GR0',26, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('HE0',27, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('HU0',28, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('IT0',29, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('IT1',30, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('JP0',31, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('KO0',32, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('LV0',33, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('LT0',34, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('NO0',35, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('PL0',36, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('PT1',37, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('PT0',38, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('RO0',39, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('RU0',40, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('YU0',41, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('SK0',42, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('SI0',43, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('ES0',45, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('SV0',46, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('TA0',47, TRUE);
INSERT INTO dms.ALCATEL_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES ('TR0',48, TRUE);


INSERT INTO dms.CHARSET(ID,NAME) values (1,'Big5');
INSERT INTO dms.CHARSET(ID,NAME) values (2,'Big5-HKSCS');
INSERT INTO dms.CHARSET(ID,NAME) values (3,'EUC-JP');
INSERT INTO dms.CHARSET(ID,NAME) values (4,'EUC-KR');
INSERT INTO dms.CHARSET(ID,NAME) values (5,'GB18030');
INSERT INTO dms.CHARSET(ID,NAME) values (6,'GB2312');
INSERT INTO dms.CHARSET(ID,NAME) values (7,'GBK');
INSERT INTO dms.CHARSET(ID,NAME) values (8,'IBM-Thai');
INSERT INTO dms.CHARSET(ID,NAME) values (9,'IBM00858');
INSERT INTO dms.CHARSET(ID,NAME) values (10,'IBM01140');
INSERT INTO dms.CHARSET(ID,NAME) values (11,'IBM01141');
INSERT INTO dms.CHARSET(ID,NAME) values (12,'IBM01142');
INSERT INTO dms.CHARSET(ID,NAME) values (13,'IBM01143');
INSERT INTO dms.CHARSET(ID,NAME) values (14,'IBM01144');
INSERT INTO dms.CHARSET(ID,NAME) values (15,'IBM01145');
INSERT INTO dms.CHARSET(ID,NAME) values (16,'IBM01146');
INSERT INTO dms.CHARSET(ID,NAME) values (17,'IBM01147');
INSERT INTO dms.CHARSET(ID,NAME) values (18,'IBM01148');
INSERT INTO dms.CHARSET(ID,NAME) values (19,'IBM01149');
INSERT INTO dms.CHARSET(ID,NAME) values (20,'IBM037');
INSERT INTO dms.CHARSET(ID,NAME) values (21,'IBM1026');
INSERT INTO dms.CHARSET(ID,NAME) values (22,'IBM1047');
INSERT INTO dms.CHARSET(ID,NAME) values (23,'IBM273');
INSERT INTO dms.CHARSET(ID,NAME) values (24,'IBM277');
INSERT INTO dms.CHARSET(ID,NAME) values (25,'IBM278');
INSERT INTO dms.CHARSET(ID,NAME) values (26,'IBM280');
INSERT INTO dms.CHARSET(ID,NAME) values (27,'IBM284');
INSERT INTO dms.CHARSET(ID,NAME) values (28,'IBM285');
INSERT INTO dms.CHARSET(ID,NAME) values (29,'IBM297');
INSERT INTO dms.CHARSET(ID,NAME) values (30,'IBM420');
INSERT INTO dms.CHARSET(ID,NAME) values (31,'IBM424');
INSERT INTO dms.CHARSET(ID,NAME) values (32,'IBM437');
INSERT INTO dms.CHARSET(ID,NAME) values (33,'IBM500');
INSERT INTO dms.CHARSET(ID,NAME) values (34,'IBM775');
INSERT INTO dms.CHARSET(ID,NAME) values (35,'IBM850');
INSERT INTO dms.CHARSET(ID,NAME) values (36,'IBM852');
INSERT INTO dms.CHARSET(ID,NAME) values (37,'IBM855');
INSERT INTO dms.CHARSET(ID,NAME) values (38,'IBM857');
INSERT INTO dms.CHARSET(ID,NAME) values (39,'IBM860');
INSERT INTO dms.CHARSET(ID,NAME) values (40,'IBM861');
INSERT INTO dms.CHARSET(ID,NAME) values (41,'IBM862');
INSERT INTO dms.CHARSET(ID,NAME) values (42,'IBM863');
INSERT INTO dms.CHARSET(ID,NAME) values (43,'IBM864');
INSERT INTO dms.CHARSET(ID,NAME) values (44,'IBM865');
INSERT INTO dms.CHARSET(ID,NAME) values (45,'IBM866');
INSERT INTO dms.CHARSET(ID,NAME) values (46,'IBM868');
INSERT INTO dms.CHARSET(ID,NAME) values (47,'IBM869');
INSERT INTO dms.CHARSET(ID,NAME) values (48,'IBM870');
INSERT INTO dms.CHARSET(ID,NAME) values (49,'IBM871');
INSERT INTO dms.CHARSET(ID,NAME) values (50,'IBM918');
INSERT INTO dms.CHARSET(ID,NAME) values (51,'ISO-2022-CN');
INSERT INTO dms.CHARSET(ID,NAME) values (52,'ISO-2022-JP');
INSERT INTO dms.CHARSET(ID,NAME) values (53,'ISO-2022-JP-2');
INSERT INTO dms.CHARSET(ID,NAME) values (54,'ISO-2022-KR');
INSERT INTO dms.CHARSET(ID,NAME) values (55,'ISO-8859-1');
INSERT INTO dms.CHARSET(ID,NAME) values (56,'ISO-8859-13');
INSERT INTO dms.CHARSET(ID,NAME) values (57,'ISO-8859-15');
INSERT INTO dms.CHARSET(ID,NAME) values (58,'ISO-8859-2');
INSERT INTO dms.CHARSET(ID,NAME) values (59,'ISO-8859-3');
INSERT INTO dms.CHARSET(ID,NAME) values (60,'ISO-8859-4');
INSERT INTO dms.CHARSET(ID,NAME) values (61,'ISO-8859-5');
INSERT INTO dms.CHARSET(ID,NAME) values (62,'ISO-8859-6');
INSERT INTO dms.CHARSET(ID,NAME) values (63,'ISO-8859-7');
INSERT INTO dms.CHARSET(ID,NAME) values (64,'ISO-8859-8');
INSERT INTO dms.CHARSET(ID,NAME) values (65,'ISO-8859-9');
INSERT INTO dms.CHARSET(ID,NAME) values (66,'JIS_X0201');
INSERT INTO dms.CHARSET(ID,NAME) values (67,'JIS_X0212-1990');
INSERT INTO dms.CHARSET(ID,NAME) values (68,'KOI8-R');
INSERT INTO dms.CHARSET(ID,NAME) values (69,'KOI8-U');
INSERT INTO dms.CHARSET(ID,NAME) values (70,'Shift_JIS');
INSERT INTO dms.CHARSET(ID,NAME) values (71,'TIS-620');
INSERT INTO dms.CHARSET(ID,NAME) values (72,'US-ASCII');
INSERT INTO dms.CHARSET(ID,NAME) values (73,'UTF-16');
INSERT INTO dms.CHARSET(ID,NAME) values (74,'UTF-16BE');
INSERT INTO dms.CHARSET(ID,NAME) values (75,'UTF-16LE');
INSERT INTO dms.CHARSET(ID,NAME) values (76,'UTF-32');
INSERT INTO dms.CHARSET(ID,NAME) values (77,'UTF-32BE');
INSERT INTO dms.CHARSET(ID,NAME) values (78,'UTF-32LE');
INSERT INTO dms.CHARSET(ID,NAME) values (79,'UTF-8');
INSERT INTO dms.CHARSET(ID,NAME) values (80,'windows-1250');
INSERT INTO dms.CHARSET(ID,NAME) values (81,'windows-1251');
INSERT INTO dms.CHARSET(ID,NAME) values (82,'windows-1252');
INSERT INTO dms.CHARSET(ID,NAME) values (83,'windows-1253');
INSERT INTO dms.CHARSET(ID,NAME) values (84,'windows-1254');
INSERT INTO dms.CHARSET(ID,NAME) values (85,'windows-1255');
INSERT INTO dms.CHARSET(ID,NAME) values (86,'windows-1256');
INSERT INTO dms.CHARSET(ID,NAME) values (87,'windows-1257');
INSERT INTO dms.CHARSET(ID,NAME) values (88,'windows-1258');
INSERT INTO dms.CHARSET(ID,NAME) values (89,'windows-31j');
INSERT INTO dms.CHARSET(ID,NAME) values (90,'x-Big5-HKSCS-2001');
INSERT INTO dms.CHARSET(ID,NAME) values (91,'x-Big5-Solaris');
INSERT INTO dms.CHARSET(ID,NAME) values (92,'x-euc-jp-linux');
INSERT INTO dms.CHARSET(ID,NAME) values (93,'x-EUC-TW');
INSERT INTO dms.CHARSET(ID,NAME) values (94,'x-eucJP-Open');
INSERT INTO dms.CHARSET(ID,NAME) values (95,'x-IBM1006');
INSERT INTO dms.CHARSET(ID,NAME) values (96,'x-IBM1025');
INSERT INTO dms.CHARSET(ID,NAME) values (97,'x-IBM1046');
INSERT INTO dms.CHARSET(ID,NAME) values (98,'x-IBM1097');
INSERT INTO dms.CHARSET(ID,NAME) values (99,'x-IBM1098');
INSERT INTO dms.CHARSET(ID,NAME) values (100,'x-IBM1112');
INSERT INTO dms.CHARSET(ID,NAME) values (101,'x-IBM1122');
INSERT INTO dms.CHARSET(ID,NAME) values (102,'x-IBM1123');
INSERT INTO dms.CHARSET(ID,NAME) values (103,'x-IBM1124');
INSERT INTO dms.CHARSET(ID,NAME) values (104,'x-IBM1381');
INSERT INTO dms.CHARSET(ID,NAME) values (105,'x-IBM1383');
INSERT INTO dms.CHARSET(ID,NAME) values (106,'x-IBM33722');
INSERT INTO dms.CHARSET(ID,NAME) values (107,'x-IBM737');
INSERT INTO dms.CHARSET(ID,NAME) values (108,'x-IBM833');
INSERT INTO dms.CHARSET(ID,NAME) values (109,'x-IBM834');
INSERT INTO dms.CHARSET(ID,NAME) values (110,'x-IBM856');
INSERT INTO dms.CHARSET(ID,NAME) values (111,'x-IBM874');
INSERT INTO dms.CHARSET(ID,NAME) values (112,'x-IBM875');
INSERT INTO dms.CHARSET(ID,NAME) values (113,'x-IBM921');
INSERT INTO dms.CHARSET(ID,NAME) values (114,'x-IBM922');
INSERT INTO dms.CHARSET(ID,NAME) values (115,'x-IBM930');
INSERT INTO dms.CHARSET(ID,NAME) values (116,'x-IBM933');
INSERT INTO dms.CHARSET(ID,NAME) values (117,'x-IBM935');
INSERT INTO dms.CHARSET(ID,NAME) values (118,'x-IBM937');
INSERT INTO dms.CHARSET(ID,NAME) values (119,'x-IBM939');
INSERT INTO dms.CHARSET(ID,NAME) values (120,'x-IBM942');
INSERT INTO dms.CHARSET(ID,NAME) values (121,'x-IBM942C');
INSERT INTO dms.CHARSET(ID,NAME) values (122,'x-IBM943');
INSERT INTO dms.CHARSET(ID,NAME) values (123,'x-IBM943C');
INSERT INTO dms.CHARSET(ID,NAME) values (124,'x-IBM948');
INSERT INTO dms.CHARSET(ID,NAME) values (125,'x-IBM949');
INSERT INTO dms.CHARSET(ID,NAME) values (126,'x-IBM949C');
INSERT INTO dms.CHARSET(ID,NAME) values (127,'x-IBM950');
INSERT INTO dms.CHARSET(ID,NAME) values (128,'x-IBM964');
INSERT INTO dms.CHARSET(ID,NAME) values (129,'x-IBM970');
INSERT INTO dms.CHARSET(ID,NAME) values (130,'x-ISCII91');
INSERT INTO dms.CHARSET(ID,NAME) values (131,'x-ISO-2022-CN-CNS');
INSERT INTO dms.CHARSET(ID,NAME) values (132,'x-ISO-2022-CN-GB');
INSERT INTO dms.CHARSET(ID,NAME) values (133,'x-iso-8859-11');
INSERT INTO dms.CHARSET(ID,NAME) values (134,'x-JIS0208');
INSERT INTO dms.CHARSET(ID,NAME) values (135,'x-JISAutoDetect');
INSERT INTO dms.CHARSET(ID,NAME) values (136,'x-Johab');
INSERT INTO dms.CHARSET(ID,NAME) values (137,'x-MacArabic');
INSERT INTO dms.CHARSET(ID,NAME) values (138,'x-MacCentralEurope');
INSERT INTO dms.CHARSET(ID,NAME) values (139,'x-MacCroatian');
INSERT INTO dms.CHARSET(ID,NAME) values (140,'x-MacCyrillic');
INSERT INTO dms.CHARSET(ID,NAME) values (141,'x-MacDingbat');
INSERT INTO dms.CHARSET(ID,NAME) values (142,'x-MacGreek');
INSERT INTO dms.CHARSET(ID,NAME) values (143,'x-MacHebrew');
INSERT INTO dms.CHARSET(ID,NAME) values (144,'x-MacIceland');
INSERT INTO dms.CHARSET(ID,NAME) values (145,'x-MacRoman');
INSERT INTO dms.CHARSET(ID,NAME) values (146,'x-MacRomania');
INSERT INTO dms.CHARSET(ID,NAME) values (147,'x-MacSymbol');
INSERT INTO dms.CHARSET(ID,NAME) values (148,'x-MacThai');
INSERT INTO dms.CHARSET(ID,NAME) values (149,'x-MacTurkish');
INSERT INTO dms.CHARSET(ID,NAME) values (150,'x-MacUkraine');
INSERT INTO dms.CHARSET(ID,NAME) values (151,'x-MS932_0213');
INSERT INTO dms.CHARSET(ID,NAME) values (152,'x-MS950-HKSCS');
INSERT INTO dms.CHARSET(ID,NAME) values (153,'x-MS950-HKSCS-XP');
INSERT INTO dms.CHARSET(ID,NAME) values (154,'x-mswin-936');
INSERT INTO dms.CHARSET(ID,NAME) values (155,'x-PCK');
INSERT INTO dms.CHARSET(ID,NAME) values (156,'x-SJIS_0213');
INSERT INTO dms.CHARSET(ID,NAME) values (157,'x-UTF-16LE-BOM');
INSERT INTO dms.CHARSET(ID,NAME) values (158,'X-UTF-32BE-BOM');
INSERT INTO dms.CHARSET(ID,NAME) values (159,'X-UTF-32LE-BOM');
INSERT INTO dms.CHARSET(ID,NAME) values (160,'x-windows-50220');
INSERT INTO dms.CHARSET(ID,NAME) values (161,'x-windows-50221');
INSERT INTO dms.CHARSET(ID,NAME) values (162,'x-windows-874');
INSERT INTO dms.CHARSET(ID,NAME) values (163,'x-windows-949');
INSERT INTO dms.CHARSET(ID,NAME) values (164,'x-windows-950');
INSERT INTO dms.CHARSET(ID,NAME) values (165,'x-windows-iso2022jp');

INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('en',1,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('ar',2,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('ca',3,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('ca-ES',4,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('zh',5,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('zh-CN',6,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('cn',6,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('zh-HK',7,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('zh-TW',8,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('sh-TW',8,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('tw',8,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('hr',9,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('hr-HR',9,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('cs',10,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('cs-CZ',10,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('cz',10,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('da',11,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('da-DK',11,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('nl-BE',12,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('nl',13,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('nl-NL',13,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('en-AU',14,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('en-GB',15,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('en-US',16,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('us',16,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('et',17,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('et-EE',17,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('fi',18,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('fi-FI',18,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('fr-BE',19,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('fr-CA',20,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('fr',21,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('fr-FR',21,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('fr-CH',22,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('de-AT',23,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('de',24,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('de-DE',24,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('de-CH',25,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('el',26,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('el-GR',26,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('iw',27,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('he',27,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('hu',28,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('hu-HU',28,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('it',29,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('it-IT',29,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('it-CH',30,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('ja',31,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('ja-JP',31,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('ko',32,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('ko-KR',32,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('ko-HR',32,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('kr',32,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('lv',33,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('lv-LV',33,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('lt',34,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('lt-LT',34,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('no',35,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('no-NO',35,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('pl',36,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('pl-PL',36,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('pt-BR',37,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('pt',38,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('pt-PT',38,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('ro',39,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('ro-RO',39,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('ru',40,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('ru-RU',40,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('sr',41,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('sr-YU',41,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('sk',42,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('sk-SK',42,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('sl',43,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('sl-SI',43,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('es-MX',44,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('es',45,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('es-ES',45,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('sv',46,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('sv-SE',46,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('th',47,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('th-TH',47,FALSE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('tr',48,TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(CODE,LANGUAGE_ID,DEFAULT_CODE) VALUES('tr-TR',48,FALSE);

-- extra code for dictionary.conf .
INSERT INTO dms.ISO_LANGUAGE_CODE(LANGUAGE_ID,CODE,DEFAULT_CODE) VALUES (49,'en-CN',TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(LANGUAGE_ID,CODE,DEFAULT_CODE) VALUES (50,'en-GR',TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(LANGUAGE_ID,CODE,DEFAULT_CODE) VALUES (51,'en-CA',TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(LANGUAGE_ID,CODE,DEFAULT_CODE) VALUES (52,'en-TW',TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(LANGUAGE_ID,CODE,DEFAULT_CODE) VALUES (53,'en-MA',TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(LANGUAGE_ID,CODE,DEFAULT_CODE) VALUES (54,'en-RU',TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(LANGUAGE_ID,CODE,DEFAULT_CODE) VALUES (55,'es-AR',TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(LANGUAGE_ID,CODE,DEFAULT_CODE) VALUES (56,'fr-MA',TRUE);
INSERT INTO dms.ISO_LANGUAGE_CODE(LANGUAGE_ID,CODE,DEFAULT_CODE) VALUES (57,'sr-CS',TRUE);
