package Web;

import org.glassfish.jersey.server.ResourceConfig;

public class Configuration extends ResourceConfig {
    public Configuration(){
        register(CorsFilter.class);
    }
}
