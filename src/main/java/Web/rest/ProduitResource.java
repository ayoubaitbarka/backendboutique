package Web.rest;

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


    // fonctionne url : http://localhost:8080/boutique_war/api/produits
    // pour limite de produits : http://localhost:8080/boutique_war/api/produits?limit=5
    // pour filtrer par id de categorie : http://localhost:8080/boutique_war/api/produits?categoryId=2
    // pour filtrer par id de categorie et limite de produits : http://localhost:8080/boutique_war/api/produits?categoryId=2&limit=5
    @GET
    public Response getAllOrFilterProduits(
            @QueryParam("categoryId") Long categoryId,
            @QueryParam("limit") Integer limit
            // Vous pourriez ajouter d'autres paramètres de filtrage ici (featured, etc.)
    ) {
        try {
            List<Produit> produits;
            if (categoryId != null) {
                // Filtrer par catégorie si categoryId est fourni
                System.out.println("DEBUG: Filtering products by category ID: " + categoryId + ", Limit: " + limit);
                produits = repositoryProduit.findByCategoryId(categoryId, limit);
            }
            // Le cas "featured" est géré par un endpoint séparé /featured
            // else if (featured != null && featured) {
            //    produits = repositoryProduit.findFeatured();
            // }
            else {
                // Aucun filtre spécifique, retourner tous les produits (ou appliquer une pagination par défaut si voulu)
                System.out.println("DEBUG: Fetching all products.");
                produits = repositoryProduit.findAll();
                // TODO: Ajouter une pagination par défaut à findAll si la liste peut devenir très grande.
                // Exemple simple de limitation si limit est fourni SANS categoryId:
                // if (limit != null && limit > 0 && limit < produits.size()) {
                //     produits = produits.subList(0, limit);
                // }
            }
            return Response.ok(produits).build();
        } catch (Exception e) {
            System.err.println("Error in ProduitResource.getAllOrFilterProduits: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An internal error occurred while retrieving products.\"}")
                    .build();
        }
    }


             // fonctionne url : http://localhost:8080/boutique_war/api/produits/featured
    @GET
    @Path("/featured")
    public Response getFeaturedProduits() {
        try {
            System.out.println("DEBUG: Fetching featured products.");
            List<Produit> produits = repositoryProduit.findFeatured();
            return Response.ok(produits).build();
        } catch (Exception e) {
            System.err.println("Error in ProduitResource.getFeaturedProduits: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An internal error occurred while retrieving featured products.\"}")
                    .build();
        }
    }










}