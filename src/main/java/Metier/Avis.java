package Metier;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Avis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Client client;

    @ManyToOne
    @JsonBackReference // <-- AJOUTER CECI : Empêche la récursion lors de la sérialisation depuis Produit
    private Produit produit;

    private String commentaire;
    private int note;

    private LocalDateTime dateAvis;


}
