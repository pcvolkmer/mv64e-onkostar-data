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

package dev.pcvolkmer.onco.datamapper.fhir.tnm;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import dev.pcvolkmer.mv64e.model.MtbDiagnosis;
import dev.pcvolkmer.mv64e.model.TumorStaging;
import dev.pcvolkmer.mv64e.model.TumorStagingMethodCoding;
import dev.pcvolkmer.mv64e.model.TumorStagingTnmClassification;
import dev.pcvolkmer.onco.datamapper.fhir.ManyMapper;
import dev.pcvolkmer.onco.datamapper.fhir.ObservationMapper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hl7.fhir.r4.model.*;
import org.jspecify.annotations.Nullable;

public abstract class AbstractTnmMapper extends ObservationMapper<TumorStaging>
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

    result.setMeta(new Meta().setSource(this.fhirMetaSource).addProfile(this.getProfile()));

    result.setStatus(Observation.ObservationStatus.FINAL);

    final var method = sourceItem.getMethod();

    if (method != null && null != this.mapPrefix(method.getCode())) {
      final var codeableConcept = new CodeableConcept();
      codeableConcept
          .setCoding(List.of(this.getExtensionCoding()))
          .addExtension(
              new Extension()
                  .setUrl(
                      "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/StructureDefinition/mii-ex-onko-tnm-cp-praefix")
                  .setValue(
                      new CodeableConcept(
                          new Coding()
                              .setSystem("https://www.uicc.org/resources/tnm")
                              .setCode(this.mapPrefix(method.getCode()))
                              .setDisplay(this.mapPrefix(method.getCode())))));
      result.setCode(codeableConcept);
    }

    // Subject in MapToMany since it is not available for TumorStaging

    final var dateTimeType = new DateTimeType(sourceItem.getDate());
    dateTimeType.setPrecision(TemporalPrecisionEnum.DAY);
    result.setEffective(dateTimeType);

    final var tnmClassification = sourceItem.getTnmClassification();
    if (null != tnmClassification) {
      final var value = getClassificationValue(tnmClassification);
      if (value != null) {
        result.setValue(
            new CodeableConcept()
                .addCoding(
                    new Coding()
                        .setSystem("https://www.uicc.org/resources/tnm")
                        .setCode(value.getCode())
                        .setDisplay(value.getCode())));
      }
    }

    // Method (Version) not available in DNPM

    return result;
  }

  protected abstract dev.pcvolkmer.mv64e.model.Coding getClassificationValue(
      TumorStagingTnmClassification classification);

  @Nullable
  private String mapPrefix(TumorStagingMethodCoding.CodeEnum dnpmValue) {
    switch (dnpmValue) {
      case CLINICAL:
        return "c";
      case PATHOLOGIC:
        return "p";
      default:
        return null;
    }
  }

  @Override
  public void addManyToBundle(Bundle bundle, MtbDiagnosis sourceItem) {
    final var patientReference =
        new Reference()
            .setReference(
                String.format(
                    "Patient?identifier=%s/sid/patient-id|%s",
                    this.fhirSystemBaseUrl, sourceItem.getPatient().getId()));

    final var newItems = this.mapToMany(sourceItem);
    IntStream.range(0, newItems.size())
        .forEach(
            idx -> {
              final var requestUrl =
                  String.format(
                      "Observation?identifier=%s|%s_%s-%d",
                      this.getSystem(), sourceItem.getId(), idSuffix(), idx);

              final var newItem = newItems.get(idx);
              newItem.setSubject(patientReference);
              newItem.setIdentifier(
                  List.of(
                      new Identifier()
                          .setSystem(this.getSystem())
                          .setValue(
                              String.format("%s_%s-%d", sourceItem.getId(), idSuffix(), idx))));

              bundle
                  .addEntry()
                  .setResource(newItem)
                  .setFullUrl(this.fullUrlUrn(requestUrl))
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

  protected abstract String idSuffix();

  protected abstract String getProfile();

  protected abstract Coding getExtensionCoding();
}
