# Supermercado BD - Trabalho de Banco de Dados

**Disciplina:** Banco de Dados 2026.1
**Grupo:** Artur Antunes, Pedro Ferraz, Ricardo Machado, Victor Uen

Aplicação **Java Desktop (Swing) + MySQL** com integração via **JDBC puro + PreparedStatement** (sem ORM).

---

## 1. Estrutura

```
Projeto_Etapa05/
├── README.md                     ← este arquivo
├── pom.xml                       ← build Maven (mysql-connector-j)
│
├── sql/
│   ├── 01_criar_tabelas.sql              ← DDL (15 tabelas)
│   ├── 02_inserir_dados.sql              ← DML (30+ tuplas/tabela)
│   ├── 03_consultas_views_indices.sql    ← 4 consultas + 2 views + 2 índices
│   └── 04_funcoes_procedimentos_triggers.sql ← 2 funções + 2 procs + 2 triggers
│
└── src/main/java/
    ├── Principal.java                    ← main da aplicação
    ├── conexao/
    │   └── ConexaoBanco.java             ← ÚNICO ponto de conexão JDBC
    ├── modelo/                           ← POJOs (LogAlteracao, ResumoFuncionario)
    ├── dao/
    │   └── RelatoriosDAO.java            ← consultas, views, funcs, procs, logs
    └── ui/
        ├── JanelaPrincipal.java          ← tela com abas
        ├── TabelaUtil.java
        ├── PainelConsultas.java          ← 4 consultas + 2 views
        ├── PainelFuncoesProcedimentos.java ← 2 funções + 2 procedimentos
        └── PainelLogs.java               ← logs gerados pelos triggers
```

---

## 2. Como executar

### 2.1  Pré-requisitos

| Software         | Versão           |
|------------------|------------------|
| Java JDK         | 17 ou 21         |
| MySQL Server     | 8.0 ou superior  |
| Maven *(opcional)* | 3.8+           |

### 2.2  Criar o banco (uma vez)

Abra o MySQL Workbench (ou `mysql -u root -p` no terminal) e rode os scripts **na ordem**:

```sql
SOURCE C:/.../Projeto_Etapa05/sql/01_criar_tabelas.sql;
SOURCE C:/.../Projeto_Etapa05/sql/02_inserir_dados.sql;
SOURCE C:/.../Projeto_Etapa05/sql/03_consultas_views_indices.sql;
SOURCE C:/.../Projeto_Etapa05/sql/04_funcoes_procedimentos_triggers.sql;
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

---

## 3. Abas da aplicação

| Aba                       | O que faz                                                                                  |
|---------------------------|--------------------------------------------------------------------------------------------|
| Consultas/Views           | Executa as 4 consultas e 2 views (botões individuais; a consulta de período tem filtro de data) |
| Funções/Procedimentos     | Chama `fn_total_venda`, `fn_categoria_funcionario`, `pr_atualizar_preco_departamento`, `pr_gerar_resumo_funcionarios` |
| Logs (Triggers)           | Mostra a tabela `log_alteracoes` (alimentada pelos dois triggers)                          |

---

## 4. Roteiro sugerido de teste

1. **Abrir a aba "Consultas/Views"** e clicar em cada um dos 6 botões para confirmar que as 4 consultas e as 2 views retornam dados.
2. **Abrir a aba "Funções/Procedimentos"**:
   - Clicar em `fn_total_venda(nfe) ->` com o valor padrão `NFE0000000001` — deve retornar `R$ 21,95` na área "Saída das funções".
   - Clicar em `fn_categoria_funcionario(matricula) ->` com `FUNC004` — deve retornar `Prata` (3 vendas).
   - Clicar em `pr_atualizar_preco_departamento(dept, %)` com `DEPT05` e `10` — atualiza o preço dos produtos do departamento de Bebidas em 10% (isso também dispara o trigger `tg_log_alteracao_preco`, populando a aba Logs).
   - Clicar em `pr_gerar_resumo_funcionarios() [CURSOR]` — popula a tabela "Resumo gerado pelo CURSOR" abaixo, com uma linha por funcionário.
3. **Abrir a aba "Logs"** e clicar em "Atualizar logs" — devem aparecer as linhas geradas pelo trigger `tg_log_alteracao_preco` quando você rodou o procedimento de atualização de preço no passo anterior.
4. Para ver o **trigger `tg_log_nova_venda`** em ação, insira manualmente uma venda pelo MySQL Workbench:
   ```sql
   INSERT INTO vende (nfe, data_venda, pagamento, matricula_func, cpf_cliente)
   VALUES ('NFE9999999999', CURRENT_DATE, 'pix', 'FUNC004', '20000000001');
   ```
   Volte na aba Logs, clique em "Atualizar logs" e a nova linha aparece.

---

## 5. Bibliotecas usadas

- `java.sql.*` — JDBC padrão da linguagem.
- `mysql-connector-j` (versão 8.4.0) — driver JDBC oficial do MySQL.
- Swing — parte da JRE, usado para a interface.

Nenhum ORM (Hibernate, JPA, MyBatis, etc.), nenhum gerador de CRUD, nenhum container web.
