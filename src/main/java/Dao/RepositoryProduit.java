package Dao;

import Metier.Categorie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction; // Import pour la transaction manuelle
// Pas d'imports CDI ou Transactional ici
import Metier.Produit;
import jakarta.persistence.TypedQuery;

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






      // nouvelle methodes pour faire la liés avec le front :************************************************

    /**
     * Récupère tous les produits marqués comme "featured".
     * @return Une liste de produits "featured", ou une liste vide en cas d'erreur.
     */
    public List<Produit> findFeatured() {
        EntityManager em = null;
        try {
            em = session.EntityManager();
            // Pas de transaction nécessaire pour une lecture simple
            return em.createQuery("SELECT p FROM Produit p WHERE p.featured = true", Produit.class)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error fetching featured produits: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // Retourner une liste vide en cas d'erreur
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }


    /**
     * Récupère les produits appartenant à une catégorie spécifique, avec une limite optionnelle.
     * @param categoryId L'ID de la catégorie.
     * @param limit Le nombre maximum de produits à retourner (null ou <= 0 pour ignorer la limite).
     * @return Une liste de produits de la catégorie, ou une liste vide en cas d'erreur.
     */
    public List<Produit> findByCategoryId(Long categoryId, Integer limit) {
        EntityManager em = null;
        try {
            em = session.EntityManager();
            // Pas de transaction nécessaire pour une lecture
            TypedQuery<Produit> query = em.createQuery(
                    "SELECT p FROM Produit p WHERE p.categorie.id = :catId ORDER BY p.nom ASC", // Ajout d'un tri par défaut
                    Produit.class
            );
            query.setParameter("catId", categoryId);

            if (limit != null && limit > 0) {
                query.setMaxResults(limit); // Appliquer la limite si fournie
            }

            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error fetching produits by category ID " + categoryId + ": " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }


    // --- MÉTHODE updateProduit MODIFIÉE (TRÈS IMPORTANT) ---
    /**
     * Met à jour un produit existant avec les nouvelles données fournies.
     * Gère la mise à jour de tous les champs, y compris la relation catégorie.
     *
     * @param id L'ID du produit à mettre à jour.
     * @param produitData Les nouvelles données du produit (peut être détaché).
     * @return Un Optional contenant le produit mis à jour et managé, ou Optional.empty() si non trouvé ou en cas d'erreur.
     */
    public Optional<Produit> updateProduit(Long id, Produit produitData) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = session.EntityManager();
            tx = em.getTransaction();
            tx.begin();

            Produit existingProduit = em.find(Produit.class, id); // Trouver l'entité existante

            if (existingProduit != null) {
                // Mettre à jour tous les champs pertinents depuis produitData
                existingProduit.setNom(produitData.getNom());
                existingProduit.setDescription(produitData.getDescription()); // Ajouter description
                existingProduit.setPrix(produitData.getPrix()); // Assurez-vous que produitData a un BigDecimal
                existingProduit.setQuantiteStock(produitData.getQuantiteStock());
                existingProduit.setImageUrl(produitData.getImageUrl());     // Ajouter imageUrl
                existingProduit.setRating(produitData.getRating());         // Ajouter rating
                existingProduit.setFeatured(produitData.isFeatured());     // Ajouter featured (utiliser isFeatured pour boolean)

                // Mettre à jour la catégorie (si fournie et valide)
                if (produitData.getCategorie() != null && produitData.getCategorie().getId() != null) {
                    Categorie newCategorie = em.find(Categorie.class, produitData.getCategorie().getId());
                    if (newCategorie != null) {
                        existingProduit.setCategorie(newCategorie); // Attacher la catégorie managée trouvée
                    } else {
                        // Gérer le cas où l'ID de catégorie fourni n'existe pas
                        System.err.println("Update Error: Category with ID " + produitData.getCategorie().getId() + " not found.");
                        tx.rollback(); // Annuler la transaction car la catégorie est invalide
                        return Optional.empty(); // Retourner vide pour indiquer l'échec
                    }
                } else if (produitData.getCategorie() == null || produitData.getCategorie().getId() == null){
                    // Si aucune info de catégorie n'est fournie dans la requête,
                    // on ne change pas la catégorie existante.
                    // Si vous *voulez* permettre de rendre la catégorie null (si la BDD l'autorise),
                    // il faudrait ajouter un cas ici: existingProduit.setCategorie(null);
                    // MAIS notre BDD a id_cat NOT NULL, donc on ne peut pas la mettre à null.
                    System.out.println("INFO: No category update requested or category ID missing in request for product ID " + id);
                }

                // Pas besoin de em.merge() car existingProduit est déjà managé.
                // Les changements seront automatiquement détectés et persistés au commit.
                tx.commit(); // Valider la transaction
                return Optional.of(existingProduit); // Retourner l'entité mise à jour
            } else {
                tx.rollback(); // Annuler si le produit n'a pas été trouvé
                return Optional.empty(); // Produit non trouvé
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback(); // Annuler en cas d'erreur
            }
            System.err.println("Error updating produit with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Optional.empty(); // Retourner vide en cas d'erreur
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Fermer l'EntityManager
            }
        }
    }









}