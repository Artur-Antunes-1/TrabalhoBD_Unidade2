create database bdproj;
use  bdproj;

CREATE TABLE Departamento (
    coddepto int PRIMARY KEY AUTO_INCREMENT,
    nome varchar(100) not null
);

CREATE TABLE Curso (
    codcurso int PRIMARY KEY,
    nome varchar(100) not null,
    descricao varchar(255),
    coddepto int not null,
    CONSTRAINT fk_curso_depto FOREIGN KEY (coddepto)
        REFERENCES departamento (coddepto)
);

CREATE TABLE Disciplina (
    coddisc int PRIMARY KEY,
    nome varchar(100) not null,
    ch int not null,
    codcurso int not null,
    CONSTRAINT fk_disciplina_curso FOREIGN KEY (codcurso)
        REFERENCES curso (codcurso)
);

CREATE TABLE Aluno (
    matricula int PRIMARY KEY,
    nome varchar(100) not null,
    dtnasc date not null,
    dtmatricula date not null,
    codcurso int not null,
    CONSTRAINT fk_aluno_curso FOREIGN KEY (codcurso)
        REFERENCES curso (codcurso)
);
CREATE TABLE Aluno_telefone (
    Matricula int not null,
    telefone varchar(20) not null,
    PRIMARY KEY (matricula, telefone),
    CONSTRAINT fk_aluno_tel FOREIGN KEY (matricula)
        REFERENCES aluno (matricula)
);

CREATE TABLE DiscCurso (
    coddisc int not null,
    matricula int not null,
    ano int not null,
    semestre int not null,
    PRIMARY KEY (coddisc, matricula),
    CONSTRAINT fk_dc_disc FOREIGN KEY (coddisc)
        REFERENCES disciplina (coddisc),
    CONSTRAINT fk_dc_aluno FOREIGN KEY (matricula)
        REFERENCES aluno (matricula),
    CONSTRAINT chk_semestre CHECK (semestre in (1,2))
);


INSERT INTO Departamento (Nome) VALUES
('Computação'),
('Engenharia de Software'),
('Sistemas de Informação'),
('Redes de Computadores'),
('Ciência de Dados');
-- Curso
INSERT INTO Curso (CodCurso, Nome, Descricao, CodDepto) VALUES
(1, 'Ciência da Computação', 'Bacharelado em Computação', 1),
(2, 'Qualidade de Software', 'Bacharelado em Eng. Software', 2),
(3, 'Gerenciamento de Sistemas Inteligentes', 'Bacharelado em Sistemas de Informação', 3),
(4, 'Cibersegurança', 'Tecnólogo em Redes de Computadores', 4),
(5, 'Banco de Dados e IA', 'Bacharelado em Ciência de Dados', 5);
-- Disciplina
INSERT INTO Disciplina (CodDisc, Nome, CH, CodCurso) VALUES (1, 'Algoritmos e Programação', 60, 1),
(2, 'Estruturas de Dados', 80, 1),
(3, 'Banco de Dados Relacionais', 100, 2),
(4, 'Redes de Computadores I', 90, 4),
(5, 'Aprendizado de Máquina', 120, 5);
-- Aluno
INSERT INTO Aluno (Matricula, Nome, DtNasc, DtMatricula, CodCurso) VALUES
(1, 'Ana Silva', '2000-05-15', '2020-02-10', 1),
(2, 'Bruno Souza', '1999-08-22', '2019-03-05', 2),
(3, 'Carla Mendes', '2001-01-12', '2021-01-20', 3),
(4, 'Daniel Oliveira', '1998-07-30', '2018-02-15', 4),
(5, 'Eduardo Santos', '2002-11-10', '2022-08-01', 5),
(6, 'Luigi Nascimento', '1997-05-03', '2018-03-15', 4);
-- Aluno_telefone
INSERT INTO Aluno_telefone (Matricula, Telefone) VALUES
(1, '81988887777'),
(2, '81991112222'),
(3, '81992223333'),
(4, '81993334444'),
(5, '81994445555'),
(1, '81988885432');
-- DiscCurso
INSERT INTO DiscCurso (CodDisc, Matricula, Ano, Semestre) VALUES 
(1, 1, 2023, 1),
(2, 2, 2023, 1),
(3, 3, 2023, 2),
(4, 4, 2022, 2),
(5, 5, 2022, 1),
(4, 3, 2024, 2),
(5, 4, 2024, 1);

