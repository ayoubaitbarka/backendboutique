package Metier;

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
    private Produit produit;

    private String commentaire;
    private int note;

    private LocalDateTime dateAvis;


}
