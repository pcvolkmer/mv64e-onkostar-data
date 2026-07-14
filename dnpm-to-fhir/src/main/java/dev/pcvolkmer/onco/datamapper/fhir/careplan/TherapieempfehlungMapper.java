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
import dev.pcvolkmer.mv64e.model.MtbMedicationRecommendation;
import dev.pcvolkmer.onco.datamapper.fhir.MedicationRequestMapper;
import dev.pcvolkmer.onco.datamapper.fhir.diagnosis.MtbDiagnoseMapper;
import org.hl7.fhir.r4.model.*;

public class TherapieempfehlungMapper extends MedicationRequestMapper<MtbMedicationRecommendation> {

  @Override
  protected String getPatientId(MtbMedicationRecommendation item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(MtbMedicationRecommendation item) {
    return String.format("%s_medicationrequest", item.getId());
  }

  @Override
  public MedicationRequest map(MtbMedicationRecommendation sourceItem) {
    var result = new MedicationRequest();
    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource(this.fhirMetaSource)
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-therapieempfehlung"));

    if (null != sourceItem.getLevelOfEvidence()
        && null != sourceItem.getLevelOfEvidence().getGrading()) {
      final var evidenzlevelExtension =
          new Extension()
              .setUrl(
                  "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-ex-mtb-empfehlung-evidenzgraduierung");
      final var evidenzlevelValue =
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setCode(sourceItem.getLevelOfEvidence().getGrading().getCode().getValue())
                      .setSystem("dnpm-dip/mtb/level-of-evidence/grading")
                      .setDisplay(sourceItem.getLevelOfEvidence().getGrading().getDisplay()));

      if (null != sourceItem.getLevelOfEvidence().getAddendums()) {
        sourceItem
            .getLevelOfEvidence()
            .getAddendums()
            .forEach(
                addendum ->
                    evidenzlevelValue.addCoding(
                        new Coding()
                            .setCode(addendum.getCode().getValue())
                            .setSystem("dnpm-dip/mtb/level-of-evidence/addendum")
                            .setDisplay(addendum.getDisplay())));
      }

      evidenzlevelExtension.setValue(evidenzlevelValue);

      result.addExtension(evidenzlevelExtension);
    }

    // Current active care plan? No Information in DNPM - but required!
    result.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
    result.setIntent(MedicationRequest.MedicationRequestIntent.PROPOSAL);

    final var dateValue = new DateTimeType();
    dateValue.setValue(sourceItem.getIssuedOn());
    dateValue.setPrecision(TemporalPrecisionEnum.DAY);
    result.setAuthoredOnElement(dateValue);

    final var reason = sourceItem.getReason();
    if (null != reason) {
      final var reasonReference =
          new Reference()
              .setReference(
                  String.format(
                      "Condition?identifier=%s|%s_mtbdiagnose",
                      new MtbDiagnoseMapper().getSystem(), reason.getId()));
      result.addReasonReference(reasonReference);
    }

    final var medication = new CodeableConcept();
    if (null != sourceItem.getMedication()) {
      sourceItem
          .getMedication()
          .forEach(
              medCoding ->
                  medication.addCoding(
                      new Coding()
                          .setSystem(medCoding.getSystem().getValue())
                          .setCode(medCoding.getCode())
                          .setDisplay(medCoding.getDisplay())));
    }

    result.setMedication(medication);

    result.setSubject(this.getPatientReference(sourceItem));

    return result;
  }
}
