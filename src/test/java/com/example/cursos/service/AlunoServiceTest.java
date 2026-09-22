package com.example.cursos.service;

import com.example.cursos.dto.AlunoRequestDTO;
import com.example.cursos.model.Aluno;
import com.example.cursos.repository.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlunoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    private AlunoService alunoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        alunoService = new AlunoService(alunoRepository);
    }

    @Test
    void listar_retornaTodosAlunos() {
        List<Aluno> esperado = List.of(new Aluno("Maria", "maria@email.com", "2024001"));
        when(alunoRepository.findAll()).thenReturn(esperado);

        List<Aluno> resultado = alunoService.listar();

        assertThat(resultado).isEqualTo(esperado);
        verify(alunoRepository).findAll();
    }

    @Test
    void criar_salvaEDevolveAlunoPersistido() {
        AlunoRequestDTO dto = new AlunoRequestDTO();
        dto.setNome("Maria");
        dto.setEmail("maria@email.com");
        dto.setMatricula("2024001");

        Aluno salvo = new Aluno(dto.getNome(), dto.getEmail(), dto.getMatricula());
        salvo.setId(1L);
        when(alunoRepository.save(any(Aluno.class))).thenReturn(salvo);

        Aluno resultado = alunoService.criar(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Maria");
        assertThat(resultado.getMatricula()).isEqualTo("2024001");
        verify(alunoRepository).save(any(Aluno.class));
    }
}
