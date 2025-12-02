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

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TumorausbreitungCatalogue;
import dev.pcvolkmer.mv64e.mtb.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class to load and map prozedur data from database table 'dk_dnpm_uf_tumorausbreitung'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class KpaTumorausbreitungDataMapper extends AbstractSubformDataMapper<TumorStaging> {

  public KpaTumorausbreitungDataMapper(final TumorausbreitungCatalogue catalogue) {
    super(catalogue);
  }

  /**
   * Loads and maps Prozedur related by database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded data set
   */
  @Override
  public TumorStaging getById(final int id) {
    var data = catalogue.getById(id);
    return this.map(data);
  }

  @Override
  protected TumorStaging map(final ResultSet resultSet) {
    var builder = TumorStaging.builder();
    builder
        .date(resultSet.getDate("zeitpunkt"))
        .method(getTumorStagingMethodCoding(resultSet.getString("typ")))
        .otherClassifications(
            List.of(
                Coding.builder()
                    .code(resultSet.getString("wert"))
                    .system("dnpm-dip/mtb/diagnosis/kds-tumor-spread")
                    .build()))
        .tnmClassification(getTnmClassification(resultSet));

    return builder.build();
  }

  private TumorStagingMethodCoding getTumorStagingMethodCoding(final String value) {
    if (value == null
        || !Arrays.stream(TumorStagingMethodCodingCode.values())
            .map(TumorStagingMethodCodingCode::toValue)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        TumorStagingMethodCoding.builder().system("dnpm-dip/mtb/tumor-staging/method");
    try {
      resultBuilder.code(TumorStagingMethodCodingCode.forValue(value));
    } catch (IOException e) {
      throw new IllegalStateException("No valid code found");
    }

    return resultBuilder.build();
  }

  private TnmClassification getTnmClassification(final ResultSet resultSet) {
    var tnpmClassificationBuilder = TnmClassification.builder();

    var hasContent = false;

    var tnmt = resultSet.getString("tnmt");
    if (tnmt != null && !tnmt.isBlank()) {
      tnpmClassificationBuilder.tumor(
          Coding.builder().code(String.format("T%s", tnmt)).system("UICC").build());
      hasContent = true;
    }

    var tnmn = resultSet.getString("tnmn");
    if (tnmn != null && !tnmn.isBlank()) {
      tnpmClassificationBuilder.nodes(
          Coding.builder().code(String.format("N%s", tnmn)).system("UICC").build());
      hasContent = true;
    }

    var tnmm = resultSet.getString("tnmm");
    if (tnmm != null && !tnmm.isBlank()) {
      tnpmClassificationBuilder.metastasis(
          Coding.builder().code(String.format("M%s", tnmm)).system("UICC").build());
      hasContent = true;
    }

    if (hasContent) {
      return tnpmClassificationBuilder.build();
    }

    return null;
  }
}
