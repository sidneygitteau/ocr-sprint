package com.example.piecereader.model;

public class PermisVerso extends DocumentNumerised{
    String numeroPermis;

    public PermisVerso(String numeroPermis) {
        this.numeroPermis = numeroPermis;
    }

    public String getNumeroPermis() {
        return numeroPermis;
    }
}
