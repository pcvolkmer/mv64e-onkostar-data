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

import dev.pcvolkmer.mv64e.model.Msi;
import dev.pcvolkmer.mv64e.model.MsiInterpretationCoding;
import dev.pcvolkmer.onco.datamapper.fhir.ObservationMapper;
import org.hl7.fhir.r4.model.*;

public class MsiMapper extends ObservationMapper<Msi> {
  @Override
  protected String getPatientId(Msi item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(Msi item) {
    return String.format("%s_msi", item.getId());
  }

  @Override
  public Observation map(Msi sourceItem) {
    var result = new Observation();
    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource(this.fhirMetaSource)
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-mikrosatelliteninstabilitaet"));

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
                    .setCode("81695-9")
                    .setDisplay(
                        "Microsatellite instability [Interpretation] in Cancer specimen Qualitative")));

    result.setValue(
        new Quantity()
            .setValue(sourceItem.getValue())
            .setSystem("http://unitsofmeasure.org")
            .setCode("%"));

    final var interpretation = sourceItem.getInterpretation();
    if (null != interpretation) {
      result.addInterpretation(
          new CodeableConcept().addCoding(mapMsiInterpretationCoding(interpretation)));
    }

    // TODO: Methode in DNPM problematisch. Soll immer "bioinformatic" sein, auch wenn es
    // "histologic" ist

    result.setSubject(this.getPatientReference(sourceItem));

    return result;
  }

  private Coding mapMsiInterpretationCoding(MsiInterpretationCoding interpretation) {
    final var result = new Coding().setSystem("http://loinc.org");

    switch (interpretation.getCode()) {
      case STABLE:
        result.setCode("LA14122-8").setDisplay("Stable");
        break;
      case MSI_LOW:
        result.setCode("LA26202-4").setDisplay("MSI-L");
        break;
      case MSI_HIGH:
        result.setCode("LA26203-2").setDisplay("MSI-H");
        break;
      // TODO: MMR?
      case MMR_DEFICIENT:
      case MMR_PROFICIENT:
        throw new IllegalArgumentException("MMR interpretation not supported");
    }

    return result;
  }
}
