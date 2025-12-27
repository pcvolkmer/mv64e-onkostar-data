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

package dev.pcvolkmer.mv64e.datamapper.mapper;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.EinzelempfehlungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapieplanCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.IgnorableMappingException;
import dev.pcvolkmer.mv64e.mtb.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractEinzelempfehlungDataMapper<T> extends AbstractSubformDataMapper<T> {

  private static final String GRADING_SYSTEM = "dnpm-dip/mtb/level-of-evidence/grading";
  private static final String ADDENDUM_SYSTEM = "dnpm-dip/mtb/level-of-evidence/addendum";

  protected final TherapieplanCatalogue therapieplanCatalogue;
  protected final Logger log;

  protected AbstractEinzelempfehlungDataMapper(
      final EinzelempfehlungCatalogue einzelempfehlungCatalogue,
      final TherapieplanCatalogue therapieplanCatalogue,
      final Logger log) {
    super(einzelempfehlungCatalogue);
    this.therapieplanCatalogue = therapieplanCatalogue;
    this.log = log;
  }

  @NullMarked
  protected Date getCarePlanDate(ResultSet carePlanResultSet) {
    var date = carePlanResultSet.getDate("datum");
    if (null != date) {
      return date;
    }
    throw new IgnorableMappingException("Cannot map datum for ProcedureRecommendation");
  }

  @NullMarked
  protected String getCarePlanKpaId(ResultSet carePlanResultSet) {
    var kpaId = carePlanResultSet.getString("ref_dnpm_klinikanamnese");
    if (null != kpaId) {
      return kpaId;
    }
    throw new IgnorableMappingException("Cannot map KPA as Diagnosis");
  }

  @Nullable
  protected RecommendationPriorityCoding getRecommendationPriority(ResultSet resultSet) {
    var prio = resultSet.getInteger("prio");
    if (null == prio) {
      throw new IgnorableMappingException(
          String.format("No priority found for recommendation %s", resultSet.getId()));
    }
    return getRecommendationPriorityCoding(resultSet.getInteger("prio"));
  }

  protected RecommendationPriorityCoding getRecommendationPriorityCoding(
      @NonNull String code, int version) {
    if (!Arrays.stream(RecommendationPriorityCodingCode.values())
        .map(RecommendationPriorityCodingCode::toValue)
        .collect(Collectors.toSet())
        .contains(code)) {
      return null;
    }

    var resultBuilder =
        RecommendationPriorityCoding.builder().system("dnpm-dip/recommendation/priority");

    try {
      resultBuilder.code(RecommendationPriorityCodingCode.forValue(code)).display(code);
    } catch (IOException e) {
      return null;
    }

    return resultBuilder.build();
  }

  @Nullable
  protected LevelOfEvidence getLevelOfEvidence(@NonNull ResultSet resultSet) {

    var resultBuilder = LevelOfEvidence.builder();

    var evidenzlevel = resultSet.getString("evidenzlevel");

    if (null == evidenzlevel) {
      return null;
    }

    switch (evidenzlevel) {
      case "1":
        resultBuilder.grading(
            LevelOfEvidenceGradingCoding.builder()
                .code(LevelOfEvidenceGradingCodingCode.M1A)
                .display(LevelOfEvidenceGradingCodingCode.M1A.toValue())
                .system(GRADING_SYSTEM)
                .build());
        break;
      case "2":
        resultBuilder.grading(
            LevelOfEvidenceGradingCoding.builder()
                .code(LevelOfEvidenceGradingCodingCode.M1B)
                .display(LevelOfEvidenceGradingCodingCode.M1B.toValue())
                .system(GRADING_SYSTEM)
                .build());
        break;
      case "3":
        resultBuilder.grading(
            LevelOfEvidenceGradingCoding.builder()
                .code(LevelOfEvidenceGradingCodingCode.M1C)
                .display(LevelOfEvidenceGradingCodingCode.M1C.toValue())
                .system(GRADING_SYSTEM)
                .build());
        break;
      case "4":
        resultBuilder.grading(
            LevelOfEvidenceGradingCoding.builder()
                .code(LevelOfEvidenceGradingCodingCode.M2A)
                .display(LevelOfEvidenceGradingCodingCode.M2A.toValue())
                .system(GRADING_SYSTEM)
                .build());
        break;
      case "5":
        resultBuilder.grading(
            LevelOfEvidenceGradingCoding.builder()
                .code(LevelOfEvidenceGradingCodingCode.M2B)
                .display(LevelOfEvidenceGradingCodingCode.M2B.toValue())
                .system(GRADING_SYSTEM)
                .build());
        break;
      case "6":
        resultBuilder.grading(
            LevelOfEvidenceGradingCoding.builder()
                .code(LevelOfEvidenceGradingCodingCode.M2C)
                .display(LevelOfEvidenceGradingCodingCode.M2C.toValue())
                .system(GRADING_SYSTEM)
                .build());
        break;
      case "7":
        resultBuilder.grading(
            LevelOfEvidenceGradingCoding.builder()
                .code(LevelOfEvidenceGradingCodingCode.M3)
                .display(LevelOfEvidenceGradingCodingCode.M3.toValue())
                .system(GRADING_SYSTEM)
                .build());
        break;
      case "8":
        resultBuilder.grading(
            LevelOfEvidenceGradingCoding.builder()
                .code(LevelOfEvidenceGradingCodingCode.M4)
                .display(LevelOfEvidenceGradingCodingCode.M4.toValue())
                .system(GRADING_SYSTEM)
                .build());
        break;
    }

    var evidenzlevelZusatz = new ArrayList<LevelOfEvidenceAddendumCoding>();
    if (resultSet.isTrue("evidenzlevel_zusatz_is")) {
      evidenzlevelZusatz.add(
          LevelOfEvidenceAddendumCoding.builder()
              .code(LevelOfEvidenceAddendumCodingCode.IS)
              .display(LevelOfEvidenceAddendumCodingCode.IS.toValue())
              .system(ADDENDUM_SYSTEM)
              .build());
    }
    if (resultSet.isTrue("evidenzlevel_zusatz_iv")) {
      evidenzlevelZusatz.add(
          LevelOfEvidenceAddendumCoding.builder()
              .code(LevelOfEvidenceAddendumCodingCode.IV)
              .display(LevelOfEvidenceAddendumCodingCode.IV.toValue())
              .system(ADDENDUM_SYSTEM)
              .build());
    }
    if (resultSet.isTrue("evidenzlevel_zusatz_z")) {
      evidenzlevelZusatz.add(
          LevelOfEvidenceAddendumCoding.builder()
              .code(LevelOfEvidenceAddendumCodingCode.Z)
              .display(LevelOfEvidenceAddendumCodingCode.Z.toValue())
              .system(ADDENDUM_SYSTEM)
              .build());
    }
    if (resultSet.isTrue("evidenzlevel_zusatz_r")) {
      evidenzlevelZusatz.add(
          LevelOfEvidenceAddendumCoding.builder()
              .code(LevelOfEvidenceAddendumCodingCode.R)
              .display(LevelOfEvidenceAddendumCodingCode.R.toValue())
              .system(ADDENDUM_SYSTEM)
              .build());
    }

    resultBuilder.addendums(evidenzlevelZusatz);

    var evidenzlevelPublication = resultSet.getString("evidenzlevel_publication");
    if (null != evidenzlevelPublication) {
      resultBuilder.publications(getPublicationReferences(evidenzlevelPublication));
    }

    return resultBuilder.build();
  }

  @NullMarked
  private List<PublicationReference> getPublicationReferences(String fieldContent) {
    return Arrays.stream(fieldContent.split("\n"))
        .map(String::trim)
        .map(
            // Mappe nur PubMed-Ids (Ziffern) oder DOI (Pattern)
            line -> {
              if (line.matches("^\\d+$")) {
                return PublicationReference.builder()
                    .id(line)
                    .system(PublicationSystem.PUBMED_NCBI_NLM_NIH_GOV)
                    .type("Publication")
                    .build();
              }
              if (line.matches("^\\d{2}\\.\\d{4}/\\d+(\\.\\d+)?$")) {
                return PublicationReference.builder()
                    .id(line)
                    .system(PublicationSystem.DOI_ORG)
                    .type("Publication")
                    .build();
              }
              return null;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Maps integer value to RecommendationPriorityCoding
   *
   * @param value The priority value
   * @return The mapped RecommendationPriorityCoding
   */
  @Nullable
  protected RecommendationPriorityCoding getRecommendationPriorityCoding(Integer value) {
    var resultBuilder =
        RecommendationPriorityCoding.builder()
            .system("dnpm-dip/recommendation/priority")
            .display(String.format("%d", value));
    if (null == value) {
      return null;
    }
    switch (value) {
      case 1:
        resultBuilder.code(RecommendationPriorityCodingCode.CODE_1);
        break;
      case 2:
        resultBuilder.code(RecommendationPriorityCodingCode.CODE_2);
        break;
      case 3:
        resultBuilder.code(RecommendationPriorityCodingCode.CODE_3);
        break;
      case 4:
      default:
        resultBuilder.code(RecommendationPriorityCodingCode.CODE_4);
    }

    return resultBuilder.build();
  }
}
