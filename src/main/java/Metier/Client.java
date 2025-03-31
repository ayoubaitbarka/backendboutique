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
//@DiscriminatorValue("CLIENT")
public class Client extends User {
    private String adressePostale;
    @OneToMany(mappedBy = "client")
    private List<Commande> commandes;

}

