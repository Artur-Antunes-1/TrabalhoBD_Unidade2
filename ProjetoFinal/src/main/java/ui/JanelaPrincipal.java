package ui;

import javax.swing.*;
import java.awt.*;

/**
 * Janela principal da aplicacao.
 * Reune todos os paineis em abas (JTabbedPane).
 */
public class JanelaPrincipal extends JFrame {

    public JanelaPrincipal() {
        super("Supermercado BD - Trabalho de Banco de Dados (2026.1)");

        JTabbedPane abas = new JTabbedPane();

        // ---- Abas de CRUD (Etapa 06: 5 tabelas com CRUD) ------------
        abas.addTab("Clientes",     new PainelClientes());
        abas.addTab("Produtos",     new PainelProdutos());
        abas.addTab("Funcionarios", new PainelFuncionarios());
        abas.addTab("Filiais",      new PainelFiliais());
        abas.addTab("Vendas",       new PainelVendas());

        // ---- Abas de relatorio / Etapas 04 e 05 ---------------------
        abas.addTab("Consultas/Views",       new PainelConsultas());
        abas.addTab("Funcoes/Procedimentos", new PainelFuncoesProcedimentos());
        abas.addTab("Logs (Triggers)",       new PainelLogs());

        // ---- Dashboard (extra +0,5) ---------------------------------
        abas.addTab("Dashboard", new PainelDashboard());

        setContentPane(abas);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
