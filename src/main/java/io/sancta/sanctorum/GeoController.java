package io.sancta.sanctorum;

import io.sancta.sanctorum.dao.CityDAO;
import io.sancta.sanctorum.dao.CountryDAO;

import io.sancta.sanctorum.domain.City;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;

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

    public void run() {
        List<City> cities = fetchData();
        cities.forEach(System.out::println);
    }

    private SessionFactory prepareRelationalDataBase() {
        return new Configuration().configure().buildSessionFactory();
    }

    private List<City> fetchData() {
        List<City> allCity = new ArrayList<>();

        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            int totalCount = cityDao.getTotalCount();
            int step = 500;
            for (int i = 0; i < totalCount; i = i + step) {
                List<City> items = cityDao.getItems(i, step);
                allCity.addAll(items);
            }
            session.getTransaction().commit();
        }
        return allCity;
    }
}
