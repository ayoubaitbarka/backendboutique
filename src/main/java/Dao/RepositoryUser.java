package Dao;

import jakarta.persistence.EntityManager;
import Metier.Commande;
import Metier.Livreur;
import Metier.User;

import java.util.ArrayList;
import java.util.List;

public class RepositoryUser {
    Session session = new Session();
    EntityManager em = session.EntityManager();

    public void save(User user) {


        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void remove(User user) {

        try {
            em.getTransaction().begin();

            User usr = em.find(User.class, user.getId());
            if (usr != null) {
                em.remove(usr);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public User getUserByName(String name) {

        try {
            return em.createQuery("SELECT u FROM User u WHERE u.nom = :name", User.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public void updateUser(User user) {

        try {
            em.getTransaction().begin();
            var us=em.find(User.class,user);
            em.merge(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public List<Commande> getCommandesNonLivrer(Livreur livreur){
        List<Commande> commandes=new ArrayList<>();
        try {
            em.getTransaction().begin();
            var query=em.createQuery("select l.commandes from Livreur l" +
                            " where l.id=:id",Commande.class)
                    .setParameter("id",livreur.getId()) ;

            commandes.addAll(query.getResultList());
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        return commandes;
    }
}
