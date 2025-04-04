

package Metier;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
// IMPORTER CES DEUX LIGNES:
import com.fasterxml.jackson.annotation.JsonManagedReference; // Pour la relation Avis
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "produit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... autres champs (nom, description, prix, etc.) ...
    @Column(nullable = false, length = 255)
    private String nom;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(name = "quantite_stock", nullable = false)
    private int quantiteStock;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(precision = 3, scale = 1)
    private BigDecimal rating;

    @Column(nullable = false)
    private boolean featured;
    // --- Relation Catégorie ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cat", nullable = false)
    @JsonBackReference // <-- AJOUTER CECI : Empêche la récursion lors de la sérialisation depuis Produit
    private Categorie categorie;

    // --- Relation Avis --- (Appliquer la même logique si Avis a une référence vers Produit)
    @OneToMany(mappedBy = "produit", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonManagedReference // <-- AJOUTER CECI (si Avis a un @JsonBackReference vers Produit)
    private List<Avis> avis = new ArrayList<>();

}






/*
package Metier;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
// PAS BESOIN de JsonBackReference pour 'categorie' ici

@Entity
@Table(name = "produit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nom;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(name = "quantite_stock", nullable = false)
    private int quantiteStock;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(precision = 3, scale = 1)
    private BigDecimal rating;

    @Column(nullable = false)
    private boolean featured;

    // --- Relation Catégorie ---
    @ManyToOne(fetch = FetchType.EAGER) // EAGER assure que la catégorie est chargée
    @JoinColumn(name = "id_cat", nullable = false)
    // @JsonBackReference // <-- SUPPRIMER CETTE LIGNE
    private Categorie categorie; // Jackson va maintenant essayer de sérialiser cet objet

    // --- Relation Avis --- (Laisser JsonManagedReference ici est correct si Avis a un @JsonBackReference vers Produit)
    @OneToMany(mappedBy = "produit", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true) // Préférer LAZY pour les listes
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonManagedReference
    private List<Avis> avis = new ArrayList<>();

}

*/









