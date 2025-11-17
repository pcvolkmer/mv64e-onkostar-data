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

import dev.pcvolkmer.mv64e.mtb.MtbProcedureRecommendationCategoryCoding;
import dev.pcvolkmer.mv64e.mtb.MtbProcedureRecommendationCategoryCodingCode;
import dev.pcvolkmer.mv64e.mtb.ProcedureRecommendation;
import dev.pcvolkmer.mv64e.mtb.Reference;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.EinzelempfehlungCatalogue;
import dev.pcvolkmer.onco.datamapper.exceptions.DataAccessException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

/**
 * Mapper class to load and map diagnosis data from database table 'dk_dnpm_einzelempfehlung'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class EinzelempfehlungProzedurDataMapper
    extends AbstractEinzelempfehlungDataMapper<ProcedureRecommendation> {

  public EinzelempfehlungProzedurDataMapper(EinzelempfehlungCatalogue einzelempfehlungCatalogue) {
    super(einzelempfehlungCatalogue);
  }

  @Override
  protected ProcedureRecommendation map(ResultSet resultSet) {
    // Fetch date from care plan due to https://github.com/pcvolkmer/onkostar-plugin-dnpm/issues/213
    var carePlan = this.catalogue.getParentById(resultSet.getId());
    var date = carePlan.getDate("datum");
    if (null == date) {
      throw new DataAccessException("Cannot map datum for ProcedureRecommendation");
    }

    var kpaId = carePlan.getString("ref_dnpm_klinikanamnese");
    if (null == kpaId) {
      throw new DataAccessException("Cannot map KPA as Diagnosis");
    }

    var resultBuilder =
        ProcedureRecommendation.builder()
            .id(resultSet.getString("id"))
            .patient(resultSet.getPatientReference())
            .priority(getRecommendationPriorityCoding(resultSet.getInteger("prio")))
            // TODO Fix id?
            .reason(Reference.builder().id(kpaId).build())
            .issuedOn(date)
            .levelOfEvidence(getLevelOfEvidence(resultSet));

    if (null != resultSet.getString("evidenzlevel")) {
      resultBuilder.priority(
          getRecommendationPriorityCoding(
              resultSet.getString("evidenzlevel"),
              resultSet.getInteger("evidenzlevel_propcat_version")));
    }

    // Nur der erste Eintrag!
    if (!resultSet.getMerkmalList("art_der_therapie").isEmpty()) {
      resultBuilder.code(
          getMtbProcedureRecommendationCategoryCoding(
              resultSet.getMerkmalList("art_der_therapie").get(0),
              resultSet.getInteger("art_der_therapie_propcat_version")));
    }

    // As of now: Simple variant and CSV only! - Not used but present for completeness
    if (null != resultSet.getString("st_mol_alt_variante_json")) {
      resultBuilder.supportingVariants(
          JsonToMolAltVarianteMapper.map(resultSet.getString("st_mol_alt_variante_json")));
    }

    return resultBuilder.build();
  }

  @Override
  public ProcedureRecommendation getById(int id) {
    return this.map(this.catalogue.getById(id));
  }

  @NullMarked
  @Override
  public List<ProcedureRecommendation> getByParentId(final int parentId) {
    return catalogue.getAllByParentId(parentId).stream()
        // Filter Prozedurempfehlung (Weitere Empfehlungen)
        .filter(it -> "sonstige".equals(it.getString("empfehlungskategorie")))
        .map(this::map)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());
  }

  private MtbProcedureRecommendationCategoryCoding getMtbProcedureRecommendationCategoryCoding(
      String code, int version) {
    if (code == null
        || !Arrays.stream(MtbProcedureRecommendationCategoryCodingCode.values())
            .map(MtbProcedureRecommendationCategoryCodingCode::toValue)
            .collect(Collectors.toSet())
            .contains(code)) {
      return null;
    }

    var resultBuilder =
        MtbProcedureRecommendationCategoryCoding.builder()
            .system("dnpm-dip/mtb/recommendation/procedure/category");

    try {
      resultBuilder.code(MtbProcedureRecommendationCategoryCodingCode.forValue(code)).display(code);
    } catch (IOException e) {
      return null;
    }

    return resultBuilder.build();
  }
}
