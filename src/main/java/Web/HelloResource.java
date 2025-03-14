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
        List<Produit> produits = repositoryProduit.Produits();
        produits.forEach(System.out::println);
        return produits;
    }
}
