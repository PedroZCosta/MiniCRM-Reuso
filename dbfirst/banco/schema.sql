
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `id_cliente` int NOT NULL AUTO_INCREMENT,
  `atualizado_em` datetime(6) NOT NULL,
  `criado_em` datetime(6) NOT NULL,
  `email` varchar(160) NOT NULL,
  `empresa` varchar(120) DEFAULT NULL,
  `excluido` bit(1) NOT NULL,
  `nome` varchar(120) NOT NULL,
  `status` enum('ATIVO','INATIVO','PROSPECT') NOT NULL,
  `telefone` varchar(20) DEFAULT NULL,
  `id_vendedor` int NOT NULL,
  PRIMARY KEY (`id_cliente`),
  KEY `FKsn714u8ael2eiyov98ub9vvp1` (`id_vendedor`),
  CONSTRAINT `FKsn714u8ael2eiyov98ub9vvp1` FOREIGN KEY (`id_vendedor`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `historico_etapa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historico_etapa` (
  `id_historico` int NOT NULL AUTO_INCREMENT,
  `alterado_em` datetime(6) NOT NULL,
  `etapa_anterior` enum('CONTATO','GANHA','PERDIDA','PROPOSTA','PROSPECCAO') DEFAULT NULL,
  `etapa_nova` enum('CONTATO','GANHA','PERDIDA','PROPOSTA','PROSPECCAO') NOT NULL,
  `id_oportunidade` int NOT NULL,
  `id_usuario` int NOT NULL,
  PRIMARY KEY (`id_historico`),
  KEY `FKdumvisdk7v0x7u38ggtc8mn12` (`id_oportunidade`),
  KEY `FKrbxy52xma3ket1t3d532v3x95` (`id_usuario`),
  CONSTRAINT `FKdumvisdk7v0x7u38ggtc8mn12` FOREIGN KEY (`id_oportunidade`) REFERENCES `oportunidade` (`id_oportunidade`),
  CONSTRAINT `FKrbxy52xma3ket1t3d532v3x95` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `interacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interacao` (
  `id_interacao` int NOT NULL AUTO_INCREMENT,
  `criado_em` datetime(6) NOT NULL,
  `data_interacao` datetime(6) NOT NULL,
  `observacoes` text,
  `tipo` enum('EMAIL','LIGACAO','REUNIAO') NOT NULL,
  `id_cliente` int NOT NULL,
  `id_usuario` int NOT NULL,
  PRIMARY KEY (`id_interacao`),
  KEY `FK6lym9f6oyb2e3ipeme43rtbid` (`id_cliente`),
  KEY `FKj6n4fspujfbiwnsagdpgr57q2` (`id_usuario`),
  CONSTRAINT `FK6lym9f6oyb2e3ipeme43rtbid` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  CONSTRAINT `FKj6n4fspujfbiwnsagdpgr57q2` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `motivo_perda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `motivo_perda` (
  `id_motivo` tinyint NOT NULL AUTO_INCREMENT,
  `descricao` varchar(60) NOT NULL,
  PRIMARY KEY (`id_motivo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `notificacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificacao` (
  `id_notificacao` int NOT NULL AUTO_INCREMENT,
  `gerada_em` datetime(6) NOT NULL,
  `lida` bit(1) NOT NULL,
  `mensagem` varchar(255) NOT NULL,
  `tipo` enum('D1','D15','HOJE','REUNIAO','VENCIDA') NOT NULL,
  `id_tarefa` int DEFAULT NULL,
  `id_usuario` int NOT NULL,
  PRIMARY KEY (`id_notificacao`),
  KEY `FKdah20j3hws6fyss28na762eju` (`id_tarefa`),
  KEY `FKlklb6xjbjlatiuvdywow2ig5s` (`id_usuario`),
  CONSTRAINT `FKdah20j3hws6fyss28na762eju` FOREIGN KEY (`id_tarefa`) REFERENCES `tarefa` (`id_tarefa`),
  CONSTRAINT `FKlklb6xjbjlatiuvdywow2ig5s` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `oportunidade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oportunidade` (
  `id_oportunidade` int NOT NULL AUTO_INCREMENT,
  `atualizado_em` datetime(6) NOT NULL,
  `criado_em` datetime(6) NOT NULL,
  `data_prevista` date DEFAULT NULL,
  `etapa` enum('CONTATO','GANHA','PERDIDA','PROPOSTA','PROSPECCAO') NOT NULL,
  `fechada_em` datetime(6) DEFAULT NULL,
  `titulo` varchar(120) NOT NULL,
  `valor_estimado` decimal(12,2) NOT NULL,
  `id_cliente` int NOT NULL,
  `id_motivo_perda` tinyint DEFAULT NULL,
  `id_vendedor` int NOT NULL,
  PRIMARY KEY (`id_oportunidade`),
  KEY `FKtplw02c3tfwkn4qtio5hcpeau` (`id_cliente`),
  KEY `FKrxrgun0ityxrxus161q4modsv` (`id_motivo_perda`),
  KEY `FK7f2s3p199aepg07k8p1t8p4sl` (`id_vendedor`),
  CONSTRAINT `FK7f2s3p199aepg07k8p1t8p4sl` FOREIGN KEY (`id_vendedor`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `FKrxrgun0ityxrxus161q4modsv` FOREIGN KEY (`id_motivo_perda`) REFERENCES `motivo_perda` (`id_motivo`),
  CONSTRAINT `FKtplw02c3tfwkn4qtio5hcpeau` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tarefa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tarefa` (
  `id_tarefa` int NOT NULL AUTO_INCREMENT,
  `concluida` bit(1) NOT NULL,
  `concluida_em` datetime(6) DEFAULT NULL,
  `criado_em` datetime(6) NOT NULL,
  `data_vencimento` date NOT NULL,
  `descricao` text,
  `titulo` varchar(120) NOT NULL,
  `id_cliente` int DEFAULT NULL,
  `id_oportunidade` int DEFAULT NULL,
  `id_usuario` int NOT NULL,
  PRIMARY KEY (`id_tarefa`),
  KEY `FK1anhroi0h6alih0wrb8nuk2q` (`id_cliente`),
  KEY `FKb8bi67xylwpxvpa9bwertaar8` (`id_oportunidade`),
  KEY `FKky19t41n18yhyhw739ocwwofy` (`id_usuario`),
  CONSTRAINT `FK1anhroi0h6alih0wrb8nuk2q` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  CONSTRAINT `FKb8bi67xylwpxvpa9bwertaar8` FOREIGN KEY (`id_oportunidade`) REFERENCES `oportunidade` (`id_oportunidade`),
  CONSTRAINT `FKky19t41n18yhyhw739ocwwofy` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `token_recuperacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `token_recuperacao` (
  `id_token` int NOT NULL AUTO_INCREMENT,
  `codigo_hash` varchar(255) NOT NULL,
  `criado_em` datetime(6) NOT NULL,
  `expira_em` datetime(6) NOT NULL,
  `usado` bit(1) NOT NULL,
  `id_usuario` int NOT NULL,
  PRIMARY KEY (`id_token`),
  KEY `FKbi75rsxrvtf5mwgn7hxaw1nth` (`id_usuario`),
  CONSTRAINT `FKbi75rsxrvtf5mwgn7hxaw1nth` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `ativo` bit(1) NOT NULL,
  `atualizado_em` datetime(6) NOT NULL,
  `bloqueado_ate` datetime(6) DEFAULT NULL,
  `criado_em` datetime(6) NOT NULL,
  `email` varchar(160) NOT NULL,
  `nome` varchar(120) NOT NULL,
  `perfil` enum('ADMIN','GERENTE','VENDEDOR') NOT NULL,
  `senha_hash` varchar(255) NOT NULL,
  `tentativas_login` tinyint NOT NULL,
  `trocar_senha` bit(1) NOT NULL,
  `ultimo_acesso` datetime(6) DEFAULT NULL,
  `id_gerente` int DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `UK5171l57faosmj8myawaucatdw` (`email`),
  KEY `FKnkejpolfup2j1ea206tux8vxa` (`id_gerente`),
  CONSTRAINT `FKnkejpolfup2j1ea206tux8vxa` FOREIGN KEY (`id_gerente`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

