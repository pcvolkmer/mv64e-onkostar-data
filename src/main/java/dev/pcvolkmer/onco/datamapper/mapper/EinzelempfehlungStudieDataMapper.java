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

import dev.pcvolkmer.mv64e.mtb.MtbStudyEnrollmentRecommendation;
import dev.pcvolkmer.mv64e.mtb.Reference;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.EinzelempfehlungCatalogue;
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
public class EinzelempfehlungStudieDataMapper
    extends AbstractEinzelempfehlungDataMapper<MtbStudyEnrollmentRecommendation> {

  public EinzelempfehlungStudieDataMapper(EinzelempfehlungCatalogue einzelempfehlungCatalogue) {
    super(einzelempfehlungCatalogue);
  }

  @Override
  protected MtbStudyEnrollmentRecommendation map(ResultSet resultSet) {
    var resultBuilder =
        MtbStudyEnrollmentRecommendation.builder()
            .id(resultSet.getString("id"))
            .patient(resultSet.getPatientReference())
            .priority(getRecommendationPriorityCoding(resultSet.getInteger("prio")))
            // TODO Fix id?
            .reason(Reference.builder().id(resultSet.getString("id")).build())
            .issuedOn(resultSet.getDate("datum"))
            .medication(JsonToMedicationMapper.map(resultSet.getString("wirkstoffe_json")))
            .levelOfEvidence(getLevelOfEvidence(resultSet))
            .study(JsonToStudyMapper.map(resultSet.getString("studien_alle_json")));

    // As of now: Simple variant and CSV only!
    if (null != resultSet.getString("st_mol_alt_variante_json")) {
      resultBuilder.supportingVariants(
          JsonToMolAltVarianteMapper.map(resultSet.getString("st_mol_alt_variante_json")));
    }

    return resultBuilder.build();
  }

  @Override
  public MtbStudyEnrollmentRecommendation getById(int id) {
    return this.map(this.catalogue.getById(id));
  }

  @NullMarked
  @Override
  public List<MtbStudyEnrollmentRecommendation> getByParentId(final int parentId) {
    return catalogue.getAllByParentId(parentId).stream()
        // Filter Wirkstoffempfehlung (Systemische Therapie)
        .filter(it -> "studie".equals(it.getString("empfehlungskategorie")))
        .map(this::map)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());
  }
}
