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

import dev.pcvolkmer.mv64e.mtb.ConsentProvision;
import dev.pcvolkmer.mv64e.mtb.ModelProjectConsent;
import dev.pcvolkmer.mv64e.mtb.ModelProjectConsentPurpose;
import dev.pcvolkmer.mv64e.mtb.Provision;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.ConsentMvCatalogue;
import dev.pcvolkmer.onco.datamapper.datacatalogues.ConsentMvVerlaufCatalogue;
import java.util.*;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

/**
 * Mapper class to load and map diagnosis data from database table 'dk_dnpm_consentmv'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class ConsentMvDataMapper implements DataMapper<ModelProjectConsent> {

  private final ConsentMvVerlaufCatalogue consentMvVerlaufCatalogue;

  @SuppressWarnings("unused")
  public ConsentMvDataMapper(
      final ConsentMvCatalogue catalogue,
      final ConsentMvVerlaufCatalogue consentMvVerlaufCatalogue) {
    this.consentMvVerlaufCatalogue = consentMvVerlaufCatalogue;
  }

  /**
   * Loads and maps consent data using the consent form database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded Consent data
   */
  @Override
  public ModelProjectConsent getById(int id) {
    try {
      var builder = ModelProjectConsent.builder();
      builder.version(getLatestVersion(id)).provisions(getProvisions(id));
      return builder.build();
    } catch (Exception e) {
      return null;
    }
  }

  @NullMarked
  private static Comparator<ResultSet> getResultSetDateComparator() {
    return (rs1, rs2) -> {
      var date1 = rs1.getDate("date");
      var date2 = rs2.getDate("date");
      if (null == date1 || null == date2) {
        return 0;
      }
      return date2.compareTo(date1);
    };
  }

  private String getLatestVersion(int id) {
    return consentMvVerlaufCatalogue.getAllByParentId(id).stream()
        .sorted(getResultSetDateComparator())
        .map(resultSet -> resultSet.getString("version"))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse("");
  }

  @NullMarked
  private List<Provision> getProvisions(final int id) {
    var result = new ArrayList<Provision>();

    var all =
        consentMvVerlaufCatalogue.getAllByParentId(id).stream()
            .sorted(getResultSetDateComparator())
            .collect(Collectors.toList());

    var latest = all.stream().findFirst();
    if (latest.isEmpty()) {
      return result;
    }

    all.stream()
        .filter(
            rs -> {
              var sequencing = rs.getString("sequencing");
              return null != sequencing && !sequencing.isBlank();
            })
        .findFirst()
        .ifPresent(
            rs ->
                result.add(
                    Provision.builder()
                        .date(rs.getDate("date"))
                        .purpose(ModelProjectConsentPurpose.SEQUENCING)
                        .type(
                            ConsentProvision.PERMIT.toValue().equals(rs.getString("sequencing"))
                                ? ConsentProvision.PERMIT
                                : ConsentProvision.DENY)
                        .build()));

    all.stream()
        .filter(
            rs -> {
              var caseidentification = rs.getString("caseidentification");
              return null != caseidentification && !caseidentification.isBlank();
            })
        .findFirst()
        .ifPresent(
            rs ->
                result.add(
                    Provision.builder()
                        .date(rs.getDate("date"))
                        .purpose(ModelProjectConsentPurpose.CASE_IDENTIFICATION)
                        .type(
                            ConsentProvision.PERMIT
                                    .toValue()
                                    .equals(rs.getString("caseidentification"))
                                ? ConsentProvision.PERMIT
                                : ConsentProvision.DENY)
                        .build()));

    all.stream()
        .filter(
            rs -> {
              var reidentification = rs.getString("reidentification");
              return null != reidentification && !reidentification.isBlank();
            })
        .findFirst()
        .ifPresent(
            rs ->
                result.add(
                    Provision.builder()
                        .date(rs.getDate("date"))
                        .purpose(ModelProjectConsentPurpose.REIDENTIFICATION)
                        .type(
                            ConsentProvision.PERMIT
                                    .toValue()
                                    .equals(rs.getString("reidentification"))
                                ? ConsentProvision.PERMIT
                                : ConsentProvision.DENY)
                        .build()));

    return result;
  }
}
