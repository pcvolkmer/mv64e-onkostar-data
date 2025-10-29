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

import dev.pcvolkmer.onco.datamapper.CustomMetadata;
import dev.pcvolkmer.onco.datamapper.datacatalogues.KpaCatalogue;
import dev.pcvolkmer.onco.datamapper.datacatalogues.PatientCatalogue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper class to load and map custom metadata from database
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class CustomMetadataDataMapper implements DataMapper<CustomMetadata> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final KpaCatalogue kpaCatalogue;
  private final PatientCatalogue patientCatalogue;

  public CustomMetadataDataMapper(
      final KpaCatalogue kpaCatalogue, final PatientCatalogue patientCatalogue) {
    this.kpaCatalogue = kpaCatalogue;
    this.patientCatalogue = patientCatalogue;
  }

  @Override
  public CustomMetadata getById(int id) {
    var kpaData = kpaCatalogue.getById(id);
    var patientData = patientCatalogue.getById(kpaData.getInteger("patient_id"));

    return new CustomMetadata(
        kpaData.getString("fallnummermv"), patientData.getString("verischerungsnummer"));
  }

  /**
   * Loads and maps using the case id
   *
   * @param caseId The case id
   * @return The loaded Mtb file
   */
  public CustomMetadata getByCaseId(String caseId) {
    return this.getById(this.kpaCatalogue.getProcedureIdByCaseId(caseId));
  }

  /**
   * Loads and maps using the patient id and tumor id
   *
   * @param patientId The patients id (not database id)
   * @param tumorId The tumor identification
   * @return The loaded Mtb file
   */
  public CustomMetadata getLatestByPatientIdAndTumorId(String patientId, int tumorId) {
    return this.getById(
        this.kpaCatalogue.getLatestProcedureIdByPatientIdAndTumor(patientId, tumorId));
  }
}
