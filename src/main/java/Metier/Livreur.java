package Metier;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@DiscriminatorValue("LIVREUR")
public class Livreur extends User {
    private String entrepriseLivraison;
    @OneToMany(mappedBy = "livreur")
    private List<Commande> commandes;
}
