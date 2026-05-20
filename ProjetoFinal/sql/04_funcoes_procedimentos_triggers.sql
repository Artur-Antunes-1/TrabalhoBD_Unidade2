-- =====================================================================
-- ETAPA 05  -  Funcoes, Procedimentos e Triggers
-- =====================================================================
USE supermercado;

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
    DETERMINISTIC
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
-- 1.2  fn_categoria_funcionario(p_matricula)  - usa IF/ELSE
--      Classifica o funcionario em "Ouro", "Prata" ou "Bronze"
--      conforme o numero de vendas registradas.
--      Justificativa: a regra de negocio de premiacao precisa estar
--      no banco para ser aplicada por triggers, procedimentos e telas.
-- ---------------------------------------------------------------------
DROP FUNCTION IF EXISTS fn_categoria_funcionario;
DELIMITER $$
CREATE FUNCTION fn_categoria_funcionario(p_matricula VARCHAR(20))
    RETURNS VARCHAR(20)
    DETERMINISTIC
    READS SQL DATA
BEGIN
    DECLARE v_qtd_vendas INT DEFAULT 0;
    DECLARE v_categoria  VARCHAR(20);

    SELECT COUNT(*) INTO v_qtd_vendas
    FROM   vende
    WHERE  matricula_func = p_matricula;

    -- Estrutura condicional (IF / ELSEIF / ELSE)
    IF v_qtd_vendas >= 5 THEN
        SET v_categoria = 'Ouro';
    ELSEIF v_qtd_vendas >= 2 THEN
        SET v_categoria = 'Prata';
    ELSE
        SET v_categoria = 'Bronze';
    END IF;

    RETURN v_categoria;
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
-- 2.2  pr_gerar_resumo_funcionarios  -  procedimento com CURSOR
--
--      Por que CURSOR e necessario aqui?
--      Para cada funcionario nos:
--          1) calculamos o total de vendas (count e valor)
--          2) chamamos a funcao fn_categoria_funcionario
--          3) inserimos UMA linha na tabela resumo_vendas_funcionario
--      Como cada iteracao gera uma linha NOVA na tabela de resumo
--      (e nao um UPDATE em uma tabela existente), um simples UPDATE
--      nao resolveria.  A logica precisa ser por linha, e o cursor
--      e a forma natural de iterar linha a linha em PL/SQL.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS pr_gerar_resumo_funcionarios;
DELIMITER $$
CREATE PROCEDURE pr_gerar_resumo_funcionarios()
BEGIN
    DECLARE v_fim         INT DEFAULT 0;
    DECLARE v_matricula   VARCHAR(20);
    DECLARE v_nome        VARCHAR(100);
    DECLARE v_qtd_vendas  INT;
    DECLARE v_valor_total DECIMAL(10,2);
    DECLARE v_categoria   VARCHAR(20);

    -- Cursor que percorre todos os funcionarios
    DECLARE cur_funcs CURSOR FOR
        SELECT matricula, nome FROM funcionario;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_fim = 1;

    -- Limpa o resumo anterior antes de regerar
    DELETE FROM resumo_vendas_funcionario;

    OPEN cur_funcs;

    le_funcs: LOOP
        FETCH cur_funcs INTO v_matricula, v_nome;
        IF v_fim = 1 THEN
            LEAVE le_funcs;
        END IF;

        -- 1) Quantidade de vendas
        SELECT COUNT(*) INTO v_qtd_vendas
        FROM   vende
        WHERE  matricula_func = v_matricula;

        -- 2) Valor total movimentado por esse funcionario
        SELECT COALESCE(SUM(vp.quantidade * p.preco_base), 0)
        INTO   v_valor_total
        FROM   vende v
        JOIN   vende_produto vp ON vp.nfe = v.nfe
        JOIN   produto       p  ON p.codigo = vp.cod_produto
        WHERE  v.matricula_func = v_matricula;

        -- 3) Reaproveita a funcao para classificar
        SET v_categoria = fn_categoria_funcionario(v_matricula);

        INSERT INTO resumo_vendas_funcionario
            (matricula, nome, total_vendas, valor_total, classificacao)
        VALUES
            (v_matricula, v_nome, v_qtd_vendas, v_valor_total, v_categoria);
    END LOOP le_funcs;

    CLOSE cur_funcs;
END $$
DELIMITER ;


-- =====================================================================
-- 3) TRIGGERS
-- =====================================================================

-- ---------------------------------------------------------------------
-- 3.1  tg_log_nova_venda  -  AFTER INSERT em vende
--      Justificativa: toda vez que uma venda e registrada queremos
--      manter um historico/auditoria em log_alteracoes.  Esse log
--      ajuda a investigar problemas e e visualizado pela aplicacao
--      na aba "Logs".
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS tg_log_nova_venda;
DELIMITER $$
CREATE TRIGGER tg_log_nova_venda
AFTER INSERT ON vende
FOR EACH ROW
BEGIN
    INSERT INTO log_alteracoes (tabela, tipo_evento, descricao)
    VALUES (
        'vende',
        'INSERT',
        CONCAT('Nova venda NF-e ', NEW.nfe,
               ' registrada por ', NEW.matricula_func,
               ' em ', NEW.data_venda)
    );
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
