/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2025  Paul-Christian Volkmer
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
 *
 */

package dev.pcvolkmer.mv64e.datamapper.mapper;

import dev.pcvolkmer.mv64e.datamapper.PropertyCatalogue;
import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.KpaCatalogue;
import dev.pcvolkmer.mv64e.model.*;
import org.jspecify.annotations.NullMarked;

/**
 * Mapper class to load and map patient data from database table 'dk_dnpm_kpa'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
@NullMarked
public class KpaPatientDataMapper implements DataMapper<Patient> {

  private final KpaCatalogue kpaCatalogue;
  private final PropertyCatalogue propertyCatalogue;

  public KpaPatientDataMapper(
      final KpaCatalogue kpaCatalogue, final PropertyCatalogue propertyCatalogue) {
    this.kpaCatalogue = kpaCatalogue;
    this.propertyCatalogue = propertyCatalogue;
  }

  /**
   * Loads and maps a patient using the kpa procedures database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded Patient data
   */
  @Override
  @NullMarked
  public Patient getById(int id) {
    var kpaData = kpaCatalogue.getById(id);

    var builder = Patient.builder();
    builder
        .id(kpaData.getString("patient_id"))
        .gender(getGenderCoding(kpaData))
        .birthDate(kpaData.getDate("geburtsdatum"))
        .dateOfDeath(kpaData.getDate("todesdatum"))
        .healthInsurance(getHealthInsurance(kpaData));
    return builder.build();
  }

  private GenderCoding getGenderCoding(ResultSet data) {
    var genderCodingBuilder =
        GenderCoding.builder()
            .code(GenderCoding.CodeEnum.UNKNOWN)
            .display("Unbekannt")
            .system("Gender");

    String geschlecht = data.getString("geschlecht");
    if (null != geschlecht) {
      switch (geschlecht) {
        case "m":
          genderCodingBuilder.code(GenderCoding.CodeEnum.MALE).display("Männlich");
          break;
        case "w":
          genderCodingBuilder.code(GenderCoding.CodeEnum.FEMALE).display("Weiblich");
          break;
        case "d":
        case "x":
          genderCodingBuilder.code(GenderCoding.CodeEnum.OTHER).display("Divers");
          break;
        default:
          genderCodingBuilder.code(GenderCoding.CodeEnum.UNKNOWN).display("Unbekannt");
      }
    }
    return genderCodingBuilder.build();
  }

  private PatientHealthInsurance getHealthInsurance(ResultSet data) {
    var resultBuilder =
        PatientHealthInsurance.builder()
            .reference(
                Reference.builder()
                    .id(data.getString("krankenkasse"))
                    .system("https://www.dguv.de/arge-ik")
                    .type("HealthInsurance")
                    .build());

    var healthInsuranceCodingBuilder =
        HealthInsuranceTypeCoding.builder()
            .system("http://fhir.de/CodeSystem/versicherungsart-de-basis");

    final var artDerKrankenkasse = data.getString("artderkrankenkasse");
    final var artDerKrankenkassePropcat = data.getInteger("artderkrankenkasse_propcat_version");

    if (null == artDerKrankenkasse || null == artDerKrankenkassePropcat) {
      healthInsuranceCodingBuilder.code(HealthInsuranceTypeCoding.CodeEnum.UNK);
      return resultBuilder.type(healthInsuranceCodingBuilder.build()).build();
    }

    switch (artDerKrankenkasse) {
      case "GKV":
        healthInsuranceCodingBuilder.code(HealthInsuranceTypeCoding.CodeEnum.GKV);
        break;
      case "PKV":
        healthInsuranceCodingBuilder.code(HealthInsuranceTypeCoding.CodeEnum.PKV);
        break;
      case "BG":
        healthInsuranceCodingBuilder.code(HealthInsuranceTypeCoding.CodeEnum.BG);
        break;
      case "SEL":
        healthInsuranceCodingBuilder.code(HealthInsuranceTypeCoding.CodeEnum.SEL);
        break;
      case "SOZ":
        healthInsuranceCodingBuilder.code(HealthInsuranceTypeCoding.CodeEnum.SOZ);
        break;
      case "GPV":
        healthInsuranceCodingBuilder.code(HealthInsuranceTypeCoding.CodeEnum.GPV);
        break;
      case "PPV":
        healthInsuranceCodingBuilder.code(HealthInsuranceTypeCoding.CodeEnum.PPV);
        break;
      case "BEI":
        healthInsuranceCodingBuilder.code(HealthInsuranceTypeCoding.CodeEnum.BEI);
        break;
      case "SKT":
        healthInsuranceCodingBuilder.code(HealthInsuranceTypeCoding.CodeEnum.SKT);
        break;
      default:
        healthInsuranceCodingBuilder.code(HealthInsuranceTypeCoding.CodeEnum.UNK);
    }

    var healthInsurancePropertyEntry =
        propertyCatalogue.getByCodeAndVersion(artDerKrankenkasse, artDerKrankenkassePropcat);
    healthInsuranceCodingBuilder.display(healthInsurancePropertyEntry.getDescription());

    return resultBuilder.type(healthInsuranceCodingBuilder.build()).build();
  }
}
