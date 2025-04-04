package Dao;

import Metier.Categorie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.Collections;
import java.util.List;

public class RepositoryCategorie {

    private Session session = new Session(); // Utilise la même session factory

    public List<Categorie> findAll() {
        EntityManager em = null;
        try {
            em = session.EntityManager();
            return em.createQuery("SELECT c FROM Categorie c ORDER BY c.nom", Categorie.class).getResultList();
        } catch (Exception e) {
            System.err.println("Error fetching all categories: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    // Ajoutez save, update, delete, findById si nécessaire...
}