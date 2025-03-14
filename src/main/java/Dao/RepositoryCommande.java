package Dao;

import jakarta.persistence.EntityManager;
import Metier.Commande;

public class RepositoryCommande {
    Session session = new Session();

    public void save(Commande commande) {
        EntityManager em = session.EntityManager();
        try {
            em.getTransaction().begin();
            em.persist(commande);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }


    public void remove(Commande commande) {
        EntityManager em = session.EntityManager();
        try {
            em.getTransaction().begin();

            Commande cmd = em.find(Commande.class,commande.getId());
            if (cmd != null) {
                em.remove(cmd);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }



    public void updateCommand(Commande commande) {
        EntityManager em = session.EntityManager();
        try {
            em.getTransaction().begin();
            em.merge(commande);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public Commande getCommandeById(Long id) {
        EntityManager em = session.EntityManager();
        try {
            return em.find(Commande.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

}
