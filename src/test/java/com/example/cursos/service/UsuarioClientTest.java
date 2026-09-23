package com.example.cursos.service;

import com.example.cursos.dto.UsuarioResponseDTO;
import com.example.cursos.exception.RecursoNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UsuarioClientTest {

    @Test
    void buscarUsuario_existente_retornaUsuario() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://usuarios.invalid");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://usuarios.invalid/users/1"))
                .andRespond(withSuccess("{\"id\":1,\"email\":\"cliente@email.com\"}", MediaType.APPLICATION_JSON));

        UsuarioClient client = new UsuarioClient(builder);
        UsuarioResponseDTO usuario = client.buscarUsuario(1L);

        assertThat(usuario.getEmail()).isEqualTo("cliente@email.com");
        server.verify();
    }

    @Test
    void buscarUsuario_naoEncontrado_lancaRecursoNotFound() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://usuarios.invalid");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://usuarios.invalid/users/99"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        UsuarioClient client = new UsuarioClient(builder);

        assertThatThrownBy(() -> client.buscarUsuario(99L))
                .isInstanceOf(RecursoNotFoundException.class)
                .hasMessageContaining("99");
    }
}
