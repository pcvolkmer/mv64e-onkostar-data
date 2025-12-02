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
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.AbstractSubformDataCatalogue;
import dev.pcvolkmer.mv64e.mtb.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

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

  protected MtbTherapyIntentCoding getMtbTherapyIntentCoding(String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(MtbTherapyIntentCodingCode.values())
            .map(MtbTherapyIntentCodingCode::toValue)
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
        resultBuilder.code(MtbTherapyIntentCodingCode.X);
        break;
      case "K":
        resultBuilder.code(MtbTherapyIntentCodingCode.K);
        break;
      case "P":
        resultBuilder.code(MtbTherapyIntentCodingCode.P);
        break;
      case "S":
        resultBuilder.code(MtbTherapyIntentCodingCode.S);
        break;
    }

    return resultBuilder.build();
  }

  protected TherapyStatusCoding getTherapyStatusCoding(String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(TherapyStatusCodingCode.values())
            .map(TherapyStatusCodingCode::toValue)
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
        resultBuilder.code(TherapyStatusCodingCode.NOT_DONE);
        break;
      case "on-going":
        resultBuilder.code(TherapyStatusCodingCode.ON_GOING);
        break;
      case "stopped":
        resultBuilder.code(TherapyStatusCodingCode.STOPPED);
        break;
      case "completed":
        resultBuilder.code(TherapyStatusCodingCode.COMPLETED);
        break;
    }

    return resultBuilder.build();
  }

  protected MtbTherapyStatusReasonCoding getMtbTherapyStatusReasonCoding(
      String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(MtbTherapyStatusReasonCodingCode.values())
            .map(MtbTherapyStatusReasonCodingCode::toValue)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        MtbTherapyStatusReasonCoding.builder()
            .system("dnpm-dip/therapy/status-reason")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());

    try {
      resultBuilder.code(MtbTherapyStatusReasonCodingCode.forValue(value));
    } catch (IOException e) {
      return null;
    }

    return resultBuilder.build();
  }

  protected MtbSystemicTherapyRecommendationFulfillmentStatusCoding
      getMtbSystemicTherapyRecommendationFulfillmentStatusCoding(String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(MtbSystemicTherapyRecommendationFulfillmentStatusCodingCode.values())
            .map(MtbSystemicTherapyRecommendationFulfillmentStatusCodingCode::toValue)
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
          MtbSystemicTherapyRecommendationFulfillmentStatusCodingCode.forValue(value));
    } catch (IOException e) {
      return null;
    }

    return resultBuilder.build();
  }

  protected MtbSystemicTherapyCategoryCoding getMtbSystemicTherapyCategoryCoding(
      String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(MtbSystemicTherapyCategoryCodingCode.values())
            .map(MtbSystemicTherapyCategoryCodingCode::toValue)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        MtbSystemicTherapyCategoryCoding.builder()
            .system("dnpm-dip/therapy/category")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());
    try {
      resultBuilder.code(MtbSystemicTherapyCategoryCodingCode.forValue(value));
    } catch (IOException e) {
      return null;
    }

    return resultBuilder.build();
  }

  protected MtbSystemicTherapyDosageDensityCoding getMtbSystemicTherapyDosageDensityCoding(
      String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(MtbSystemicTherapyDosageDensityCodingCode.values())
            .map(MtbSystemicTherapyDosageDensityCodingCode::toValue)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        MtbSystemicTherapyDosageDensityCoding.builder()
            .system("dnpm-dip/therapy/status-density")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());
    try {
      resultBuilder.code(MtbSystemicTherapyDosageDensityCodingCode.forValue(value));
    } catch (IOException e) {
      return null;
    }

    return resultBuilder.build();
  }

  protected OncoProcedureCoding getOncoProcedureCoding(String value, Integer version) {
    if (value == null
        || version == null
        || !Arrays.stream(OncoProcedureCodingCode.values())
            .map(OncoProcedureCodingCode::toValue)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder =
        OncoProcedureCoding.builder()
            .system("dnpm-dip/therapy/type")
            .display(propertyCatalogue.getByCodeAndVersion(value, version).getShortdesc());

    try {
      resultBuilder.code(OncoProcedureCodingCode.forValue(value));
    } catch (IOException e) {
      throw new IllegalStateException("No valid code found");
    }

    return resultBuilder.build();
  }
}
