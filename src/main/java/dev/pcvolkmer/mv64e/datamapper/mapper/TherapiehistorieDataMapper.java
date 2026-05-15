/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026  Paul-Christian Volkmer
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

package dev.pcvolkmer.mv64e.datamapper.mapper;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.EinzelempfehlungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.FollowUpCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapielinieCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapieplanCatalogue;
import dev.pcvolkmer.mv64e.mtb.SystemicTherapy;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

/**
 * Mapper class to load and map therapy history data from database table 'dk_dnpm_therapieplan',
 * 'dk_dnpm_uf_einzelempfehlung', 'dk_dnpm_followup' and 'dk_dnpm_therapielinie'
 *
 * @author Paul-Christian Volkmer
 * @since 0.8
 */
public class TherapiehistorieDataMapper implements DataMapper<List<SystemicTherapy>> {

  private final KpaTherapielinieDataMapper therapielinieMapper;

  private final TherapieplanCatalogue therapieplanCatalogue;

  private final EinzelempfehlungCatalogue einzelempfehlungCatalogue;

  private final FollowUpCatalogue followUpCatalogue;

  private final TherapielinieCatalogue therapielinieCatalogue;

  public TherapiehistorieDataMapper(
      KpaTherapielinieDataMapper therapielinieMapper,
      TherapieplanCatalogue therapieplanCatalogue,
      EinzelempfehlungCatalogue einzelempfehlungCatalogue,
      FollowUpCatalogue followUpCatalogue,
      TherapielinieCatalogue therapielinieCatalogue) {
    this.therapieplanCatalogue = therapieplanCatalogue;
    this.einzelempfehlungCatalogue = einzelempfehlungCatalogue;
    this.therapielinieMapper = therapielinieMapper;
    this.therapielinieCatalogue = therapielinieCatalogue;
    this.followUpCatalogue = followUpCatalogue;
  }

  @NullMarked
  @Override
  public List<SystemicTherapy> getById(int id) {
    return therapieplanCatalogue.getByKpaId(id).stream()
        .flatMap(
            carePlanId ->
                this.einzelempfehlungCatalogue.getAllByParentId(carePlanId).stream()
                    .filter(it -> "systemisch".equals(it.getString("empfehlungskategorie")))
                    .map(ResultSet::getId)
                    .distinct()
                    .filter(Objects::nonNull)
                    .map(this::mapSystemicTherapiesFromRecommendation))
        .collect(Collectors.toList());
  }

  @NullMarked
  private SystemicTherapy mapSystemicTherapiesFromRecommendation(int recommendationId) {
    var systemicTherapies =
        this.followUpCatalogue.getByRecommendationId(recommendationId).stream()
            .map(therapielinieCatalogue::getAllByParentId)
            .flatMap(therapies -> therapies.stream().map(ResultSet::getId).filter(Objects::nonNull))
            .map(this.therapielinieMapper::getById)
            .filter(Objects::nonNull)
            .filter(
                mtbSystemicTherapy ->
                    null != mtbSystemicTherapy.getPeriod()
                        && null != mtbSystemicTherapy.getPeriod().getStart())
            .sorted(
                Comparator.comparing(
                    mtbSystemicTherapy -> mtbSystemicTherapy.getPeriod().getStart()))
            .collect(Collectors.toList());
    return SystemicTherapy.builder().history(systemicTherapies).build();
  }
}
