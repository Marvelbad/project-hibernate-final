package io.sancta.sanctorum;

import io.sancta.sanctorum.dao.CityDAO;
import io.sancta.sanctorum.dao.CountryDAO;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.SessionFactory;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeoController {

    SessionFactory sessionFactory;
    CityDAO cityDao;
    CountryDAO countryDao;

    public GeoController() {
        sessionFactory = prepareRelationalDataBase();


    }

    private SessionFactory prepareRelationalDataBase() {
        return null;
    }
}
