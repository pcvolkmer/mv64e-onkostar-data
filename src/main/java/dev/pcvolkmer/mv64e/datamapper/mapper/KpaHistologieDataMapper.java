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
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.HistologieCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.MolekulargenetikCatalogue;
import dev.pcvolkmer.mv64e.mtb.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper class to load and map prozedur data from database table 'dk_dnpm_vorbefunde'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class KpaHistologieDataMapper extends AbstractSubformDataMapper<HistologyReport> {

  private static final Logger logger = LoggerFactory.getLogger(KpaHistologieDataMapper.class);
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
  @Nullable
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

  @NullMarked
  public List<Integer> getMolGenIdsFromHistoOfTypeSequence(final int parentId) {
    var seqHistos =
        catalogue.getAllByParentId(parentId).stream()
            .filter(this::isOfTypeSeqencing)
            .collect(Collectors.toList());
    logger.info("Found {} histologies of type sequence", seqHistos.size());

    return seqHistos.stream()
        .map(histo -> histo.getInteger("histologie"))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private boolean isOfTypeSeqencing(final ResultSet resultSet) {
    var histoId = resultSet.getInteger("histologie");
    if (null == histoId) {
      return false;
    }

    return molekulargenetikCatalogue.isOfTypeSeqencing(histoId);
  }

  @Nullable
  @Override
  protected HistologyReport map(final ResultSet resultSet) {
    var histoId = resultSet.getInteger("histologie");
    if (null == histoId) {
      return null;
    }

    var builder = HistologyReport.builder();
    var osMolGen = molekulargenetikCatalogue.getById(histoId);

    var histologieReportResultBuilder = HistologyReportResults.builder();

    getTumorMorphology(resultSet, osMolGen)
        .ifPresent(histologieReportResultBuilder::tumorMorphology);

    getTumorCellContent(resultSet, osMolGen)
        .ifPresent(histologieReportResultBuilder::tumorCellContent);

    builder
        .id(resultSet.getId().toString())
        .patient(resultSet.getPatientReference())
        .issuedOn(resultSet.getDate("erstellungsdatum"))
        .specimen(Reference.builder().id(osMolGen.getId().toString()).type("Specimen").build())
        .results(histologieReportResultBuilder.build());

    return builder.build();
  }

  private Optional<TumorMorphology> getTumorMorphology(ResultSet resultSet, ResultSet osMolGen) {
    var builder =
        TumorMorphology.builder()
            .id(resultSet.getId().toString())
            .patient(resultSet.getPatientReference())
            .specimen(Reference.builder().id(osMolGen.getId().toString()).type("Specimen").build());

    var tumorMorphologyCoding = getTumorMorphologyCoding(resultSet);
    if (null == tumorMorphologyCoding) {
      return Optional.empty();
    }

    return Optional.of(builder.value(tumorMorphologyCoding).build());
  }

  private Optional<TumorCellContent> getTumorCellContent(ResultSet resultSet, ResultSet osMolGen) {
    var builder =
        TumorCellContent.builder()
            .id(resultSet.getId().toString())
            .patient(resultSet.getPatientReference())
            .specimen(Reference.builder().id(osMolGen.getId().toString()).type("Specimen").build())
            .method(
                TumorCellContentMethodCoding.builder()
                    .code(TumorCellContentMethodCodingCode.HISTOLOGIC)
                    .build());

    var tumorzellgehaltValue = resultSet.getLong("tumorzellgehalt");
    if (null == tumorzellgehaltValue) {
      return Optional.empty();
    }
    return Optional.of(builder.value(tumorzellgehaltValue / 100.0).build());
  }

  @Nullable
  private Coding getTumorMorphologyCoding(ResultSet resultSet) {
    var morphologie = resultSet.getString("morphologie");
    var morphologiePropcatVersion = resultSet.getInteger("morphologie_propcat_version");

    if (null == morphologie || null == morphologiePropcatVersion) {
      return null;
    }

    var propertyCatalogueEntry =
        propertyCatalogue.getByCodeAndVersion(morphologie, morphologiePropcatVersion);

    return Coding.builder()
        .code(propertyCatalogueEntry.getCode())
        .display(propertyCatalogueEntry.getShortdesc())
        .version(propertyCatalogueEntry.getVersionDescription())
        .build();
  }
}
