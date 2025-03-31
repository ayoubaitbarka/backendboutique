package Web.rest;

import Metier.Admin;
import Metier.User;
import Dao.RepositoryUser;
import Securite.JwtTokenProvider;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
public class AuthResource {

    private final RepositoryUser repositoryUser = new RepositoryUser();
    private final JwtTokenProvider tokenProvider = new JwtTokenProvider();


                            // OK khdaama jrrbtha b postman :
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Admin userCredentials) {
        if (userCredentials == null || userCredentials.getEmail() == null || userCredentials.getMotDePasse() == null) {
            System.out.println("mochkiiiiiiiiil m999wd");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Email and password are required.\"}")
                    .build();
        }

        try {
            // Vérifier si l'utilisateur existe dans la base de données
            User user = repositoryUser.findForLogin(userCredentials.getEmail(), userCredentials.getMotDePasse())
                    .filter(u -> u.getMotDePasse().equals(userCredentials.getMotDePasse())) // Validation du mot de passe (ne jamais faire ça en production, utiliser un hachage)
                    .orElse(null);

            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Invalid credentials.\"}")
                        .build();
            }

            // Générer un JWT pour l'utilisateur
            String token = tokenProvider.generateToken(user);

            System.out.println("le token dyalna :"+token);

            // Retourner le token dans la réponse
            return Response.ok("{\"token\":\"" + token + "\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred while processing the login.\"}")
                    .build();
        }
    }
}
