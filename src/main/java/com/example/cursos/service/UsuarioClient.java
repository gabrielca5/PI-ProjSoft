package com.example.cursos.service;

import com.example.cursos.dto.UsuarioResponseDTO;
import com.example.cursos.exception.RecursoNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class UsuarioClient {

    private final RestClient restClient;

    @Autowired
    public UsuarioClient(@Value("${usuarios.api.base-url}") String baseUrl) {
        this(RestClient.builder().baseUrl(baseUrl));
    }

    UsuarioClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public UsuarioResponseDTO buscarUsuario(Long id) {
        try {
            return restClient.get()
                    .uri("/users/{id}", id)
                    .retrieve()
                    .body(UsuarioResponseDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNotFoundException("Usuário", id);
        }
    }
}
