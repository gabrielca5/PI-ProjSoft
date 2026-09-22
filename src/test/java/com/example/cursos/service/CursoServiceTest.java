package com.example.cursos.service;

import com.example.cursos.dto.CursoRequestDTO;
import com.example.cursos.exception.CursoNotFoundException;
import com.example.cursos.model.Curso;
import com.example.cursos.repository.CursoRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CursoServiceTest {

    @Mock
    private CursoRepository cursoRepository;

    private CursoService cursoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cursoService = new CursoService(cursoRepository);
    }

    @Test
    void listar_semFiltro_retornaTodosNaoDeletados() {
        List<Curso> esperado = List.of(new Curso("Java", "desc", 40, BigDecimal.TEN));
        when(cursoRepository.findByDeletadoFalse()).thenReturn(esperado);

        List<Curso> resultado = cursoService.listar(null);

        assertThat(resultado).isEqualTo(esperado);
        verify(cursoRepository).findByDeletadoFalse();
        verify(cursoRepository, never()).findByDeletadoFalseAndNomeStartingWithIgnoreCase(any());
    }

    @Test
    void listar_comFiltroEmBranco_retornaTodosNaoDeletados() {
        when(cursoRepository.findByDeletadoFalse()).thenReturn(List.of());

        cursoService.listar("   ");

        verify(cursoRepository).findByDeletadoFalse();
    }

    @Test
    void listar_comFiltro_retornaCursosQueComecamComOFiltro() {
        List<Curso> esperado = List.of(new Curso("Java Avançado", "desc", 40, BigDecimal.TEN));
        when(cursoRepository.findByDeletadoFalseAndNomeStartingWithIgnoreCase("Java")).thenReturn(esperado);

        List<Curso> resultado = cursoService.listar("Java");

        assertThat(resultado).isEqualTo(esperado);
        verify(cursoRepository).findByDeletadoFalseAndNomeStartingWithIgnoreCase("Java");
    }

    @Test
    void criar_salvaEDevolveCursoPersistido() {
        CursoRequestDTO dto = new CursoRequestDTO();
        dto.setNome("Spring Boot");
        dto.setDescricao("curso de spring");
        dto.setCargaHoraria(20);
        dto.setPreco(BigDecimal.valueOf(99.9));

        Curso salvo = new Curso(dto.getNome(), dto.getDescricao(), dto.getCargaHoraria(), dto.getPreco());
        salvo.setId(1L);
        when(cursoRepository.save(any(Curso.class))).thenReturn(salvo);

        Curso resultado = cursoService.criar(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Spring Boot");
        assertThat(resultado.isDeletado()).isFalse();
        verify(cursoRepository).save(any(Curso.class));
    }

    @Test
    void deletar_marcaCursoComoDeletado() {
        Curso curso = new Curso("Java", "desc", 40, BigDecimal.TEN);
        curso.setId(1L);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any(Curso.class))).thenAnswer(inv -> inv.getArgument(0));

        cursoService.deletar(1L);

        assertThat(curso.isDeletado()).isTrue();
        verify(cursoRepository).save(eq(curso));
    }

    @Test
    void deletar_cursoInexistente_lancaExcecao() {
        when(cursoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cursoService.deletar(99L))
                .isInstanceOf(CursoNotFoundException.class)
                .hasMessageContaining("99");

        verify(cursoRepository, never()).save(any());
    }
}
