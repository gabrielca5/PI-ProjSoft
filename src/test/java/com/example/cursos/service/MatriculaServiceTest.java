package com.example.cursos.service;

import com.example.cursos.dto.MatriculaRequestDTO;
import com.example.cursos.exception.RecursoNotFoundException;
import com.example.cursos.model.Aluno;
import com.example.cursos.model.Curso;
import com.example.cursos.model.Matricula;
import com.example.cursos.repository.AlunoRepository;
import com.example.cursos.repository.CursoRepository;
import com.example.cursos.repository.MatriculaRepository;
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

class MatriculaServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private AlunoRepository alunoRepository;

    private MatriculaService matriculaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        matriculaService = new MatriculaService(matriculaRepository, cursoRepository, alunoRepository);
    }

    @Test
    void listarPorCurso_retornaMatriculasDoCurso() {
        List<Matricula> esperado = List.of(new Matricula(null, null));
        when(matriculaRepository.findByCursoId(1L)).thenReturn(esperado);

        List<Matricula> resultado = matriculaService.listarPorCurso(1L);

        assertThat(resultado).isEqualTo(esperado);
        verify(matriculaRepository).findByCursoId(1L);
    }

    @Test
    void matricular_salvaMatriculaVinculandoCursoEAluno() {
        Curso curso = new Curso("Java", "desc", 40, BigDecimal.TEN);
        curso.setId(1L);
        Aluno aluno = new Aluno("Maria", "maria@email.com", "2024001");
        aluno.setId(2L);

        MatriculaRequestDTO dto = new MatriculaRequestDTO();
        dto.setCursoId(1L);
        dto.setAlunoId(2L);

        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(alunoRepository.findById(2L)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));

        Matricula resultado = matriculaService.matricular(dto);

        assertThat(resultado.getCurso()).isEqualTo(curso);
        assertThat(resultado.getAluno()).isEqualTo(aluno);
        verify(matriculaRepository).save(any(Matricula.class));
    }

    @Test
    void matricular_cursoInexistente_lancaExcecao() {
        MatriculaRequestDTO dto = new MatriculaRequestDTO();
        dto.setCursoId(99L);
        dto.setAlunoId(2L);
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matriculaService.matricular(dto))
                .isInstanceOf(RecursoNotFoundException.class)
                .hasMessageContaining("Curso")
                .hasMessageContaining("99");

        verify(alunoRepository, never()).findById(any());
        verify(matriculaRepository, never()).save(any());
    }

    @Test
    void matricular_alunoInexistente_lancaExcecao() {
        Curso curso = new Curso("Java", "desc", 40, BigDecimal.TEN);
        curso.setId(1L);
        MatriculaRequestDTO dto = new MatriculaRequestDTO();
        dto.setCursoId(1L);
        dto.setAlunoId(99L);

        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(alunoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matriculaService.matricular(dto))
                .isInstanceOf(RecursoNotFoundException.class)
                .hasMessageContaining("Aluno")
                .hasMessageContaining("99");

        verify(matriculaRepository, never()).save(any());
    }
}
