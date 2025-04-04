

package Web;

import Dao.RepositoryProduit;
import Metier.Produit;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/besmilah")
public class HelloResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Produit hello() {
        return new Produit();

    }

}


   /*




    public List<Produit> hello() {
        RepositoryProduit repositoryProduit=new RepositoryProduit();
        return repositoryProduit.Produits();

    }




    public String hello() {
        return "salma";
    }


}
*/


/*
package Web;

import Dao.RepositoryProduit;
import Metier.Produit;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/besmilah")
public class HelloResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Produit> hello() {
        RepositoryProduit repositoryProduit=new RepositoryProduit();
        List<Produit> produits = repositoryProduit.findAll();
        produits.forEach(System.out::println);
        return produits;
    }
}
*/

