package Metier;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor

public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String description;
    private double prix;
    private int quantiteStock;

    @OneToMany(mappedBy = "produit",fetch = FetchType.EAGER)
    private List<Avis> avis;

    public Produit(String nom, String description, double prix,int quantiteStock) {
        this.description=description;
        this.nom=nom;
        this.prix=prix;
        this.quantiteStock=quantiteStock;
        avis=new ArrayList<>();
    }


    @Override
    public String toString() {
        return "ID :"+id+" ,nom: "+nom+" ,description "+description+" ,prix :"+prix;
    }
}

