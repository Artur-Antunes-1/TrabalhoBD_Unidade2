-- =====================================================================
-- BANCO DE DADOS - 2026.1
-- Projeto Pratico  -  MODULO 02  -  ETAPA 04
-- Tema: Supermercado BD
-- Grupo: Artur Antunes, Pedro Ferraz, Ricardo Machado, Victor Uen
-- SGBD: MySQL 8.0+
--
-- Conteudo desta entrega (Etapa 04):
--   1) 04 CONSULTAS para visualizacao de dados:
--       1.1  1 consulta com JOIN + GROUP BY + HAVING
--       1.2  1 consulta com 2 JOINs + WHERE
--       1.3  1 consulta com ANTI JOIN (LEFT JOIN ... IS NULL)
--       1.4  1 consulta com SUBCONSULTA
--   2) 02 VISOES (views) com justificativa:
--       2.1  1 view com 3 JOINs + WHERE
--       2.2  1 view com 1 JOIN + SUBCONSULTA
--   3) 02 INDICES novos, realmente necessarios:
--       3.1  Indice em vende(data_venda)
--       3.2  Indice em produto(preco_base)
--
-- Pre-requisito: este script assume que o banco "supermercado", as 13
-- tabelas do esquema relacional e os dados de povoamento ja foram
-- criados conforme a Entrega 03 (MOD 01).
-- =====================================================================

USE supermercado;

-- =====================================================================
-- 1) CONSULTAS  (4 consultas, conforme exigido)
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1.1  JOIN + GROUP BY + HAVING
--      Pergunta: quais funcionarios fizeram MAIS de uma venda?
--      Justificativa: identificar os funcionarios mais produtivos da
--      rede, dado que cada NF-e emitida corresponde a uma venda.  O
--      HAVING filtra o agrupamento (nao da para usar WHERE pois a
--      contagem so existe depois do GROUP BY).
-- ---------------------------------------------------------------------
SELECT  f.matricula,
        f.nome,
        COUNT(v.nfe) AS total_vendas
FROM    funcionario f
JOIN    vende       v ON v.matricula_func = f.matricula
GROUP BY f.matricula, f.nome
HAVING  COUNT(v.nfe) > 1
ORDER BY total_vendas DESC, f.nome;

-- ---------------------------------------------------------------------
-- 1.2  2 JOINS + WHERE
--      Pergunta: vendas de outubro/2025 com nome do cliente e do
--      funcionario que registrou.
--      Justificativa: relatorio operacional usado pela gerencia para
--      auditar quem vendeu para quem em um determinado periodo.  Como
--      vende guarda apenas chaves (matricula_func, cpf_cliente),
--      precisamos de 2 JOINs para trazer os nomes.
-- ---------------------------------------------------------------------
SELECT  v.nfe,
        v.data_venda,
        c.nome AS cliente,
        f.nome AS funcionario
FROM    vende v
JOIN    cliente     c ON c.cpf       = v.cpf_cliente
JOIN    funcionario f ON f.matricula = v.matricula_func
WHERE   v.data_venda BETWEEN '2025-10-01' AND '2025-10-31'
ORDER BY v.data_venda, v.nfe;

-- ---------------------------------------------------------------------
-- 1.3  ANTI JOIN  (LEFT JOIN ... IS NULL)
--      Pergunta: clientes cadastrados que NUNCA realizaram nenhuma
--      compra.
--      Justificativa: lista alvo para campanhas de reativacao /
--      marketing.  O anti join (LEFT JOIN com filtro IS NULL no lado
--      direito) e o jeito canonico de expressar "que NAO existem em".
-- ---------------------------------------------------------------------
SELECT  c.cpf,
        c.nome,
        c.telefone
FROM    cliente c
LEFT JOIN vende v ON v.cpf_cliente = c.cpf
WHERE   v.cpf_cliente IS NULL
ORDER BY c.nome;

-- ---------------------------------------------------------------------
-- 1.4  SUBCONSULTA
--      Pergunta: produtos com preco_base ACIMA da media geral.
--      Justificativa: identificar itens "premium" do supermercado para
--      acoes de marketing especificas (campanhas de luxo, vitrines,
--      cupons de desconto seletivos).  A media e calculada UMA vez
--      pela subconsulta no WHERE.
-- ---------------------------------------------------------------------
SELECT  p.codigo,
        p.nome,
        p.preco_base
FROM    produto p
WHERE   p.preco_base > (SELECT AVG(preco_base) FROM produto)
ORDER BY p.preco_base DESC;


-- =====================================================================
-- 2) VISOES (VIEWS)
-- =====================================================================

-- ---------------------------------------------------------------------
-- 2.1  View com 3 JOINS + WHERE
--      Nome: vw_vendas_detalhadas
--      Justificativa: a tela "Consultas" e o relatorio gerencial
--      precisam exibir, em uma unica linha, a NF-e, o nome do cliente,
--      do funcionario e dos produtos vendidos em quantidade maior que
--      1 (ou seja, vendas "no atacado" / multi-itens).  Como esses
--      dados estao espalhados em 5 tabelas (vende, cliente,
--      funcionario, vende_produto, produto), encapsular em uma view
--      torna a consulta legivel, reutilizavel e padroniza o calculo
--      do valor de cada item (quantidade * preco_base).
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
JOIN    vende_produto vp ON vp.nfe      = v.nfe
JOIN    produto       p  ON p.codigo    = vp.cod_produto
WHERE   vp.quantidade > 1;

-- ---------------------------------------------------------------------
-- 2.2  View com 1 JOIN + SUBCONSULTA
--      Nome: vw_funcionarios_destaque
--      Justificativa: o RH quer ver rapidamente quais funcionarios
--      estao vendendo ACIMA (ou igual) a media de vendas da empresa.
--      O calculo "media de vendas por funcionario" e nao trivial
--      (precisa de uma subconsulta que primeiro agrupa por
--      funcionario para depois tirar a media dessas contagens).
--      Encapsular tudo em uma view evita que esse calculo seja
--      reescrito (e potencialmente quebrado) em cada relatorio.
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
--      acelera todos os relatorios por periodo da aplicacao
--      (faturamento diario, vendas do mes, fechamento de caixa, ...).
-- ---------------------------------------------------------------------
CREATE INDEX idx_vende_data_venda ON vende(data_venda);

-- ---------------------------------------------------------------------
-- 3.2  Indice em produto.preco_base
--      Justificativa: a consulta 1.4 compara preco_base com a media
--      geral via subconsulta (SELECT AVG(preco_base) FROM produto) e
--      tambem aplica um filtro de range (preco_base > media).  Sem
--      indice, todo SELECT que filtra ou ordena por preco faz full
--      scan.  O indice em preco_base acelera tanto o predicado da
--      1.4 quanto consultas tipicas de catalogo ("produtos com
--      preco entre X e Y", "ranking de itens mais caros") - todas
--      muito frequentes no dominio do supermercado.
-- ---------------------------------------------------------------------
CREATE INDEX idx_produto_preco_base ON produto(preco_base);
