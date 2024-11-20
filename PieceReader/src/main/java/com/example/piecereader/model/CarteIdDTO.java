package com.example.piecereader.model;

public class CarteIdDTO extends DocumentNumerised {
    private String nom, prenom, lieuNaissance, dateNaissance, dateExpiration, numDoc, numDuBas, sexe, nationalite;

    public CarteIdDTO() {
    }

    public CarteIdDTO(String nom, String prenom, String lieuNaissance, String dateNaissance, String dateExpiration, String numDoc, String numDuBas, String sexe, String nationalite) {
        this.nom = nom;
        this.prenom = prenom;
        this.lieuNaissance = lieuNaissance;
        this.dateNaissance = dateNaissance;
        this.dateExpiration = dateExpiration;
        this.numDoc = numDoc;
        this.numDuBas = numDuBas;
        this.sexe = sexe;
        this.nationalite = nationalite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(String dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getNumDoc() {
        return numDoc;
    }

    public void setNumDoc(String numDoc) {
        this.numDoc = numDoc;
    }

    public String getNumDuBas() {
        return numDuBas;
    }

    public void setNumDuBas(String numDuBas) {
        this.numDuBas = numDuBas;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getNationalite() {
        return nationalite;
    }

    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }
}