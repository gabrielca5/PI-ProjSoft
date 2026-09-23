package com.example.cursos.service;

import com.example.cursos.dto.TransacaoRequestDTO;
import com.example.cursos.dto.UsuarioResponseDTO;
import com.example.cursos.exception.RecursoNotFoundException;
import com.example.cursos.model.Transacao;
import com.example.cursos.repository.TransacaoRepository;
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

class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private UsuarioClient usuarioClient;

    private TransacaoService transacaoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transacaoService = new TransacaoService(transacaoRepository, usuarioClient);
    }

    @Test
    void listar_semClienteId_retornaTodas() {
        List<Transacao> esperado = List.of(new Transacao());
        when(transacaoRepository.findAll()).thenReturn(esperado);

        List<Transacao> resultado = transacaoService.listar(null);

        assertThat(resultado).isEqualTo(esperado);
        verify(transacaoRepository, never()).findByClienteId(any());
    }

    @Test
    void listar_comClienteId_filtraPorCliente() {
        List<Transacao> esperado = List.of(new Transacao());
        when(transacaoRepository.findByClienteId(1L)).thenReturn(esperado);

        List<Transacao> resultado = transacaoService.listar(1L);

        assertThat(resultado).isEqualTo(esperado);
        verify(transacaoRepository, never()).findAll();
    }

    @Test
    void criar_clienteValido_calculaValorTotalESalvaComEmail() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO();
        dto.setClienteId(1L);
        dto.setCodigoAcao("PETR4");
        dto.setQuantidade(10);
        dto.setPrecoUnitario(BigDecimal.valueOf(20));
        dto.setDataTransacao(LocalDate.of(2026, 1, 1));

        UsuarioResponseDTO usuario = new UsuarioResponseDTO();
        usuario.setId(1L);
        usuario.setEmail("cliente@email.com");
        when(usuarioClient.buscarUsuario(1L)).thenReturn(usuario);
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

        Transacao resultado = transacaoService.criar(dto);

        assertThat(resultado.getClienteId()).isEqualTo(1L);
        assertThat(resultado.getClienteEmail()).isEqualTo("cliente@email.com");
        assertThat(resultado.getValorTotal()).isEqualByComparingTo(BigDecimal.valueOf(200));
        verify(transacaoRepository).save(any(Transacao.class));
    }

    @Test
    void criar_clienteInexistente_naoSalva() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO();
        dto.setClienteId(99L);
        when(usuarioClient.buscarUsuario(99L)).thenThrow(new RecursoNotFoundException("Usuário", 99L));

        assertThatThrownBy(() -> transacaoService.criar(dto))
                .isInstanceOf(RecursoNotFoundException.class)
                .hasMessageContaining("Usuário")
                .hasMessageContaining("99");

        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void deletar_existente_removeTransacao() {
        when(transacaoRepository.existsById(1L)).thenReturn(true);

        transacaoService.deletar(1L);

        verify(transacaoRepository).deleteById(1L);
    }

    @Test
    void deletar_inexistente_lancaExcecao() {
        when(transacaoRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> transacaoService.deletar(99L))
                .isInstanceOf(RecursoNotFoundException.class)
                .hasMessageContaining("Transação")
                .hasMessageContaining("99");

        verify(transacaoRepository, never()).deleteById(any());
    }
}
