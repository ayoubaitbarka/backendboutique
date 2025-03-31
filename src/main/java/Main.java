

import Dao.RepositoryProduit;
import Metier.Produit;
import jakarta.inject.Inject;


public class Main {

    public static void main(String[] args) {
        RepositoryProduit repositoryProduit=new RepositoryProduit();
        Produit produit = new Produit("t9achr", "t9ichra", 12, 12);
        repositoryProduit.save(produit);

    }
}


