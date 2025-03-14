package Metier;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import Metier.Enums.MoyenPaiement;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Commande commande;

    @Enumerated(EnumType.STRING)
    private MoyenPaiement moyenPaiement;

    private boolean valide;

    private LocalDateTime datePaiement;

}
