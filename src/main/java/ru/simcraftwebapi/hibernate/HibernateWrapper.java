package ru.simcraftwebapi.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Singleton
public class HibernateWrapper {
    final static Logger logger = LoggerFactory.getLogger(HibernateWrapper.class);

    static {}

    public static Object getOnlyFirstObject(String className) {
        Session session = HibernateSessionFactory.factory.openSession();
        Transaction tx = null;
        Object result = null;
        logger.debug(String.format("Get only first value from %s", className));
        try {
            tx = session.beginTransaction();
            List objects = session.createQuery("FROM " + className).list();
            for (Iterator iterator = objects.iterator(); iterator.hasNext();) {
                result = iterator.next();
                logger.debug("got value: " + result.toString());
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            logger.error(e.getMessage());
        } finally {
            session.close();
        }
        return result;
    }

    public static List getAllObjects(String className) {
        Session session = HibernateSessionFactory.factory.openSession();
        Transaction tx = null;
        List objects = new ArrayList();
        logger.debug(String.format("Get all values from %s", className));
        try {
            tx = session.beginTransaction();
            objects = session.createQuery("FROM " + className).list();
            logger.debug(String.format("got %s objects", objects.size()));
            tx.commit();
        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            logger.error(e.getMessage());
        } finally {
            session.close();
        }
        return objects;
    }

    public static void saveObject(Object object){
        Session session = HibernateSessionFactory.factory.openSession();
        logger.debug(String.format("saving object to db"));
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(object);
            tx.commit();
            logger.debug("object saved");
        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            logger.error(e.getMessage());
        } finally {
            session.close();
        }
    }

    public static void updateObject(Object object){
        Session session = HibernateSessionFactory.factory.openSession();
        logger.debug(String.format("updating object in db"));
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(object);
            tx.commit();
            logger.debug("object updated");
        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            logger.error(e.getMessage());
        } finally {
            session.close();
        }
    }

    public static void deleteObject(Object object) {
        Session session = HibernateSessionFactory.factory.openSession();
        logger.debug(String.format("deleting object in db"));
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(object);
            tx.commit();
            logger.debug("object deleted");
        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            logger.error(e.getMessage());
        } finally {
            session.close();
        }
    }
}
