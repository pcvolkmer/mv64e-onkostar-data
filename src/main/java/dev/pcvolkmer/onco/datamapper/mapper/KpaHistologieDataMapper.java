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

import dev.pcvolkmer.mv64e.mtb.*;
import dev.pcvolkmer.onco.datamapper.PropertyCatalogue;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.HistologieCatalogue;
import dev.pcvolkmer.onco.datamapper.datacatalogues.MolekulargenetikCatalogue;
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
public class KpaHistologieDataMapper extends AbstractSubformDataMapper<HistologyReport> {

  private final MolekulargenetikCatalogue molekulargenetikCatalogue;
  private final PropertyCatalogue propertyCatalogue;

  public KpaHistologieDataMapper(
      final HistologieCatalogue catalogue,
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
  public HistologyReport getById(final int id) {
    var data = catalogue.getById(id);
    return this.map(data);
  }

  @NullMarked
  @Override
  public List<HistologyReport> getByParentId(final int parentId) {
    return catalogue.getAllByParentId(parentId).stream()
        .map(this::map)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());
  }

  @Override
  protected HistologyReport map(final ResultSet resultSet) {
    var builder = HistologyReport.builder();

    var osMolGen = molekulargenetikCatalogue.getById(resultSet.getInteger("histologie"));

    if (null != osMolGen) {
      builder
          .id(resultSet.getId().toString())
          .patient(resultSet.getPatientReference())
          .issuedOn(resultSet.getDate("erstellungsdatum"))
          .specimen(Reference.builder().id(osMolGen.getId().toString()).type("Specimen").build())
          .results(
              HistologyReportResults.builder()
                  .tumorCellContent(
                      TumorCellContent.builder()
                          .id(resultSet.getId().toString())
                          .patient(resultSet.getPatientReference())
                          .specimen(
                              Reference.builder()
                                  .id(osMolGen.getId().toString())
                                  .type("Specimen")
                                  .build())
                          .value(resultSet.getLong("tumorzellgehalt") / 100.0)
                          // TODO: Nicht in OS.Molekulargenetik
                          .method(
                              TumorCellContentMethodCoding.builder()
                                  .code(TumorCellContentMethodCodingCode.HISTOLOGIC)
                                  .build())
                          .build())
                  .tumorMorphology(
                      TumorMorphology.builder()
                          .id(resultSet.getId().toString())
                          .patient(resultSet.getPatientReference())
                          .specimen(
                              Reference.builder()
                                  .id(osMolGen.getId().toString())
                                  .type("Specimen")
                                  .build())
                          .value(getTumorMorphologyCoding(resultSet))
                          .build())
                  .build());

      return builder.build();
    }

    return null;
  }

  private Coding getTumorMorphologyCoding(ResultSet resultSet) {
    var propertyCatalogueEntry =
        propertyCatalogue.getByCodeAndVersion(
            resultSet.getString("morphologie"),
            resultSet.getInteger("morphologie_propcat_version"));

    return Coding.builder()
        .code(propertyCatalogueEntry.getCode())
        .display(propertyCatalogueEntry.getShortdesc())
        .version(propertyCatalogueEntry.getVersionDescription())
        .build();
  }
}
