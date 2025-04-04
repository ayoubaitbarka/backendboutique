
package Metier;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
// IMPORTER CES DEUX LIGNES:
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference; // Bien qu'on ne l'utilise pas DIRECTEMENT ici, c'est bon de savoir qu'elle existe

@Entity
@Table(name = "categorie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(length = 255)
    private String description;

    @OneToMany(mappedBy = "categorie", fetch = FetchType.EAGER) // Vous avez mis EAGER ici
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonManagedReference // <-- AJOUTER CECI : Gère la sérialisation de la liste
    private List<Produit> produits = new ArrayList<>();

    public Categorie(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }
}




/*
package Metier;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "categorie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(length = 255)
    private String description;

    // Préférer LAZY ici pour éviter de charger tous les produits quand on charge une catégorie
    // Mais EAGER + @JsonManagedReference fonctionne aussi, même si c'est moins performant
    @OneToMany(mappedBy = "categorie", fetch = FetchType.EAGER) // Changé en LAZY (recommandé)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonManagedReference // Correct ici pour gérer la sérialisation depuis Categorie
    private List<Produit> produits = new ArrayList<>();

    public Categorie(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }
}
*/








