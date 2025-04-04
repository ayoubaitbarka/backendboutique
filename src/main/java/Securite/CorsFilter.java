package Securite; // Ou votre package approprié

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = "/api/*") // Cible les requêtes vers votre API
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("INFO: CORS Filter initialized.");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // IMPORTANT: Mettez ici l'origine EXACTE de votre frontend React en développement
        // Par exemple 'http://localhost:5173' ou 'http://localhost:3000'
        // Pour la production, ce sera l'URL de votre site déployé.
        // Utiliser "*" est moins sûr mais fonctionne pour les tests rapides.
        String allowedOrigin = "http://localhost:3000"; // <- METTEZ VOTRE PORT FRONTEND ICI ! (ou '*')
        response.setHeader("Access-Control-Allow-Origin", allowedOrigin);

        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization, X-Requested-With, remember-me");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");

        System.out.println("DEBUG: CORS Filter - Request URI: " + request.getRequestURI() + ", Method: " + request.getMethod() + ", Origin: " + request.getHeader("Origin"));

        // Gérer la requête preflight OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("DEBUG: CORS Filter - OPTIONS request detected. Responding OK.");
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // Passer la requête aux autres filtres/servlets
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        // Nettoyage si nécessaire
    }
}