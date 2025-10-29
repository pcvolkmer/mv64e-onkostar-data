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
import dev.pcvolkmer.onco.datamapper.datacatalogues.EinzelempfehlungCatalogue;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class to load and map diagnosis data from database table 'dk_dnpm_einzelempfehlung'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class EinzelempfehlungWirkstoffDataMapper
    extends AbstractEinzelempfehlungDataMapper<MtbMedicationRecommendation> {

  private final PropertyCatalogue propertyCatalogue;

  public EinzelempfehlungWirkstoffDataMapper(
      EinzelempfehlungCatalogue einzelempfehlungCatalogue, PropertyCatalogue propertyCatalogue) {
    super(einzelempfehlungCatalogue);
    this.propertyCatalogue = propertyCatalogue;
  }

  @Override
  protected MtbMedicationRecommendation map(ResultSet resultSet) {
    var resultBuilder =
        MtbMedicationRecommendation.builder()
            .id(resultSet.getString("id"))
            .patient(resultSet.getPatientReference())
            .priority(getRecommendationPriorityCoding(resultSet.getInteger("prio")))
            // TODO Fix id?
            // .reason(Reference.builder().id(resultSet.getString("id")).build())
            .issuedOn(resultSet.getDate("datum"))
            .medication(JsonToMedicationMapper.map(resultSet.getString("wirkstoffe_json")))
            .levelOfEvidence(getLevelOfEvidence(resultSet));

    if (!resultSet.getMerkmalList("art_der_therapie").isEmpty()) {
      resultBuilder.category(
          resultSet.getMerkmalList("art_der_therapie").stream()
              .map(
                  value ->
                      getMtbMedicationRecommendationCategoryCoding(
                          value, resultSet.getInteger("art_der_therapie_propcat_version")))
              .collect(Collectors.toList()));
    }

    if (null != resultSet.getString("empfehlungsart")) {
      resultBuilder.useType(
          getMtbMedicationRecommendationUseTypeCoding(
              resultSet.getString("empfehlungsart"),
              resultSet.getInteger("empfehlungsart_propcat_version")));
    }

    // As of now: Simple variant and CSV only!
    if (null != resultSet.getString("st_mol_alt_variante_json")) {
      resultBuilder.supportingVariants(
          JsonToMolAltVarianteMapper.map(resultSet.getString("st_mol_alt_variante_json")));
    }

    return resultBuilder.build();
  }

  @Override
  public MtbMedicationRecommendation getById(int id) {
    return this.map(this.catalogue.getById(id));
  }

  @Override
  public List<MtbMedicationRecommendation> getByParentId(final int parentId) {
    return catalogue.getAllByParentId(parentId).stream()
        // Filter Wirkstoffempfehlung (Systemische Therapie)
        .filter(it -> "systemisch".equals(it.getString("empfehlungskategorie")))
        .map(this::map)
        .collect(Collectors.toList());
  }

  private MtbMedicationRecommendationCategoryCoding getMtbMedicationRecommendationCategoryCoding(
      String code, int version) {
    if (code == null
        || !Arrays.stream(MtbMedicationRecommendationCategoryCodingCode.values())
            .map(MtbMedicationRecommendationCategoryCodingCode::toValue)
            .collect(Collectors.toSet())
            .contains(code)) {
      return null;
    }

    var resultBuilder =
        MtbMedicationRecommendationCategoryCoding.builder()
            .system("dnpm-dip/mtb/recommendation/systemic-therapy/category");

    try {
      resultBuilder
          .code(MtbMedicationRecommendationCategoryCodingCode.forValue(code))
          .display(propertyCatalogue.getByCodeAndVersion(code, version).getShortdesc());
    } catch (IOException e) {
      return null;
    }

    return resultBuilder.build();
  }

  private MtbMedicationRecommendationUseTypeCoding getMtbMedicationRecommendationUseTypeCoding(
      String code, int version) {
    if (code == null
        || !Arrays.stream(MtbMedicationRecommendationUseTypeCodingCode.values())
            .map(MtbMedicationRecommendationUseTypeCodingCode::toValue)
            .collect(Collectors.toSet())
            .contains(code)) {
      return null;
    }

    var resultBuilder =
        MtbMedicationRecommendationUseTypeCoding.builder()
            .system("dnpm-dip/mtb/recommendation/systemic-therapy/use-type");

    try {
      resultBuilder
          .code(MtbMedicationRecommendationUseTypeCodingCode.forValue(code))
          .display(propertyCatalogue.getByCodeAndVersion(code, version).getShortdesc());
    } catch (IOException e) {
      return null;
    }

    return resultBuilder.build();
  }
}
