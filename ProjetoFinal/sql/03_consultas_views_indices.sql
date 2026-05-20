-- =====================================================================
-- ETAPA 04  -  Consultas, Visoes e Indices
-- =====================================================================
USE supermercado;

-- =====================================================================
-- 1) CONSULTAS  (4 consultas, conforme exigido)
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1.1  JOIN + GROUP BY + HAVING
--      Pergunta: quais funcionarios fizeram MAIS de 1 venda?
--      Justificativa: encontrar os funcionarios mais produtivos.
-- ---------------------------------------------------------------------
SELECT  f.matricula,
        f.nome,
        COUNT(v.nfe) AS total_vendas
FROM    funcionario f
JOIN    vende       v ON v.matricula_func = f.matricula
GROUP BY f.matricula, f.nome
HAVING  COUNT(v.nfe) > 1;

-- ---------------------------------------------------------------------
-- 1.2  2 JOINS + WHERE
--      Pergunta: vendas de outubro/2025 com nome do cliente e funcionario.
--      Justificativa: relatorio operacional do dia-a-dia.
-- ---------------------------------------------------------------------
SELECT  v.nfe,
        v.data_venda,
        c.nome AS cliente,
        f.nome AS funcionario
FROM    vende v
JOIN    cliente     c ON c.cpf       = v.cpf_cliente
JOIN    funcionario f ON f.matricula = v.matricula_func
WHERE   v.data_venda BETWEEN '2025-10-01' AND '2025-10-31';

-- ---------------------------------------------------------------------
-- 1.3  ANTI JOIN  (LEFT JOIN ... IS NULL)
--      Pergunta: clientes cadastrados que NUNCA realizaram compra.
--      Justificativa: alvo de campanhas de reativacao.
-- ---------------------------------------------------------------------
SELECT  c.cpf,
        c.nome,
        c.telefone
FROM    cliente c
LEFT JOIN vende v ON v.cpf_cliente = c.cpf
WHERE   v.cpf_cliente IS NULL;

-- ---------------------------------------------------------------------
-- 1.4  SUBCONSULTA
--      Pergunta: produtos com preco_base ACIMA da media geral.
--      Justificativa: identificar itens premium.
-- ---------------------------------------------------------------------
SELECT  p.codigo,
        p.nome,
        p.preco_base
FROM    produto p
WHERE   p.preco_base > (SELECT AVG(preco_base) FROM produto);


-- =====================================================================
-- 2) VISOES (VIEWS)
-- =====================================================================

-- ---------------------------------------------------------------------
-- 2.1  View com 3 JOINS + WHERE
--      Nome: vw_vendas_detalhadas
--      Justificativa: a tela "Consultas" precisa exibir, em uma unica
--      linha, a NF-e, o nome do cliente, do funcionario e dos produtos
--      vendidos com quantidade > 1.  Como esses dados estao espalhados
--      em 4 tabelas, encapsular em uma view torna a consulta legivel
--      e reutilizavel pela aplicacao.
-- ---------------------------------------------------------------------
DROP VIEW IF EXISTS vw_vendas_detalhadas;
CREATE VIEW vw_vendas_detalhadas AS
SELECT  v.nfe,
        v.data_venda,
        c.nome  AS cliente,
        f.nome  AS funcionario,
        p.nome  AS produto,
        vp.quantidade,
        (vp.quantidade * p.preco_base) AS valor_item
FROM    vende v
JOIN    cliente       c  ON c.cpf       = v.cpf_cliente
JOIN    funcionario   f  ON f.matricula = v.matricula_func
JOIN    vende_produto vp ON vp.nfe       = v.nfe
JOIN    produto       p  ON p.codigo    = vp.cod_produto
WHERE   vp.quantidade > 1;

-- ---------------------------------------------------------------------
-- 2.2  View com 1 JOIN + SUBCONSULTA
--      Nome: vw_funcionarios_destaque
--      Justificativa: o RH quer ver rapidamente quais funcionarios estao
--      vendendo ACIMA da media da empresa.  Em vez de repetir o calculo
--      da media em todo lugar, deixamos isso encapsulado na view.
-- ---------------------------------------------------------------------
DROP VIEW IF EXISTS vw_funcionarios_destaque;
CREATE VIEW vw_funcionarios_destaque AS
SELECT  f.matricula,
        f.nome,
        COUNT(v.nfe) AS total_vendas
FROM    funcionario f
JOIN    vende       v ON v.matricula_func = f.matricula
GROUP BY f.matricula, f.nome
HAVING  COUNT(v.nfe) >= (
    SELECT AVG(qtd_por_func)
    FROM (
        SELECT COUNT(nfe) AS qtd_por_func
        FROM   vende
        GROUP BY matricula_func
    ) AS sub
);


-- =====================================================================
-- 3) INDICES
-- =====================================================================
-- OBS importante:
--   No MySQL/InnoDB, toda FOREIGN KEY ja cria automaticamente um indice
--   na coluna referenciadora.  Portanto, indices em vende.matricula_func
--   e vende.cpf_cliente (ambas FK) NAO seriam novos indices - seriam
--   redundantes.  Por isso escolhemos campos que NAO tem indice
--   automatico, mas sao USADOS pelas consultas/views acima.

-- ---------------------------------------------------------------------
-- 3.1  Indice em vende.data_venda
--      Justificativa: a consulta 1.2 filtra vendas por intervalo de
--      datas (WHERE data_venda BETWEEN ? AND ?).  Sem indice, cada
--      filtro por periodo faz full scan na tabela vende.  Como vende
--      cresce diariamente (uma linha por NF-e emitida), um indice em
--      data_venda transforma o BETWEEN em um range scan eficiente e
--      acelera todos os relatorios por periodo da aplicacao.
-- ---------------------------------------------------------------------
CREATE INDEX idx_vende_data_venda ON vende(data_venda);

-- ---------------------------------------------------------------------
-- 3.2  Indice em produto.preco_base
--      Justificativa: a consulta 1.4 compara preco_base com a media
--      geral (subconsulta com AVG) e a aplicacao tende a precisar de
--      ranking por preco (produtos premium, oferta de itens baratos,
--      filtros por faixa de preco).  Sem indice, todo SELECT que
--      filtra ou ordena por preco faz full scan.  O indice em
--      preco_base acelera tanto o predicado da 1.4 quanto consultas
--      do tipo "produtos com preco entre X e Y".
-- ---------------------------------------------------------------------
CREATE INDEX idx_produto_preco_base ON produto(preco_base);
