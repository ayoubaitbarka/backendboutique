package Dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;



import Metier.Commande;
import Metier.Livreur;
import Metier.User;


import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class RepositoryUser {
    private Session session = new Session();
    // PAS d'injection @PersistenceContext

    /**
     * Persiste un nouvel utilisateur dans la base de données.
     * Gère la transaction manuellement.
     * Le hashage du mot de passe DOIT être fait AVANT d'appeler cette méthode.
     * @param user L'utilisateur à sauvegarder (sans ID initial).
     * @return L'utilisateur persisté avec son ID généré, ou null en cas d'erreur.
     */

                      // OK
    public User save(User user) {
        EntityManager em = session.EntityManager();

        try {
            em.getTransaction().begin(); // Début de la transaction
            em.persist(user);
            em.getTransaction().commit(); // Fin de la transaction
            return user;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Error saving user: " + e.getMessage());
            e.printStackTrace();
            return null; // Indiquer l'échec
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Toujours fermer l'EM
            }
        }
    }


                // Pour faire L'authentification : OK
    /**
     * Gère l'EntityManager manuellement.
     * @param email,motDePasse L'email et le mot de passe de l'utilisateur à rechercher.
     * @return Un Optional contenant l'utilisateur s'il est trouvé, sinon Optional.empty().
     */
    public Optional<User> findForLogin(String email, String motDePasse) {
        EntityManager em = session.EntityManager();
        try {
           em.getTransaction().begin();
            User user = em.createQuery("SELECT u FROM User u WHERE u.email = :email AND u.motDePasse = :motDePasse", User.class)
                    .setParameter("email", email)
                    .setParameter("motDePasse", motDePasse)
                    .getSingleResult(); // Lance NoResultException si non trouvé
            return Optional.of(user);
        } catch (NoResultException e) {
            // Cas attendu si l'utilisateur n'existe pas
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error finding user by email '" + email + "': " + e.getMessage());
            e.printStackTrace();
            return Optional.empty(); // Retourner vide en cas d'autre erreur
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

                                     // OK
    /**
     * Supprime un utilisateur de la base de données en utilisant son objet.
     * Gère la transaction manuellement.
     * Préférer deleteById pour les API REST car l'objet user passé peut être détaché.
     * @param user L'utilisateur à supprimer.
     */
    public void remove(User user) {
        EntityManager em = session.EntityManager();

        try {
            em.getTransaction().begin();



            em.getTransaction().commit();
            }
        catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Error removing user instance: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }


    /**                               // OK
     * Supprime un utilisateur par son ID.
     * Gère la transaction manuellement.
     * @param id L'ID de l'utilisateur à supprimer.
     * @return true si l'utilisateur a été trouvé et supprimé, false sinon.
     */
    public boolean deleteById(Long id) {
        EntityManager em = session.EntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, id); // Trouver dans cette transaction
            if (user != null) {
                em.remove(user);
                em.getTransaction().commit();
                return true;
            } else {
               // Pas trouvé, transaction inutile
                em.getTransaction().rollback();
                return false;
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Error removing user instance: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }



                            // OK :
    /**
     * Recherche un utilisateur par son ID.
     * Gère l'EntityManager manuellement.
     * @param id L'ID de l'utilisateur.
     * @return L'utilisateur trouvé, ou null si non trouvé ou en cas d'erreur.
     */
    public User findById(Long id) {
          EntityManager em = session.EntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, id);
            em.getTransaction().commit();
            return user;
        } catch (Exception e) {
            System.err.println("Error finding user by ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }



                                    // OK :
    /**
     * Recherche un utilisateur par son nom
     * Gère l'EntityManager manuellement.
     * @param name Le nom (username) de l'utilisateur à rechercher.
     * @return Un Optional contenant l'utilisateur s'il est trouvé, sinon Optional.empty().
     */
    public Optional<User> findByUsername(String name) {
            EntityManager em = session.EntityManager();
        try {

           User user = em.createQuery("SELECT u FROM User u WHERE u.nom = :name", User.class)
                    .setParameter("name", name)
                    .getSingleResult();;// Lance NoResultException si non trouvé
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error finding user by username '" + name + "': " + e.getMessage());
            e.printStackTrace();
            return Optional.empty(); // Retourner vide en cas d'autre erreur
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }


                                 // PAs encore :  ba9iiiiiii

    /**
     * Met à jour les informations d'un utilisateur existant.
     * Gère la transaction manuellement.
     * Ne met à jour le mot de passe QUE si userData.getPassword() n'est pas null/vide.
     * Le hashage du nouveau mot de passe doit être fait AVANT d'appeler cette méthode.
     * @param id L'ID de l'utilisateur à mettre à jour.
     * @param userData Un objet User contenant les nouvelles données (le mot de passe peut être null et DOIT être déjà hashé).
     * @return Un Optional contenant l'utilisateur mis à jour si trouvé, sinon Optional.empty().
     */
    public Optional<User> updateUser(Long id, User userData) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = session.EntityManager();
            tx = em.getTransaction();
            tx.begin();

            User existingUser = em.find(User.class, id); // Trouver dans cette transaction
            if (existingUser != null) {
                // Mettre à jour les champs
                existingUser.setNom(userData.getNom());
                existingUser.setEmail(userData.getEmail());
                existingUser.setTelephone(userData.getTelephone());
                existingUser.setRole(userData.getRole());

                // Mise à jour conditionnelle du mot de passe (userData.getPassword() doit être le HASH)
                if (userData.getMotDePasse() != null && !userData.getMotDePasse().trim().isEmpty()) {
                    existingUser.setMotDePasse(userData.getMotDePasse());
                }

                // Pas besoin de em.merge() car existingUser est managé.
                tx.commit(); // Commit enregistre les changements
                return Optional.of(existingUser);
            } else {
                tx.rollback(); // Non trouvé, annuler la transaction
                return Optional.empty();
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Error updating user with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }


                          //   OK
    /**
     * Récupère la liste de tous les utilisateurs.
     * Gère l'EntityManager manuellement.
     * @return Une liste (potentiellement vide) de tous les utilisateurs.
     */
    public List<User> findAll() {
        EntityManager em = session.EntityManager();
        try {
            // Pas de transaction nécessaire pour SELECT
            return em.createQuery("SELECT u FROM User u", User.class).getResultList();
        } catch (Exception e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // Retourner vide en cas d'erreur
        }
        finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }


                                  // PAs encore :  ba9iiiiiii

    /**
     * Récupère les commandes non livrées associées à un livreur spécifique.
     * Gère l'EntityManager manuellement.
     * @param livreur Le livreur concerné.
     * @return Une liste des commandes non livrées du livreur.
     */
    public List<Commande> getCommandesNonLivrer(Livreur livreur) {
        // Vérification simple
        if (livreur == null || livreur.getId() == null) {
            System.err.println("Attempted to get commandes for null or transient Livreur");
            return Collections.emptyList();
        }

        EntityManager em = session.EntityManager();
        try {
            // Pas de transaction nécessaire pour SELECT

            return em.createQuery("SELECT c FROM Commande c WHERE c.livreur = :livreur AND c.statut <> :statutLivre", Commande.class)
                    .setParameter("livreur", livreur) // Passer l'objet directement si relation mappée
                    .setParameter("statutLivre", "LIVREE") // Utiliser un paramètre pour le statut
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error fetching non-delivered commandes for livreur ID " + livreur.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}