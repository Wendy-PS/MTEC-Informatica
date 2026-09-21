CREATE DATABASE  IF NOT EXISTS `mtec_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `mtec_db`;
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: mtec_db
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `idCliente` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `cpf_cnpj` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `endereco` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ativo` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`idCliente`),
  UNIQUE KEY `uq_cpf_cnpj` (`cpf_cnpj`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` VALUES (1,'Ana Paula Santos','235.480.000-29','(27) 99001-1001','Rua das Flores, 120 - Vila Velha/ES',1),(2,'Bruno Henrique Lima','235.480.001-00','(27) 99002-1002','Av. Central, 455 - Vitória/ES',1),(3,'Carla Mendes Rocha','235.480.002-90','(27) 99003-1003','Rua Santa Luzia, 78 - Cariacica/ES',1),(4,'Daniel Oliveira Costa','235.480.003-71','(27) 99004-1004','Av. Beira Mar, 90 - Serra/ES',1),(5,'Eduarda Ferreira Alves','235.480.004-52','(27) 99005-1005','Rua Projetada, 310 - Vila Velha/ES',1),(6,'Felipe Martins Souza','235.480.005-33','(27) 99006-1006','Rua do Comércio, 44 - Vitória/ES',1),(7,'Gabriela Nunes Pereira','235.480.006-14','(27) 99007-1007','Av. Norte Sul, 700 - Serra/ES',1),(8,'Henrique Barbosa Silva','235.480.007-03','(27) 99008-1008','Rua Rio Branco, 155 - Cariacica/ES',1),(9,'Isabela Teixeira Ramos','235.480.008-86','(27) 99009-1009','Rua São João, 88 - Vitória/ES',1),(10,'João Pedro Almeida','235.480.009-67','(27) 99010-1010','Av. Carlos Lindenberg, 1220 - Vila Velha/ES',1),(11,'Karina Lopes Vieira','235.480.010-09','(27) 99011-1011','Rua das Flores, 120 - Vila Velha/ES',1),(12,'Leonardo Gomes Batista','235.480.011-81','(27) 99012-1012','Av. Central, 455 - Vitória/ES',1),(13,'Mariana Castro Freitas','235.480.012-62','(27) 99013-1013','Rua Santa Luzia, 78 - Cariacica/ES',1),(14,'Nicolas Andrade Moreira','235.480.013-43','(27) 99014-1014','Av. Beira Mar, 90 - Serra/ES',1),(15,'Otávio Ribeiro Cardoso','235.480.014-24','(27) 99015-1015','Rua Projetada, 310 - Vila Velha/ES',1),(16,'Patrícia Monteiro Dias','235.480.015-05','(27) 99016-1016','Rua do Comércio, 44 - Vitória/ES',1),(17,'Rafael Azevedo Moraes','235.480.016-96','(27) 99017-1017','Av. Norte Sul, 700 - Serra/ES',1),(18,'Sabrina Fernandes Cunha','235.480.017-77','(27) 99018-1018','Rua Rio Branco, 155 - Cariacica/ES',1),(19,'Thiago Neves Rocha','235.480.018-58','(27) 99019-1019','Rua São João, 88 - Vitória/ES',1),(20,'Vanessa Cristina Araújo','235.480.019-39','(27) 99020-1020','Av. Carlos Lindenberg, 1220 - Vila Velha/ES',1),(21,'William Matheus Martins','235.480.020-72','(27) 99021-1021','Rua das Flores, 120 - Vila Velha/ES',1),(22,'Yasmin Rodrigues Campos','235.480.021-53','(27) 99022-1022','Av. Central, 455 - Vitória/ES',1),(23,'Bianca Souza Amaral','235.480.022-34','(27) 99023-1023','Rua Santa Luzia, 78 - Cariacica/ES',1),(24,'Caio Vinícius Pires','235.480.023-15','(27) 99024-1024','Av. Beira Mar, 90 - Serra/ES',1),(25,'Débora Lima Carvalho','235.480.024-04','(27) 99025-1025','Rua Projetada, 310 - Vila Velha/ES',1),(26,'Erick Santos Farias','235.480.025-87','(27) 99026-1026','Rua do Comércio, 44 - Vitória/ES',1),(27,'Fernanda Alves Nascimento','235.480.026-68','(27) 99027-1027','Av. Norte Sul, 700 - Serra/ES',1),(28,'Gustavo Henrique Reis','235.480.027-49','(27) 99028-1028','Rua Rio Branco, 155 - Cariacica/ES',1),(29,'Helena Duarte Machado','235.480.028-20','(27) 99029-1029','Rua São João, 88 - Vitória/ES',1),(30,'Igor Gabriel Tavares','235.480.029-00','(27) 99030-1030','Av. Carlos Lindenberg, 1220 - Vila Velha/ES',1);
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `produto`
--

DROP TABLE IF EXISTS `produto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `produto` (
  `idPRODUTO` int NOT NULL AUTO_INCREMENT,
  `tipo` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `marca` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `modelo` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `especificacoes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CLIENTE_idCliente` int NOT NULL,
  PRIMARY KEY (`idPRODUTO`),
  KEY `fk_produto_cliente` (`CLIENTE_idCliente`),
  CONSTRAINT `fk_produto_cliente` FOREIGN KEY (`CLIENTE_idCliente`) REFERENCES `cliente` (`idCliente`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `produto`
--

LOCK TABLES `produto` WRITE;
/*!40000 ALTER TABLE `produto` DISABLE KEYS */;
INSERT INTO `produto` VALUES (1,'Notebook','Lenovo','IdeaPad 3','Ryzen 5, 8GB RAM, SSD 512GB',1),(2,'Desktop','HP','ProDesk','Intel i3, 4GB RAM, HD 500GB',1),(3,'Desktop','HP','ProDesk','Intel i3, 4GB RAM, HD 500GB',2),(4,'Impressora','Epson','L3150','EcoTank, Wi-Fi',3),(5,'Notebook','Acer','Aspire 5','Intel i7, 16GB RAM, SSD 512GB',3),(6,'Notebook','Acer','Aspire 5','Intel i7, 16GB RAM, SSD 512GB',4),(7,'Desktop','Positivo','Master D','Intel i5, 8GB RAM, SSD 240GB',5),(8,'Monitor','LG','24MK430H','24 polegadas, HDMI',5),(9,'Monitor','LG','24MK430H','24 polegadas, HDMI',6),(10,'Notebook','Samsung','Book','Intel i5, 8GB RAM, SSD 256GB',7),(11,'Desktop','Dell','Optiplex','Intel i5, 8GB RAM, HD 1TB',7),(12,'Desktop','Dell','Optiplex','Intel i5, 8GB RAM, HD 1TB',8),(13,'Impressora','HP','DeskJet 2776','Multifuncional Wi-Fi',9),(14,'Notebook','Dell','Inspiron 15','Intel i5, 8GB RAM, SSD 256GB',9),(15,'Notebook','Dell','Inspiron 15','Intel i5, 8GB RAM, SSD 256GB',10),(16,'Notebook','Lenovo','IdeaPad 3','Ryzen 5, 8GB RAM, SSD 512GB',11),(17,'Desktop','HP','ProDesk','Intel i3, 4GB RAM, HD 500GB',11),(18,'Desktop','HP','ProDesk','Intel i3, 4GB RAM, HD 500GB',12),(19,'Impressora','Epson','L3150','EcoTank, Wi-Fi',13),(20,'Notebook','Acer','Aspire 5','Intel i7, 16GB RAM, SSD 512GB',13),(21,'Notebook','Acer','Aspire 5','Intel i7, 16GB RAM, SSD 512GB',14),(22,'Desktop','Positivo','Master D','Intel i5, 8GB RAM, SSD 240GB',15),(23,'Monitor','LG','24MK430H','24 polegadas, HDMI',15),(24,'Monitor','LG','24MK430H','24 polegadas, HDMI',16),(25,'Notebook','Samsung','Book','Intel i5, 8GB RAM, SSD 256GB',17),(26,'Desktop','Dell','Optiplex','Intel i5, 8GB RAM, HD 1TB',17),(27,'Desktop','Dell','Optiplex','Intel i5, 8GB RAM, HD 1TB',18),(28,'Impressora','HP','DeskJet 2776','Multifuncional Wi-Fi',19),(29,'Notebook','Dell','Inspiron 15','Intel i5, 8GB RAM, SSD 256GB',19),(30,'Notebook','Dell','Inspiron 15','Intel i5, 8GB RAM, SSD 256GB',20),(31,'Notebook','Lenovo','IdeaPad 3','Ryzen 5, 8GB RAM, SSD 512GB',21),(32,'Desktop','HP','ProDesk','Intel i3, 4GB RAM, HD 500GB',21),(33,'Desktop','HP','ProDesk','Intel i3, 4GB RAM, HD 500GB',22),(34,'Impressora','Epson','L3150','EcoTank, Wi-Fi',23),(35,'Notebook','Acer','Aspire 5','Intel i7, 16GB RAM, SSD 512GB',23),(36,'Notebook','Acer','Aspire 5','Intel i7, 16GB RAM, SSD 512GB',24),(37,'Desktop','Positivo','Master D','Intel i5, 8GB RAM, SSD 240GB',25),(38,'Monitor','LG','24MK430H','24 polegadas, HDMI',25),(39,'Monitor','LG','24MK430H','24 polegadas, HDMI',26),(40,'Notebook','Samsung','Book','Intel i5, 8GB RAM, SSD 256GB',27),(41,'Desktop','Dell','Optiplex','Intel i5, 8GB RAM, HD 1TB',27),(42,'Desktop','Dell','Optiplex','Intel i5, 8GB RAM, HD 1TB',28),(43,'Impressora','HP','DeskJet 2776','Multifuncional Wi-Fi',29),(44,'Notebook','Dell','Inspiron 15','Intel i5, 8GB RAM, SSD 256GB',29),(45,'Notebook','Dell','Inspiron 15','Intel i5, 8GB RAM, SSD 256GB',30);
/*!40000 ALTER TABLE `produto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `relatorio_orcamento`
--

DROP TABLE IF EXISTS `relatorio_orcamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `relatorio_orcamento` (
  `idRELATORIO_ORCAMENTO` int NOT NULL AUTO_INCREMENT,
  `defeito_relatado` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `diagnostico_tecnico` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pecas_orcadas` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mao_obra_orcada` decimal(10,2) DEFAULT NULL,
  `valor_total_orcamento` decimal(10,2) DEFAULT NULL,
  `status_orcamento` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `abertura` date DEFAULT NULL,
  `validade` date DEFAULT NULL,
  `CLIENTE_idCliente` int DEFAULT NULL,
  `SERVICO_idSERVICO` int DEFAULT NULL,
  `PRODUTO_idPRODUTO` int DEFAULT NULL,
  `TECNICO_idTECNICO` int DEFAULT NULL,
  PRIMARY KEY (`idRELATORIO_ORCAMENTO`),
  KEY `fk_relatorio_servico` (`SERVICO_idSERVICO`),
  KEY `fk_relatorio_produto` (`PRODUTO_idPRODUTO`),
  KEY `fk_relatorio_tecnico` (`TECNICO_idTECNICO`),
  KEY `fk_orcamento_cliente` (`CLIENTE_idCliente`),
  CONSTRAINT `fk_orcamento_cliente` FOREIGN KEY (`CLIENTE_idCliente`) REFERENCES `cliente` (`idCliente`),
  CONSTRAINT `fk_relatorio_produto` FOREIGN KEY (`PRODUTO_idPRODUTO`) REFERENCES `produto` (`idPRODUTO`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_relatorio_servico` FOREIGN KEY (`SERVICO_idSERVICO`) REFERENCES `servico` (`idSERVICO`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_relatorio_tecnico` FOREIGN KEY (`TECNICO_idTECNICO`) REFERENCES `tecnico` (`idTECNICO`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `relatorio_orcamento`
--

LOCK TABLES `relatorio_orcamento` WRITE;
/*!40000 ALTER TABLE `relatorio_orcamento` DISABLE KEYS */;
INSERT INTO `relatorio_orcamento` VALUES (1,'Sistema operacional lento','','0.00',180.00,180.00,'Aprovado','2026-06-01','2026-06-06',1,1,2,6),(2,'Tela com falhas de imagem','','0.00',250.00,250.00,'Aprovado','2026-06-02','2026-06-07',2,2,3,3),(3,'Impressora não imprime','','0.00',300.00,300.00,'Servico Manual','2026-06-03','2026-06-08',3,3,5,2),(4,'Formatação e instalação de programas','','0.00',450.00,450.00,'Aprovado','2026-06-04','2026-06-09',4,4,6,3),(5,'Troca de fonte e limpeza interna','','0.00',600.00,600.00,'Aprovado','2026-06-05','2026-06-10',5,5,8,5),(6,'Backup e restauração de arquivos','','0.00',90.00,90.00,'Servico Manual','2026-06-06','2026-06-11',6,6,9,3),(7,'Remoção de vírus e otimização','','0.00',150.00,150.00,'Aprovado','2026-06-07','2026-06-12',7,7,11,2),(8,'Computador não liga','','0.00',220.00,220.00,'Aprovado','2026-06-08','2026-06-13',8,8,12,6),(9,'Sistema operacional lento','','0.00',380.00,380.00,'Servico Manual','2026-06-09','2026-06-14',9,9,14,6),(10,'Tela com falhas de imagem','','0.00',120.00,120.00,'Aprovado','2026-06-10','2026-06-15',10,10,15,4),(11,'Impressora não imprime','','0.00',180.00,180.00,'Aprovado','2026-06-11','2026-06-16',11,11,17,3),(12,'Formatação e instalação de programas','','0.00',250.00,250.00,'Servico Manual','2026-06-12','2026-06-17',12,12,18,6),(13,'Troca de fonte e limpeza interna','','0.00',300.00,300.00,'Aprovado','2026-06-13','2026-06-18',13,13,20,5),(14,'Backup e restauração de arquivos','','0.00',450.00,450.00,'Aprovado','2026-06-14','2026-06-19',14,14,21,2),(15,'Remoção de vírus e otimização','','0.00',600.00,600.00,'Servico Manual','2026-06-15','2026-06-20',15,15,23,5),(16,'Computador não liga','','0.00',90.00,90.00,'Aprovado','2026-06-16','2026-06-21',16,16,24,6),(17,'Sistema operacional lento','','0.00',150.00,150.00,'Aprovado','2026-06-17','2026-06-22',17,17,26,3),(18,'Tela com falhas de imagem','','0.00',220.00,220.00,'Servico Manual','2026-06-18','2026-06-23',18,18,27,4),(19,'Impressora não imprime','','0.00',380.00,380.00,'Aprovado','2026-06-19','2026-06-24',19,19,29,2),(20,'Formatação e instalação de programas','','0.00',120.00,120.00,'Aprovado','2026-06-20','2026-06-25',20,20,30,3),(21,'Troca de fonte e limpeza interna','','0.00',180.00,180.00,'Servico Manual','2026-06-21','2026-06-26',21,21,32,5),(22,'Backup e restauração de arquivos','','0.00',250.00,250.00,'Aprovado','2026-06-22','2026-06-27',22,22,33,3),(23,'Remoção de vírus e otimização','','0.00',300.00,300.00,'Aprovado','2026-06-23','2026-06-28',23,23,35,4),(24,'Computador não liga','','0.00',450.00,450.00,'Servico Manual','2026-06-24','2026-06-29',24,24,36,6),(25,'Cliente relatou lentidão no equipamento','','0.00',135.00,135.00,'Aguardando','2026-06-11','2026-06-18',14,NULL,21,2),(26,'Equipamento apresenta superaquecimento','','0.00',170.00,170.00,'Aguardando','2026-06-12','2026-06-19',15,NULL,22,5),(27,'Solicitação de upgrade de memória','','0.00',205.00,205.00,'Aguardando','2026-06-13','2026-06-20',16,NULL,24,3),(28,'Orçamento para manutenção preventiva','','0.00',240.00,240.00,'Aguardando','2026-06-14','2026-06-21',17,NULL,25,5),(29,'Análise para troca de HD por SSD','','0.00',275.00,275.00,'Aguardando','2026-06-15','2026-06-22',18,NULL,27,4),(30,'Cliente solicitou orçamento para troca de tela','','0.00',310.00,310.00,'Aguardando','2026-06-16','2026-06-23',19,NULL,28,3),(31,'Cliente relatou lentidão no equipamento','','0.00',345.00,345.00,'Aguardando','2026-06-17','2026-06-24',20,NULL,30,4),(32,'Equipamento apresenta superaquecimento','','0.00',380.00,380.00,'Aguardando','2026-06-18','2026-06-25',21,NULL,31,5),(33,'Solicitação de upgrade de memória','','0.00',415.00,415.00,'Aguardando','2026-06-19','2026-06-26',22,NULL,33,2),(34,'Orçamento para manutenção preventiva','','0.00',450.00,450.00,'Aguardando','2026-06-20','2026-06-27',23,NULL,34,6),(35,'Análise para troca de HD por SSD','','0.00',485.00,485.00,'Aguardando','2026-06-21','2026-06-28',24,NULL,36,4),(36,'Cliente solicitou orçamento para troca de tela','','0.00',520.00,520.00,'Aguardando','2026-06-22','2026-06-29',25,NULL,37,3);
/*!40000 ALTER TABLE `relatorio_orcamento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `servico`
--

DROP TABLE IF EXISTS `servico`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `servico` (
  `idSERVICO` int NOT NULL AUTO_INCREMENT,
  `data_abertura` date NOT NULL,
  `prazo_entrega` date DEFAULT NULL,
  `data_conclusao` date DEFAULT NULL,
  `status_os` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Aguardando Aprovacao',
  `CLIENTE_idCliente` int NOT NULL,
  `TECNICO_idTECNICO` int DEFAULT NULL,
  `PRODUTO_idPRODUTO` int DEFAULT NULL,
  PRIMARY KEY (`idSERVICO`),
  KEY `fk_servico_cliente` (`CLIENTE_idCliente`),
  KEY `fk_servico_tecnico` (`TECNICO_idTECNICO`),
  KEY `fk_servico_produto` (`PRODUTO_idPRODUTO`),
  CONSTRAINT `fk_servico_cliente` FOREIGN KEY (`CLIENTE_idCliente`) REFERENCES `cliente` (`idCliente`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_servico_produto` FOREIGN KEY (`PRODUTO_idPRODUTO`) REFERENCES `produto` (`idPRODUTO`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_servico_tecnico` FOREIGN KEY (`TECNICO_idTECNICO`) REFERENCES `tecnico` (`idTECNICO`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `servico`
--

LOCK TABLES `servico` WRITE;
/*!40000 ALTER TABLE `servico` DISABLE KEYS */;
INSERT INTO `servico` VALUES (1,'2026-06-01','2026-06-06',NULL,'Em Andamento',1,6,2),(2,'2026-06-02','2026-06-07',NULL,'Em Andamento',2,3,3),(3,'2026-06-03','2026-06-08',NULL,'Em Andamento',3,2,5),(4,'2026-06-04','2026-06-09','2026-06-07','Em Andamento',4,3,6),(5,'2026-06-05','2026-06-10','2026-06-08','Em Andamento',5,5,8),(6,'2026-06-06','2026-06-11','2026-06-09','Concluído',6,3,9),(7,'2026-06-07','2026-06-12',NULL,'Em Andamento',7,2,11),(8,'2026-06-08','2026-06-13',NULL,'Em Andamento',8,6,12),(9,'2026-06-09','2026-06-14',NULL,'Em Andamento',9,6,14),(10,'2026-06-10','2026-06-15','2026-06-13','Em Andamento',10,4,15),(11,'2026-06-11','2026-06-16','2026-06-14','Em Andamento',11,3,17),(12,'2026-06-12','2026-06-17','2026-06-15','Concluído',12,6,18),(13,'2026-06-13','2026-06-18',NULL,'Em Andamento',13,5,20),(14,'2026-06-14','2026-06-19',NULL,'Em Andamento',14,2,21),(15,'2026-06-15','2026-06-20',NULL,'Em Andamento',15,5,23),(16,'2026-06-16','2026-06-21','2026-06-19','Em Andamento',16,6,24),(17,'2026-06-17','2026-06-22','2026-06-20','Em Andamento',17,3,26),(18,'2026-06-18','2026-06-23','2026-06-21','Concluído',18,4,27),(19,'2026-06-19','2026-06-24',NULL,'Em Andamento',19,2,29),(20,'2026-06-20','2026-06-25',NULL,'Em Andamento',20,3,30),(21,'2026-06-21','2026-06-26',NULL,'Em Andamento',21,5,32),(22,'2026-06-22','2026-06-27','2026-06-25','Em Andamento',22,3,33),(23,'2026-06-23','2026-06-28','2026-06-26','Concluído',23,4,35),(24,'2026-06-24','2026-06-29','2026-06-27','Concluído',24,6,36);
/*!40000 ALTER TABLE `servico` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tecnico`
--

DROP TABLE IF EXISTS `tecnico`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tecnico` (
  `idTECNICO` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `cpf` varchar(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `telefone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `especialidade` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Ativo',
  `observacoes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `login` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `senha` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idTECNICO`),
  UNIQUE KEY `cpf` (`cpf`),
  UNIQUE KEY `login` (`login`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tecnico`
--

LOCK TABLES `tecnico` WRITE;
/*!40000 ALTER TABLE `tecnico` DISABLE KEYS */;
INSERT INTO `tecnico` VALUES (1,'Administrador','529.982.247-25','(27) 99999-9999','admin@mtec.com','Sistemas','Ativo','','admin','123'),(2,'Lucas','198.872.837-11','(27) 99589-3887','lucas@gmail.com','Hardware','Ativo','','Lucas','lucas123'),(3,'Vinicius','357.343.487-83','(27) 91843-8923','vinicius@gmail.com','Software','Ativo','','Vinicius','vinicius123'),(4,'Wendy','600.314.377-02','(27) 99873-2459','wendy@gmail.com','Sistemas','Ativo','','Wendy','wendy123'),(5,'Marilha','371.049.017-01','(27) 99203-8409','marilha@gmail.com','Suporte Técnico','Ativo','','Marilha','marilha123'),(6,'Luana','822.248.027-81','(27) 98237-4582','luana@gmail.com','Redes','Ativo','','Luana','luana123');
/*!40000 ALTER TABLE `tecnico` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-24 23:14:22
