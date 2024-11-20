package com.example.piecereader.model;

public class CarteGrise extends DocumentNumerised{
    String nom;
    String prenom;
    String adresse;
    String marqueTypeVehicule;
    String plaque;
    String pays;

    public CarteGrise(String nom, String prenom, String adresse, String marqueTypeVehicule, String plaque, String pays) {
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.marqueTypeVehicule = marqueTypeVehicule;
        this.plaque = plaque;
        this.pays = pays;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getMarqueTypeVehicule() {
        return marqueTypeVehicule;
    }

    public String getPlaque() {
        return plaque;
    }

    public String getPays() {
        return pays;
    }
}
