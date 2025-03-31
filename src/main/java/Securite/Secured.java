package Securite;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NameBinding // Lie cette annotation aux filtres/intercepteurs JAX-RS
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD}) // Applicable aux classes et méthodes
public @interface Secured {
    /**
     * Spécifie les rôles autorisés à accéder à la ressource annotée.
     * Si vide (par défaut), tout utilisateur authentifié est autorisé (après validation du token).
     */
    String[] roles() default {};
}