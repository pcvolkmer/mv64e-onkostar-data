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

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.*;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import dev.pcvolkmer.mv64e.datamapper.mapper.exceptionhandler.TryAndLog;
import dev.pcvolkmer.mv64e.model.*;
import java.util.*;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper class to load and map therapy history data from database table 'dk_dnpm_therapieplan',
 * 'dk_dnpm_uf_einzelempfehlung', 'dk_dnpm_followup' and 'dk_dnpm_therapielinie'
 *
 * @author Paul-Christian Volkmer
 * @since 0.8
 */
public class StudieneinschlusshistorieDataMapper
    implements DataMapper<List<PatientRecordStudyInclusionsInner>> {

  private final Logger logger = LoggerFactory.getLogger(StudieneinschlusshistorieDataMapper.class);

  private final StudieneinschlussCatalogue studieneinschlussCatalogue;

  private final TherapieplanCatalogue therapieplanCatalogue;

  private final EinzelempfehlungCatalogue einzelempfehlungCatalogue;

  private final FollowUpCatalogue followUpCatalogue;

  private final PropertyCatalogue propertyCatalogue;

  public StudieneinschlusshistorieDataMapper(
      StudieneinschlussCatalogue studieneinschlussCatalogue,
      TherapieplanCatalogue therapieplanCatalogue,
      EinzelempfehlungCatalogue einzelempfehlungCatalogue,
      FollowUpCatalogue followUpCatalogue,
      PropertyCatalogue propertyCatalogue) {
    this.studieneinschlussCatalogue = studieneinschlussCatalogue;
    this.therapieplanCatalogue = therapieplanCatalogue;
    this.einzelempfehlungCatalogue = einzelempfehlungCatalogue;
    this.followUpCatalogue = followUpCatalogue;
    this.propertyCatalogue = propertyCatalogue;
  }

  @NullMarked
  @Override
  public List<PatientRecordStudyInclusionsInner> getById(int id) {
    return therapieplanCatalogue.getByKpaId(id).stream()
        .flatMap(
            carePlanId ->
                this.einzelempfehlungCatalogue.getAllByParentId(carePlanId).stream()
                    .map(ResultSet::getId)
                    .distinct()
                    .filter(Objects::nonNull)
                    .map(
                        recommendationId ->
                            mapStudyInclusionsFromRecommendation(recommendationId, id)))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Nullable
  private PatientRecordStudyInclusionsInner mapStudyInclusionsFromRecommendation(
      int recommendationId, int reasonId) {
    var studyInclusions =
        this.followUpCatalogue.getByRecommendationId(recommendationId).stream()
            .map(studieneinschlussCatalogue::getAllByParentId)
            .flatMap(Collection::stream)
            .map(resultSet -> mapStudyInclusion(resultSet, reasonId))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    if (studyInclusions.isEmpty()) {
      return null;
    }
    return PatientRecordStudyInclusionsInner.builder().history(studyInclusions).build();
  }

  @Nullable
  private StudyInclusion mapStudyInclusion(ResultSet resultSet, int reasonId) {
    var builder =
        StudyInclusion.builder()
            .id(resultSet.getString("id"))
            .patient(resultSet.getPatientReference())
            .reason(Reference.builder().id(String.valueOf(reasonId)).type("MTBDiagnosis").build());
    try {
      var erfassungsdatum = resultSet.getDate("erfassungsdatum");
      if (null == erfassungsdatum) {
        logger.warn("Cannot map 'Therapielinie': 'Erfassungsdatum' is missing");
        return null;
      }

      TryAndLog.tryAndLogWithResult(() -> this.getPeriodDate(resultSet))
          .andTry(value -> value.ifPresent(builder::period));

      var studyReferenceBuilder = StudyReference.builder().type("Study");

      final var studie = resultSet.getString("studie");
      if (null == studie) {
        logger.warn("Cannot map 'Studieneinschluss': 'Studie' is missing");
        return null;
      }
      studyReferenceBuilder.id(studie);

      final var studiensystem = resultSet.getString("studiensystem");
      if (null == studiensystem) {
        logger.warn("Cannot map 'Studieneinschluss': 'Studiensystem' is missing");
        return null;
      }
      studyReferenceBuilder.system(EinzelempfehlungStudieDataMapper.getStudySystem(studiensystem));

      builder.study(List.of(studyReferenceBuilder.build()));

      builder
          .recordedOn(erfassungsdatum)
          .medication(JsonToMedicationMapper.map(resultSet.getString("wirkstoffe_json")));

      resultSet.ifPropertyNotNull(
          "status",
          String.class,
          (value, version) -> builder.status(getTherapyStatusCoding(value, version)));

      resultSet.ifPropertyNotNull(
          "statusgrund",
          String.class,
          (value, version) ->
              builder.statusReason(getMtbTherapyStatusReasonCoding(value, version)));

      resultSet.ifValueNotNull(
          "ref_einzelempfehlung",
          String.class,
          id ->
              builder.basedOn(
                  Reference.builder().id(id).type("MTBStudyEnrollmentRecommendation").build()));

      return builder.build();
    } catch (Exception e) {
      throw new DataAccessException(
          String.format("Cannot map MtbSystemicTherapy! %s", e.getMessage()));
    }
  }

  @NullMarked
  private Optional<PeriodDate> getPeriodDate(ResultSet resultSet) {
    var pdb = PeriodDate.builder();
    final var beginn = resultSet.getDate("beginn");
    if (null == beginn) {
      return Optional.empty();
    }
    pdb.start(beginn);
    if (resultSet.getDate("ende") != null) pdb.end(resultSet.getDate("ende"));
    return Optional.of(pdb.build());
  }

  @Nullable
  private TherapyStatusCoding getTherapyStatusCoding(String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(TherapyStatusCoding.CodeEnum.values())
            .map(TherapyStatusCoding.CodeEnum::toString)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        TherapyStatusCoding.builder()
            .system("dnpm-dip/therapy/status")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());

    switch (value) {
      case "not-done":
        resultBuilder.code(TherapyStatusCoding.CodeEnum.NOT_DONE);
        break;
      case "on-going":
        resultBuilder.code(TherapyStatusCoding.CodeEnum.ON_GOING);
        break;
      case "stopped":
        resultBuilder.code(TherapyStatusCoding.CodeEnum.STOPPED);
        break;
      case "completed":
        resultBuilder.code(TherapyStatusCoding.CodeEnum.COMPLETED);
        break;
      default:
        return null;
    }

    return resultBuilder.build();
  }

  @Nullable
  private MtbTherapyStatusReasonCoding getMtbTherapyStatusReasonCoding(
      String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(MtbTherapyStatusReasonCoding.CodeEnum.values())
            .map(MtbTherapyStatusReasonCoding.CodeEnum::toString)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        MtbTherapyStatusReasonCoding.builder()
            .system("dnpm-dip/therapy/status-reason")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());

    try {
      resultBuilder.code(MtbTherapyStatusReasonCoding.CodeEnum.fromValue(value));
    } catch (IllegalArgumentException e) {
      return null;
    }

    return resultBuilder.build();
  }
}
