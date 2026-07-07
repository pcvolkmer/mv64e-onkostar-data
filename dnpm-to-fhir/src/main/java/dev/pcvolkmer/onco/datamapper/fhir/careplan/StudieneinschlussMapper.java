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
import dev.pcvolkmer.mv64e.mtb.MtbStudyEnrollmentRecommendation;
import dev.pcvolkmer.onco.datamapper.fhir.ServiceRequestMapper;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.ServiceRequest;

public class StudieneinschlussMapper
    extends ServiceRequestMapper<MtbStudyEnrollmentRecommendation> {

  @Override
  protected String getPatientId(MtbStudyEnrollmentRecommendation item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(MtbStudyEnrollmentRecommendation item) {
    return String.format("%s_studieneinschluss", item.getId());
  }

  @Override
  public ServiceRequest map(MtbStudyEnrollmentRecommendation sourceItem) {
    var result = new ServiceRequest();
    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource("#dnpm")
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-studieneinschluss-anfrage"));

    // Current active care plan? No Information in DNPM - but required!
    result.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);
    result.setIntent(ServiceRequest.ServiceRequestIntent.PROPOSAL);

    final var dateValue = new DateTimeType();
    dateValue.setValue(sourceItem.getIssuedOn());
    dateValue.setPrecision(TemporalPrecisionEnum.DAY);
    result.setAuthoredOnElement(dateValue);

    result.setSubject(this.getPatientReference(sourceItem));

    return result;
  }
}
