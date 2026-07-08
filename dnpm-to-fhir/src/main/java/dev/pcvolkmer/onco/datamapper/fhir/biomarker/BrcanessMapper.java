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

import dev.pcvolkmer.mv64e.mtb.Brcaness;
import dev.pcvolkmer.onco.datamapper.fhir.ObservationMapper;
import org.hl7.fhir.r4.model.*;

public class BrcanessMapper extends ObservationMapper<Brcaness> {
  @Override
  protected String getPatientId(Brcaness item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(Brcaness item) {
    return String.format("%s_brcaness", item.getId());
  }

  @Override
  public Observation map(Brcaness sourceItem) {
    var result = new Observation();
    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource("#dnpm")
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-brcaness"));

    result.setStatus(Observation.ObservationStatus.FINAL);

    result
        .addCategory()
        .addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
        .setCode("laboratory")
        .setDisplay("Laboratory");

    result.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem(
                        "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-molekulare-biomarker")
                    .setCode("brcaness")
                    .setDisplay("BRCAness")));

    result.setValue(
        new Quantity()
            .setValue(sourceItem.getValue())
            .setUnit("Score")
            .setSystem("http://unitsofmeasure.org")
            .setCode("1"));

    final var confidenceRange = sourceItem.getConfidenceRange();
    if (null != confidenceRange) {
      result.addComponent(
          new Observation.ObservationComponentComponent()
              .setCode(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem(
                                  "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-molekulare-biomarker")
                              .setCode("brcaness-confidence-range-min")
                              .setDisplay("BRCAness Confidence Range Minimum")))
              .setValue(
                  new Quantity()
                      .setValue(confidenceRange.getMin())
                      .setUnit("Score")
                      .setSystem("http://unitsofmeasure.org")
                      .setCode("1")));
      result.addComponent(
          new Observation.ObservationComponentComponent()
              .setCode(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem(
                                  "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-molekulare-biomarker")
                              .setCode("brcaness-confidence-range-max")
                              .setDisplay("BRCAness Confidence Range Maximum")))
              .setValue(
                  new Quantity()
                      .setValue(confidenceRange.getMax())
                      .setUnit("Score")
                      .setSystem("http://unitsofmeasure.org")
                      .setCode("1")));
    }

    final var specimen = sourceItem.getSpecimen();
    if (null != specimen) {
      // TODO optional specimen reference
    }

    result.setSubject(this.getPatientReference(sourceItem));

    return result;
  }
}
