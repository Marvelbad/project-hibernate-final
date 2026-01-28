package io.sancta.sanctorum;

import io.sancta.sanctorum.dao.CityDAO;
import io.sancta.sanctorum.dao.CountryDAO;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeoController {

    SessionFactory sessionFactory;
    CityDAO cityDao;
    CountryDAO countryDao;

    public GeoController() {
        sessionFactory = prepareRelationalDataBase();
        countryDao = new CountryDAO(sessionFactory);
        cityDao = new CityDAO(sessionFactory);
    }

    private SessionFactory prepareRelationalDataBase() {
        return new Configuration().configure().buildSessionFactory();
    }
}
