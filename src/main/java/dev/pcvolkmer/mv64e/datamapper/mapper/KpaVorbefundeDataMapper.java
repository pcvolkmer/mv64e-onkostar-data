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
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.MolekulargenetikCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.VorbefundeCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import dev.pcvolkmer.mv64e.mtb.MolecularDiagnosticReportCoding;
import dev.pcvolkmer.mv64e.mtb.MolecularDiagnosticReportCodingCode;
import dev.pcvolkmer.mv64e.mtb.PriorDiagnosticReport;
import dev.pcvolkmer.mv64e.mtb.Reference;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

/**
 * Mapper class to load and map prozedur data from database table 'dk_dnpm_vorbefunde'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class KpaVorbefundeDataMapper extends AbstractSubformDataMapper<PriorDiagnosticReport> {

  private final MolekulargenetikCatalogue molekulargenetikCatalogue;
  private final PropertyCatalogue propertyCatalogue;

  public KpaVorbefundeDataMapper(
      final VorbefundeCatalogue catalogue,
      final MolekulargenetikCatalogue molekulargenetikCatalogue,
      final PropertyCatalogue propertyCatalogue) {
    super(catalogue);
    this.molekulargenetikCatalogue = molekulargenetikCatalogue;
    this.propertyCatalogue = propertyCatalogue;
  }

  /**
   * Loads and maps Prozedur related by database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded data set
   */
  @Override
  public PriorDiagnosticReport getById(final int id) {
    var data = catalogue.getById(id);
    return this.map(data);
  }

  @NullMarked
  @Override
  public List<PriorDiagnosticReport> getByParentId(final int parentId) {
    try {
      return catalogue.getAllByParentId(parentId).stream()
          .map(this::map)
          .filter(Objects::nonNull)
          .distinct()
          .collect(Collectors.toList());
    } catch (DataAccessException e) {
      return Collections.emptyList();
    }
  }

  @Override
  protected PriorDiagnosticReport map(final ResultSet resultSet) {
    var builder = PriorDiagnosticReport.builder();
    var einsendenummer = resultSet.getString("befundnummer");

    if (einsendenummer == null || einsendenummer.equalsIgnoreCase("unbekannt")) return null;

    var osMolGen = molekulargenetikCatalogue.getByEinsendenummer(einsendenummer);
    if (osMolGen == null) return null;

    builder
        .id(resultSet.getId().toString())
        .patient(resultSet.getPatientReference())
        .issuedOn(resultSet.getDate("erstellungsdatum"))
        .specimen(Reference.builder().id(osMolGen.getId().toString()).type("Specimen").build())
        .type(
            getMolecularDiagnosticReportCoding(
                resultSet.getString("artderdiagnostik"),
                resultSet.getInteger("artderdiagnostik_propcat_version")))
        .results(List.of(resultSet.getString("ergebnisse")));

    return builder.build();
  }

  private MolecularDiagnosticReportCoding getMolecularDiagnosticReportCoding(
      String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(MolecularDiagnosticReportCodingCode.values())
            .map(MolecularDiagnosticReportCodingCode::toValue)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        MolecularDiagnosticReportCoding.builder()
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());
    try {
      resultBuilder.code(MolecularDiagnosticReportCodingCode.forValue(value));
    } catch (IOException e) {
      return null;
    }

    return resultBuilder.build();
  }
}
