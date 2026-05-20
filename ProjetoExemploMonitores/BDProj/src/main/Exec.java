package main;

import dao.DepartamentoDAO;
import dao.CursoDAO;
import model.Departamento;
import model.Curso;

import java.util.List;

public class Exec {
    public static void main(String[] args) {
        DepartamentoDAO deptoDAO = new DepartamentoDAO();
        CursoDAO cursoDAO = new CursoDAO();

        try {
            
            cursoDAO.inserir(new Curso(6, "GTI", "Tecnólogo em Gestão da Informação", 1));
            
            System.out.println("Cursos cadastrados:");
            List<Curso> cursos = cursoDAO.listar();
            cursos.forEach(c -> System.out.println(c.getCodcurso() + " - " + c.getNome()));
            


            System.out.println("\nDepartamentos cadastrados:");
            List<Departamento> departamentos = deptoDAO.listar();
            departamentos.forEach(d -> System.out.println(d.getCoddepto() + " - " + d.getNome()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

