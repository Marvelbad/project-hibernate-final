package io.sancta.sanctorum;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.sancta.sanctorum.dao.CityDAO;
import io.sancta.sanctorum.dao.CountryDAO;

import io.sancta.sanctorum.domain.City;
import io.sancta.sanctorum.domain.Country;
import io.sancta.sanctorum.redis.CityCountry;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeoController {

    SessionFactory sessionFactory;
    CityDAO cityDao;
    CountryDAO countryDao;

    RedisClient redisClient;

    public GeoController() {
        sessionFactory = prepareRelationalDataBase();
        cityDao = new CityDAO(sessionFactory);
        countryDao = new CountryDAO(sessionFactory);

        redisClient = prepareRedisClient();
    }

    public void run() {
        List<City> cities = fetchData();
        List<CityCountry> cityCountries = transformData(cities);

        sessionFactory.getCurrentSession().close();

        shutdown();
    }

    private SessionFactory prepareRelationalDataBase() {
        return new Configuration().configure().buildSessionFactory();
    }

    private RedisClient prepareRedisClient() {
        RedisClient redisClient = RedisClient.create(RedisURI.create("localhost", 6379));

        try (StatefulRedisConnection<String, String> connect = redisClient.connect()) {
            System.out.println("Connect to redis");
        }

        return redisClient;
    }

    private void shutdown() {
        if (Objects.nonNull(sessionFactory)) sessionFactory.close();

        if (Objects.nonNull(redisClient)) redisClient.close();
    }

    private List<City> fetchData() {
        List<City> allCity = new ArrayList<>();

        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            List<Country> countries = countryDao.getAll();

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

    private List<CityCountry> transformData(List<City> cities) {
        return null;
    }
}
