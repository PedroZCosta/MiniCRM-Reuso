
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

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` (`id_cliente`, `atualizado_em`, `criado_em`, `email`, `empresa`, `excluido`, `nome`, `status`, `telefone`, `id_vendedor`) VALUES (1,'2026-11-09 18:48:59.966613','2026-11-09 18:48:59.966627','contato@paoquente.com.br','Padaria Pão Quente Ltda',_binary '\0','Padaria Pão Quente','PROSPECT','(41) 3333-1001',3),(2,'2026-11-09 18:49:00.014941','2026-11-09 18:49:00.014951','oficina@rapida.com.br','Oficina Rápida ME',_binary '\0','Oficina Mecânica Rápida','PROSPECT','(41) 3333-1002',4),(3,'2026-11-09 18:49:00.275405','2026-11-09 18:49:00.042355','clinica@sorriso.com.br','Clínica Sorriso S/A',_binary '\0','Clínica Sorriso','ATIVO','(41) 3333-1003',3),(4,'2026-11-09 18:49:00.067844','2026-11-09 18:49:00.067852','secretaria@aprendermais.com.br','Aprender Mais Educação',_binary '\0','Escola Aprender Mais','PROSPECT','(41) 3333-1004',4),(5,'2026-11-09 18:49:00.097440','2026-11-09 18:49:00.097448','compras@bompreco.com.br','Mercado Bom Preço Ltda',_binary '\0','Mercado Bom Preço','PROSPECT','(41) 3333-1005',3),(6,'2026-11-09 18:49:00.121793','2026-11-09 18:49:00.121803','recepcao@studiofit.com.br','Studio Fit Academia',_binary '\0','Studio Fit','PROSPECT','(41) 3333-1006',4);
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `historico_etapa` WRITE;
/*!40000 ALTER TABLE `historico_etapa` DISABLE KEYS */;
INSERT INTO `historico_etapa` (`id_historico`, `alterado_em`, `etapa_anterior`, `etapa_nova`, `id_oportunidade`, `id_usuario`) VALUES (1,'2026-11-09 18:49:00.237673','PROSPECCAO','CONTATO',1,3),(2,'2026-11-09 18:49:00.252261','PROSPECCAO','PROPOSTA',2,4),(3,'2026-11-09 18:49:00.265848','PROSPECCAO','GANHA',3,3),(4,'2026-11-09 18:49:00.287003','PROSPECCAO','CONTATO',4,4),(5,'2026-11-09 18:49:00.300200','PROSPECCAO','PERDIDA',5,3),(6,'2026-11-09 18:49:00.317267','PROSPECCAO','PROPOSTA',6,4);
/*!40000 ALTER TABLE `historico_etapa` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `interacao` WRITE;
/*!40000 ALTER TABLE `interacao` DISABLE KEYS */;
INSERT INTO `interacao` (`id_interacao`, `criado_em`, `data_interacao`, `observacoes`, `tipo`, `id_cliente`, `id_usuario`) VALUES (1,'2026-11-09 18:48:59.999265','2026-11-01 10:00:00.000000','Primeiro contato com Padaria Pão Quente','LIGACAO',1,3),(2,'2026-11-09 18:49:00.030160','2026-11-02 10:00:00.000000','Primeiro contato com Oficina Mecânica Rápida','EMAIL',2,4),(3,'2026-11-09 18:49:00.056348','2026-11-03 10:00:00.000000','Primeiro contato com Clínica Sorriso','REUNIAO',3,3),(4,'2026-11-09 18:49:00.086007','2026-11-04 10:00:00.000000','Primeiro contato com Escola Aprender Mais','LIGACAO',4,4),(5,'2026-11-09 18:49:00.111426','2026-11-05 10:00:00.000000','Primeiro contato com Mercado Bom Preço','EMAIL',5,3),(6,'2026-11-09 18:49:00.135934','2026-11-06 10:00:00.000000','Primeiro contato com Studio Fit','REUNIAO',6,4);
/*!40000 ALTER TABLE `interacao` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `motivo_perda` WRITE;
/*!40000 ALTER TABLE `motivo_perda` DISABLE KEYS */;
INSERT INTO `motivo_perda` (`id_motivo`, `descricao`) VALUES (1,'preço'),(2,'concorrente'),(3,'sem verba'),(4,'sem resposta');
/*!40000 ALTER TABLE `motivo_perda` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `notificacao` WRITE;
/*!40000 ALTER TABLE `notificacao` DISABLE KEYS */;
/*!40000 ALTER TABLE `notificacao` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `oportunidade` WRITE;
/*!40000 ALTER TABLE `oportunidade` DISABLE KEYS */;
INSERT INTO `oportunidade` (`id_oportunidade`, `atualizado_em`, `criado_em`, `data_prevista`, `etapa`, `fechada_em`, `titulo`, `valor_estimado`, `id_cliente`, `id_motivo_perda`, `id_vendedor`) VALUES (1,'2026-11-09 18:49:00.241431','2026-11-09 18:49:00.151444','2026-12-15','CONTATO',NULL,'Sistema de pedidos online',18500.00,1,NULL,3),(2,'2026-11-09 18:49:00.254513','2026-11-09 18:49:00.170924','2026-12-15','PROPOSTA',NULL,'Consultoria de estoque',7200.00,2,NULL,4),(3,'2026-11-09 18:49:00.268670','2026-11-09 18:49:00.184162','2026-12-15','GANHA','2026-11-09 18:49:00.265584','Plano de manutenção anual',12900.00,3,NULL,3),(4,'2026-11-09 18:49:00.289387','2026-11-09 18:49:00.199228','2026-12-15','CONTATO',NULL,'Portal da escola',25000.00,4,NULL,4),(5,'2026-11-09 18:49:00.303751','2026-11-09 18:49:00.212507','2026-12-15','PERDIDA','2026-11-09 18:49:00.299512','Integração com PDV',9800.00,5,1,3),(6,'2026-11-09 18:49:00.319454','2026-11-09 18:49:00.224897','2026-12-15','PROPOSTA',NULL,'App de agendamento',15300.00,6,NULL,4);
/*!40000 ALTER TABLE `oportunidade` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `tarefa` WRITE;
/*!40000 ALTER TABLE `tarefa` DISABLE KEYS */;
INSERT INTO `tarefa` (`id_tarefa`, `concluida`, `concluida_em`, `criado_em`, `data_vencimento`, `descricao`, `titulo`, `id_cliente`, `id_oportunidade`, `id_usuario`) VALUES (1,_binary '\0',NULL,'2026-11-09 18:49:00.335372','2026-11-12','Tarefa de exemplo','Enviar proposta revisada',1,NULL,3),(2,_binary '\0',NULL,'2026-11-09 18:49:00.349521','2026-11-10','Tarefa de exemplo','Ligar para confirmar reunião',2,NULL,4),(3,_binary '\0',NULL,'2026-11-09 18:49:00.360297','2026-11-25','Tarefa de exemplo','Preparar demonstração',3,NULL,3),(4,_binary '\0',NULL,'2026-11-09 18:49:00.372134','2026-11-07','Tarefa de exemplo','Cobrar retorno do contrato',4,NULL,4);
/*!40000 ALTER TABLE `tarefa` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `token_recuperacao` WRITE;
/*!40000 ALTER TABLE `token_recuperacao` DISABLE KEYS */;
/*!40000 ALTER TABLE `token_recuperacao` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` (`id_usuario`, `ativo`, `atualizado_em`, `bloqueado_ate`, `criado_em`, `email`, `nome`, `perfil`, `senha_hash`, `tentativas_login`, `trocar_senha`, `ultimo_acesso`, `id_gerente`) VALUES (1,_binary '','2026-11-09 18:48:58.838824',NULL,'2026-11-09 18:46:40.748350','teste@email.com','Administrador','ADMIN','$2a$10$t3DAWU.WbB0f.zAILP.yye69yJNM88Q7jc/tEEyfive2RBKw35wuK',0,_binary '\0','2026-11-09 18:48:58.835138',NULL),(2,_binary '','2026-11-09 18:48:59.234483',NULL,'2026-11-09 18:48:58.951989','marina.gerente@minicrm.com','Marina Gerente','GERENTE','$2a$10$R5RbDDDgEs665dp6tc1nze6rXyekwgBIFaXueY1nnDozi9kz.YAF.',0,_binary '\0','2026-11-09 18:48:59.230635',NULL),(3,_binary '','2026-11-09 18:48:59.602806',NULL,'2026-11-09 18:48:59.313107','carlos.vendedor@minicrm.com','Carlos Vendedor','VENDEDOR','$2a$10$vxTi79CU17Ctu97E.GhpC.gJ3Vemuobpim03Uq2LBPWbEWhyRre5u',0,_binary '\0','2026-11-09 18:48:59.599078',2),(4,_binary '','2026-11-09 18:48:59.947130',NULL,'2026-11-09 18:48:59.679870','ana.vendedora@minicrm.com','Ana Vendedora','VENDEDOR','$2a$10$QD9P6z4FtdNJvA0gepVkN.mCZp4hOlk2Db8t3FhW2UmJHPeIBrDuS',0,_binary '\0','2026-11-09 18:48:59.944604',2);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

