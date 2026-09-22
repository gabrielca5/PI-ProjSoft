package com.example.cursos;

import com.example.cursos.model.Aluno;
import com.example.cursos.model.Curso;
import com.example.cursos.model.Professor;
import com.example.cursos.repository.AlunoRepository;
import com.example.cursos.repository.CursoRepository;
import com.example.cursos.repository.MateriaRepository;
import com.example.cursos.repository.MatriculaRepository;
import com.example.cursos.repository.ProfessorRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Teste de integração das relações Materia -> Curso/Professor e Matricula -> Curso/Aluno
// (rotas "flat": /materias e /matriculas, nenhuma delas aninhada em /cursos)
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RelacionamentosIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("cursos_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limparBanco() {
        matriculaRepository.deleteAll();
        materiaRepository.deleteAll();
        cursoRepository.deleteAll();
        professorRepository.deleteAll();
        alunoRepository.deleteAll();
    }

    @Test
    void deveCriarProfessor() throws Exception {
        String body = objectMapper.writeValueAsString(new NovoProfessorPayload("Ana", "ana@email.com", "Java"));

        mockMvc.perform(post("/professores")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Ana")))
                .andExpect(jsonPath("$.especialidade", is("Java")));
    }

    @Test
    void deveCriarAluno() throws Exception {
        String body = objectMapper.writeValueAsString(new NovoAlunoPayload("Maria", "maria@email.com", "2024001"));

        mockMvc.perform(post("/alunos")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.matricula", is("2024001")));
    }

    @Test
    void deveCriarMateriaVinculadaAoCursoEProfessor() throws Exception {
        Curso curso = cursoRepository.save(new Curso("Spring Boot", "desc", 40, BigDecimal.TEN));
        Professor professor = professorRepository.save(new Professor("Ana", "ana@email.com", "Java"));

        String body = objectMapper.writeValueAsString(new NovaMateriaPayload("JPA", 20, professor.getId(), curso.getId()));

        mockMvc.perform(post("/materias")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("JPA")))
                .andExpect(jsonPath("$.professor.nome", is("Ana")));

        mockMvc.perform(get("/materias").param("cursoId", curso.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void deveRetornar404AoCriarMateriaComCursoInexistente() throws Exception {
        Professor professor = professorRepository.save(new Professor("Ana", "ana@email.com", "Java"));
        String body = objectMapper.writeValueAsString(new NovaMateriaPayload("JPA", 20, professor.getId(), 9999L));

        mockMvc.perform(post("/materias")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveMatricularAlunoNoCurso() throws Exception {
        Curso curso = cursoRepository.save(new Curso("Spring Boot", "desc", 40, BigDecimal.TEN));
        Aluno aluno = alunoRepository.save(new Aluno("Maria", "maria@email.com", "2024001"));

        String body = objectMapper.writeValueAsString(new NovaMatriculaPayload(curso.getId(), aluno.getId()));

        mockMvc.perform(post("/matriculas")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aluno.nome", is("Maria")));

        mockMvc.perform(get("/matriculas").param("cursoId", curso.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void deveRetornar404AoMatricularAlunoInexistente() throws Exception {
        Curso curso = cursoRepository.save(new Curso("Spring Boot", "desc", 40, BigDecimal.TEN));
        String body = objectMapper.writeValueAsString(new NovaMatriculaPayload(curso.getId(), 9999L));

        mockMvc.perform(post("/matriculas")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound());
    }

    private record NovoProfessorPayload(String nome, String email, String especialidade) {
    }

    private record NovoAlunoPayload(String nome, String email, String matricula) {
    }

    private record NovaMateriaPayload(String nome, Integer cargaHoraria, Long professorId, Long cursoId) {
    }

    private record NovaMatriculaPayload(Long cursoId, Long alunoId) {
    }
}
