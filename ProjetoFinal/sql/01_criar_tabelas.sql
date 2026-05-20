-- =====================================================================
-- SUPERMERCADO BD  -  Etapa 03 (criacao das tabelas)
-- SGBD: MySQL 8.0+
-- Grupo: Artur Antunes, Pedro Ferraz, Ricardo Machado, Victor Uen
-- =====================================================================
-- Este script:
--   1. Apaga o banco se ja existir
--   2. Cria o banco "supermercado"
--   3. Cria as 13 tabelas do esquema relacional + tabelas de apoio
--   4. Aplica constraints (PK, FK, CHECK, UNIQUE, DEFAULT)
--   5. Define ON UPDATE CASCADE e ON DELETE SET NULL onde foram pedidos
-- =====================================================================

DROP DATABASE IF EXISTS supermercado;
CREATE DATABASE supermercado CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE supermercado;

-- ---------------------------------------------------------------------
-- 1. FILIAL  -  unidades fisicas da rede
-- ---------------------------------------------------------------------
CREATE TABLE filial (
    cnpj    VARCHAR(14)  NOT NULL,
    rua     VARCHAR(100) DEFAULT NULL,
    numero  VARCHAR(10)  DEFAULT NULL,
    cep     CHAR(8)      DEFAULT NULL,
    CONSTRAINT pk_filial       PRIMARY KEY (cnpj),
    CONSTRAINT chk_filial_cnpj CHECK (LENGTH(cnpj) = 14)
);

-- ---------------------------------------------------------------------
-- 2. FILIAL_TELEFONE  -  atributo multivalorado
--    ON UPDATE CASCADE: se mudarmos o CNPJ da filial, os telefones acompanham
-- ---------------------------------------------------------------------
CREATE TABLE filial_telefone (
    telefone    VARCHAR(15) NOT NULL,
    cnpj_filial VARCHAR(14) NOT NULL,
    CONSTRAINT pk_filial_telefone PRIMARY KEY (telefone, cnpj_filial),
    CONSTRAINT fk_filtel_filial   FOREIGN KEY (cnpj_filial)
        REFERENCES filial(cnpj)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 3. FUNCIONARIO  -  pessoas que trabalham na rede
--    auto-relacionamento atraves de "supervisor"
--    ON DELETE SET NULL: se o supervisor sair, subordinados ficam sem chefe
-- ---------------------------------------------------------------------
CREATE TABLE funcionario (
    matricula    VARCHAR(20)  NOT NULL,
    cpf          VARCHAR(11)  DEFAULT NULL,
    nome         VARCHAR(100) NOT NULL,
    telefone1    VARCHAR(15)  NOT NULL,
    telefone2    VARCHAR(15)  DEFAULT NULL,
    tipo         VARCHAR(15)  NOT NULL DEFAULT 'operacional',
    cnpj_filial  VARCHAR(14)  NOT NULL,
    supervisor   VARCHAR(20)  DEFAULT NULL,
    CONSTRAINT pk_funcionario       PRIMARY KEY (matricula),
    CONSTRAINT uq_funcionario_cpf   UNIQUE (cpf),
    CONSTRAINT chk_funcionario_tipo CHECK (tipo IN ('operacional','administrativo')),
    CONSTRAINT fk_func_filial       FOREIGN KEY (cnpj_filial)
        REFERENCES filial(cnpj)
        ON UPDATE CASCADE,
    CONSTRAINT fk_func_supervisor   FOREIGN KEY (supervisor)
        REFERENCES funcionario(matricula)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- ---------------------------------------------------------------------
-- 4. DEPENDENTE  -  entidade fraca (depende do funcionario)
-- ---------------------------------------------------------------------
CREATE TABLE dependente (
    matricula  VARCHAR(20)  NOT NULL,
    mat_func   VARCHAR(20)  NOT NULL,
    nome       VARCHAR(100) NOT NULL,
    CONSTRAINT pk_dependente      PRIMARY KEY (matricula, mat_func),
    CONSTRAINT fk_dep_funcionario FOREIGN KEY (mat_func)
        REFERENCES funcionario(matricula)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 5. CLIENTE  -  consumidores que aparecem no caixa
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
    cpf      VARCHAR(11)  NOT NULL,
    nome     VARCHAR(100) DEFAULT NULL,
    telefone VARCHAR(15)  DEFAULT NULL,
    CONSTRAINT pk_cliente PRIMARY KEY (cpf)
);

-- ---------------------------------------------------------------------
-- 6. PRODUTO  -  itens vendidos pela loja
--    CHECK garante que preco_base nao seja negativo
-- ---------------------------------------------------------------------
CREATE TABLE produto (
    codigo      VARCHAR(20)   NOT NULL,
    nome        VARCHAR(100)  DEFAULT NULL,
    preco_base  DECIMAL(10,2) DEFAULT 0.00,
    CONSTRAINT pk_produto        PRIMARY KEY (codigo),
    CONSTRAINT chk_produto_preco CHECK (preco_base >= 0)
);

-- ---------------------------------------------------------------------
-- 7. DEPARTAMENTO  -  setores/categorias do mercado
--    UNIQUE em "categoria" evita duplicidade de nomes de setor
-- ---------------------------------------------------------------------
CREATE TABLE departamento (
    codigo    VARCHAR(20)  NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    CONSTRAINT pk_departamento  PRIMARY KEY (codigo),
    CONSTRAINT uq_dep_categoria UNIQUE (categoria)
);

-- ---------------------------------------------------------------------
-- 8. DEPARTAMENTO_PRODUTO  -  N:N entre departamento e produto
-- ---------------------------------------------------------------------
CREATE TABLE departamento_produto (
    cod_departamento VARCHAR(20) NOT NULL,
    cod_produto      VARCHAR(20) NOT NULL,
    CONSTRAINT pk_departamento_produto PRIMARY KEY (cod_departamento, cod_produto),
    CONSTRAINT fk_dp_departamento      FOREIGN KEY (cod_departamento)
        REFERENCES departamento(codigo)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_dp_produto           FOREIGN KEY (cod_produto)
        REFERENCES produto(codigo)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 9. FORNECEDOR  -  empresas que entregam produtos
-- ---------------------------------------------------------------------
CREATE TABLE fornecedor (
    cnpj   VARCHAR(14)  NOT NULL,
    nome   VARCHAR(100) DEFAULT NULL,
    rua    VARCHAR(100) DEFAULT NULL,
    numero VARCHAR(10)  DEFAULT NULL,
    cep    CHAR(8)      DEFAULT NULL,
    CONSTRAINT pk_fornecedor       PRIMARY KEY (cnpj),
    CONSTRAINT chk_fornecedor_cnpj CHECK (LENGTH(cnpj) = 14)
);

-- ---------------------------------------------------------------------
-- 10. FORNECEDOR_TELEFONE  -  multivalorado
-- ---------------------------------------------------------------------
CREATE TABLE fornecedor_telefone (
    cnpj_fornecedor VARCHAR(14) NOT NULL,
    telefone        VARCHAR(15) NOT NULL,
    CONSTRAINT pk_forn_tel    PRIMARY KEY (cnpj_fornecedor, telefone),
    CONSTRAINT fk_forntel_for FOREIGN KEY (cnpj_fornecedor)
        REFERENCES fornecedor(cnpj)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 11. ENTREGA  -  produto entregue por fornecedor a uma filial
--     numero_entrega usa AUTO_INCREMENT (sequencia automatica)
-- ---------------------------------------------------------------------
CREATE TABLE entrega (
    numero_entrega INT          NOT NULL AUTO_INCREMENT,
    data_entrega   DATE         NOT NULL DEFAULT (CURRENT_DATE),
    quantidade     INT          NOT NULL DEFAULT 1,
    cnpj_fornecedor VARCHAR(14) NOT NULL,
    cod_produto     VARCHAR(20) NOT NULL,
    cnpj_filial     VARCHAR(14) NOT NULL,
    CONSTRAINT pk_entrega         PRIMARY KEY (numero_entrega),
    CONSTRAINT chk_entrega_qtd    CHECK (quantidade > 0),
    CONSTRAINT fk_ent_fornecedor  FOREIGN KEY (cnpj_fornecedor)
        REFERENCES fornecedor(cnpj)
        ON UPDATE CASCADE,
    CONSTRAINT fk_ent_produto     FOREIGN KEY (cod_produto)
        REFERENCES produto(codigo)
        ON UPDATE CASCADE,
    CONSTRAINT fk_ent_filial      FOREIGN KEY (cnpj_filial)
        REFERENCES filial(cnpj)
        ON UPDATE CASCADE
);

-- ---------------------------------------------------------------------
-- 12. VENDE  -  uma venda no caixa (NF-e)
--     cpf_cliente pode ser NULL  =  venda sem identificacao do cliente
--     ON DELETE SET NULL: ao apagar o cliente preservamos o historico
-- ---------------------------------------------------------------------
CREATE TABLE vende (
    nfe             VARCHAR(44) NOT NULL,
    data_venda      DATE        NOT NULL DEFAULT (CURRENT_DATE),
    pagamento       VARCHAR(20) NOT NULL DEFAULT 'dinheiro',
    matricula_func  VARCHAR(20) NOT NULL,
    cpf_cliente     VARCHAR(11) DEFAULT NULL,
    CONSTRAINT pk_vende        PRIMARY KEY (nfe),
    CONSTRAINT chk_vende_pgto  CHECK (pagamento IN ('dinheiro','cartao','pix','boleto')),
    CONSTRAINT fk_vende_func   FOREIGN KEY (matricula_func)
        REFERENCES funcionario(matricula)
        ON UPDATE CASCADE,
    CONSTRAINT fk_vende_cli    FOREIGN KEY (cpf_cliente)
        REFERENCES cliente(cpf)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- ---------------------------------------------------------------------
-- 13. VENDE_PRODUTO  -  itens de cada venda (N:N)
-- ---------------------------------------------------------------------
CREATE TABLE vende_produto (
    nfe         VARCHAR(44) NOT NULL,
    cod_produto VARCHAR(20) NOT NULL,
    quantidade  INT         NOT NULL DEFAULT 1,
    CONSTRAINT pk_vende_produto    PRIMARY KEY (nfe, cod_produto),
    CONSTRAINT chk_vp_quantidade   CHECK (quantidade > 0),
    CONSTRAINT fk_vp_vende         FOREIGN KEY (nfe)
        REFERENCES vende(nfe)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_vp_produto       FOREIGN KEY (cod_produto)
        REFERENCES produto(codigo)
        ON UPDATE CASCADE
);

-- =====================================================================
-- TABELAS DE APOIO  (Etapa 05)
-- =====================================================================

-- ---------------------------------------------------------------------
-- 14. LOG_ALTERACOES  -  alimentada por triggers
-- ---------------------------------------------------------------------
CREATE TABLE log_alteracoes (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    tabela      VARCHAR(50)  NOT NULL,
    tipo_evento VARCHAR(50)  NOT NULL,
    descricao   VARCHAR(500) NOT NULL,
    data_hora   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 15. RESUMO_VENDAS_FUNCIONARIO  -  alimentada pelo procedimento com CURSOR
-- ---------------------------------------------------------------------
CREATE TABLE resumo_vendas_funcionario (
    matricula     VARCHAR(20)  NOT NULL,
    nome          VARCHAR(100) NOT NULL,
    total_vendas  INT          NOT NULL,
    valor_total   DECIMAL(10,2) NOT NULL,
    classificacao VARCHAR(20)  NOT NULL,
    data_geracao  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_resumo_func PRIMARY KEY (matricula)
);
