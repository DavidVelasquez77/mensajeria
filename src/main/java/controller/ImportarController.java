package controller;

import service.CargaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ImportarController {

    @Autowired
    private CargaService servicioDeCarga;

    @PostMapping("/importar")
    public ResponseEntity<String> subirArchivo(@RequestParam("file") MultipartFile archivo) {

        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El archivo viene vacio.");
        }
        String resultado = servicioDeCarga.procesarArchivo(archivo);
        if (resultado.startsWith("Error")) {
            return ResponseEntity.badRequest().body(resultado);
        }
        return ResponseEntity.ok(resultado);
    }
}