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

package dev.pcvolkmer.mv64e.datamapper.datacatalogues;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Load raw result sets from database table 'dk_molekulargenetik'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class MolekulargenetikCatalogue extends AbstractDataCatalogue {

  private MolekulargenetikCatalogue(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  @Override
  protected String getTableName() {
    return "dk_molekulargenetik";
  }

  @NullMarked
  public static MolekulargenetikCatalogue create(JdbcTemplate jdbcTemplate) {
    return new MolekulargenetikCatalogue(jdbcTemplate);
  }

  /**
   * Retrieves a list of distinct molecular genetics record IDs associated with a given patient ID.
   * The query ensures uniqueness via DISTINCT and maps the result set to a list of Integer IDs
   * representing a mg record each.
   *
   * @param patientId the internal database ID of the patient
   * @return a list of unique molecular genetics record IDs related to the patient
   */
  public List<Integer> getByPatientId(int patientId) {
    return this.jdbcTemplate
        .queryForList(
            "SELECT DISTINCT mg.id "
                + "FROM dk_molekulargenetik mg "
                + "JOIN prozedur molprozedur ON molprozedur.id = mg.id "
                + "JOIN patient pat ON pat.id = molprozedur.patient_id "
                + "WHERE pat.id = ? "
                + "AND molprozedur.geloescht = 0",
            patientId)
        .stream()
        .map(ResultSet::from)
        .map(rs -> rs.getInteger("id"))
        .collect(Collectors.toList());
  }

  /**
   * Get procedure IDs by related Therapieplan procedure id Related form references in
   * Einzelempfehlung, Rebiopsie, Reevaluation
   *
   * @param therapieplanId The procedure id
   * @return The procedure ids
   */
  public List<Integer> getByTherapieplanId(int therapieplanId) {
    return this.jdbcTemplate
        .queryForList(
            "SELECT DISTINCT ref_molekulargenetik FROM dk_dnpm_uf_einzelempfehlung JOIN prozedur ON (prozedur.id = dk_dnpm_uf_einzelempfehlung.id) "
                + " WHERE ref_molekulargenetik IS NOT NULL AND hauptprozedur_id = ? "
                + " UNION SELECT ref_molekulargenetik FROM dk_dnpm_uf_rebiopsie JOIN prozedur ON (prozedur.id = dk_dnpm_uf_rebiopsie.id) "
                + " WHERE ref_molekulargenetik IS NOT NULL AND hauptprozedur_id = ? "
                + " UNION SELECT ref_molekulargenetik FROM dk_dnpm_uf_reevaluation JOIN prozedur ON (prozedur.id = dk_dnpm_uf_reevaluation.id) "
                + " WHERE ref_molekulargenetik IS NOT NULL AND hauptprozedur_id = ?;",
            therapieplanId,
            therapieplanId,
            therapieplanId)
        .stream()
        .map(ResultSet::from)
        .map(rs -> rs.getInteger("ref_molekulargenetik"))
        .collect(Collectors.toList());
  }

  /**
   * Get procedure IDs used in related KPA/Therapieplan procedures Related form references in
   * Einzelempfehlung, Rebiopsie, Reevaluation
   *
   * @param kpaId The procedure id
   * @return The procedure ids
   */
  public List<Integer> getIdsByKpaId(int kpaId) {
    return this.jdbcTemplate
        .queryForList(
            "SELECT DISTINCT ref_molekulargenetik FROM dk_dnpm_uf_einzelempfehlung JOIN prozedur ON (prozedur.id = dk_dnpm_uf_einzelempfehlung.id) "
                + " WHERE ref_molekulargenetik IS NOT NULL AND hauptprozedur_id IN (SELECT id FROM dk_dnpm_therapieplan WHERE ref_dnpm_klinikanamnese = ?) "
                + " UNION SELECT ref_molekulargenetik FROM dk_dnpm_uf_rebiopsie JOIN prozedur ON (prozedur.id = dk_dnpm_uf_rebiopsie.id) "
                + " WHERE ref_molekulargenetik IS NOT NULL AND hauptprozedur_id IN (SELECT id FROM dk_dnpm_therapieplan WHERE ref_dnpm_klinikanamnese = ?) "
                + " UNION SELECT ref_molekulargenetik FROM dk_dnpm_uf_reevaluation JOIN prozedur ON (prozedur.id = dk_dnpm_uf_reevaluation.id) "
                + " WHERE ref_molekulargenetik IS NOT NULL AND hauptprozedur_id IN (SELECT id FROM dk_dnpm_therapieplan WHERE ref_dnpm_klinikanamnese = ?);",
            kpaId,
            kpaId,
            kpaId)
        .stream()
        .map(ResultSet::from)
        .map(rs -> rs.getInteger("ref_molekulargenetik"))
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Get procedure result set by einsendenummer
   *
   * @param einsendenummer The case id related to this procedure
   * @return The procedure id
   */
  public ResultSet getByEinsendenummer(String einsendenummer) {
    var result =
        this.jdbcTemplate.queryForList(
            String.format(
                "SELECT patient.patienten_id, %s.*, prozedur.* FROM %s JOIN prozedur ON (prozedur.id = %s.id) JOIN patient ON (patient.id = prozedur.patient_id) WHERE geloescht = 0 AND %s.einsendenummer = ?",
                getTableName(), getTableName(), getTableName(), getTableName()),
            einsendenummer);

    if (result.isEmpty()) {
      throw new DataAccessException("No record found for einsendenummer: " + einsendenummer);
    } else if (result.size() > 1) {
      throw new DataAccessException("Multiple records found for einsendenummer: " + einsendenummer);
    }

    var resultSet = ResultSet.from(result.get(0));

    if (resultSet.getRawData().containsKey("id")) {
      var merkmale = getMerkmaleById(resultSet.getId());
      if (merkmale.isEmpty()) {
        return resultSet;
      }
      merkmale.forEach((key, value) -> resultSet.getRawData().put(key, value));
    }

    return resultSet;
  }

  public String getSampleConservationFromMgc(int molekulargenetikCatalogueId) {

    return this.jdbcTemplate.queryForObject(
        "SELECT DISTINCT prop_materialfixierung.shortdesc "
            + "FROM dk_molekulargenetik mg "
            + "LEFT JOIN property_catalogue_version_entry AS prop_materialfixierung "
            + "ON ( prop_materialfixierung.property_version_id = mg.materialfixierung_propcat_version "
            + "AND prop_materialfixierung.code = mg.materialfixierung) "
            + "WHERE mg.id = ? "
            + "LIMIT 1",
        String.class,
        molekulargenetikCatalogueId);
  }
}
