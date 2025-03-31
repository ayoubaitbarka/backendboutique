

import Dao.RepositoryProduit;
import Dao.RepositoryUser;
import Metier.Admin;
import Metier.Enums.Role;
import Metier.Produit;
import Metier.User;
import jakarta.inject.Inject;

import java.util.Optional;


public class Main {

    /*
    public static void main(String[] args) {
        RepositoryProduit repositoryProduit=new RepositoryProduit();
        Produit produit = new Produit("t9achr", "t9ichra", 12, 12);
        repositoryProduit.save(produit);

    }


     */

  /*
    public static void main(String[] args) {
        RepositoryUser repo = new RepositoryUser();

        Optional<User> user = repo.findForLogin("admin@gmail.com", "123456");
        if (((java.util.Optional<?>) user).isPresent()) {
            System.out.println("Connexion réussie !");
        } else {
            System.out.println("Échec : vérifiez les logs SQL");
        }
    }

   */

    public static void main(String[] args) {


        RepositoryUser repo = new RepositoryUser();

        // Création d'un Admin
        // Admin admin = new Admin("Dupont", "Jean", "admin@example.com", "securePass123", "0123456789", Role.ADMIN);

        // Ajout dans la base de données
        //repo.save(admin);

        Optional<User> user = repo.findForLogin("admin@example.com", "securePass123");
        if (user.isPresent()) {
            System.out.println("Connexion réussie !");
        } else {
            System.out.println("Échec : vérifiez les logs SQL");
        }


    }

}


