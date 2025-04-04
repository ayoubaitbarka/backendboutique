package Web.rest;

import Dao.RepositoryCategorie; // Créez ce repository !
import Metier.Categorie;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON) // Même si on ne consomme rien ici
public class CategorieResource {

    private final RepositoryCategorie repositoryCategorie = new RepositoryCategorie(); // Instance manuelle


      // fonctione : url : http://localhost:8080/boutique_war/api/categories
    @GET
    public Response getAllCategories() {
        try {
            List<Categorie> categories = repositoryCategorie.findAll();
            return Response.ok(categories).build();
        } catch (Exception e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Failed to retrieve categories.\"}")
                    .build();
        }
    }

}