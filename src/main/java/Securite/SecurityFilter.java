package Securite;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo; // Pour accéder à la méthode/classe cible
import jakarta.ws.rs.core.Context;          // Pour injecter ResourceInfo/SecurityContext
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext; // Utile pour définir le principal/rôles
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Secured // !! Important: Lie ce filtre aux ressources annotées @Secured
@Provider
@Priority(Priorities.AUTHENTICATION) // Exécuté tôt dans la chaîne des filtres
public class SecurityFilter implements ContainerRequestFilter {

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    private  JwtTokenProvider jwtTokenProvider;

    @Context
    private ResourceInfo resourceInfo; // Injecté pour obtenir des infos sur la ressource cible

    // Optionnel: Injecter SecurityContext si on veut le modifier
    @Context
    private SecurityContext currentSecurityContext;

    // Initialiser dans le constructeur ou directement
    public SecurityFilter() {
        this.jwtTokenProvider = new JwtTokenProvider(); // <-- Instancier manuellement ici
        System.out.println("INFO: SecurityFilter created, instantiating JwtTokenProvider manually.");
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // 1. Récupérer l'en-tête Authorization
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // 2. Vérifier si l'en-tête est présent et bien formé (Bearer <token>)
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext, "Credentials required (Bearer token missing or malformed)");
            return;
        }

        // 3. Extraire le token JWT
        String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

        // 4. Valider le token (signature, expiration) en utilisant le Provider
        if (!jwtTokenProvider.validateToken(token)) {
            abortWithUnauthorized(requestContext, "Invalid or expired token");
            return;
        }

        // --- Authentification Réussie ---
        // Le token est valide. Maintenant, vérifions l'autorisation.

        // 5. Extraire les rôles de l'utilisateur depuis le token
        Set<String> userRoles = jwtTokenProvider.getRoles(token);
        // Optionnel: Extraire le nom d'utilisateur pour le SecurityContext
        String username = jwtTokenProvider.getUsername(token);

        // 6. Récupérer les rôles requis par l'annotation @Secured sur la méthode/classe
        Set<String> requiredRoles = extractRolesFromResource();

        // 7. Vérifier si l'utilisateur possède au moins un des rôles requis
        if (!requiredRoles.isEmpty() && !checkPermissions(requiredRoles, userRoles)) {
            // L'utilisateur est authentifié mais n'a pas les bons rôles
            abortWithForbidden(requestContext, "Insufficient permissions");
            return;
        }

        // 8. (Optionnel mais recommandé) Mettre à jour le SecurityContext de la requête
        // Cela permet aux ressources JAX-RS d'utiliser requestContext.getSecurityContext().isUserInRole(...)
        // et requestContext.getSecurityContext().getUserPrincipal()
        final SecurityContext securityContext = requestContext.getSecurityContext();
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                // Retourne un Principal basé sur le nom d'utilisateur du token
                return () -> username;
            }

            @Override
            public boolean isUserInRole(String role) {
                // Vérifie si l'utilisateur a le rôle demandé (basé sur le token)
                return userRoles.contains(role);
            }

            @Override
            public boolean isSecure() {
                // Délègue au contexte original (typiquement, vérifie si HTTPS)
                return securityContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                // Indique que l'authentification s'est faite via Bearer token
                return AUTHENTICATION_SCHEME;
            }
        });

        // Si on arrive ici, l'utilisateur est authentifié ET autorisé.
        // La requête continue vers la méthode de la ressource JAX-RS.
        System.out.println("ACCESS GRANTED - User: " + username + ", Roles: " + userRoles + ", Required: " + requiredRoles);
    }

    // Vérifie si l'en-tête Authorization commence bien par "Bearer "
    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.toLowerCase()
                .startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    // Arrête la requête avec une réponse 401 Unauthorized
    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, AUTHENTICATION_SCHEME + " realm=\"example\"") // Important pour les clients
                        .entity(message)
                        .type("text/plain")
                        .build());
    }

    // Arrête la requête avec une réponse 403 Forbidden
    private void abortWithForbidden(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                        .entity(message)
                        .type("text/plain")
                        .build());
    }

    // Extrait les rôles définis dans l'annotation @Secured de la méthode ou de la classe
    private Set<String> extractRolesFromResource() {
        Method resourceMethod = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();
        Set<String> roles = new HashSet<>();

        // Priorité à l'annotation sur la méthode
        if (resourceMethod != null && resourceMethod.isAnnotationPresent(Secured.class)) {
            Secured secured = resourceMethod.getAnnotation(Secured.class);
            roles.addAll(Arrays.asList(secured.roles()));
        }
        // Sinon, chercher sur la classe
        else if (resourceClass != null && resourceClass.isAnnotationPresent(Secured.class)) {
            Secured secured = resourceClass.getAnnotation(Secured.class);
            roles.addAll(Arrays.asList(secured.roles()));
        }

        // Filtrer les chaînes vides ou nulles potentiellement issues de `default {}`
        return roles.stream()
                .filter(role -> role != null && !role.trim().isEmpty())
                .collect(Collectors.toSet());
    }

    // Vérifie si l'ensemble des rôles utilisateur contient au moins un des rôles requis
    private boolean checkPermissions(Set<String> requiredRoles, Set<String> userRoles) {
        // Si @Secured n'a pas spécifié de rôles (requiredRoles est vide),
        // alors l'authentification suffit.
        if (requiredRoles.isEmpty()) {
            return true;
        }
        // Sinon, il faut une intersection entre les rôles requis et les rôles de l'utilisateur
        return userRoles.stream().anyMatch(requiredRoles::contains);
        // Alternative: return !Collections.disjoint(userRoles, requiredRoles);
    }
}