package Dao;


import jakarta.persistence.EntityManager;
import Metier.Avis;
import Metier.Client;
import Metier.Produit;

import java.util.ArrayList;
import java.util.List;


public class RepositoryAvis {
    private Session session=new Session();
    private EntityManager em=session.EntityManager();

    public void save(Avis avis){
        var em=session.EntityManager();
        var tran=em.getTransaction();
        try {
            tran.begin();
            em.persist(avis);
            tran.commit();
        }catch (Exception e){
            if (tran.isActive())
                tran.rollback();
        }
    }

    public void remove(Avis avis){
        var em=session.EntityManager();
        var tran=em.getTransaction();
        try {
            tran.begin();
            var temp_avis=em.find(Avis.class,avis);
            if (temp_avis != null)
                   em.remove(temp_avis);

            tran.commit();
        }catch (Exception e){
            if (tran.isActive())
                tran.rollback();
        }
    }

    public List<Produit> getProduiByClient(Client client){
        var tran=em.getTransaction();
        List<Produit> produits=new ArrayList<>();
        try {
            tran.begin();
            var query=em.createQuery("select produit from " +
                    "Avis .produit produit where Avis.client.id= :id", Produit.class);
            query.setHint("id",client.getId());
            produits.addAll(query.getResultList());
            tran.commit();
        }catch (Exception e){
            if (tran.isActive())
                tran.rollback();
        }

        return produits;
    }

    public List<Client> getProduiByUser(Client client){
        var tran=em.getTransaction();
        List<Client> clients=new ArrayList<>();
        try {
            tran.begin();
            var query=em.createQuery("select client from " +
                    "Avis.client client where Avis.produit.id=:id", Client.class);
            query.setHint("id",client.getId());
            clients.addAll(query.getResultList());
            tran.commit();
        }catch (Exception e){
            if (tran.isActive())
                tran.rollback();
        }

        return clients;
    }
}

