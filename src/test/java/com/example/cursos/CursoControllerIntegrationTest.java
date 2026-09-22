package com.example.cursos;

import com.example.cursos.model.Curso;
import com.example.cursos.repository.CursoRepository;
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

// Teste de integração das 3 rotas pedidas: GET, POST e DELETE /cursos
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CursoControllerIntegrationTest {

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
    private ObjectMapper objectMapper;

    @BeforeEach
    void limparBanco() {
        cursoRepository.deleteAll();
    }

    @Test
    void deveListarApenasCursosNaoDeletados() throws Exception {
        cursoRepository.save(new Curso("Java Básico", "desc", 40, BigDecimal.TEN));
        Curso deletado = new Curso("Python Básico", "desc", 30, BigDecimal.ONE);
        deletado.setDeletado(true);
        cursoRepository.save(deletado);

        mockMvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Java Básico")));
    }

    @Test
    void deveFiltrarCursosPeloComecoDoNome() throws Exception {
        cursoRepository.save(new Curso("Java Básico", "desc", 40, BigDecimal.TEN));
        cursoRepository.save(new Curso("JavaScript", "desc", 20, BigDecimal.ONE));
        cursoRepository.save(new Curso("Python", "desc", 20, BigDecimal.ONE));

        mockMvc.perform(get("/cursos").param("nome", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deveCriarCurso() throws Exception {
        String body = objectMapper.writeValueAsString(new NovoCursoPayload("Spring Boot", "desc", 20, BigDecimal.valueOf(199.9)));

        mockMvc.perform(post("/cursos")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Spring Boot")))
                .andExpect(jsonPath("$.deletado", is(false)));
    }

    @Test
    void deveDeletarLogicamenteOCurso() throws Exception {
        Curso curso = cursoRepository.save(new Curso("Java", "desc", 40, BigDecimal.TEN));

        mockMvc.perform(delete("/cursos/" + curso.getId()))
                .andExpect(status().isNoContent());

        Curso atualizado = cursoRepository.findById(curso.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(atualizado.isDeletado());

        // não aparece mais na listagem
        mockMvc.perform(get("/cursos"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deveRetornar404AoDeletarCursoInexistente() throws Exception {
        mockMvc.perform(delete("/cursos/9999"))
                .andExpect(status().isNotFound());
    }

    private record NovoCursoPayload(String nome, String descricao, Integer cargaHoraria, BigDecimal preco) {
    }
}
