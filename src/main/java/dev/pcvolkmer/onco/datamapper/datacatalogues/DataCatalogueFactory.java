/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2025  Paul-Christian Volkmer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package dev.pcvolkmer.onco.datamapper.datacatalogues;

import dev.pcvolkmer.onco.datamapper.exceptions.DataCatalogueCreationException;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Simple catalogue factory to get a catalogue instance
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
@NullMarked
public class DataCatalogueFactory {

  private final JdbcTemplate jdbcTemplate;
  private final Map<Class<? extends DataCatalogue>, DataCatalogue> catalogues = new HashMap<>();

  private DataCatalogueFactory(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Nullable private static DataCatalogueFactory obj;

  public static synchronized DataCatalogueFactory initialize(final JdbcTemplate jdbcTemplate) {
    if (null == obj) {
      obj = new DataCatalogueFactory(jdbcTemplate);
    }
    return obj;
  }

  public static synchronized DataCatalogueFactory instance() {
    if (null == obj) {
      throw new IllegalStateException("CatalogueFactory not initialized");
    }
    return obj;
  }

  /**
   * Get Catalogue of required type
   *
   * @param clazz The catalogues class
   * @param <T> The catalogue type
   * @return The catalogue if it exists
   */
  @SuppressWarnings("unchecked")
  public synchronized <T extends DataCatalogue> T catalogue(Class<T> clazz) {
    return (T)
        catalogues.computeIfAbsent(
            clazz,
            c -> {
              if (c == EcogCatalogue.class) {
                return EcogCatalogue.create(jdbcTemplate);
              } else if (c == HistologieCatalogue.class) {
                return HistologieCatalogue.create(jdbcTemplate);
              } else if (c == KpaCatalogue.class) {
                return KpaCatalogue.create(jdbcTemplate);
              } else if (c == PatientCatalogue.class) {
                return PatientCatalogue.create(jdbcTemplate);
              } else if (c == ProzedurCatalogue.class) {
                return ProzedurCatalogue.create(jdbcTemplate);
              } else if (c == TherapielinieCatalogue.class) {
                return TherapielinieCatalogue.create(jdbcTemplate);
              } else if (c == TumorausbreitungCatalogue.class) {
                return TumorausbreitungCatalogue.create(jdbcTemplate);
              } else if (c == TumorgradingCatalogue.class) {
                return TumorgradingCatalogue.create(jdbcTemplate);
              } else if (c == VerwandteCatalogue.class) {
                return VerwandteCatalogue.create(jdbcTemplate);
              } else if (c == VorbefundeCatalogue.class) {
                return VorbefundeCatalogue.create(jdbcTemplate);
              } else if (c == TherapieplanCatalogue.class) {
                return TherapieplanCatalogue.create(jdbcTemplate);
              } else if (c == EinzelempfehlungCatalogue.class) {
                return EinzelempfehlungCatalogue.create(jdbcTemplate);
              } else if (c == MolekulargenetikCatalogue.class) {
                return MolekulargenetikCatalogue.create(jdbcTemplate);
              } else if (c == MolekulargenuntersuchungCatalogue.class) {
                return MolekulargenuntersuchungCatalogue.create(jdbcTemplate);
              } else if (c == MolekulargenMsiCatalogue.class) {
                return MolekulargenMsiCatalogue.create(jdbcTemplate);
              } else if (c == MolekularImmunhistoCatalogue.class) {
                return MolekularImmunhistoCatalogue.create(jdbcTemplate);
              } else if (c == MolekularPcrCatalogue.class) {
                return MolekularPcrCatalogue.create(jdbcTemplate);
              } else if (c == RebiopsieCatalogue.class) {
                return RebiopsieCatalogue.create(jdbcTemplate);
              } else if (c == ReevaluationCatalogue.class) {
                return ReevaluationCatalogue.create(jdbcTemplate);
              } else if (c == ConsentMvCatalogue.class) {
                return ConsentMvCatalogue.create(jdbcTemplate);
              } else if (c == ConsentMvVerlaufCatalogue.class) {
                return ConsentMvVerlaufCatalogue.create(jdbcTemplate);
              } else if (c == KeimbahndiagnoseCatalogue.class) {
                return KeimbahndiagnoseCatalogue.create(jdbcTemplate);
              }
              throw new DataCatalogueCreationException(clazz);
            });
  }

  /**
   * Checks if a catalogue of this type is available
   *
   * @param clazz The catalogues class
   * @return true if it is available
   */
  public synchronized boolean hasCatalogue(Class<? extends DataCatalogue> clazz) {
    return catalogues.containsKey(clazz);
  }
}
