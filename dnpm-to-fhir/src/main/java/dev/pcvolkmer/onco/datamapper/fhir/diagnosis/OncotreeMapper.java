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
import dev.pcvolkmer.mv64e.model.HistologyReport;
import dev.pcvolkmer.mv64e.model.MtbDiagnosis;
import dev.pcvolkmer.mv64e.model.PatientRecord;
import dev.pcvolkmer.onco.datamapper.fhir.ManyMapper;
import dev.pcvolkmer.onco.datamapper.fhir.ObservationMapper;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.jspecify.annotations.Nullable;

public class OncotreeMapper extends ObservationMapper<MtbDiagnosis>
    implements ManyMapper<PatientRecord, Observation> {

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

    diagnoses.forEach(
        diagnosis -> {
          final var result = this.map(diagnosis);
          result.setSubject(this.getPatientReference(diagnosis));

          final var topography = diagnosis.getTopography();
          final var histologies = diagnosis.getHistology();
          if (null != topography && null != histologies) {
            histologies.forEach(
                ref -> {
                  final var histology = this.getHistologie(sourceItem, ref.getId());
                  if (null != histology) {
                    // TODO: add value codable concept here - mapped from histology and
                    // topography to oncotree - for now: fake value
                    result.setValue(
                        new CodeableConcept()
                            .addCoding(
                                new Coding()
                                    .setSystem("http://data.mskcc.org/ontologies/oncotree")
                                    .setCode(
                                        // TODO: Replace!
                                        String.format(
                                            "placeholder-oncotree:%s-%s",
                                            histology
                                                .getResults()
                                                .getTumorMorphology()
                                                .getValue()
                                                .getCode(),
                                            topography.getCode()))));
                  }
                });
          }
          bundle
              .addEntry()
              .setResource(result)
              .getRequest()
              .setMethod(Bundle.HTTPVerb.PUT)
              .setUrl(this.getRequestUrl(diagnosis));
        });
  }

  @Override
  public List<Observation> mapToMany(PatientRecord sourceItem) {
    throw new UnsupportedOperationException("Not implemented");
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
}
