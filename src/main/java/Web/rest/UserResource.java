package Web.rest;


import Dao.RepositoryProduit;
import Metier.Admin;
import Metier.Enums.Role;
import Metier.Produit;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext; // JAX-RS standard, à conserver
import jakarta.ws.rs.core.UriBuilder;

import Metier.User;
import Dao.RepositoryUser; // Toujours besoin de l'import du Repository
import Securite.Secured;   // Annotation de sécurité (fonctionne indépendamment de CDI)

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO: Implémenter un vrai service de hashage de mot de passe (ex: BCrypt, Argon2)
// import org.mindrot.jbcrypt.BCrypt; // Exemple avec jBCrypt

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    // PAS d'@Inject ici
    // Instanciation manuelle de RepositoryUser.
    // Chaque requête à cette ressource (si elle est en scope requête par défaut)
    // créera une nouvelle instance du repository. C'est acceptable ici car
    // le repository modifié n'a pas d'état et obtient un EM frais à chaque appel.
    private final RepositoryUser repositoryUser = new RepositoryUser();

    // --- Point d'accès pour l'utilisateur courant ("moi") ---

    /**
     * Récupère les informations de l'utilisateur actuellement authentifié.
     * Requiert une authentification valide (n'importe quel rôle).
     * Utilise le SecurityContext de JAX-RS standard.
     */
    @GET
    @Path("/me")
    @Secured // Authentification requise (suppose un filtre JAX-RS)
    public Response getMyInfo(@Context SecurityContext securityContext) {
        String currentUsername = securityContext.getUserPrincipal().getName();
        if (currentUsername == null) {
            // Il peut être utile de logguer cette situation côté serveur
            System.err.println("Error in /me: Could not get username from SecurityContext.");
            return Response.status(Response.Status.UNAUTHORIZED) // Ou INTERNAL_SERVER_ERROR selon la cause
                    .entity("{\"error\":\"Could not identify current user session.\"}")
                    .build();
        }

        try {
            // Utilisation de l'instance manuelle du repository
            Optional<User> userOpt = repositoryUser.findByUsername(currentUsername);

            return userOpt
                    .map(user -> {
                        user.setMotDePasse(null); // Ne JAMAIS renvoyer le mot de passe/hash
                        return Response.ok(user).build();
                    })
                    .orElseGet(() -> {
                        // Logguer si l'utilisateur authentifié n'est pas trouvé en BDD (incohérence)
                        System.err.println("Error in /me: Authenticated user '" + currentUsername + "' not found in database.");
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"error\":\"Authenticated user data profile not found.\"}")
                                .build();
                    });
        } catch (Exception e) {
            // Log l'erreur survenue dans la ressource elle-même
            System.err.println("Error in UserResource.getMyInfo for user '" + currentUsername + "': " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error retrieving current user info.\"}").build();
        }
    }

    // --- Opérations CRUD pour les administrateurs ---

    /**
     * Crée un nouvel utilisateur.
     * Requiert le rôle ADMIN.
     * Le mot de passe DOIT être hashé ici avant d'être passé au repository.
     */
    @POST
    @Secured(roles = {"ADMIN"})
    public Response createUser(User user) {
        if (user == null || user.getNom() == null || user.getNom().trim().isEmpty() || user.getMotDePasse() == null || user.getMotDePasse().isEmpty() || user.getRole() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid user data. Name, password, and role are required.\"}").build();
        }

        try {
            // Vérifier si l'utilisateur existe déjà (utilise l'instance manuelle)
            if (repositoryUser.findByUsername(user.getNom()).isPresent()) {
                return Response.status(Response.Status.CONFLICT).entity("{\"error\":\"Username '" + user.getNom() + "' already exists.\"}").build();
            }

            // *** HASHAGE DU MOT DE PASSE ICI (ACTION CRITIQUE) ***
            try {
                // Remplacer ce placeholder par une vraie bibliothèque de hashage sécurisée
                // Exemple avec jBCrypt:
                // String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)); // 12 = work factor
                // user.setPassword(hashedPassword);

                // Placeholder actuel - NE PAS UTILISER EN PRODUCTION
                System.out.println("WARN: Hashing password with placeholder logic!"); // Log pour rappel
                String placeholderHash = "hashed_" + user.getMotDePasse();
                user.setMotDePasse(placeholderHash);

            } catch (Exception hashException) {
                System.err.println("FATAL: Password hashing failed: " + hashException.getMessage());
                hashException.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Password processing failed.\"}").build();
            }

            // Sauvegarde via l'instance manuelle
            User savedUser = repositoryUser.save(user);

            if (savedUser != null && savedUser.getId() != null) {
                savedUser.setMotDePasse(null); // Ne pas renvoyer le hash dans la réponse

                URI createdUri = UriBuilder.fromResource(UserResource.class).path("{id}").resolveTemplate("id", savedUser.getId()).build();
                return Response.created(createdUri).entity(savedUser).build();
            } else {
                // Si save retourne null (erreur logguée dans le repo)
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Failed to save the user data.\"}").build();
            }

        } catch (Exception e) {
            System.err.println("Error in UserResource.createUser: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An unexpected error occurred while creating the user.\"}").build();
        }
    }




    /**
     * Récupère un utilisateur spécifique par son ID.
     * Requiert le rôle ADMIN.
     */
    @GET
    @Path("/{id}")
    @Secured(roles = {"ADMIN"})
    public Response getUserById(@PathParam("id") Long id) {
        try {
            // Utilise l'instance manuelle
            User user = repositoryUser.findById(id);
            if (user != null) {
                user.setMotDePasse(null); // Masquer mot de passe/hash
                return Response.ok(user).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"User with ID " + id + " not found.\"}").build();
            }
        } catch (Exception e) {
            System.err.println("Error in UserResource.getUserById for ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error retrieving user.\"}").build();
        }
    }




    /**
     * Récupère la liste de tous les utilisateurs.
     * Requiert le rôle ADMIN.
     */
    @GET
    @Secured(roles = {"ADMIN"})
    public Response getAllUsers() {
        try {
            // Utilise l'instance manuelle
           // List<User> users = repositoryUser.findAll();
            // Enlever les mots de passe avant de renvoyer la liste
            List<User> users = new ArrayList<>();

// Ajout manuel des utilisateurs avec des valeurs différentes

            users.add(new Admin("Martin", "Sophie", "sophie.martin@email.com", "sophie456", "0611223344", Role.ADMIN));
            users.add(new Admin("Durand", "Paul", "paul.durand@email.com", "paul789", "0622334455", Role.ADMIN));
            List<User> usersSafe = users.stream().peek(u -> u.setMotDePasse(null)).collect(Collectors.toList());
            return Response.ok(usersSafe).build();
        } catch (Exception e) {
            System.err.println("Error in UserResource.getAllUsers: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error retrieving all users.\"}").build();
        }
    }

    /**
     * Met à jour un utilisateur existant.
     * Requiert le rôle ADMIN.
     * Hash le mot de passe s'il est fourni, sinon l'ancien est conservé par le repo.
     */
    @PUT
    @Path("/{id}")
    @Secured(roles = {"ADMIN"})
    public Response updateUser(@PathParam("id") Long id, User userData) {
        // Vérification minimale des données entrantes
        if (userData == null || userData.getNom() == null || userData.getNom().trim().isEmpty() || userData.getRole() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid user data for update. Name and role are required.\"}").build();
        }

        try {
            // Hasher le mot de passe SEULEMENT s'il est fourni dans la requête
            if (userData.getMotDePasse() != null && !userData.getMotDePasse().trim().isEmpty()) {
                try {
                    // Remplacer ce placeholder par une vraie bibliothèque de hashage sécurisée
                    // Exemple avec jBCrypt:
                    // String hashedPassword = BCrypt.hashpw(userData.getPassword(), BCrypt.gensalt(12));
                    // userData.setPassword(hashedPassword);

                    // Placeholder actuel - NE PAS UTILISER EN PRODUCTION
                    System.out.println("WARN: Hashing password with placeholder logic during update!"); // Log pour rappel
                    String placeholderHash = "hashed_" + userData.getMotDePasse();
                    userData.setMotDePasse(placeholderHash);

                } catch (Exception hashException) {
                    System.err.println("FATAL: Password hashing failed during update: " + hashException.getMessage());
                    hashException.printStackTrace();
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Password processing failed.\"}").build();
                }
            } else {
                // Si non fourni, mettre explicitement à null pour que le repository l'ignore
                userData.setMotDePasse(null);
            }

            // Appel de la méthode du repository instancié manuellement
            Optional<User> updatedUserOpt = repositoryUser.updateUser(id, userData);

            return updatedUserOpt
                    .map(user -> {
                        user.setMotDePasse(null); // Ne pas renvoyer le hash
                        return Response.ok(user).build();
                    })
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\":\"User with ID " + id + " not found for update.\"}")
                            .build());
        } catch (Exception e) {
            System.err.println("Error in UserResource.updateUser for ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An error occurred while updating the user.\"}").build();
        }
    }




    /**
     * Supprime un utilisateur par son ID.
     * Requiert le rôle ADMIN.
     */
    @DELETE
    @Path("/{id}")
    @Secured(roles = {"ADMIN"})
    public Response deleteUser(@PathParam("id") Long id) {
        try {
            // Utilise l'instance manuelle
            boolean deleted = repositoryUser.deleteById(id);
            if (deleted) {
                return Response.noContent().build(); // 204
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"User with ID " + id + " not found, cannot delete.\"}").build(); // 404
            }
        } catch (Exception e) {
            System.err.println("Error in UserResource.deleteUser for ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An error occurred while deleting the user.\"}").build(); // 500
        }
    }


    //************************************************************************************************




}