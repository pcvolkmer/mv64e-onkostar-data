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

import dev.pcvolkmer.mv64e.mtb.Cnv;
import org.hl7.fhir.r4.model.*;

public class CnvMapper extends ObservationMapper<Cnv> {
  @Override
  protected String getPatientId(Cnv item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(Cnv item) {
    return item.getId();
  }

  @Override
  public Observation map(Cnv sourceItem) {
    var result = new Observation();
    result.addIdentifier().setSystem(this.getSystem()).setValue(sourceItem.getId());

    result.setMeta(
        new Meta()
            .setSource("#dnpm")
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-copy-number-variant"));

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
                    .setCode("LA26398-0")
                    .setSystem("http://loinc.org")
                    .setDisplay("Sequencing")));
    // Chromosom
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("48000-4")
                            .setSystem("http://loinc.org")
                            .setDisplay(
                                "Chromosome [Identifier] in Blood or Tissue by Molecular genetics method")))
            .setValue(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode(sourceItem.getChromosome().toValue())
                            .setSystem("http://terminology.hl7.org/CodeSystem/chromosome-human"))));

    // Total Copy Number
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("82155-3")
                            .setSystem("http://loinc.org")
                            .setDisplay("Genomic structural variant copy number")))
            .setValue(
                new Quantity()
                    .setValue(sourceItem.getTotalCopyNumber())
                    .setSystem("http://loinc.org")
                    .setCode(sourceItem.getTotalCopyNumber().toString())));

    // Relative Copy Number of Allele A and B
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("relative-copy-number")
                            .setSystem(
                                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-molekulare-biomarker")
                            .setDisplay("Relative Copy Number of Allele A and B")))
            .setValue(
                new Quantity()
                    .setValue(sourceItem.getRelativeCopyNumber())
                    .setSystem("http://unitsofmeasure.org")
                    .setCode("1")));

    // Copy Number of Allele A
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("copy-number-allele-a")
                            .setSystem(
                                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-molekulare-biomarker")
                            .setDisplay("Copy Number of Allele A")))
            .setValue(
                new Quantity()
                    .setValue(sourceItem.getCnA())
                    .setSystem("http://unitsofmeasure.org")
                    .setCode("1")));

    // Copy Number of Allele A
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode("copy-number-allele-b")
                            .setSystem(
                                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/CodeSystem/mii-cs-mtb-molekulare-biomarker")
                            .setDisplay("Copy Number of Allele B")))
            .setValue(
                new Quantity()
                    .setValue(sourceItem.getCnB())
                    .setSystem("http://unitsofmeasure.org")
                    .setCode("1")));

    // Gene(s)
    sourceItem
        .getReportedAffectedGenes()
        .forEach(
            gene ->
                result.addComponent(
                    new Observation.ObservationComponentComponent()
                        .setCode(
                            new CodeableConcept()
                                .addCoding(
                                    new Coding()
                                        .setCode("48018-6")
                                        .setSystem("http://loinc.org/")
                                        .setDisplay("Gene studied [ID]")))
                        .setValue(
                            new CodeableConcept()
                                .addCoding(
                                    new Coding()
                                        .setCode(gene.getCode())
                                        .setSystem("https://www.genenames.org/")
                                        .setDisplay(gene.getDisplay())))));

    // Gain/Loss
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding().setCode("type").setSystem("https://bwhc.de/mtb/cnv-type")))
            .setValue(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setCode(sourceItem.getType().getCode().toValue())
                            .setSystem("dnpm-dip/mtb/ngs-report/cnv/type")
                            .setDisplay(sourceItem.getType().getCode().toValue()))));

    result.setSubject(this.getPatientReference(sourceItem));

    return result;
  }
}
