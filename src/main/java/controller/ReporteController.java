package controller;

import service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reporte")
@CrossOrigin(origins = "*")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    // Este endpoint devuelve el XML final
    @GetMapping(produces = "application/xml")
    public ResponseEntity<String> obtenerReporteFinal() {

        String xmlSalida = reporteService.generarXmlSalida();

        return ResponseEntity.ok(xmlSalida);
    }
}