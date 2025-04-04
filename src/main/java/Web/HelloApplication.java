package Web;

import Web.rest.AuthResource;
import Web.rest.CategorieResource;
import Web.rest.UserResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import Web.rest.ProduitResource;
import Securite.SecurityFilter;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class HelloApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(ProduitResource.class);
        classes.add(AuthResource.class);
        classes.add(UserResource.class);
        classes.add(SecurityFilter.class);
        classes.add(CategorieResource.class);
        return classes;
    }
}