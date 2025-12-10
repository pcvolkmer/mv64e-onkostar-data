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
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.EinzelempfehlungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapieplanCatalogue;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import dev.pcvolkmer.mv64e.mtb.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;

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
      EinzelempfehlungCatalogue einzelempfehlungCatalogue,
      TherapieplanCatalogue therapieplanCatalogue,
      PropertyCatalogue propertyCatalogue) {
    super(
        einzelempfehlungCatalogue,
        therapieplanCatalogue,
        LoggerFactory.getLogger(EinzelempfehlungWirkstoffDataMapper.class));
    this.propertyCatalogue = propertyCatalogue;
  }

  @Override
  protected MtbMedicationRecommendation map(ResultSet resultSet) {
    // Fetch date from care plan due to https://github.com/pcvolkmer/onkostar-plugin-dnpm/issues/213
    var hauptprozedurid = resultSet.getParentId();
    if (null == hauptprozedurid) {
      throw new DataAccessException("Cannot fetch 'Therapieplan'");
    }
    var carePlan = this.therapieplanCatalogue.getById(hauptprozedurid);

    var resultBuilder =
        MtbMedicationRecommendation.builder()
            .id(resultSet.getString("id"))
            .patient(resultSet.getPatientReference())
            .reason(Reference.builder().id(this.getCarePlanKpaId(carePlan)).build())
            .issuedOn(this.getCarePlanDate(carePlan))
            .medication(JsonToMedicationMapper.map(resultSet.getString("wirkstoffe_json")))
            .levelOfEvidence(getLevelOfEvidence(resultSet));

    MapperUtils.tryAndReturnOrLog(() -> getRecommendationPriority(resultSet), log)
        .ifPresent(resultBuilder::priority);

    final var artDerTherapie = resultSet.getMerkmalList("art_der_therapie");
    final var artDerTherapiePropcat = resultSet.getInteger("art_der_therapie_propcat_version");
    if (!artDerTherapie.isEmpty() && null != artDerTherapiePropcat) {
      resultBuilder.category(
          artDerTherapie.stream()
              .map(
                  value ->
                      getMtbMedicationRecommendationCategoryCoding(value, artDerTherapiePropcat))
              .collect(Collectors.toList()));
    }

    final var empfehlungsart = resultSet.getString("empfehlungsart");
    final var empfehlungsartPropcat = resultSet.getInteger("empfehlungsart_propcat_version");
    if (null != empfehlungsart && null != empfehlungsartPropcat) {
      resultBuilder.useType(
          getMtbMedicationRecommendationUseTypeCoding(empfehlungsart, empfehlungsartPropcat));
    }

    // As of now: Simple variant and CSV only!
    var supportingVariants = resultSet.getString("st_mol_alt_variante_json");
    if (null != supportingVariants) {
      resultBuilder.supportingVariants(JsonToMolAltVarianteMapper.map(supportingVariants));
    }

    return resultBuilder.build();
  }

  @Nullable
  @Override
  public MtbMedicationRecommendation getById(int id) {
    return this.map(this.catalogue.getById(id));
  }

  @NullMarked
  @Override
  public List<MtbMedicationRecommendation> getByParentId(final int parentId) {
    return catalogue.getAllByParentId(parentId).stream()
        // Filter Wirkstoffempfehlung (Systemische Therapie)
        .filter(it -> "systemisch".equals(it.getString("empfehlungskategorie")))
        .map(this::map)
        .filter(Objects::nonNull)
        .distinct()
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
