package Dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Session {
    private static final EntityManagerFactory emf= Persistence.createEntityManagerFactory("boutique");

    public EntityManager EntityManager(){
           EntityManager entityManager;
           entityManager=emf.createEntityManager();
           return entityManager;
    }
}
