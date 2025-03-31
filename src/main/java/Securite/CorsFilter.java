
                                      //   Pourquoi ce filtre est-il nécessaire ?

                                      // nha fiin osswlt test dyal post login o radii yakhd token ayssiftoo l front
                                      // omn moraha f ay requete ankhdmo dak token.

                    /*
                        ce filtre est nécessaire pour gérer les requêtes CORS (Cross-Origin Resource Sharing) dans votre application JAX-RS.
                        explication puls détaillées :
                        CORS est un mécanisme de sécurité qui permet à un serveur de déclarer à un navigateur web quelles origines (domaines) sont autorisées à accéder à ses ressources.

                     j'ai essayé de l'utilisé lorsque je voulu tester la requetes avec la methode post via le navigateur avec :

                   // Fonction pour se connecter et obtenir le token
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

                    */








package Securite; // Ou un autre package approprié comme Web.config

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching; // Important pour gérer OPTIONS tôt
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider // Rend ce filtre découvrable par JAX-RS
@PreMatching // Important pour intercepter les requêtes OPTIONS *avant* qu'elles n'atteignent vos ressources
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {

        System.out.println("CORS Filter: Adding headers to response for origin: " + requestContext.getHeaderString("Origin")); // Log pour déboguer

        // === En-têtes pour la réponse réelle ===
        // Autorise TOUTES les origines (ATTENTION en production !)
        // Remplacez "*" par l'URL exacte de votre frontend (ex: "http://localhost:3000") en production
        responseContext.getHeaders().add(
                "Access-Control-Allow-Origin", "*");

        // Liste des méthodes HTTP autorisées
        responseContext.getHeaders().add(
                "Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

        // Liste des en-têtes que le client peut envoyer (important pour Content-Type et Authorization)
        responseContext.getHeaders().add(
                "Access-Control-Allow-Headers", "origin, content-type, accept, authorization");

        // Autorise le navigateur à envoyer des cookies ou des en-têtes d'autorisation
        // Doit être "true" si vous utilisez des tokens Bearer ou des sessions
        // Si c'est "true", Access-Control-Allow-Origin NE PEUT PAS être "*" en production.
        responseContext.getHeaders().add(
                "Access-Control-Allow-Credentials", "true");

        // Optionnel: Combien de temps le navigateur peut mettre en cache la réponse preflight (en secondes)
        // responseContext.getHeaders().add(
        //        "Access-Control-Max-Age", "1209600"); // 2 semaines

        // === Gestion spécifique de la requête preflight OPTIONS ===
        // Si la requête entrante était une requête OPTIONS (preflight)
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            System.out.println("CORS Filter: Handling OPTIONS preflight request");
            // On a déjà ajouté les headers ci-dessus. Il suffit de renvoyer OK.
            // On pourrait aussi mettre les headers Allow-Methods/Headers ici spécifiquement
            // mais les ajouter à toutes les réponses fonctionne aussi pour OPTIONS.
            responseContext.setStatus(Response.Status.OK.getStatusCode());
            // Important: On ne veut pas que JAX-RS continue à chercher une ressource @OPTIONS
            // La méthode filter() va se terminer, et la réponse OK avec les headers sera envoyée.
            // NOTE: Dans certaines implémentations JAX-RS/serveurs, il faut explicitement
            // faire un `requestContext.abortWith(Response.ok().build());` ici.
            // Testez ce qui fonctionne pour votre environnement (Tomcat?).
            // L'approche actuelle modifie la réponse qui sera envoyée APRES que le filtre
            // ait terminé, ce qui est généralement correct pour OPTIONS via ContainerResponseFilter.
        }
    }
}