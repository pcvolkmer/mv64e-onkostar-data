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
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.AbstractSubformDataCatalogue;
import dev.pcvolkmer.mv64e.model.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Abstract Mapper class for similar 'dk_dnpm_therapielinie' and 'dk_dnpm_uf_procedure'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public abstract class AbstractKpaTherapieverlaufDataMapper<T> extends AbstractSubformDataMapper<T> {

  private final PropertyCatalogue propertyCatalogue;

  AbstractKpaTherapieverlaufDataMapper(
      final AbstractSubformDataCatalogue catalogue, final PropertyCatalogue propertyCatalogue) {
    super(catalogue);
    this.propertyCatalogue = propertyCatalogue;
  }

  @NullMarked
  protected abstract Optional<PeriodDate> getPeriodDate(ResultSet resultSet);

  @Nullable
  protected MtbTherapyIntentCoding getMtbTherapyIntentCoding(String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(MtbTherapyIntentCoding.CodeEnum.values())
            .map(MtbTherapyIntentCoding.CodeEnum::toString)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        MtbTherapyIntentCoding.builder()
            .system("dnpm-dip/therapy/intent")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());

    switch (value) {
      case "X":
        resultBuilder.code(MtbTherapyIntentCoding.CodeEnum.X);
        break;
      case "K":
        resultBuilder.code(MtbTherapyIntentCoding.CodeEnum.K);
        break;
      case "P":
        resultBuilder.code(MtbTherapyIntentCoding.CodeEnum.P);
        break;
      case "S":
        resultBuilder.code(MtbTherapyIntentCoding.CodeEnum.S);
        break;
      default:
        return null;
    }

    return resultBuilder.build();
  }

  @Nullable
  protected TherapyStatusCoding getTherapyStatusCoding(String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(TherapyStatusCoding.CodeEnum.values())
            .map(TherapyStatusCoding.CodeEnum::toString)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        TherapyStatusCoding.builder()
            .system("dnpm-dip/therapy/status")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());

    switch (value) {
      case "not-done":
        resultBuilder.code(TherapyStatusCoding.CodeEnum.NOT_DONE);
        break;
      case "on-going":
        resultBuilder.code(TherapyStatusCoding.CodeEnum.ON_GOING);
        break;
      case "stopped":
        resultBuilder.code(TherapyStatusCoding.CodeEnum.STOPPED);
        break;
      case "completed":
        resultBuilder.code(TherapyStatusCoding.CodeEnum.COMPLETED);
        break;
      default:
        return null;
    }

    return resultBuilder.build();
  }

  @Nullable
  protected MtbTherapyStatusReasonCoding getMtbTherapyStatusReasonCoding(
      String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(MtbTherapyStatusReasonCoding.CodeEnum.values())
            .map(MtbTherapyStatusReasonCoding.CodeEnum::toString)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        MtbTherapyStatusReasonCoding.builder()
            .system("dnpm-dip/therapy/status-reason")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());

    try {
      resultBuilder.code(MtbTherapyStatusReasonCoding.CodeEnum.fromValue(value));
    } catch (IllegalArgumentException e) {
      return null;
    }

    return resultBuilder.build();
  }

  @Nullable
  protected MtbSystemicTherapyRecommendationFulfillmentStatusCoding
      getMtbSystemicTherapyRecommendationFulfillmentStatusCoding(String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(MtbSystemicTherapyRecommendationFulfillmentStatusCoding.CodeEnum.values())
            .map(MtbSystemicTherapyRecommendationFulfillmentStatusCoding.CodeEnum::toString)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        MtbSystemicTherapyRecommendationFulfillmentStatusCoding.builder()
            .system("dnpm-dip/therapy/recommendation-fulfillment-status")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());
    try {
      resultBuilder.code(
          MtbSystemicTherapyRecommendationFulfillmentStatusCoding.CodeEnum.fromValue(value));
    } catch (IllegalArgumentException e) {
      return null;
    }

    return resultBuilder.build();
  }

  @Nullable
  protected MtbSystemicTherapyCategoryCoding getMtbSystemicTherapyCategoryCoding(
      String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(MtbSystemicTherapyCategoryCoding.CodeEnum.values())
            .map(MtbSystemicTherapyCategoryCoding.CodeEnum::toString)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        MtbSystemicTherapyCategoryCoding.builder()
            .system("dnpm-dip/therapy/category")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());
    try {
      resultBuilder.code(MtbSystemicTherapyCategoryCoding.CodeEnum.fromValue(value));
    } catch (IllegalArgumentException e) {
      return null;
    }

    return resultBuilder.build();
  }

  @Nullable
  protected MtbSystemicTherapyDosageDensityCoding getMtbSystemicTherapyDosageDensityCoding(
      String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(MtbSystemicTherapyDosageDensityCoding.CodeEnum.values())
            .map(MtbSystemicTherapyDosageDensityCoding.CodeEnum::toString)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        MtbSystemicTherapyDosageDensityCoding.builder()
            .system("dnpm-dip/therapy/status-density")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());
    try {
      resultBuilder.code(MtbSystemicTherapyDosageDensityCoding.CodeEnum.fromValue(value));
    } catch (IllegalArgumentException e) {
      return null;
    }

    return resultBuilder.build();
  }

  @Nullable
  protected OncoProcedureTypeCoding getOncoProcedureCoding(String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(OncoProcedureTypeCoding.CodeEnum.values())
            .map(OncoProcedureTypeCoding.CodeEnum::toString)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        OncoProcedureTypeCoding.builder()
            .system("dnpm-dip/therapy/type")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());

    try {
      resultBuilder.code(OncoProcedureTypeCoding.CodeEnum.fromValue(value));
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("No valid code found");
    }

    return resultBuilder.build();
  }
}
