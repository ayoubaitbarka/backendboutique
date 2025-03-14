package Dao;

import jakarta.persistence.EntityManager;
import Metier.Produit;

import java.util.ArrayList;
import java.util.List;

public class RepositoryProduit {
    private Session session = new Session();
    public void save(Produit produit) {
        EntityManager entityManager = session.EntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(produit);
            entityManager.flush();
            entityManager.clear();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()){
                entityManager.getTransaction().rollback();
            }
        }
    }

    public void delete(Produit produit) {
        EntityManager entityManager = session.EntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.remove(produit);
            entityManager.flush();  // Sauvegarde le résultat
            entityManager.clear();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()){
                entityManager.getTransaction().rollback();
            }
        }
    }


    public void updateProduit(Produit produit) {
        EntityManager entityManager = session.EntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.merge(produit); // Merge effectue la mise à jour de l'entité
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()){
                entityManager.getTransaction().rollback();
            }
        }
    }


    public List<Produit> Produits() {
        EntityManager entityManager = session.EntityManager();
        List<Produit> produits = new ArrayList<>();
        try {
            produits = entityManager
                    .createQuery("SELECT p FROM Produit p", Produit.class)
                    .getResultList();
        } catch (Exception e) {

        }
        return produits;
    }


    public Produit getProduitByName(String str){
      EntityManager enm=session.EntityManager();
       Produit produit=enm.createQuery("select p from Produit p where p.nom like :str ",Produit.class).setHint("str","%"+str+"%").getSingleResult();
      return  produit;
    }
}
