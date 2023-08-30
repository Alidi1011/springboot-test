package org.aarteaga.test.springboot.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aarteaga.test.springboot.app.models.Cuenta;
import org.aarteaga.test.springboot.app.models.TransaccionDto;
import org.aarteaga.test.springboot.app.services.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CuentaControllerRestTemplateTest {

  @Autowired
  private TestRestTemplate client;

  private ObjectMapper objectMapper;

  @LocalServerPort
  private int puerto;
 @BeforeEach
  void setUp(){
    objectMapper = new ObjectMapper();
 }

 @Test
 @Order(1)
  void listar() throws JsonProcessingException {
   TransaccionDto transaccionDto = new TransaccionDto();
   transaccionDto.setMonto(new BigDecimal("100"));
   transaccionDto.setCuentaOrigenId(1L);
   transaccionDto.setCuentaDestinoId(2L);
   transaccionDto.setBancoId(1L);

   ResponseEntity<String> response = client.postForEntity(crearUri("/api/cuentas/transferir") , transaccionDto, String.class);

   System.out.println("PUERTO: " + puerto);
   String json = response.getBody();

   assertEquals(HttpStatus.OK, response.getStatusCode());
   assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
   assertNotNull(json);
   assertTrue(json.contains("Transferencia realizada con éxito!"));
   assertTrue(json.contains("{\"cuentaOrigenId\":1,\"cuentaDestinoId\":2,\"monto\":100,\"bancoId\":1}"));

   JsonNode jsonNode = objectMapper.readTree(json);
   assertEquals("Transferencia realizada con éxito!", jsonNode.path("mensaje").asText());
   assertEquals(LocalDate.now().toString(), jsonNode.path("date").asText());
   assertEquals("100", jsonNode.path("transaccion").path("monto").asText());
   assertEquals(1L, jsonNode.path("transaccion").path("cuentaOrigenId").asLong());

   Map<String, Object> response2 = new HashMap<>();
   response2.put("date", LocalDate.now().toString());
   response2.put("status", "OK");
   response2.put("mensaje", "Transferencia realizada con éxito!");
   response2.put("transaccion", transaccionDto);

   assertEquals(objectMapper.writeValueAsString(response2), json);
 }

 @Test
 @Order(2)
 void testDetalle(){
   ResponseEntity<Cuenta> respuesta = client.getForEntity(crearUri("/api/cuentas/1"), Cuenta.class);
   Cuenta cuenta = respuesta.getBody();
   assertEquals(HttpStatus.OK, respuesta.getStatusCode());
   assertEquals(MediaType.APPLICATION_JSON, respuesta.getHeaders().getContentType());

   assertNotNull(cuenta);
   assertEquals(1L, cuenta.getId());
   assertEquals("Andrés", cuenta.getPersona());
   assertEquals("900.00", cuenta.getSaldo().toPlainString());
   assertEquals(new Cuenta(1L, "Andrés", new BigDecimal("900.00")), cuenta);

 }

 private String crearUri(String uri){
   return "http://localhost:" + puerto + uri;
 }
}