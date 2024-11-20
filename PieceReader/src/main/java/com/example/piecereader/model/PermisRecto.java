package com.example.piecereader.model;

public class PermisRecto extends DocumentNumerised {
    String nom;
    String prenom;
    String dateExperitation;

    public PermisRecto(String nom, String prenom, String dateExperitation) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateExperitation = dateExperitation;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getDateExperitation() {
        return dateExperitation;
    }
}
