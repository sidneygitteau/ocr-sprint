package com.example.piecereader.service;

import com.example.piecereader.model.DocumentNumerised;
import com.example.piecereader.model.TypeDocument;

public interface ServicePieceReader {
    public DocumentNumerised convert(TypeDocument typeDocument, byte[] image) throws Exception;
}
