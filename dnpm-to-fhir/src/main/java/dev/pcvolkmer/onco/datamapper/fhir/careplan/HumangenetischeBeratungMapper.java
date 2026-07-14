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

package dev.pcvolkmer.onco.datamapper.fhir.careplan;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import dev.pcvolkmer.mv64e.model.MtbCarePlan;
import dev.pcvolkmer.onco.datamapper.fhir.ServiceRequestMapper;
import org.hl7.fhir.r4.model.*;

public class HumangenetischeBeratungMapper extends ServiceRequestMapper<MtbCarePlan> {

  @Override
  protected String getPatientId(MtbCarePlan item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(MtbCarePlan item) {
    return String.format("%s_humangenberatung", item.getId());
  }

  @Override
  public ServiceRequest map(MtbCarePlan sourceItem) {
    var result = new ServiceRequest();
    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource(this.fhirMetaSource)
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-humangenetische-beratung-auftrag"));

    // Current active care plan? No Information in DNPM - but required!
    result.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);
    result.setIntent(ServiceRequest.ServiceRequestIntent.PROPOSAL);

    result.setSubject(this.getPatientReference(sourceItem));

    final var dateValue = new DateTimeType();
    dateValue.setValue(sourceItem.getIssuedOn());
    dateValue.setPrecision(TemporalPrecisionEnum.DAY);
    result.setAuthoredOnElement(dateValue);

    result.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("788339009")
                    .setDisplay("Genetic consultation (procedure)")));

    result.addReasonCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("dnpm-dip/mtb/recommendation/genetic-counseling/reason")
                    .setCode("other")
                    .setDisplay("Andere")));

    return result;
  }
}
