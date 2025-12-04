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

import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import org.jspecify.annotations.NullMarked;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Load raw result sets from database table 'dk_dnpm_kpa'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class KpaCatalogue extends AbstractDataCatalogue {

  private KpaCatalogue(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  @Override
  protected String getTableName() {
    return "dk_dnpm_kpa";
  }

  @NullMarked
  public static KpaCatalogue create(JdbcTemplate jdbcTemplate) {
    return new KpaCatalogue(jdbcTemplate);
  }

  /**
   * Get procedure database id by case id
   *
   * @param caseId The case id
   * @return The procedure id
   */
  public int getProcedureIdByCaseId(String caseId) {
    var result =
        this.jdbcTemplate.query(
            "SELECT dk_dnpm_kpa.id FROM dk_dnpm_kpa JOIN prozedur ON (prozedur.id = dk_dnpm_kpa.id) WHERE prozedur.geloescht = 0 AND dk_dnpm_kpa.fallnummermv = ?",
            (resultSet, i) -> resultSet.getInt(1),
            caseId);

    if (result.isEmpty()) {
      throw new DataAccessException("No record found for case: " + caseId);
    } else if (result.size() > 1) {
      throw new DataAccessException("Multiple procedure IDs found for case: " + caseId);
    }

    return result.get(0);
  }

  /**
   * Get latest procedure database id by patient id and tumor id
   *
   * @param patientId The patients id (not database id)
   * @param tumorId The tumor identifier
   * @return The procedure id
   */
  public int getLatestProcedureIdByPatientIdAndTumor(String patientId, int tumorId) {
    var sql =
        "SELECT prozedur.id FROM dk_dnpm_kpa "
            + "    JOIN prozedur ON (prozedur.id = dk_dnpm_kpa.id) "
            + "    JOIN erkrankung_prozedur ON (erkrankung_prozedur.prozedur_id = prozedur.id) "
            + "    JOIN erkrankung ON (erkrankung_prozedur.erkrankung_id = erkrankung.id) "
            + "    JOIN patient ON (patient.id = prozedur.patient_id) "
            + "    WHERE patient.patienten_id = ? AND erkrankung.tumoridentifikator = ? "
            + "    ORDER BY dk_dnpm_kpa.anmeldedatummtb DESC "
            + "    LIMIT 1";

    var result =
        this.jdbcTemplate.query(sql, (resultSet, i) -> resultSet.getInt(1), patientId, tumorId);

    if (result.isEmpty()) {
      throw new DataAccessException(
          String.format("No record found for patient '%s' and tumor '%d'", patientId, tumorId));
    } else if (result.size() > 1) {
      // This should not happen due to LIMIT 1
      throw new DataAccessException(
          String.format(
              "Multiple records found for patient '%s' and tumor '%d'", patientId, tumorId));
    }

    return result.get(0);
  }

  /**
   * Get patient database id by case id
   *
   * @param caseId The case id
   * @return The patients database id
   */
  public int getPatientIdByCaseId(String caseId) {
    var result =
        this.jdbcTemplate.query(
            "SELECT patient_id FROM dk_dnpm_kpa JOIN prozedur ON (prozedur.id = dk_dnpm_kpa.id) WHERE prozedur.geloescht = 0 AND fallnummermv = ?",
            (resultSet, i) -> resultSet.getInt(1),
            caseId);

    if (result.isEmpty()) {
      throw new DataAccessException("No record found for case: " + caseId);
    } else if (result.size() > 1) {
      throw new DataAccessException("Multiple patient IDs found for case: " + caseId);
    }

    return result.get(0);
  }
}
