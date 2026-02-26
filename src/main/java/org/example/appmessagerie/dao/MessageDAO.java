package org.example.appmessagerie.dao;

import org.example.appmessagerie.entities.Message;
import org.example.appmessagerie.entities.User;
import org.example.appmessagerie.utils.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class MessageDAO {

    public void save(Message message) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(message);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void update(Message message) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(message);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Message> findConversation(User u1, User u2) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT m FROM Message m WHERE (m.sender = :u1 AND m.receiver = :u2) OR (m.sender = :u2 AND m.receiver = :u1) ORDER BY m.dateEnvoi ASC", Message.class)
                    .setParameter("u1", u1)
                    .setParameter("u2", u2)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Message> findUnreadMessages(User receiver) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT m FROM Message m WHERE m.receiver = :receiver AND m.status = 'ENVOYE' ORDER BY m.dateEnvoi ASC", Message.class)
                    .setParameter("receiver", receiver)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
