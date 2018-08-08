package ru.simcraftwebapi.hibernate;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class HibernateSessionFactory {
    final static Logger logger = LoggerFactory.getLogger(HibernateSessionFactory.class);

    static SessionFactory factory;

    static {
        try {
            factory = new org.hibernate.cfg.Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            logger.error("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    static void shutdown(){
        factory.close();
    }
}
