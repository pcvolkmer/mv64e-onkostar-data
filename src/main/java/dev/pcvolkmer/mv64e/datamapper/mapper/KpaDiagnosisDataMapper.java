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

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.HistologieCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.KeimbahndiagnoseCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.KpaCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TumorausbreitungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TumorgradingCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import dev.pcvolkmer.mv64e.mtb.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Mapper class to load and map diagnosis data from database table 'dk_dnpm_kpa'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class KpaDiagnosisDataMapper implements DataMapper<MtbDiagnosis> {

  private final KpaCatalogue kpaCatalogue;
  private final HistologieCatalogue histologieCatalogue;
  private final TumorausbreitungCatalogue tumorausbreitungCatalogue;
  private final TumorgradingCatalogue tumorgradingCatalogue;
  private final KeimbahndiagnoseCatalogue keimbahndiagnoseCatalogue;
  private final PropertyCatalogue propertyCatalogue;

  public KpaDiagnosisDataMapper(
      final KpaCatalogue kpaCatalogue,
      final HistologieCatalogue histologieCatalogue,
      final TumorausbreitungCatalogue tumorausbreitungCatalogue,
      final TumorgradingCatalogue tumorgradingCatalogue,
      final KeimbahndiagnoseCatalogue keimbahndiagnoseCatalogue,
      final PropertyCatalogue propertyCatalogue) {
    this.kpaCatalogue = kpaCatalogue;
    this.histologieCatalogue = histologieCatalogue;
    this.tumorausbreitungCatalogue = tumorausbreitungCatalogue;
    this.tumorgradingCatalogue = tumorgradingCatalogue;
    this.keimbahndiagnoseCatalogue = keimbahndiagnoseCatalogue;
    this.propertyCatalogue = propertyCatalogue;
  }

  /**
   * Loads and maps a diagnosis using the kpa procedures database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded MtbDiagnosis file
   */
  @Override
  @NonNull
  public MtbDiagnosis getById(int id) {
    var data = kpaCatalogue.getById(id);

    final var icd10 = data.getString("icd10");
    final var icd10PropcatVersion = data.getInteger("icd10_propcat_version");

    if (null == icd10 || null == icd10PropcatVersion) {
      throw new DataAccessException("Cannot get expected ICD10 code or property catalogue entry");
    }

    var builder = MtbDiagnosis.builder();
    builder
        .id(data.getString("id"))
        .patient(data.getPatientReference())
        .code(
            Coding.builder()
                .code(icd10)
                .system("http://fhir.de/CodeSystem/bfarm/icd-10-gm")
                .display(
                    propertyCatalogue
                        .getByCodeAndVersion(icd10, icd10PropcatVersion)
                        .getShortdesc())
                .version(
                    propertyCatalogue
                        .getByCodeAndVersion(icd10, icd10PropcatVersion)
                        .getVersionDescription())
                .build())
        .recordedOn(data.getDate("datumerstdiagnose"))
        .topography(Coding.builder().code(data.getString("icdo3lokalisation")).build())
        .type(getType(data))
        .grading(getGrading(id))
        .staging(getStaging(id))
        .germlineCodes(getGermlineCodes(id))
        .histology(getHistologyReferences(id));

    final var leitlinienstatus = data.getString("leitlinienstatus");
    final var leitlinienstatusPropcatVersion = data.getInteger("leitlinienstatus_propcat_version");
    if (null != leitlinienstatus && null != leitlinienstatusPropcatVersion) {
      builder.guidelineTreatmentStatus(
          getMtbDiagnosisGuidelineTreatmentStatusCoding(
              leitlinienstatus, leitlinienstatusPropcatVersion));
    }

    return builder.build();
  }

  @NonNull
  private List<Reference> getHistologyReferences(final int id) {
    return histologieCatalogue.getAllByParentId(id).stream()
        .map(
            resultSet ->
                Reference.builder().id(resultSet.getString("id")).type("HistologyReport").build())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Nullable
  private MtbDiagnosisGuidelineTreatmentStatusCoding getMtbDiagnosisGuidelineTreatmentStatusCoding(
      @NonNull final String code, @NonNull final Integer version) {
    if (!Arrays.stream(MtbDiagnosisGuidelineTreatmentStatusCodingCode.values())
        .map(MtbDiagnosisGuidelineTreatmentStatusCodingCode::toValue)
        .collect(Collectors.toSet())
        .contains(code)) {
      return null;
    }

    var resultBuilder =
        MtbDiagnosisGuidelineTreatmentStatusCoding.builder()
            .display(propertyCatalogue.getByCodeAndVersion(code, version).getShortdesc())
            .system("dnpm-dip/mtb/diagnosis/guideline-treatment-status");
    try {
      resultBuilder.code(MtbDiagnosisGuidelineTreatmentStatusCodingCode.forValue(code));
    } catch (IOException e) {
      throw new IllegalStateException("No valid code found");
    }

    return resultBuilder.build();
  }

  @Nullable
  private Grading getGrading(final int id) {
    var all =
        tumorgradingCatalogue.getAllByParentId(id).stream()
            .map(
                resultSet -> {
                  var builder = TumorGrading.builder().date(resultSet.getDate("zeitpunkt"));

                  final var tumorgrading = resultSet.getString("tumorgrading");
                  final var tumorgradingPropcat =
                      resultSet.getInteger("tumorgrading_propcat_version");

                  final var whograd = resultSet.getString("whograd");
                  final var whogradPropcat = resultSet.getInteger("whograd_propcat_version");

                  if (null != tumorgrading
                      && !tumorgrading.isBlank()
                      && null != tumorgradingPropcat) {
                    var propertyEntry =
                        propertyCatalogue.getByCodeAndVersion(tumorgrading, tumorgradingPropcat);
                    builder.codes(
                        List.of(
                            Coding.builder()
                                .code(resultSet.getString("tumorgrading"))
                                .system("https://www.basisdatensatz.de/feld/161/grading")
                                // TODO Annahme: "v1" ist Version 2025
                                .version(
                                    propertyEntry.getVersionDescription().equals("v1")
                                        ? "2025"
                                        : null)
                                .display(propertyEntry.getShortdesc())
                                .build()));
                    return builder.build();
                  } else if (null != whograd && !whograd.isBlank() && null != whogradPropcat) {
                    var propertyEntry =
                        propertyCatalogue.getByCodeAndVersion(whograd, whogradPropcat);
                    builder.codes(
                        List.of(
                            Coding.builder()
                                .code(whograd)
                                .system("dnpm-dip/mtb/who-grading-cns-tumors")
                                .version(propertyEntry.getVersionDescription())
                                .display(propertyEntry.getShortdesc())
                                .build()));
                    return builder.build();
                  }

                  return null;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (all.isEmpty()) {
      return null;
    }

    return Grading.builder().history(all).build();
  }

  @Nullable
  private Staging getStaging(final int id) {
    var subMapper = new KpaTumorausbreitungDataMapper(tumorausbreitungCatalogue);

    var all =
        tumorausbreitungCatalogue.getAllByParentId(id).stream()
            .map(it -> subMapper.getById(it.getId()))
            .collect(Collectors.toList());
    if (all.isEmpty()) {
      return null;
    }

    return Staging.builder().history(all).build();
  }

  @NullMarked
  private List<Coding> getGermlineCodes(final int id) {
    return keimbahndiagnoseCatalogue.getAllByParentId(id).stream()
        .map(
            it -> {
              final var icd10 = it.getString("icd10");
              final var icd10PropcatVersion = it.getInteger("icd10_propcat_version");

              if (null == icd10 || icd10.isBlank() || null == icd10PropcatVersion) {
                return null;
              }

              return Coding.builder()
                  .code(it.getString("icd10"))
                  .system("http://fhir.de/CodeSystem/bfarm/icd-10-gm")
                  .display(
                      propertyCatalogue
                          .getByCodeAndVersion(icd10, icd10PropcatVersion)
                          .getShortdesc())
                  .version(
                      propertyCatalogue
                          .getByCodeAndVersion(icd10, icd10PropcatVersion)
                          .getVersionDescription())
                  .build();
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Nullable
  private Type getType(@NonNull final ResultSet resultSet) {
    var diagnosisCoding = MtbDiagnosisCoding.builder();
    var code = resultSet.getString("diagnosetyp");
    if (code == null
        || !Arrays.stream(ValueCode.values())
            .map(ValueCode::toValue)
            .collect(Collectors.toSet())
            .contains(code)) {
      return null;
    }

    try {
      diagnosisCoding.code(ValueCode.forValue(code));
    } catch (IOException e) {
      throw new IllegalStateException("No valid code found");
    }

    return Type.builder()
        .history(
            List.of(
                History.builder()
                    .date(resultSet.getDate("datumerstdiagnose"))
                    .value(diagnosisCoding.build())
                    .build()))
        .build();
  }
}
