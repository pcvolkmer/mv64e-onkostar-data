/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026 the original author or authors.
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
 */

package dev.pcvolkmer.onco.datamapper.fhir.diagnosis;

import static java.nio.charset.StandardCharsets.UTF_8;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import dev.pcvolkmer.mv64e.model.HistologyReport;
import dev.pcvolkmer.mv64e.model.MtbDiagnosis;
import dev.pcvolkmer.mv64e.model.PatientRecord;
import dev.pcvolkmer.onco.datamapper.fhir.ManyMapper;
import dev.pcvolkmer.onco.datamapper.fhir.ObservationMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;
import org.jspecify.annotations.Nullable;

public class OncotreeMapper extends ObservationMapper<MtbDiagnosis>
    implements ManyMapper<PatientRecord, Observation> {

  private static final String MAPPING_FILE = "ontology_mappings.txt";
  private final List<OncotreeOntologyMapping> oncotreeOntologyMappings;

  public OncotreeMapper() {
    this.oncotreeOntologyMappings = loadOncotreeOntologyMappings();
  }

  @Override
  protected String getPatientId(MtbDiagnosis item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(MtbDiagnosis item) {
    return String.format("%s_oncotree", item.getId());
  }

  @Override
  public Observation map(MtbDiagnosis sourceItem) {
    var result = new Observation();

    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource(this.fhirMetaSource)
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-oncotree"));

    result.setStatus(Observation.ObservationStatus.FINAL);

    result.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("371469007")
                    .setDisplay("Histologic grade of neoplasm (observable entity)")));

    result.setSubject(this.getPatientReference(sourceItem));

    if (null != sourceItem.getRecordedOn()) {
      final var dateTimeType = new DateTimeType(sourceItem.getRecordedOn());
      dateTimeType.setPrecision(TemporalPrecisionEnum.DAY);
      result.setEffective(dateTimeType);
    }

    return result;
  }

  @Override
  public void addManyToBundle(Bundle bundle, PatientRecord sourceItem) {
    final var diagnoses = sourceItem.getDiagnoses();
    if (null == diagnoses) {
      return;
    }

    for (final var diagnosis : diagnoses) {
      createObservation(sourceItem, diagnosis)
          .ifPresent(
              result ->
                  bundle
                      .addEntry()
                      .setResource(result)
                      .getRequest()
                      .setMethod(Bundle.HTTPVerb.PUT)
                      .setUrl(this.getRequestUrl(diagnosis)));
    }
  }

  @Override
  public List<Observation> mapToMany(PatientRecord sourceItem) {
    final var diagnoses = sourceItem.getDiagnoses();

    if (diagnoses == null) {
      return List.of();
    }

    return diagnoses.stream()
        .map(diagnosis -> createObservation(sourceItem, diagnosis))
        .flatMap(Optional::stream)
        .collect(Collectors.toList());
  }

  // Only create Oncotree Observation if there is an oncotree code
  private Optional<Observation> createObservation(
      PatientRecord patientRecord, MtbDiagnosis diagnosis) {

    return findOncotreeCode(patientRecord, diagnosis)
        .map(
            oncotreeCode -> {
              final var observation = this.map(diagnosis);

              observation.setValue(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem("http://data.mskcc.org/ontologies/oncotree")
                              .setCode(oncotreeCode)));

              return observation;
            });
  }

  // If there are multiple histology-codes only the first one that leads to a mathc with an oncotree
  // code is used
  private Optional<String> findOncotreeCode(PatientRecord patientRecord, MtbDiagnosis diagnosis) {

    final var topography = diagnosis.getTopography();
    final var histologyReferences = diagnosis.getHistology();

    if (topography == null || topography.getCode() == null || histologyReferences == null) {
      return Optional.empty();
    }

    final var icdO3Topography = topography.getCode();

    for (final var histologyReference : histologyReferences) {
      if (histologyReference == null || histologyReference.getId() == null) {
        continue;
      }

      final var histologyReport = getHistologie(patientRecord, histologyReference.getId());
      if (histologyReport == null) {
        continue;
      }

      final var icdO3Morphology =
          histologyReport.getResults().getTumorMorphology().getValue().getCode();
      if (icdO3Morphology == null) {
        continue;
      }

      final var oncotreeCode = findOncotreeCode(icdO3Topography, icdO3Morphology);
      if (oncotreeCode.isPresent()) {
        return oncotreeCode;
      }
    }

    return Optional.empty();
  }

  // Find Oncotree code via mapping file using Topography (Localisation) and Morphology (Histology)
  // codes
  private Optional<String> findOncotreeCode(String icdO3Topography, String icdO3Morphology) {

    return this.oncotreeOntologyMappings.stream()
        .filter(
            mapping ->
                icdO3Topography.equals(mapping.icdO3TCode)
                    && icdO3Morphology.equals(mapping.icdO3MCode))
        .map(mapping -> mapping.oncotreeCode)
        .findFirst();
  }

  private List<OncotreeOntologyMapping> loadOncotreeOntologyMappings() {
    final var inputStream = OncotreeMapper.class.getClassLoader().getResourceAsStream(MAPPING_FILE);

    if (inputStream == null) {
      throw new IllegalStateException("Oncotree mapping file not found: " + MAPPING_FILE);
    }

    try (var reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8))) {

      return reader
          .lines()
          // skip header
          .skip(1)
          // keep empty columns
          .map(line -> line.split("\t", -1))
          .filter(fields -> fields.length >= 5)
          .map(fields -> new OncotreeOntologyMapping(fields[0], fields[3], fields[4]))
          // filter rows where ICDO_TOPOGRAPHY_CODE or ICDO_MORPHOLOGY_CODE are blank
          .filter(mapping -> !mapping.icdO3TCode.isBlank())
          .filter(mapping -> !mapping.icdO3MCode.isBlank())
          .collect(Collectors.toList());

    } catch (IOException e) {
      throw new UncheckedIOException(
          "Oncotree mapping file could not be loaded: " + MAPPING_FILE, e);
    }
  }

  @Nullable
  private HistologyReport getHistologie(PatientRecord sourceItem, String id) {
    final var histologyReports = sourceItem.getHistologyReports();
    if (null == histologyReports) {
      return null;
    }
    final var result =
        histologyReports.stream()
            .filter(histologie -> id.equals(histologie.getId()))
            .findFirst()
            .orElse(null);

    if (null == result) {
      throw new IllegalArgumentException("No histology report found with id " + id);
    }
    return result;
  }

  private static class OncotreeOntologyMapping {
    public String oncotreeCode;
    public String icdO3TCode;

    public String icdO3MCode;

    public OncotreeOntologyMapping(String oncotreeCode, String icdO3TCode, String icdO3MCode) {
      this.oncotreeCode = oncotreeCode;
      this.icdO3TCode = icdO3TCode;
      this.icdO3MCode = icdO3MCode;
    }
  }
}
