package com.example.cursos.service;

import com.example.cursos.dto.AvaliacaoRequestDTO;
import com.example.cursos.dto.UsuarioResponseDTO;
import com.example.cursos.exception.RecursoNotFoundException;
import com.example.cursos.model.Avaliacao;
import com.example.cursos.repository.AvaliacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository AvaliacaoRepository;

    @Mock
    private UsuarioClient usuarioClient;

    private AvaliacaoService AvaliacaoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AvaliacaoService = new AvaliacaoService(AvaliacaoRepository);
    }

    @Test
    void listar_semClienteId_retornaTodas() {
        List<Avaliacao> esperado = List.of(new Avaliacao());
        when(AvaliacaoRepository.findAll()).thenReturn(esperado);

        List<Avaliacao> resultado = AvaliacaoService.listar(null);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void listar_comClienteId_filtraPorCliente() {
        List<Avaliacao> esperado = List.of(new Avaliacao());

        List<Avaliacao> resultado = AvaliacaoService.listar(1L);

        assertThat(resultado).isEqualTo(esperado);
        verify(AvaliacaoRepository, never()).findAll();
    }

    @Test
    void criar_clienteValido_calculaValorTotalESalvaComEmail() {
        AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO();
        dto.setClienteId(1L);
        dto.setCodigoAcao("PETR4");
        dto.setQuantidade(10);
        dto.setPrecoUnitario(BigDecimal.valueOf(20));
        dto.setDataAvaliacao(LocalDate.of(2026, 1, 1));

        UsuarioResponseDTO usuario = new UsuarioResponseDTO();
        usuario.setId(1L);
        usuario.setEmail("cliente@email.com");
        when(usuarioClient.buscarUsuario(1L)).thenReturn(usuario);
        when(AvaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> inv.getArgument(0));

        Avaliacao resultado = AvaliacaoService.criar(dto);

        assertThat(resultado.getClienteId()).isEqualTo(1L);
        assertThat(resultado.getClienteEmail()).isEqualTo("cliente@email.com");
        assertThat(resultado.getValorTotal()).isEqualByComparingTo(BigDecimal.valueOf(200));
        verify(AvaliacaoRepository).save(any(Avaliacao.class));
    }

    @Test
    void criar_clienteInexistente_naoSalva() {
        AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO();
        dto.setClienteId(99L);
        when(usuarioClient.buscarUsuario(99L)).thenThrow(new RecursoNotFoundException("Usuário", 99L));

        assertThatThrownBy(() -> AvaliacaoService.criar(dto))
                .isInstanceOf(RecursoNotFoundException.class)
                .hasMessageContaining("Usuário")
                .hasMessageContaining("99");

        verify(AvaliacaoRepository, never()).save(any());
    }



}
