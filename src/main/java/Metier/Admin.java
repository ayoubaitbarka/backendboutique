package Metier;

import Metier.Enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
//@DiscriminatorValue("ADMIN")
public class Admin extends User {

    public Admin() {
    }

    public Admin(String nom, String prenom, String email, String password, String telephone, Role role) {
        super(nom, prenom, email, password, telephone, role);
    }


}
