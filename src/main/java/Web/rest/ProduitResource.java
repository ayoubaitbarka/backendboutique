package Web.rest;


                        /*
                        *  fin akhoya mohammed mrtaah hhhhhhhhh
                        *  ltht rah les methodes lii khdamin drt foo9 mnhoom ok o url bach drya iban lik dakchi
                        *  mohiim hbt lttht rah atl9anii charh dakchii lii drt.
                        *
                        * bniisba ldook les repository rah khrb9t fihom flwl ms mnb3d rdiit dakchi lwlanii les methode slii mbdliin chwiiya
                        *
                        *
                        *
                        *
                        * */

// Pas d'import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import Metier.Produit;
import Dao.RepositoryProduit;
import Securite.Secured;

import java.net.URI;
// import java.util.ArrayList; // Plus besoin si on utilise la vraie méthode
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/produits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProduitResource {

    private final RepositoryProduit repositoryProduit = new RepositoryProduit();


                    // OK : khdama url : http://localhost:8080/boutique_war/api/produits/
    @GET
    public Response getAllProduits() {
        try {

            List<Produit> produits = repositoryProduit.findAll();
            return Response.ok(produits).build(); // Retourne 200 OK avec la liste (vide ou non)
        } catch (Exception e) {
            // Log l'erreur qui pourrait survenir DANS la ressource elle-même
            // (les erreurs du repo sont logguées dans le repo)
            System.err.println("Error in ProduitResource.getAllProduits: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An internal error occurred while retrieving products.\"}")
                    .build();
        }
    }



                           // OK : khdama url : http://localhost:8080/boutique_war/api/produits/2

    @GET
    @Path("/{id}")
    public Response getProduitById(@PathParam("id") Long id) {
        try {
            Produit produit = repositoryProduit.findById(id); // Utilise le repo instancié
            if (produit != null) {
                return Response.ok(produit).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Product with ID " + id + " not found.\"}")
                        .build();
            }
        } catch (Exception e) {
            System.err.println("Error in ProduitResource.getProduitById for ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An internal error occurred while retrieving the product.\"}")
                    .build();
        }
    }



    // OK : khdama url : http://localhost:8080/boutique_war/api/produits/search/t9achr  <--- smya dyal produit f bdd
    @GET
    @Path("/search/{nom}")
    public Response searchProduitsByName(@PathParam("nom") String nomTerme) {
        try {
            List<Produit> produits = repositoryProduit.findProduitByName(nomTerme); // Utilise le repo instancié
            return Response.ok(produits).build();
        } catch (Exception e) {
            System.err.println("Error in ProduitResource.searchProduitsByName for term '" + nomTerme + "': " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An internal error occurred while searching for products.\"}")
                    .build();
        }
    }

         /* fhad l methode bdbt hiiya fiin ohlt! khassna ndiiro authentification user admin howa lwl
         * ohitach bdiina tandiro securité au meme temps khassna ntb9ooha
         * choof classe AuthRessource tma fiin drt methode dyal login oo une fois idiir login khasso iretourner wahd token l front omn moraha anb9aw nkhdmooh f ay requete bra idiirha
         * bach ndiir le test rah mchiit saybt wahd fonction ohaawlt ndiirha f console dyan navigateur ms taytl3 wahd erreur.
         *
         * hahiiya l foct :    (rah khdm dakchii flkhr ms 9raa bach tfhm ach tarii)



            async function login(email, password) {
                    const url = "http://localhost:8080/boutique_war/api/auth/login"; // L'URL de l'API pour la connexion

                    const response = await fetch(url, {
                    method: "POST",
                    headers: {
                    "Content-Type": "application/json"
                                     },
                    body: JSON.stringify({
      email: email,
      motDePasse: password
    })
  });

  if (response.ok) {
    const data = await response.json();
    const token = data.token;  // Le token JWT que tu obtiens
    console.log("Token:", token);
    return token;
  } else {
    console.error("Erreurrrr de connexion:", response.statusText);
    return null;
  }
}

// Exemple d'appel pour récupérer un token
login("admin@gmail.com", "123456").then(token => {
  if (token) {
    // Utiliser le token pour faire un appel API sécurisé
    fetchProduit(token);
  }
});





         *  mohiim swlt chat 3la dak erreur galiiya chii "Cross-Origin Resource Sharing"
         *  moraha gal liiya saayb diik classe li kayna f securité : CorsFilter rah athl mochkiil ms mabraatch ossf 3yiit
         *  tan3aw nchoofha lamal9itiich ntaa llhl
         *
         * fach ndiiro had test dyal post mn moraha radii n9droo ndiiro test ldook les methodes li kaynin f UserRessource
         *
         * mohiim diik classe dyal CorsFilter rah drt liiha commnataire hitach chft dakchii khdam bla biiha hhhhhhh
         *
         * mohiim rah tkhariit m3a dakchii mnb3d oo ssf diik post dyal login rah wlaat khdama mzn o rah tatrj3 token l front
         *
         *  choof classe AuthRessource tma fiin drt methode dyal login oo une fois idiir login khasso iretourner wahd token l front omn moraha anb9aw nkhdmooh f ay requete bra idiirha
         *
         *
         *
         * */

    @POST
    @Secured(roles = {"ADMIN"})
    public Response createProduit(Produit produit) {
        if (produit == null || produit.getNom() == null || produit.getNom().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Product data is invalid. Name is required.\"}")
                    .build();
        }

        try {
            Produit savedProduit = repositoryProduit.save(produit); // Utilise le repo instancié

            if (savedProduit != null && savedProduit.getId() != null) {
                // Bonne pratique : retourner l'URI de la nouvelle ressource
                URI createdUri = UriBuilder.fromResource(ProduitResource.class)
                        .path("{id}")
                        .resolveTemplate("id", savedProduit.getId())
                        .build();

                return Response.created(createdUri)
                        .entity(savedProduit)
                        .build();
            } else {
                // Si save retourne null (à cause d'une erreur gérée dans le repo)
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\":\"Failed to save the product.\"}")
                        .build();
            }
        } catch (Exception e) {
            // Capturer les exceptions non prévues par le save() lui-même
            System.err.println("Error in ProduitResource.createProduit: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An internal error occurred while creating the product.\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Secured(roles = {"ADMIN"})
    public Response updateProduit(@PathParam("id") Long id, Produit produitData) {
        if (produitData == null || produitData.getNom() == null || produitData.getNom().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Product data is invalid for update. Name is required.\"}")
                    .build();
        }

        try {
            Optional<Produit> updatedProduitOpt = repositoryProduit.updateProduit(id, produitData); // Utilise le repo instancié

            // isPresent() est plus idiomatique pour Optional
            if (updatedProduitOpt.isPresent()) {
                return Response.ok(updatedProduitOpt.get()).build();
            } else {
                // L'Optional est vide, signifie que le produit n'a pas été trouvé
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Product with ID " + id + " not found for update.\"}")
                        .build();
            }
        } catch (Exception e) {
            System.err.println("Error in ProduitResource.updateProduit for ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An internal error occurred while updating the product.\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Secured(roles = {"ADMIN"})
    public Response deleteProduit(@PathParam("id") Long id) {
        try {
            boolean deleted = repositoryProduit.deleteById(id); // Utilise le repo instancié

            if (deleted) {
                return Response.noContent().build(); // 204 No Content
            } else {
                // deleteById a retourné false, le produit n'existait pas
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Product with ID " + id + " not found, cannot delete.\"}")
                        .build();
            }
        } catch (Exception e) {
            System.err.println("Error in ProduitResource.deleteProduit for ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An internal error occurred while deleting the product.\"}")
                    .build();
        }
    }
}