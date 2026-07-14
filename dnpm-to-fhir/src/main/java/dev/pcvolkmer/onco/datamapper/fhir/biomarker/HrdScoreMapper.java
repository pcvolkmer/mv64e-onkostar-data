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

package dev.pcvolkmer.onco.datamapper.fhir.biomarker;

import dev.pcvolkmer.mv64e.model.HrdScore;
import dev.pcvolkmer.onco.datamapper.fhir.ObservationMapper;
import org.hl7.fhir.r4.model.*;

public class HrdScoreMapper extends ObservationMapper<HrdScore> {
  @Override
  protected String getPatientId(HrdScore item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(HrdScore item) {
    return String.format("%s_hrdscore", item.getId());
  }

  @Override
  public Observation map(HrdScore sourceItem) {
    var result = new Observation();
    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource(this.fhirMetaSource)
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-hrd-score"));

    result.setStatus(Observation.ObservationStatus.FINAL);

    result
        .addCategory()
        .addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
        .setCode("laboratory")
        .setDisplay("Laboratory");

    result
        .addCategory()
        .addCoding()
        .setSystem("http://hl7.org/fhir/uv/genomics-reporting/CodeSystem/tbd-codes-cs")
        .setCode("biomarker-category");

    result.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("107286-7")
                    .setDisplay(
                        "Homologous recombination deficiency status analysis [Presence] in Tissue by Molecular genetics method")));

    result.setValue(new IntegerType(sourceItem.getValue().intValue()));

    final var components = sourceItem.getComponents();
    if (null != components) {
      result.addComponent(
          new Observation.ObservationComponentComponent()
              .setCode(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem(
                                  "https://nih-ncpi.github.io/ncpi-fhir-ig/CodeSystem-ncit.html")
                              .setCode("C18016")
                              .setDisplay("Loss of Heterozygosity")))
              .setValue(new IntegerType(components.getLoh().intValue())));

      result.addComponent(
          new Observation.ObservationComponentComponent()
              .setCode(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem(
                                  "https://nih-ncpi.github.io/ncpi-fhir-ig/CodeSystem-ncit.html")
                              .setCode("C129774")
                              .setDisplay("Telomeric Allelic Imbalance Region")))
              .setValue(new IntegerType(components.getTai().intValue())));

      result.addComponent(
          new Observation.ObservationComponentComponent()
              .setCode(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem(
                                  "https://nih-ncpi.github.io/ncpi-fhir-ig/CodeSystem-ncit.html")
                              .setCode("C120466")
                              .setDisplay("Large-Scale State Transition")))
              .setValue(new IntegerType(components.getLst().intValue())));
    }

    final var interpretation = sourceItem.getInterpretation();
    if (null != interpretation) {
      result.addInterpretation(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setCode(interpretation.getCode().getValue())
                      .setSystem(interpretation.getSystem())
                      .setDisplay(interpretation.getDisplay())));
    }

    final var specimen = sourceItem.getSpecimen();
    if (null != specimen) {
      // TODO optional specimen reference
    }

    result.setSubject(this.getPatientReference(sourceItem));

    return result;
  }
}
