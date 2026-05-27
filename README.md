# Supermercado BD — Trabalho de Banco de Dados (Unidade 02)

> Aplicação **Java Desktop (Swing) + MySQL** que consolida as **Etapas 04, 05 e 06** do trabalho prático. JDBC puro com `PreparedStatement` — **sem ORM**, sem geradores de CRUD. O foco é explicitar o SQL, justificar cada índice/view/função/procedure/trigger e visualizar tudo em uma UI didática.

**Disciplina:** Banco de Dados — 2026.1
**Grupo:** Artur Antunes · Pedro Ferraz · Ricardo Machado · Victor Uen
**SGBD:** MySQL 8.0+

> 📁 **Código-fonte:** está todo em [`ProjetoFinal/`](ProjetoFinal/).

---

## Sumário

1. [Sobre o projeto](#1-sobre-o-projeto)
2. [Estrutura do projeto](#2-estrutura-do-projeto)
3. [Como executar](#3-como-executar)
4. [Funcionalidades por aba](#4-funcionalidades-por-aba)
5. [Atendimento aos requisitos do Módulo 02](#5-atendimento-aos-requisitos-do-módulo-02)
6. [O que NÃO foi usado](#6-o-que-não-foi-usado)
7. [Stack técnica](#7-stack-técnica)

---

## 1. Sobre o projeto

Modela uma rede fictícia de supermercados com **13 tabelas** (filial, funcionário, dependente, cliente, produto, departamento, fornecedor, entrega, venda + tabelas associativas e multivaloradas) e expõe toda a operação em uma janela Swing com **9 abas**: 5 CRUDs, consultas/views, funções/procedimentos, logs e um dashboard com 5 gráficos.

A camada de persistência é DAO clássico — uma classe por entidade, SQL escrito à mão, conexão centralizada em [ConexaoBanco.java](ProjetoFinal/src/main/java/conexao/ConexaoBanco.java). Cada elemento avançado (índice, view, função, procedure, trigger) traz justificativa em comentário no próprio script SQL e é demonstrável ao vivo pela UI.

---

## 2. Estrutura do projeto

```
ProjetoFinal/
├── README.md                     ← versão idêntica deste arquivo dentro da pasta do projeto
├── rodar.bat                     ← atalho: compila e roda no Windows
├── pom.xml                       ← build Maven (mysql-connector-j + JFreeChart)
├── lib/                          ← JARs avulsos (caso não use Maven)
│
├── sql/
│   ├── 01_criar_tabelas.sql                   ← Etapa 03 (DDL)
│   ├── 02_inserir_dados.sql                   ← Etapa 03 (DML, 30+ tuplas/tabela)
│   ├── 03_consultas_views_indices.sql         ← Etapa 04
│   └── 04_funcoes_procedimentos_triggers.sql  ← Etapa 05
│
└── src/main/java/
    ├── Principal.java                    ← main da aplicação
    ├── conexao/ConexaoBanco.java         ← ÚNICO ponto de conexão JDBC
    ├── modelo/                           ← POJOs (Cliente, Produto, …)
    ├── dao/                              ← DAOs com SQL explícito
    │   ├── ClienteDAO.java
    │   ├── ProdutoDAO.java
    │   ├── FilialDAO.java
    │   ├── FuncionarioDAO.java
    │   ├── VendaDAO.java
    │   └── RelatoriosDAO.java            ← consultas, views, funcs, procs e logs
    └── ui/                               ← janelas Swing
        ├── JanelaPrincipal.java          ← janela com as 9 abas
        ├── PainelClientes.java
        ├── PainelProdutos.java
        ├── PainelFuncionarios.java
        ├── PainelFiliais.java
        ├── PainelVendas.java             ← CRUD Venda + itens (vende_produto)
        ├── PainelConsultas.java          ← Etapa 04
        ├── PainelFuncoesProcedimentos.java← Etapa 05
        ├── PainelLogs.java               ← logs gerados pelo trigger de auditoria
        └── PainelDashboard.java          ← 5 gráficos + indicadores
```

---

## 3. Como executar

### 3.1  Pré-requisitos

| Software            | Versão       |
|---------------------|--------------|
| Java JDK            | 17 ou 21     |
| MySQL Server        | 8.0+         |
| Maven *(opcional)*  | 3.8+         |

### 3.2  Criar o banco (uma vez)

No MySQL Workbench ou via `mysql -u root -p`, rode os scripts **na ordem**:

```sql
SOURCE C:/.../ProjetoFinal/sql/01_criar_tabelas.sql;
SOURCE C:/.../ProjetoFinal/sql/02_inserir_dados.sql;
SOURCE C:/.../ProjetoFinal/sql/03_consultas_views_indices.sql;
SOURCE C:/.../ProjetoFinal/sql/04_funcoes_procedimentos_triggers.sql;
```

### 3.3  Conferir credenciais

Abra [ConexaoBanco.java](ProjetoFinal/src/main/java/conexao/ConexaoBanco.java) e ajuste se necessário:

```java
private static final String URL_BANCO = "jdbc:mysql://localhost:3306/supermercado";
private static final String USUARIO   = "root";
private static final String SENHA     = "root";
```

### 3.4  Rodar a aplicação

**Atalho no Windows** (compila e executa em um clique):
```bat
cd ProjetoFinal
rodar.bat
```

**Com Maven:**
```bash
cd ProjetoFinal
mvn -q exec:java -Dexec.mainClass=Principal
```

**Com IDE (IntelliJ / Eclipse / VS Code):**
- Importe a pasta `ProjetoFinal/` como projeto Maven
- Execute `Principal.java`

**Sem Maven (compilação manual com os JARs em `lib/`):**
```bash
cd ProjetoFinal
javac -d out -cp "lib/*" src/main/java/**/*.java
java  -cp "out;lib/*" Principal
```

---

## 4. Funcionalidades por aba

| Aba                       | O que faz                                                                                                       |
|---------------------------|-----------------------------------------------------------------------------------------------------------------|
| **Clientes**              | CRUD da tabela `cliente`                                                                                        |
| **Produtos**              | CRUD da tabela `produto` — *editar o preço dispara os dois triggers da Etapa 05: o `tg_limita_variacao_preco` valida e o `tg_log_alteracao_preco` registra no log* |
| **Funcionários**          | CRUD da tabela `funcionario` (com filial e supervisor)                                                          |
| **Filiais**               | CRUD da tabela `filial`                                                                                         |
| **Vendas**                | CRUD da tabela `vende` + adicionar itens (`vende_produto`)                                                      |
| **Consultas/Views**       | Executa as 4 consultas e as 2 views da Etapa 04                                                                 |
| **Funções/Procedimentos** | Chama `fn_total_venda`, `fn_porte_venda`, `pr_atualizar_preco_departamento`, `pr_promocao_produtos_parados`     |
| **Logs (Triggers)**       | Mostra a tabela `log_alteracoes`, alimentada pelo `tg_log_alteracao_preco`                                      |
| **Dashboard**             | 5 indicadores + 5 gráficos (barras, pizza, linha) com JFreeChart                                                |

---

## 5. Atendimento aos requisitos do Módulo 02

| Etapa | Item                                       | Onde está                                                              |
|-------|--------------------------------------------|------------------------------------------------------------------------|
| 04    | 4 consultas SQL                            | [03_consultas_views_indices.sql](ProjetoFinal/sql/03_consultas_views_indices.sql) + `RelatoriosDAO.consultaXxx()` + aba *Consultas* |
| 04    | 2 views                                    | [03_consultas_views_indices.sql](ProjetoFinal/sql/03_consultas_views_indices.sql) + aba *Consultas* (botões "View vw_…")            |
| 04    | 2 índices                                  | [03_consultas_views_indices.sql](ProjetoFinal/sql/03_consultas_views_indices.sql) (justificativas em comentário)                    |
| 05    | 2 funções (1 com IF/ELSE)                  | [04_funcoes_procedimentos_triggers.sql](ProjetoFinal/sql/04_funcoes_procedimentos_triggers.sql) + aba *Funções/Procedimentos*       |
| 05    | 2 procedures (1 com UPDATE em massa, 1 com CURSOR) | [04_funcoes_procedimentos_triggers.sql](ProjetoFinal/sql/04_funcoes_procedimentos_triggers.sql) + aba *Funções/Procedimentos*       |
| 05    | 2 triggers (1 valida via `SIGNAL`, 1 alimenta log de auditoria) | [04_funcoes_procedimentos_triggers.sql](ProjetoFinal/sql/04_funcoes_procedimentos_triggers.sql) — demo na aba *Produtos* + *Logs*  |
| 06    | CRUD para ≥ 4 tabelas                      | 5 abas CRUD (Cliente, Produto, Funcionário, Filial, Venda)             |
| 06    | Visualizar ≥ 1 função + 1 procedure + 1 trigger | Aba *Funções/Procedimentos* + aba *Logs* + aba *Produtos*              |
| 06    | Consultas/views acessíveis na UI           | Aba *Consultas/Views*                                                  |
| 06    | Dashboard (extra)                          | Aba *Dashboard* — 5 gráficos + 5 indicadores                           |

> **Sobre os triggers (Etapa 05):** os dois triggers formam um par sobre a tabela `produto`. O `tg_limita_variacao_preco` (BEFORE UPDATE) **impede** alterações de preço acima de ±80% com `SIGNAL SQLSTATE '45000'` — rede de segurança contra erros de digitação. O `tg_log_alteracao_preco` (AFTER UPDATE) **registra** em `log_alteracoes` toda alteração que passou pela validação. Demonstra dois usos clássicos: validação de regra de negócio e auditoria.

---

## 6. O que **NÃO** foi usado

- ❌ Hibernate, JPA, MyBatis, Spring Data ou qualquer ORM
- ❌ Frameworks de mapeamento objeto-relacional
- ❌ Geradores de CRUD automáticos
- ❌ Tomcat, Spring Boot ou container web

Apenas:
- ✔ `java.sql.*` (JDBC padrão da linguagem)
- ✔ `mysql-connector-j` (driver JDBC oficial do MySQL)
- ✔ Swing (parte da JRE) para a interface
- ✔ `JFreeChart` apenas para os gráficos do dashboard

---

## 7. Stack técnica

| Camada       | Tecnologia                                              |
|--------------|---------------------------------------------------------|
| Banco        | MySQL 8.0+ (charset `utf8mb4`)                          |
| Driver       | `mysql-connector-j 8.4.0`                               |
| Linguagem    | Java 17+                                                |
| Persistência | JDBC puro (`PreparedStatement`, DAO por entidade)       |
| UI           | Swing (JRE)                                             |
| Gráficos     | JFreeChart 1.5.4                                        |
| Build        | Maven (`pom.xml`) — alternativa: `javac` + `lib/*.jar`  |
