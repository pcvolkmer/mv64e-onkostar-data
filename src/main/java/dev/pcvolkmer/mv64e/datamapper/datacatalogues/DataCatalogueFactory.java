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

package dev.pcvolkmer.mv64e.datamapper.datacatalogues;

import dev.pcvolkmer.mv64e.datamapper.exceptions.DataCatalogueCreationException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
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

  private static final Map<
          Class<? extends DataCatalogue>, Function<JdbcTemplate, ? extends DataCatalogue>>
      FACTORIES =
          Map.ofEntries(
              Map.entry(EcogCatalogue.class, EcogCatalogue::create),
              Map.entry(HistologieCatalogue.class, HistologieCatalogue::create),
              Map.entry(KpaCatalogue.class, KpaCatalogue::create),
              Map.entry(PatientCatalogue.class, PatientCatalogue::create),
              Map.entry(ProzedurCatalogue.class, ProzedurCatalogue::create),
              Map.entry(TherapielinieCatalogue.class, TherapielinieCatalogue::create),
              Map.entry(TumorausbreitungCatalogue.class, TumorausbreitungCatalogue::create),
              Map.entry(TumorgradingCatalogue.class, TumorgradingCatalogue::create),
              Map.entry(VerwandteCatalogue.class, VerwandteCatalogue::create),
              Map.entry(VorbefundeCatalogue.class, VorbefundeCatalogue::create),
              Map.entry(TherapieplanCatalogue.class, TherapieplanCatalogue::create),
              Map.entry(EinzelempfehlungCatalogue.class, EinzelempfehlungCatalogue::create),
              Map.entry(MolekulargenetikCatalogue.class, MolekulargenetikCatalogue::create),
              Map.entry(
                  MolekulargenuntersuchungCatalogue.class,
                  MolekulargenuntersuchungCatalogue::create),
              Map.entry(MolekulargenMsiCatalogue.class, MolekulargenMsiCatalogue::create),
              Map.entry(MolekularImmunhistoCatalogue.class, MolekularImmunhistoCatalogue::create),
              Map.entry(MolekularPcrCatalogue.class, MolekularPcrCatalogue::create),
              Map.entry(RebiopsieCatalogue.class, RebiopsieCatalogue::create),
              Map.entry(ReevaluationCatalogue.class, ReevaluationCatalogue::create),
              Map.entry(ConsentMvCatalogue.class, ConsentMvCatalogue::create),
              Map.entry(ConsentMvVerlaufCatalogue.class, ConsentMvVerlaufCatalogue::create),
              Map.entry(KeimbahndiagnoseCatalogue.class, KeimbahndiagnoseCatalogue::create),
              Map.entry(FollowUpCatalogue.class, FollowUpCatalogue::create));

  private final JdbcTemplate jdbcTemplate;
  private final Map<Class<? extends DataCatalogue>, DataCatalogue> catalogues =
      new ConcurrentHashMap<>();

  @Nullable private static DataCatalogueFactory obj;

  private DataCatalogueFactory(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
  }

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
   * Get Catalogue of the required type
   *
   * @param clazz The catalogue class
   * @param <T> The catalogue type
   * @return The catalogue if it exists
   */
  @SuppressWarnings("unchecked")
  public <T extends DataCatalogue> T catalogue(Class<T> clazz) {
    return (T)
        catalogues.computeIfAbsent(
            clazz,
            c -> {
              var factory = FACTORIES.get(c);
              if (factory == null) {
                throw new DataCatalogueCreationException(c);
              }
              return factory.apply(jdbcTemplate);
            });
  }

  /**
   * Checks if a catalogue of this type is supported by the factory.
   *
   * @param clazz The catalogue class
   * @return true if the factory knows how to create this catalogue type
   */
  public boolean hasCatalogue(Class<? extends DataCatalogue> clazz) {
    return FACTORIES.containsKey(clazz);
  }
}
