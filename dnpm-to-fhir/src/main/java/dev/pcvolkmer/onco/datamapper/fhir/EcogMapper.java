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

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import dev.pcvolkmer.mv64e.mtb.PerformanceStatus;
import java.util.List;
import org.hl7.fhir.r4.model.*;

public class EcogMapper extends ObservationMapper<PerformanceStatus> {
  @Override
  protected String getPatientId(PerformanceStatus item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(PerformanceStatus item) {
    return item.getId();
  }

  @Override
  public Observation map(PerformanceStatus sourceItem) {
    var result = new Observation();
    result.addIdentifier().setSystem(this.getSystem()).setValue(sourceItem.getId());

    result.setMeta(
        new Meta()
            .setSource("#dnpm")
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/StructureDefinition/mii-pr-onko-allgemeiner-leistungszustand-ecog"));

    result.setStatus(Observation.ObservationStatus.FINAL);

    result.setCode(
        new CodeableConcept()
            .setCoding(
                List.of(
                    new Coding().setSystem("http://snomed.info/sct").setCode("423740007"),
                    new Coding().setSystem("http://loinc.org").setCode("89262-0"))));

    final var dateTimeType = new DateTimeType(sourceItem.getEffectiveDate());
    dateTimeType.setPrecision(TemporalPrecisionEnum.DAY);
    result.setEffective(dateTimeType);

    result.setSubject(this.getPatientReference(sourceItem));

    result.setValue(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("ECOG-Performance-Status")
                    .setCode(sourceItem.getValue().getCode().toValue())
                    .setDisplay(sourceItem.getValue().getDisplay())));

    return result;
  }
}
