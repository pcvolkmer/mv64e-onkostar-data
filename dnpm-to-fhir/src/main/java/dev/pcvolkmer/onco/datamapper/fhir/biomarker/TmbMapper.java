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

import dev.pcvolkmer.mv64e.mtb.Tmb;
import dev.pcvolkmer.onco.datamapper.fhir.ObservationMapper;
import org.hl7.fhir.r4.model.*;

public class TmbMapper extends ObservationMapper<Tmb> {
  @Override
  protected String getPatientId(Tmb item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(Tmb item) {
    return String.format("%s_tmb", item.getId());
  }

  @Override
  public Observation map(Tmb sourceItem) {
    var result = new Observation();
    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource("#dnpm")
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-mutationslast"));

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
                    .setSystem("http://loinc.org")
                    .setCode("94076-7")
                    .setDisplay("Mutations/Megabase [# Ratio] in Tumor")));

    result.setValue(
        new Quantity()
            .setValue(sourceItem.getValue().getValue())
            .setUnit("Mutations per megabase")
            .setSystem("http://unitsofmeasure.org")
            .setCode("1/1000000{Base}"));

    final var interpretation = sourceItem.getInterpretation();
    if (null != interpretation) {
      result.addInterpretation(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setCode(interpretation.getCode().toValue())
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
