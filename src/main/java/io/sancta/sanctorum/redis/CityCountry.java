package io.sancta.sanctorum.redis;

import io.sancta.sanctorum.domain.Continent;
import io.sancta.sanctorum.domain.Country;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Builder
@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CityCountry {

    Integer id;

    String name;

    String district;

    Integer population;

    String countryCode;

    String alternativeCountryCode;

    String countryName;

    Continent continent;

    String region;

    BigDecimal continentSurfaceArea;

    Integer countryPopulation;

    Set<Language> languages;

}
