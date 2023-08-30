package org.aarteaga.test.springboot.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aarteaga.test.springboot.app.models.TransaccionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment = RANDOM_PORT)
class CuentaControllerWebTestClientTests {
  private ObjectMapper objectMapper;
  @Autowired
  private WebTestClient client;

  @BeforeEach
  void setUp(){
    objectMapper  = new ObjectMapper();
  }
  @Test
  void testTransferir() throws JsonProcessingException {
    //given
    TransaccionDto dto = new TransaccionDto();
    dto.setCuentaOrigenId(1L);
    dto.setCuentaDestinoId(2L);
    dto.setBancoId(1L);
    dto.setMonto(new BigDecimal("100"));

    Map<String, Object> response = new HashMap<>();
    response.put("date", LocalDate.now().toString());
    response.put("status", "OK");
    response.put("mensaje", "Transferencia realizada con éxito!");
    response.put("transaccion", dto);

    //when
    //client.post().uri("http://localhost:8080/api/cuentas/transferir")
    client.post().uri("/api/cuentas/transferir")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(dto)
      .exchange()
      //then
      .expectStatus().isOk()
      .expectBody()
      .consumeWith(respuesta -> {
        try {
          JsonNode json = objectMapper.readTree(respuesta.getResponseBody());
          assertEquals("Transferencia realizada con éxito!", json.path("mensaje").asText());
          assertEquals(1L, json.path("transaccion").path("cuentaOrigenId").asLong());
          assertEquals(2L, json.path("transaccion").path("cuentaDestinoId").asLong());
          assertEquals(LocalDate.now().toString(), json.path("date").asText());
          assertEquals("100", json.path("transaccion").path("monto").asText());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

      })
      .jsonPath("$.mensaje").isNotEmpty()
      .jsonPath("$.mensaje").value(is("Transferencia realizada con éxito!"))
      .jsonPath("$.mensaje").value(valor -> assertEquals("Transferencia realizada con éxito!", valor))
      .jsonPath("$.mensaje").isEqualTo("Transferencia realizada con éxito!")
      .jsonPath("$.transaccion.cuentaOrigenId").isEqualTo(dto.getCuentaOrigenId())
      .jsonPath("$.date").isEqualTo(LocalDate.now().toString())
      .json(objectMapper.writeValueAsString(response));
  }

}