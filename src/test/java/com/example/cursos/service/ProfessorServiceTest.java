package com.example.cursos.service;

import com.example.cursos.dto.ProfessorRequestDTO;
import com.example.cursos.model.Professor;
import com.example.cursos.repository.ProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfessorServiceTest {

    @Mock
    private ProfessorRepository professorRepository;

    private ProfessorService professorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        professorService = new ProfessorService(professorRepository);
    }

    @Test
    void listar_retornaTodosProfessores() {
        List<Professor> esperado = List.of(new Professor("Ana", "ana@email.com", "Java"));
        when(professorRepository.findAll()).thenReturn(esperado);

        List<Professor> resultado = professorService.listar();

        assertThat(resultado).isEqualTo(esperado);
        verify(professorRepository).findAll();
    }

    @Test
    void criar_salvaEDevolveProfessorPersistido() {
        ProfessorRequestDTO dto = new ProfessorRequestDTO();
        dto.setNome("Ana");
        dto.setEmail("ana@email.com");
        dto.setEspecialidade("Java");

        Professor salvo = new Professor(dto.getNome(), dto.getEmail(), dto.getEspecialidade());
        salvo.setId(1L);
        when(professorRepository.save(any(Professor.class))).thenReturn(salvo);

        Professor resultado = professorService.criar(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Ana");
        assertThat(resultado.getEspecialidade()).isEqualTo("Java");
        verify(professorRepository).save(any(Professor.class));
    }
}
