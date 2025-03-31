package Securite;

import Metier.User; // Utilisé dans AuthResource, mais pas directement ici après refactoring generateToken
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys; // Pour la génération de clés sécurisées

// PAS d'import jakarta.enterprise.context.ApplicationScoped; car on n'utilise plus CDI ici

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;

// PAS d'@ApplicationScoped car cette classe sera instanciée manuellement
public class JwtTokenProvider {

    // --- Configuration de la Clé et de la Validité ---

    // IMPORTANT: La clé secrète NE DOIT PAS être codée en dur comme ceci en production.
    // Elle doit provenir d'une configuration sécurisée (variables d'environnement, secret manager, etc.).
    // La clé est générée une fois lors de la création de l'instance de JwtTokenProvider.
    // Keys.secretKeyFor génère une clé aléatoire à chaque fois, ce qui n'est PAS bon
    // si plusieurs instances sont créées ou si l'application redémarre.
    // Vous devez utiliser une clé *persistante* et *sécurisée*.
    // Pour l'instant, nous la générons comme ça pour l'exemple, mais c'est à corriger.
    private final Key key;

    // TODO: Externaliser cette clé ! Utiliser une clé fixe stockée de manière sécurisée.
    // Exemple de clé fixe (à générer une fois et stocker) - NE PAS UTILISER CETTE VALEUR EN PROD
    private static final String SECRET_KEY_EXAMPLE = "VOTRE_CLE_SECRETE_TRES_LONGUE_ET_COMPLEXE_DOIT_ETRE_ICI_AU_MOINS_512_BITS_POUR_HS512";
    // Une meilleure approche serait de lire depuis les variables d'environnement:
    // private static final String SECRET_KEY_STRING = System.getenv("JWT_SECRET_KEY");


    private final long validityInMilliseconds = 3600000; // 1 heure (configurable)

    // Constructeur pour initialiser la clé (ou la lire depuis une config)
    public JwtTokenProvider() {
        // Idéalement, charger la clé depuis une source externe ici.
        // Pour l'exemple, on utilise la clé statique (non idéal mais mieux que Keys.secretKeyFor() à chaque instance)
        // Assurez-vous que la clé est assez longue pour HS512 (64 bytes / 512 bits)
        byte[] keyBytes = SECRET_KEY_EXAMPLE.getBytes(); // Ou lire depuis getenv...
        if (keyBytes.length < 64) {
            System.err.println("FATAL: JWT Secret Key is too short for HS512!");
            // Gérer cette erreur critique (lancer une exception, arrêter l'appli ?)
            throw new IllegalArgumentException("JWT Secret Key is too short for HS512. Must be at least 64 bytes.");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        System.out.println("INFO: JwtTokenProvider initialized."); // Log simple
    }


    /**
     * Génère un token JWT pour un utilisateur donné.
     * Inclut le nom d'utilisateur comme sujet et les rôles dans une claim personnalisée.
     *
     * @param user L'objet User pour lequel générer le token.
     * @return Le token JWT sous forme de chaîne.
     */
    public String generateToken(User user) {
        if (user == null || user.getNom() == null || user.getRole() == null) {
            throw new IllegalArgumentException("Cannot generate token for null user or user with null username/role");
        }

        Claims claims = Jwts.claims().setSubject(user.getNom());

        // Stocker le rôle (ou les rôles si c'était une liste/set)
        // Ici, on suppose que user.getRole() retourne une String simple.
        // Si c'était un Set<String>, on utiliserait .collect(Collectors.toList())
        claims.put("roles", List.of(user.getRole())); // Mettre dans une liste même si un seul rôle pour la cohérence

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS512) // Utiliser la clé et l'algorithme configurés
                .compact();
    }


    /**
     * Valide un token JWT (vérifie la signature et l'expiration).
     *
     * @param token Le token JWT à valider.
     * @return true si le token est valide, false sinon.
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key) // Utiliser la MÊME clé que pour la génération
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Logguer l'erreur peut être utile pour le débogage
            System.err.println("Invalid JWT token received: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrait le nom d'utilisateur (sujet) d'un token JWT valide.
     *
     * @param token Le token JWT.
     * @return Le nom d'utilisateur, ou null si le token est invalide ou ne contient pas de sujet.
     */
    public String getUsername(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Error extracting username from token: " + e.getMessage());
            return null; // Indiquer l'échec de l'extraction
        }
    }

    /**
     * Extrait les rôles (stockés dans la claim "roles") d'un token JWT valide.
     *
     * @param token Le token JWT.
     * @return Un Set contenant les rôles, ou un Set vide si aucun rôle n'est trouvé ou si le token est invalide.
     */
    @SuppressWarnings("unchecked") // Nécessaire pour le cast de List.class sur la claim "roles"
    public Set<String> getRoles(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Collections.emptySet();
        }
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Récupérer la liste stockée dans la claim "roles"
            List<String> rolesList = claims.get("roles", List.class);

            if (rolesList != null) {
                return Set.copyOf(rolesList); // Retourne un Set immuable à partir de la liste
            } else {
                System.err.println("WARN: 'roles' claim not found or not a List in token.");
                return Collections.emptySet(); // Aucun rôle trouvé dans la claim
            }
        } catch (JwtException | IllegalArgumentException | ClassCastException e) {
            // ClassCastException si la claim "roles" n'est pas une List<String>
            System.err.println("Error extracting roles from token: " + e.getMessage());
            return Collections.emptySet(); // Retourner vide en cas d'erreur
        }
    }
}