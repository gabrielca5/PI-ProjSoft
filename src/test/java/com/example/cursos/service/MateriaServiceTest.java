package com.example.cursos.service;

import com.example.cursos.dto.MateriaRequestDTO;
import com.example.cursos.exception.RecursoNotFoundException;
import com.example.cursos.model.Curso;
import com.example.cursos.model.Materia;
import com.example.cursos.model.Professor;
import com.example.cursos.repository.CursoRepository;
import com.example.cursos.repository.MateriaRepository;
import com.example.cursos.repository.ProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MateriaServiceTest {

    @Mock
    private MateriaRepository materiaRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private ProfessorRepository professorRepository;

    private MateriaService materiaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        materiaService = new MateriaService(materiaRepository, cursoRepository, professorRepository);
    }

    @Test
    void listarPorCurso_retornaMateriasDoCurso() {
        List<Materia> esperado = List.of(new Materia("JPA", 20, null, null));
        when(materiaRepository.findByCursoId(1L)).thenReturn(esperado);

        List<Materia> resultado = materiaService.listarPorCurso(1L);

        assertThat(resultado).isEqualTo(esperado);
        verify(materiaRepository).findByCursoId(1L);
    }

    @Test
    void criar_salvaMateriaVinculadaAoCursoEProfessor() {
        Curso curso = new Curso("Java", "desc", 40, BigDecimal.TEN);
        curso.setId(1L);
        Professor professor = new Professor("Ana", "ana@email.com", "Java");
        professor.setId(2L);

        MateriaRequestDTO dto = new MateriaRequestDTO();
        dto.setNome("JPA");
        dto.setCargaHoraria(20);
        dto.setProfessorId(2L);
        dto.setCursoId(1L);

        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(professorRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(materiaRepository.save(any(Materia.class))).thenAnswer(inv -> inv.getArgument(0));

        Materia resultado = materiaService.criar(dto);

        assertThat(resultado.getNome()).isEqualTo("JPA");
        assertThat(resultado.getCurso()).isEqualTo(curso);
        assertThat(resultado.getProfessor()).isEqualTo(professor);
        verify(materiaRepository).save(any(Materia.class));
    }

    @Test
    void criar_cursoInexistente_lancaExcecao() {
        MateriaRequestDTO dto = new MateriaRequestDTO();
        dto.setProfessorId(2L);
        dto.setCursoId(99L);
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materiaService.criar(dto))
                .isInstanceOf(RecursoNotFoundException.class)
                .hasMessageContaining("Curso")
                .hasMessageContaining("99");

        verify(professorRepository, never()).findById(any());
        verify(materiaRepository, never()).save(any());
    }

    @Test
    void criar_professorInexistente_lancaExcecao() {
        Curso curso = new Curso("Java", "desc", 40, BigDecimal.TEN);
        curso.setId(1L);
        MateriaRequestDTO dto = new MateriaRequestDTO();
        dto.setProfessorId(99L);
        dto.setCursoId(1L);

        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(professorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materiaService.criar(dto))
                .isInstanceOf(RecursoNotFoundException.class)
                .hasMessageContaining("Professor")
                .hasMessageContaining("99");

        verify(materiaRepository, never()).save(any());
    }
}
