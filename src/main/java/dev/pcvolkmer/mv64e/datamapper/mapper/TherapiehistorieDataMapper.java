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
import dev.pcvolkmer.mv64e.model.MtbTherapyStatusReasonCoding;
import dev.pcvolkmer.mv64e.model.PatientRecordSystemicTherapiesInner;
import dev.pcvolkmer.mv64e.model.Reference;
import dev.pcvolkmer.mv64e.model.TherapyStatusCoding;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Mapper class to load and map therapy history data from database table 'dk_dnpm_therapieplan',
 * 'dk_dnpm_uf_einzelempfehlung', 'dk_dnpm_followup' and 'dk_dnpm_therapielinie'
 *
 * @author Paul-Christian Volkmer
 * @since 0.8
 */
public class TherapiehistorieDataMapper
    implements DataMapper<List<PatientRecordSystemicTherapiesInner>> {

  private final FollowUpTherapielinieDataMapper therapielinieMapper;

  private final FollowUpLostTherapielinieDataMapper lostTherapielinieMapper;

  private final TherapieplanCatalogue therapieplanCatalogue;

  private final EinzelempfehlungCatalogue einzelempfehlungCatalogue;

  private final FollowUpCatalogue followUpCatalogue;

  private final TherapielinieCatalogue therapielinieCatalogue;

  public TherapiehistorieDataMapper(
      FollowUpTherapielinieDataMapper therapielinieMapper,
      FollowUpLostTherapielinieDataMapper lostTherapielinieMapper,
      TherapieplanCatalogue therapieplanCatalogue,
      EinzelempfehlungCatalogue einzelempfehlungCatalogue,
      FollowUpCatalogue followUpCatalogue,
      TherapielinieCatalogue therapielinieCatalogue) {
    this.therapieplanCatalogue = therapieplanCatalogue;
    this.lostTherapielinieMapper = lostTherapielinieMapper;
    this.einzelempfehlungCatalogue = einzelempfehlungCatalogue;
    this.therapielinieMapper = therapielinieMapper;
    this.therapielinieCatalogue = therapielinieCatalogue;
    this.followUpCatalogue = followUpCatalogue;
  }

  @NullMarked
  @Override
  public List<PatientRecordSystemicTherapiesInner> getById(int id) {
    return therapieplanCatalogue.getByKpaId(id).stream()
        .flatMap(
            carePlanId ->
                this.einzelempfehlungCatalogue.getAllByParentId(carePlanId).stream()
                    .filter(it -> "systemisch".equals(it.getString("empfehlungskategorie")))
                    .map(ResultSet::getId)
                    .distinct()
                    .filter(Objects::nonNull)
                    .map(
                        recommendationId ->
                            this.mapSystemicTherapiesFromRecommendation(recommendationId, id)))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Nullable
  private PatientRecordSystemicTherapiesInner mapSystemicTherapiesFromRecommendation(
      int recommendationId, int kpaId) {
    var systemicTherapies =
        this.followUpCatalogue.getByRecommendationId(recommendationId).stream()
            .map(therapielinieCatalogue::getAllByParentId)
            .flatMap(therapies -> therapies.stream().map(ResultSet::getId).filter(Objects::nonNull))
            .map(this.therapielinieMapper::getById)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    var lostToFollowUpSystemicTherapy =
        this.followUpCatalogue.getByRecommendationId(recommendationId).stream()
            .map(this.lostTherapielinieMapper::getById)
            .filter(Objects::nonNull)
            .map(
                therapy ->
                    therapy.toBuilder()
                        .reason(
                            Reference.builder()
                                .id(String.valueOf(kpaId))
                                .system("MTBDiagnosis")
                                .build())
                        .build())
            .collect(Collectors.toList());

    if (systemicTherapies.isEmpty()) {
      systemicTherapies.addAll(lostToFollowUpSystemicTherapy);
    } else {
      final var latest = systemicTherapies.get(systemicTherapies.size() - 1);
      latest.setStatus(
          TherapyStatusCoding.builder()
              .code(TherapyStatusCoding.CodeEnum.UNKNOWN)
              .display("Nicht durchgeführt")
              .system("dnpm-dip/therapy/status")
              .build());
      latest.setStatusReason(
          MtbTherapyStatusReasonCoding.builder()
              .code(MtbTherapyStatusReasonCoding.CodeEnum.LOST_TO_FU)
              .display("Lost to follow-up")
              .system("dnpm-dip/therapy/status-reason")
              .build());
      systemicTherapies.add(latest);
    }

    if (systemicTherapies.isEmpty()) {
      return null;
    }
    return PatientRecordSystemicTherapiesInner.builder().history(systemicTherapies).build();
  }
}
