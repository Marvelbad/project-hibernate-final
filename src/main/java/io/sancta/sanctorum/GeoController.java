package io.sancta.sanctorum;

import com.google.gson.Gson;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.sancta.sanctorum.dao.CityDAO;
import io.sancta.sanctorum.dao.CountryDAO;

import io.sancta.sanctorum.domain.City;
import io.sancta.sanctorum.domain.Country;
import io.sancta.sanctorum.domain.CountryLanguage;
import io.sancta.sanctorum.redis.CityCountry;
import io.sancta.sanctorum.redis.Language;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeoController {

    SessionFactory sessionFactory;
    CityDAO cityDao;
    CountryDAO countryDao;

    RedisClient redisClient;
    Gson gson;

    public GeoController() {
        sessionFactory = prepareRelationalDataBase();
        cityDao = new CityDAO(sessionFactory);
        countryDao = new CountryDAO(sessionFactory);

        redisClient = prepareRedisClient();
        gson = new Gson().newBuilder()
                .setPrettyPrinting().create();
    }

    public void run() {
        List<City> cities = fetchData();
        List<CityCountry> cityCountries = transformData(cities);
        pushToRedis(cityCountries);

        sessionFactory.getCurrentSession().close();
        benchmarkDatabasePerformance();
        shutdown();
    }

    private void benchmarkDatabasePerformance() {
        List<Integer> ids = List.of(8, 2048, 100, 12, 168, 3421, 1179);

        long startRedis = System.currentTimeMillis();
        testRedisData(ids);
        long stopRedis = System.currentTimeMillis();


        long startMySql = System.currentTimeMillis();
        testMySqlData(ids);
        long stopMySql = System.currentTimeMillis();

        System.out.println("Redis: " + (stopRedis - startRedis) + " ms");
        System.out.println("MySQL: " + (stopMySql - startMySql) + " ms");
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
        return cities.stream()
                .map(city -> CityCountry.builder()
                        .id(city.getId())
                        .name(city.getName())
                        .district(city.getDistrict())
                        .population(city.getPopulation())
                        .countryCode(city.getCountry().getCode())
                        .alternativeCountryCode(city.getCountry().getAlternativeCode())
                        .continent(city.getCountry().getContinent())
                        .countryName(city.getCountry().getName())
                        .continent(city.getCountry().getContinent())
                        .region(city.getCountry().getRegion())
                        .continentSurfaceArea(city.getCountry().getSurfaceArea())
                        .countryPopulation(city.getCountry().getPopulation())
                        .languages(city.getCountry().getLanguages().stream()
                                .map(cl ->
                                        Language.builder()
                                                .language(cl.getLanguage())
                                                .isOfficial(cl.getIsOfficial())
                                                .percentage(cl.getPercentage())
                                                .build()
                                ).collect(Collectors.toSet())
                        ).build()
                ).toList();
    }

    private void pushToRedis(List<CityCountry> data) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {

            RedisCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                sync.set(String.valueOf(cityCountry.getId()), gson.toJson(cityCountry));
            }
        }
    }


    private void testRedisData(List<Integer> ids) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisCommands<String, String> sync = connection.sync();

            for (Integer id : ids) {
                String value = sync.get(String.valueOf(id));
                gson.fromJson(value, CityCountry.class);
            }
        }
    }

    private void testMySqlData(List<Integer> ids) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                City city = cityDao.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();
            }

            session.getTransaction().commit();
        }
    }
}