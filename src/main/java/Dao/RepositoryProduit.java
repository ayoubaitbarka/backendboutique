package Dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction; // Import pour la transaction manuelle
// Pas d'imports CDI ou Transactional ici
import Metier.Produit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RepositoryProduit {

    private Session session = new Session();

    // Pas de @Transactional
    public Produit save(Produit produit) {
        EntityManager em = null;
        EntityTransaction tx = null; // Pour la gestion manuelle de la transaction
        try {
            // Obtenir un EM frais
            em = session.EntityManager();
            tx = em.getTransaction();
            tx.begin(); // Démarrer la transaction

            em.persist(produit); // Persister l'entité

            tx.commit(); // Valider la transaction
            return produit; // L'entité est maintenant persistée et a potentiellement un ID
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback(); // Annuler la transaction en cas d'erreur
            }
            System.err.println("Error saving produit: " + e.getMessage());
            e.printStackTrace();
            // Gérer l'erreur comme approprié (lever une exception, retourner null, etc.)
            return null; // Ou lever une RuntimeException
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // !!! Très important de fermer l'EntityManager !!!
            }
        }
    }

    // Pas de @Transactional
    public boolean deleteById(Long id) {
        EntityTransaction tx = null;
        EntityManager em = null;
        try {
            em = session.EntityManager();
            tx = em.getTransaction();
            tx.begin();

            Produit produit = em.find(Produit.class, id); // Trouver l'entité dans cette transaction
            if (produit != null) {
                em.remove(produit); // Supprimer l'entité managée
                tx.commit();
                return true; // Suppression réussie
            } else {
                tx.rollback(); // Rien à supprimer, on annule la transaction (optionnel mais propre)
                return false; // Produit non trouvé
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Error deleting produit with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    // Pas de @Transactional
    public Optional<Produit> updateProduit(Long id, Produit produitData) {
        EntityManager em =null;
        EntityTransaction tx = null;
        try {
            em = session.EntityManager();
            tx = em.getTransaction();
            tx.begin();

            Produit existingProduit = em.find(Produit.class, id); // Trouver l'existant
            if (existingProduit != null) {
                // Mettre à jour les champs de l'entité managée
                existingProduit.setNom(produitData.getNom());
                existingProduit.setPrix(produitData.getPrix());
                existingProduit.setQuantiteStock(produitData.getQuantiteStock());
                // Pas besoin de merge ou persist ici, les changements sont détectés au commit

                tx.commit(); // Commit applique les changements
                return Optional.of(existingProduit); // Retourner l'entité mise à jour
            } else {
                tx.rollback(); // Annuler si non trouvé
                return Optional.empty(); // Produit non trouvé
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Error updating produit with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

                           // OK : Khdama

    // Les méthodes de lecture n'ont pas besoin de transaction explicite avec RESOURCE_LOCAL
    public List<Produit> findAll() {
        EntityManager  em = session.EntityManager();
        em.getTransaction().begin();
        try {

            List<Produit> produits  =  em.createQuery("SELECT p FROM Produit p", Produit.class)
                    .getResultList();
            em.getTransaction().commit();
            return produits;

        } catch (Exception e) {
            System.err.println("Error fetching all produits: " + e.getMessage());
            e.printStackTrace();
            em.getTransaction().rollback();
            return Collections.emptyList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Toujours fermer l'EM
            }
        }
    }


                            // OK : khdama
    public Produit findById(Long id) {
        EntityManager em = session.EntityManager();
        em.getTransaction().begin();
        try {
            Produit p = em.find(Produit.class, id); // Retourne null si non trouvé
            em.getTransaction().commit();
            return p;
         //   return new  Produit("t9achr", "t9ichra", 12, 12);

        } catch (Exception e) {
            System.err.println("Error fetching produit by ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            em.getTransaction().rollback();
            return null;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
                                   // khdama

    public List<Produit> findProduitByName(String namePattern) {
        EntityManager em = null;
        try {
            em = session.EntityManager();
            // Pas de transaction nécessaire
            return em.createQuery("SELECT p FROM Produit p WHERE lower(p.nom) LIKE lower(:pattern)", Produit.class)
                    .setParameter("pattern", "%" + namePattern + "%")
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error searching produit by name pattern '" + namePattern + "': " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }



    public boolean existsById(Long id) {
        EntityManager em = null;
        try {
            em = session.EntityManager();
            // Pas de transaction nécessaire
            Long count = em.createQuery("SELECT count(p) FROM Produit p WHERE p.id = :id", Long.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            System.err.println("Error checking existence for produit ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }



    // La méthode delete(Produit produit) est moins utile sans contexte managé,
    // mais on peut la laisser si besoin en utilisant merge.
    public void delete(Produit produit) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = session.EntityManager();
            tx = em.getTransaction();
            tx.begin();

            // Si 'produit' vient de l'extérieur, il est détaché.
            // Il faut le rattacher (merge) avant de le supprimer.
            if (!em.contains(produit) && produit.getId() != null) { // Vérifier l'ID pour éviter erreur sur new Produit()
                Produit managedProduit = em.merge(produit);
                em.remove(managedProduit);
            } else if (em.contains(produit)) {
                // Si par chance il était déjà managé (peu probable ici)
                em.remove(produit);
            }
            // Si ni l'un ni l'autre, on ne fait rien (ou on logue une erreur)

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Error deleting produit instance: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}