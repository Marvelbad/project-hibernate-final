package io.sancta.sanctorum.redis;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Builder
@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Language {

    String language;

    Boolean isOfficial;

    BigDecimal percentage;

}
