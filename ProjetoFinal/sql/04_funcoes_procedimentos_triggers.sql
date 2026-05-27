-- =====================================================================
-- BANCO DE DADOS - 2026.1
-- Projeto Pratico  -  MODULO 02  -  ETAPA 05
-- Tema: Supermercado BD
-- Grupo: Artur Antunes, Pedro Ferraz, Ricardo Machado, Victor Uen
-- SGBD: MySQL 8.0+
--
-- Conteudo desta entrega (Etapa 05):
--   1) 02 FUNCOES (FUNCTIONS) com justificativa:
--       1.1  fn_total_venda(p_nfe)              - SELECT + SUM
--       1.2  fn_porte_venda(p_nfe)              - IF / ELSEIF / ELSE
--                                                 (reaproveita fn_total_venda)
--   2) 02 PROCEDIMENTOS (PROCEDURES) com justificativa:
--       2.1  pr_atualizar_preco_departamento    - UPDATE em massa
--       2.2  pr_promocao_produtos_parados       - CURSOR sobre produto
--   3) 02 TRIGGERS com justificativa:
--       3.1  tg_limita_variacao_preco           - BEFORE UPDATE em produto
--       3.2  tg_log_alteracao_preco             - AFTER UPDATE em produto
--   4) TABELAS DE APOIO usadas pelos triggers:
--       - log_alteracoes              (alimentada pelos triggers)
--
-- Pre-requisito: este script assume que o banco "supermercado", as 13
-- tabelas do esquema relacional e os dados de povoamento ja foram
-- criados conforme a Entrega 03 (MOD 01).
-- =====================================================================

USE supermercado;


-- =====================================================================
-- 0) TABELAS DE APOIO
--    Sao criadas aqui porque so existem em funcao desta etapa:
--    log_alteracoes armazena os registros gerados pelos triggers.
--    Usamos IF NOT EXISTS para que o script seja idempotente e nao
--    conflite caso a tabela ja tenha sido criada previamente.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0.1  log_alteracoes  -  alimentada pelos triggers
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS log_alteracoes (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    tabela      VARCHAR(50)  NOT NULL,
    tipo_evento VARCHAR(50)  NOT NULL,
    descricao   VARCHAR(500) NOT NULL,
    data_hora   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- =====================================================================
-- 1) FUNCOES (FUNCTIONS)
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1.1  fn_total_venda(p_nfe)
--      Recebe o numero da NF-e e devolve o valor TOTAL daquela venda
--      (soma de quantidade * preco_base de cada item).
--      Justificativa: substitui consulta repetida toda vez que a tela
--      precisa mostrar o "total da venda".  Encapsulamento + reuso.
-- ---------------------------------------------------------------------
DROP FUNCTION IF EXISTS fn_total_venda;
DELIMITER $$
CREATE FUNCTION fn_total_venda(p_nfe VARCHAR(44))
    RETURNS DECIMAL(10,2)
    READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(10,2);

    SELECT COALESCE(SUM(vp.quantidade * p.preco_base), 0)
    INTO   v_total
    FROM   vende_produto vp
    JOIN   produto       p ON p.codigo = vp.cod_produto
    WHERE  vp.nfe = p_nfe;

    RETURN v_total;
END $$
DELIMITER ;


-- ---------------------------------------------------------------------
-- 1.2  fn_porte_venda(p_nfe)  - usa IF/ELSEIF/ELSE
--      Classifica uma venda em "GRANDE", "MEDIA" ou "PEQUENA"
--      conforme o valor total dos itens.
--      Justificativa: o porte de uma venda e usado em relatorios
--      gerenciais.  Encapsular a regra em uma funcao evita
--      duplicar a logica nas consultas e demonstra composicao de
--      funcoes: fn_porte_venda reaproveita fn_total_venda em vez
--      de recalcular o total na mao.  Usa apenas tabelas originais
--      (vende_produto, produto), atraves de fn_total_venda.
-- ---------------------------------------------------------------------
DROP FUNCTION IF EXISTS fn_porte_venda;
DELIMITER $$
CREATE FUNCTION fn_porte_venda(p_nfe VARCHAR(44))
    RETURNS VARCHAR(20)
    READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(10,2);
    DECLARE v_porte VARCHAR(20);

    -- Reaproveita fn_total_venda para obter o total da venda
    SET v_total = fn_total_venda(p_nfe);

    -- Estrutura condicional (IF / ELSEIF / ELSE)
    IF v_total >= 300 THEN
        SET v_porte = 'GRANDE';
    ELSEIF v_total >= 150 THEN
        SET v_porte = 'MEDIA';
    ELSE
        SET v_porte = 'PEQUENA';
    END IF;

    RETURN v_porte;
END $$
DELIMITER ;


-- =====================================================================
-- 2) PROCEDIMENTOS (PROCEDURES)
-- =====================================================================

-- ---------------------------------------------------------------------
-- 2.1  pr_atualizar_preco_departamento  -  procedimento de UPDATE
--      Aumenta (ou diminui, se percentual negativo) o preco de TODOS
--      os produtos de um determinado departamento.
--      Justificativa: reajuste de preco por categoria e operacao
--      muito comum em supermercado (ex: aumentar 10% em Bebidas).
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS pr_atualizar_preco_departamento;
DELIMITER $$
CREATE PROCEDURE pr_atualizar_preco_departamento(
    IN p_cod_departamento VARCHAR(20),
    IN p_percentual       DECIMAL(5,2)
)
BEGIN
    UPDATE produto p
    JOIN   departamento_produto dp ON dp.cod_produto = p.codigo
    SET    p.preco_base = ROUND(p.preco_base * (1 + p_percentual/100), 2)
    WHERE  dp.cod_departamento = p_cod_departamento;
END $$
DELIMITER ;


-- ---------------------------------------------------------------------
-- 2.2  pr_promocao_produtos_parados  -  procedimento com CURSOR
--
--      Por que CURSOR e necessario aqui?
--      Para cada produto:
--          1) calculamos o total ja vendido (SUM em vende_produto)
--          2) com base nesse valor decidimos individualmente qual
--             desconto aplicar (logica IF/ELSEIF por linha)
--          3) atualizamos o preco_base daquele produto especifico
--      Como a decisao "qual percentual aplicar" varia por produto e
--      depende de um agregado calculado para cada um, o cursor e a
--      forma natural de iterar linha a linha em PL/SQL.  Os
--      parametros permitem ao usuario escolher os percentuais.
--
--      Usa SOMENTE tabelas originais (produto, vende_produto).
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS pr_promocao_produtos_parados;
DELIMITER $$
CREATE PROCEDURE pr_promocao_produtos_parados(
    IN p_perc_sem_vendas  DECIMAL(5,2),
    IN p_perc_pouca_venda DECIMAL(5,2)
)
BEGIN
    DECLARE v_fim     INT DEFAULT 0;
    DECLARE v_codigo  VARCHAR(20);
    DECLARE v_qtd     INT;

    -- Cursor que percorre todos os produtos
    DECLARE cur_prods CURSOR FOR
        SELECT codigo FROM produto;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_fim = 1;

    OPEN cur_prods;

    le_prods: LOOP
        FETCH cur_prods INTO v_codigo;
        IF v_fim = 1 THEN
            LEAVE le_prods;
        END IF;

        -- Quantidade total ja vendida desse produto
        SELECT COALESCE(SUM(quantidade), 0) INTO v_qtd
        FROM   vende_produto
        WHERE  cod_produto = v_codigo;

        -- Decisao por linha (IF/ELSEIF dentro do loop)
        IF v_qtd = 0 THEN
            UPDATE produto
            SET    preco_base = ROUND(preco_base * (1 - p_perc_sem_vendas/100), 2)
            WHERE  codigo = v_codigo;
        ELSEIF v_qtd <= 2 THEN
            UPDATE produto
            SET    preco_base = ROUND(preco_base * (1 - p_perc_pouca_venda/100), 2)
            WHERE  codigo = v_codigo;
        END IF;
    END LOOP le_prods;

    CLOSE cur_prods;
END $$
DELIMITER ;


-- =====================================================================
-- 3) TRIGGERS
-- =====================================================================

-- ---------------------------------------------------------------------
-- 3.1  tg_limita_variacao_preco  -  BEFORE UPDATE em produto
--      Justificativa: precos de produtos sao alterados manualmente
--      pela aplicacao e tambem em massa pelas procedures
--      (pr_atualizar_preco_departamento, pr_promocao_produtos_parados).
--      Erros de digitacao (ex.: "feijao por R$ 0,01" por esquecer uma
--      casa decimal) ou parametros errados nas procedures podem causar
--      prejuizo enorme.  Este trigger funciona como rede de seguranca:
--      bloqueia qualquer UPDATE que tente alterar o preco em mais de
--      80% para cima ou para baixo, abortando a operacao com SIGNAL.
--      Forma um par com o 3.2: este IMPEDE mudancas absurdas, o 3.2
--      REGISTRA em log as mudancas que passaram.
-- ---------------------------------------------------------------------
-- Remove trigger antigo desta posicao (caso o banco ainda tenha dele)
DROP TRIGGER IF EXISTS tg_log_nova_venda;
DROP TRIGGER IF EXISTS tg_limita_variacao_preco;
DELIMITER $$
CREATE TRIGGER tg_limita_variacao_preco
BEFORE UPDATE ON produto
FOR EACH ROW
BEGIN
    -- So validamos quando havia um preco anterior > 0 e o preco mudou
    IF OLD.preco_base > 0 AND NEW.preco_base <> OLD.preco_base THEN
        IF NEW.preco_base > OLD.preco_base * 1.8
           OR NEW.preco_base < OLD.preco_base * 0.2 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT =
                'Variacao de preco maior que 80% nao permitida - confirme com gerente';
        END IF;
    END IF;
END $$
DELIMITER ;


-- ---------------------------------------------------------------------
-- 3.2  tg_log_alteracao_preco  -  AFTER UPDATE em produto
--      Justificativa: alteracoes de preco sao sensiveis (impactam
--      vendas e o financeiro).  Registrar preco antigo e novo em log
--      e essencial para auditoria.
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS tg_log_alteracao_preco;
DELIMITER $$
CREATE TRIGGER tg_log_alteracao_preco
AFTER UPDATE ON produto
FOR EACH ROW
BEGIN
    IF OLD.preco_base <> NEW.preco_base THEN
        INSERT INTO log_alteracoes (tabela, tipo_evento, descricao)
        VALUES (
            'produto',
            'UPDATE_PRECO',
            CONCAT('Produto ', NEW.codigo,
                   ' (', NEW.nome, ')',
                   ' alterou preco de R$ ', OLD.preco_base,
                   ' para R$ ',  NEW.preco_base)
        );
    END IF;
END $$
DELIMITER ;
