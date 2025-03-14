package Dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import Metier.Commande;
import Metier.LigneCommande;
import Metier.Produit;

import java.util.ArrayList;
import java.util.List;

public class RepositoryLigneCommande {
    private Session session = new Session();

    public void save(LigneCommande ligne) {
        EntityManager em = session.EntityManager();
        try {
            em.getTransaction().begin();
            em.persist(ligne);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        }
    }


    public List<Produit> getProduitsByCommande(Commande commande) {
        EntityManager em = session.EntityManager();
        List<Produit> produits = new ArrayList<>();
        try {
            TypedQuery<Produit> query = em.createQuery(
                    "SELECT lcmd.produit FROM LigneCommande lcmd WHERE lcmd.commande.id = :commandeId",
                    Produit.class
            );
            query.setParameter("commandeId", commande.getId());
            produits = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return produits;
    }


    public List<Commande> getCommandsByProduit(Produit produit) {
        EntityManager em = session.EntityManager();
        List<Commande> commandes = new ArrayList<>();
        try {
            TypedQuery<Commande> query = em.createQuery(
                    "SELECT lcmd.commande FROM LigneCommande lcmd WHERE lcmd.produit.id = :produitId",
                    Commande.class
            );
            query.setParameter("produitId", produit.getId());
            commandes = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commandes;
    }


    public void remove(LigneCommande ligneCommande) {
        EntityManager em = session.EntityManager();
        try {
            em.getTransaction().begin();
            LigneCommande lc = em.find(LigneCommande.class, ligneCommande.getId());
            if (lc != null) {
                em.remove(lc);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        }
    }
}
