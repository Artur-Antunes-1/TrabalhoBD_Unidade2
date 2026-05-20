# Supermercado BD - Trabalho de Banco de Dados (Unidade 02)

**Disciplina:** Banco de Dados 2026.1
**Grupo:** Artur Antunes, Pedro Ferraz, Ricardo Machado, Victor Uen

Aplicação **Java Desktop (Swing) + MySQL** que consolida as **Etapas 04, 05 e 06** do projeto. Toda a integração com o banco é feita com **JDBC puro + PreparedStatement** — sem ORM.

---

## 1. Estrutura do Projeto

```
ProjetoFinal/
├── README.md                     ← este arquivo
├── GUIA_DE_ESTUDO.md             ← guia para a apresentação
├── pom.xml                       ← build Maven (mysql-connector-j + jfreechart)
├── lib/                          ← JARs avulsos (caso não use Maven)
│
├── sql/
│   ├── 01_criar_tabelas.sql              ← Etapa 03 (DDL)
│   ├── 02_inserir_dados.sql              ← Etapa 03 (DML, 30+ tuplas/tabela)
│   ├── 03_consultas_views_indices.sql    ← Etapa 04
│   └── 04_funcoes_procedimentos_triggers.sql ← Etapa 05
│
└── src/main/java/
    ├── Principal.java                    ← main da aplicação
    ├── conexao/
    │   └── ConexaoBanco.java             ← ÚNICO ponto de conexão JDBC
    ├── modelo/                           ← POJOs (Cliente, Produto, ...)
    ├── dao/                              ← DAOs com SQL explícito
    │   ├── ClienteDAO.java
    │   ├── ProdutoDAO.java
    │   ├── FilialDAO.java
    │   ├── FuncionarioDAO.java
    │   ├── VendaDAO.java
    │   └── RelatoriosDAO.java            ← consultas / views / funcs / procs / logs
    └── ui/                               ← janelas Swing
        ├── JanelaPrincipal.java          ← tela com abas
        ├── PainelClientes.java           ← CRUD Cliente
        ├── PainelProdutos.java           ← CRUD Produto
        ├── PainelFuncionarios.java       ← CRUD Funcionário
        ├── PainelFiliais.java            ← CRUD Filial
        ├── PainelVendas.java             ← CRUD Venda + itens
        ├── PainelConsultas.java          ← Etapa 04 (consultas + views)
        ├── PainelFuncoesProcedimentos.java ← Etapa 05 (functions + procs)
        ├── PainelLogs.java               ← logs gerados pelos triggers
        └── PainelDashboard.java          ← 5 gráficos + indicadores
```

---

## 2. Como executar

### 2.1  Pré-requisitos

| Software        | Versão       |
|-----------------|--------------|
| Java JDK        | 17 ou 21     |
| MySQL Server    | 8.0 ou superior |
| Maven *(opcional)* | 3.8+      |

### 2.2  Criar o banco (uma vez)

Abra o MySQL Workbench (ou `mysql -u root -p` no terminal) e rode os scripts **na ordem**:

```sql
SOURCE C:/.../ProjetoFinal/sql/01_criar_tabelas.sql;
SOURCE C:/.../ProjetoFinal/sql/02_inserir_dados.sql;
SOURCE C:/.../ProjetoFinal/sql/03_consultas_views_indices.sql;
SOURCE C:/.../ProjetoFinal/sql/04_funcoes_procedimentos_triggers.sql;
```

### 2.3  Conferir credenciais

Abra `src/main/java/conexao/ConexaoBanco.java` e ajuste se necessário:

```java
private static final String URL_BANCO = "jdbc:mysql://localhost:3306/supermercado";
private static final String USUARIO   = "root";
private static final String SENHA     = "root";
```

### 2.4  Rodar a aplicação

**Com Maven:**
```bash
mvn -q exec:java -Dexec.mainClass=Principal
```

**Com IDE (IntelliJ / Eclipse / VS Code):**
- Importe como projeto Maven
- Rode `Principal.java`

**Sem Maven (compilando à mão com os JARs em `lib/`):**
```bash
javac -d out -cp "lib/*" src/main/java/**/*.java src/main/java/Principal.java
java  -cp "out;lib/*" Principal
```

---

## 3. Funcionalidades por aba

| Aba                       | O que faz                                                                                  |
|---------------------------|--------------------------------------------------------------------------------------------|
| Clientes                  | CRUD da tabela `cliente`                                                                   |
| Produtos                  | CRUD da tabela `produto` *(editar preço dispara o trigger de log)*                         |
| Funcionários              | CRUD da tabela `funcionario` (com filial e supervisor)                                     |
| Filiais                   | CRUD da tabela `filial`                                                                    |
| Vendas                    | CRUD da tabela `vende` *(inserir dispara o trigger de log)* + adicionar itens              |
| Consultas/Views           | Executa as 4 consultas e 2 views da Etapa 04                                               |
| Funções/Procedimentos     | Chama `fn_total_venda`, `fn_categoria_funcionario`, `pr_atualizar_preco_departamento`, `pr_gerar_resumo_funcionarios` |
| Logs (Triggers)           | Mostra a tabela `log_alteracoes` (alimentada pelos dois triggers)                          |
| Dashboard                 | Indicadores (totais, média) + 5 gráficos (barras, pizza, linha)                            |

---

## 4. Atendimento aos requisitos do Módulo 02

| Etapa | Item                                  | Onde está                                                        |
|-------|---------------------------------------|------------------------------------------------------------------|
| 04    | 4 consultas SQL                       | `sql/03_...sql` + `RelatoriosDAO.consultaXxx()` + aba Consultas  |
| 04    | 2 views                               | `sql/03_...sql` + aba Consultas (botões "View vw_...")           |
| 04    | 2 índices                             | `sql/03_...sql` (justificativas no comentário)                   |
| 05    | 2 funções (1 com IF/ELSE)             | `sql/04_...sql` + `RelatoriosDAO.chamarFuncao*()` + aba Funções  |
| 05    | 2 procedimentos (1 UPDATE, 1 CURSOR)  | `sql/04_...sql` + `RelatoriosDAO.chamarProcedimento*()`          |
| 05    | 2 triggers (1 atualiza log)           | `sql/04_...sql` + visualização na aba "Logs"                     |
| 06    | CRUD ≥ 4 tabelas                      | 5 abas CRUD (Cliente, Produto, Funcionário, Filial, Venda)       |
| 06    | Visualizar 1 func + 1 proc + 1 trigger | Aba "Funções/Procedimentos" + aba "Logs"                        |
| 06    | Consultas/views acessíveis na UI      | Aba "Consultas/Views"                                            |
| 06    | Dashboard (extra)                     | Aba "Dashboard" - 5 gráficos + 5 indicadores                     |

---

## 5. O que **NÃO** foi usado (para deixar claro)

- ❌ Hibernate, JPA, MyBatis, Spring Data, ou qualquer ORM
- ❌ Frameworks de mapeamento objeto-relacional
- ❌ Geradores de CRUD automáticos
- ❌ Tomcat, Spring Boot ou qualquer container web

Apenas:
- ✔ `java.sql.*` (JDBC padrão da linguagem)
- ✔ `mysql-connector-j` (driver JDBC oficial do MySQL)
- ✔ Swing (parte da JRE) para a interface
- ✔ `JFreeChart` apenas para desenhar os gráficos do dashboard
