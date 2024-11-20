package com.example.piecereader.controleur;

import com.example.piecereader.model.DocumentNumerised;
import com.example.piecereader.model.TypeDocument;
import com.example.piecereader.service.ServicePieceReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/piecereader")
public class ControleurConversion {

    private ServicePieceReader pieceReader;

    public ControleurConversion(@Autowired ServicePieceReader servicePieceReader) {
        this.pieceReader = servicePieceReader;
    }

    @PostMapping("/conversion")
    public ResponseEntity<DocumentNumerised> convertDocument(@RequestParam TypeDocument typeDocument,@RequestBody byte[] image) throws Exception {
        DocumentNumerised doc = pieceReader.convert(typeDocument,image);
        return ResponseEntity.ok(doc);
    }
}
