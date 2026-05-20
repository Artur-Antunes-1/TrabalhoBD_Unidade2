package ui;

import javax.swing.*;

/**
 * Janela principal da aplicacao.
 * Reune todos os paineis em abas (JTabbedPane).
 */
public class JanelaPrincipal extends JFrame {

    public JanelaPrincipal() {
        super("Supermercado BD - Trabalho de Banco de Dados (2026.1)");

        JTabbedPane abas = new JTabbedPane();

        abas.addTab("Clientes",              new PainelClientes());
        abas.addTab("Produtos",              new PainelProdutos());
        abas.addTab("Funcionarios",          new PainelFuncionarios());
        abas.addTab("Filiais",               new PainelFiliais());
        abas.addTab("Vendas",                new PainelVendas());
        abas.addTab("Departamentos",         new PainelDepartamentos());
        abas.addTab("Departamento-Produto",  new PainelDepartamentoProduto());
        abas.addTab("Consultas/Views",       new PainelConsultas());
        abas.addTab("Funcoes/Procedimentos", new PainelFuncoesProcedimentos());
        abas.addTab("Logs (Triggers)",       new PainelLogs());

        setContentPane(abas);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
