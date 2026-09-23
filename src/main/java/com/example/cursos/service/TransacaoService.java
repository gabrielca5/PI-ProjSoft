package com.example.cursos.service;

import com.example.cursos.dto.TransacaoRequestDTO;
import com.example.cursos.dto.UsuarioResponseDTO;
import com.example.cursos.exception.RecursoNotFoundException;
import com.example.cursos.model.Transacao;
import com.example.cursos.repository.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final UsuarioClient usuarioClient;

    public TransacaoService(TransacaoRepository transacaoRepository, UsuarioClient usuarioClient) {
        this.transacaoRepository = transacaoRepository;
        this.usuarioClient = usuarioClient;
    }

    public List<Transacao> listar(Long clienteId) {
        if (clienteId != null) {
            return transacaoRepository.findByClienteId(clienteId);
        }
        return transacaoRepository.findAll();
    }

    public Transacao criar(TransacaoRequestDTO dto) {
        UsuarioResponseDTO usuario = usuarioClient.buscarUsuario(dto.getClienteId());

        Transacao transacao = new Transacao(
                dto.getClienteId(),
                usuario.getEmail(),
                dto.getCodigoAcao(),
                dto.getQuantidade(),
                dto.getPrecoUnitario(),
                dto.getPrecoUnitario().multiply(java.math.BigDecimal.valueOf(dto.getQuantidade())),
                dto.getDataTransacao()
        );

        return transacaoRepository.save(transacao);
    }

    public void deletar(Long id) {
        if (!transacaoRepository.existsById(id)) {
            throw new RecursoNotFoundException("Transação", id);
        }
        transacaoRepository.deleteById(id);
    }
}
