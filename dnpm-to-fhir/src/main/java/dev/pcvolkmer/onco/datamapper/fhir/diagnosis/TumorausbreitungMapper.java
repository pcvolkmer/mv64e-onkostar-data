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

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import dev.pcvolkmer.mv64e.model.MtbDiagnosis;
import dev.pcvolkmer.mv64e.model.TumorStaging;
import dev.pcvolkmer.onco.datamapper.fhir.ManyMapper;
import dev.pcvolkmer.onco.datamapper.fhir.ObservationMapper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hl7.fhir.r4.model.*;

public class TumorausbreitungMapper extends ObservationMapper<TumorStaging>
    implements ManyMapper<MtbDiagnosis, Observation> {
  @Override
  protected String getPatientId(TumorStaging item) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  protected String getId(TumorStaging item) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Observation map(TumorStaging sourceItem) {
    var result = new Observation();

    result.setMeta(
        new Meta()
            .setSource("#dnpm")
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-tumorausbreitung"));

    result.setStatus(Observation.ObservationStatus.FINAL);

    result.setCategory(
        List.of(
            new CodeableConcept()
                .setCoding(
                    List.of(
                        new Coding()
                            .setSystem("http://snomed.info/sct")
                            // Hier Erstdiagnose!
                            .setCode("473302008")
                            .setDisplay("Aware of diagnosis")))));

    result.setCode(
        new CodeableConcept()
            .setCoding(
                List.of(
                    new Coding()
                        .setSystem("http://snomed.info/sct")
                        .setCode("371508000")
                        .setDisplay("Tumor Stage"))));

    final var dateTimeType = new DateTimeType(sourceItem.getDate());
    dateTimeType.setPrecision(TemporalPrecisionEnum.DAY);
    result.setEffective(dateTimeType);

    result.setValue(
        new CodeableConcept()
            .addCoding(
                new Coding().setSystem("http://snomed.info/sct").setCode(mapValue(sourceItem))));

    return result;
  }

  private String mapValue(TumorStaging sourceItem) {
    for (var coding : sourceItem.getOtherClassifications()) {
      switch (coding.getCode()) {
        case "tumor-free":
          return "58899004";
        case "local":
          return "255127006";
        case "metastasized":
          return "128462008";
        default:
          // Unknown
          return "261665006";
      }
    }
    // Unknown
    return "261665006";
  }

  @Override
  public void addManyToBundle(Bundle bundle, MtbDiagnosis sourceItem) {
    final var patientReference =
        new Reference()
            .setReference(
                String.format(
                    "Patient?identifier=https://fhir.diz.uni-marburg.de/sid/patient-id|%s",
                    sourceItem.getPatient().getId()));

    final var newItems = this.mapToMany(sourceItem);
    IntStream.range(0, newItems.size())
        .forEach(
            idx -> {
              final var requestUrl =
                  String.format(
                      "Observation?identifier=%s|%s_tumorstaging-%d",
                      this.getSystem(), sourceItem.getId(), idx);

              final var newItem = newItems.get(idx);
              newItem.setSubject(patientReference);
              newItem.setIdentifier(
                  List.of(
                      new Identifier()
                          .setSystem(this.getSystem())
                          .setValue(String.format("%s_tumorstaging-%d", sourceItem.getId(), idx))));

              bundle
                  .addEntry()
                  .setResource(newItem)
                  .setFullUrl(requestUrl)
                  .getRequest()
                  .setMethod(Bundle.HTTPVerb.PUT)
                  .setUrl(requestUrl);
            });
  }

  @Override
  public List<Observation> mapToMany(MtbDiagnosis sourceItem) {
    final var staging = sourceItem.getStaging();
    if (null != staging && null != staging.getHistory()) {
      return staging.getHistory().stream()
          .filter(Objects::nonNull)
          .map(this::map)
          .collect(Collectors.toList());
    }
    return List.of();
  }
}
