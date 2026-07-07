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
import dev.pcvolkmer.mv64e.mtb.MtbCarePlan;
import dev.pcvolkmer.onco.datamapper.fhir.CarePlanMapper;
import java.util.List;
import org.hl7.fhir.r4.model.*;

public class TherapieplanMapper extends CarePlanMapper<MtbCarePlan> {

  @Override
  protected String getPatientId(MtbCarePlan item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(MtbCarePlan item) {
    return String.format("%s_careplan", item.getId());
  }

  @Override
  public CarePlan map(MtbCarePlan sourceItem) {
    var result = new CarePlan();
    result.addIdentifier().setSystem(this.getSystem()).setValue(sourceItem.getId());

    result.setMeta(
        new Meta()
            .setSource("#dnpm")
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-therapieplan"));

    // Current active care plan? No Information in DNPM - but required!
    result.setStatus(CarePlan.CarePlanStatus.ACTIVE);
    result.setIntent(CarePlan.CarePlanIntent.PLAN);

    result.setCategory(
        List.of(
            new CodeableConcept()
                .setCoding(
                    List.of(
                        new Coding()
                            .setSystem(
                                "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/CodeSystem/mii-cs-onko-therapieplanung-typ")
                            .setCode("praeth")))));

    result.setSubject(this.getPatientReference(sourceItem));

    final var dateValue = new DateTimeType();
    dateValue.setValue(sourceItem.getIssuedOn());
    dateValue.setPrecision(TemporalPrecisionEnum.DAY);
    result.setCreatedElement(dateValue);

    final var reasonId = sourceItem.getReason().getId();
    result.addAddresses(
        new Reference()
            .setReference(
                String.format(
                    "Condition?identifier=https://fhir.diz.uni-marburg.de/sid/condition-id|%s",
                    reasonId)));

    // TODO Add planned activities
    if (null != sourceItem.getMedicationRecommendations()) {
      sourceItem
          .getMedicationRecommendations()
          .forEach(
              item ->
                  result.addActivity(
                      new CarePlan.CarePlanActivityComponent()
                          .setReference(new TherapieempfehlungMapper().getReference(item))));
    }

    if (null != sourceItem.getGeneticCounselingRecommendation()) {
      result.addActivity(
          new CarePlan.CarePlanActivityComponent()
              .setReference(new HumangenetischeBeratungMapper().getReference(sourceItem)));
    }

    if (null != sourceItem.getStudyEnrollmentRecommendations()) {
      sourceItem
          .getStudyEnrollmentRecommendations()
          .forEach(
              item ->
                  result.addActivity(
                      new CarePlan.CarePlanActivityComponent()
                          .setReference(new StudieneinschlussMapper().getReference(item))));
    }

    return result;
  }
}
