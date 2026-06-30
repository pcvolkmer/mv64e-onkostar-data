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

package dev.pcvolkmer.onco.datamapper.fhir;

import dev.pcvolkmer.mv64e.mtb.Snv;
import org.hl7.fhir.r4.model.*;

public class EinfacheVarianteMapper extends ObservationMapper<Snv> {
  @Override
  protected String getPatientId(Snv item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(Snv item) {
    return item.getId();
  }

  @Override
  public Observation map(Snv sourceItem) {
    var result = new Observation();
    result.addIdentifier().setSystem(this.getSystem()).setValue(sourceItem.getId());

    result.setMeta(
        new Meta()
            .setSource("#dnpm")
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-einfache-variante|2026.0.0"));

    result
        .addCategory()
        .addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
        .setCode("laboratory")
        .setDisplay("Laboratory");

    result
        .addCategory()
        .addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/v2-0074")
        .setCode("GE");

    result.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("69548-6")
                    .setDisplay("Genetic variant assessment")));

    result.setMethod(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setCode("LA26398")
                    .setSystem("http://loinc.org")
                    .setDisplay("Sequencing")));

    // Gene
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode(sourceItem.getGene().getCode())
                            .setSystem("http://www.genenames.org/geneId")
                            .setDisplay(sourceItem.getGene().getDisplay()))));

    // Position
    final var positionRange = new Range();
    if (null != sourceItem.getPosition()) {
      positionRange.setLow(new Quantity().setValue(sourceItem.getPosition().getStart()));
      if (null != sourceItem.getPosition().getEnd()) {
        positionRange.setHigh(new Quantity().setValue(sourceItem.getPosition().getEnd()));
      }
    }
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(new Coding().setCode("http://loinc.org").setSystem("81254-5")))
            .setValue(positionRange));

    // cDNA Change
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("48004-6")
                            .setSystem("http://loinc.org")
                            .setDisplay("DNA change (c.HGVS)")))
            .setValue(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode(sourceItem.getDnaChange())
                            .setSystem("https://www.hgvs.org"))));

    // Protein Change
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("48005-3")
                            .setSystem("http://loinc.org")
                            .setDisplay("Amino acid change (pHGVS)")))
            .setValue(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode(sourceItem.getProteinChange())
                            .setSystem("https://www.hgvs.org"))));

    // Allelfrequenz
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("81258-6")
                            .setSystem("http://loinc.org")
                            .setDisplay("Sample variant allelic frequency [NFr]")))
            .setValue(
                new Quantity()
                    .setValue(sourceItem.getAllelicFrequency())
                    .setCode("%")
                    .setSystem("http://unitsofmeasure.org")));

    result.setSubject(this.getPatientReference(sourceItem));

    return result;
  }
}
