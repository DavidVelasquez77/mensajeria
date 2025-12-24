package controller;

import service.DespachoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DespachoController {

    @Autowired
    private DespachoService servicio;

    @PostMapping("/procesar")
    public ResponseEntity<String> procesarSolicitudes() {

        String resultado = servicio.procesarDespacho();

        return ResponseEntity.ok(resultado);
    }
}