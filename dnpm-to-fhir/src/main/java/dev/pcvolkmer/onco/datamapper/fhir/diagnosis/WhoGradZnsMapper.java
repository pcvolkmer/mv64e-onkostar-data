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
import dev.pcvolkmer.mv64e.model.TumorGrading;
import dev.pcvolkmer.onco.datamapper.fhir.ManyMapper;
import dev.pcvolkmer.onco.datamapper.fhir.ObservationMapper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hl7.fhir.r4.model.*;
import org.jspecify.annotations.Nullable;

public class WhoGradZnsMapper extends ObservationMapper<TumorGrading>
    implements ManyMapper<MtbDiagnosis, Observation> {
  @Override
  protected String getPatientId(TumorGrading item) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  protected String getId(TumorGrading item) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  @Nullable
  public Observation map(TumorGrading sourceItem) {
    var result = new Observation();

    result.setMeta(
        new Meta()
            .setSource("#dnpm")
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-who-grad-tumor-zns"));

    result.setStatus(Observation.ObservationStatus.FINAL);

    result.setCode(
        new CodeableConcept()
            .setCoding(
                List.of(
                    new Coding()
                        .setSystem("http://snomed.info/sct")
                        .setCode("396921005")
                        .setDisplay("WHO grade finding for central nervous system tumor"))));

    final var dateTimeType = new DateTimeType(sourceItem.getDate());
    dateTimeType.setPrecision(TemporalPrecisionEnum.DAY);
    result.setEffective(dateTimeType);

    final var mappedValue = mapValue(sourceItem);

    if (null == mappedValue) {
      return null;
    }

    result.setValue(
        new CodeableConcept()
            .addCoding(new Coding().setSystem("http://snomed.info/sct").setCode(mappedValue)));
    return result;
  }

  @Nullable
  private String mapValue(TumorGrading sourceItem) {
    for (var coding : sourceItem.getCodes()) {
      if ("dnpm-dip/mtb/who-grading-cns-tumors".equals(coding.getSystem())) {
        switch (coding.getCode()) {
          case "1":
            return "396922003";
          case "2":
            return "396923008";
          case "3":
            return "396924002";
          case "4":
            // Unknown
            return "396925001";
        }
      }
    }
    // Unknown
    return null;
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
                      "Observation?identifier=%s|%s_znsgrading-%d",
                      this.getSystem(), sourceItem.getId(), idx);

              final var newItem = newItems.get(idx);
              newItem.setSubject(patientReference);
              newItem.setIdentifier(
                  List.of(
                      new Identifier()
                          .setSystem(this.getSystem())
                          .setValue(sourceItem.getId() + "_znsgrading-" + idx)));

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
    final var grading = sourceItem.getGrading();
    if (null != grading && null != grading.getHistory()) {
      return grading.getHistory().stream()
          .filter(Objects::nonNull)
          .map(this::map)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }
    return List.of();
  }
}
