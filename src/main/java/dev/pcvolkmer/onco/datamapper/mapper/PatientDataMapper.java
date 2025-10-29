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

package dev.pcvolkmer.onco.datamapper.mapper;

import dev.pcvolkmer.mv64e.mtb.Address;
import dev.pcvolkmer.mv64e.mtb.GenderCoding;
import dev.pcvolkmer.mv64e.mtb.GenderCodingCode;
import dev.pcvolkmer.mv64e.mtb.Patient;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.PatientCatalogue;

/**
 * Mapper class to load and map diagnosis data from database table 'dk_dnpm_kpa'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class PatientDataMapper implements DataMapper<Patient> {

  private final PatientCatalogue patientCatalogue;

  public PatientDataMapper(final PatientCatalogue patientCatalogue) {
    this.patientCatalogue = patientCatalogue;
  }

  /**
   * Loads and maps a patient using the patient database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded MtbDiagnosis file
   */
  @Override
  public Patient getById(final int id) {
    var patientData = patientCatalogue.getById(id);

    var builder = Patient.builder();
    builder
        .id(patientData.getString("patienten_id"))
        .gender(getGenderCoding(patientData))
        .birthDate(patientData.getDate("geburtsdatum"))
        .dateOfDeath(patientData.getDate("sterbedatum"))
        .address(Address.builder().municipalityCode(getMunicipalityCode(patientData)).build());
    return builder.build();
  }

  private GenderCoding getGenderCoding(final ResultSet data) {
    var genderCodingBuilder = GenderCoding.builder().system("Gender");

    String geschlecht = data.getString("geschlecht");
    switch (geschlecht) {
      case "M":
        genderCodingBuilder.code(GenderCodingCode.MALE).display("Männlich");
        break;
      case "F":
        genderCodingBuilder.code(GenderCodingCode.FEMALE).display("Weiblich");
        break;
      case "X":
        genderCodingBuilder.code(GenderCodingCode.OTHER).display("Divers");
        break;
      default:
        genderCodingBuilder.code(GenderCodingCode.UNKNOWN).display("Unbekannt");
    }
    return genderCodingBuilder.build();
  }

  private String getMunicipalityCode(final ResultSet data) {
    var gkz = data.getString("GKZ");
    if (gkz == null || gkz.trim().length() != 8) {
      return null;
    }
    return gkz.substring(0, 5);
  }
}
