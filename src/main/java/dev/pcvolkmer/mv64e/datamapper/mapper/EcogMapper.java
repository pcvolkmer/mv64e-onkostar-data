/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026 the original author or authors.
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
 */

package dev.pcvolkmer.mv64e.datamapper.mapper;

import dev.pcvolkmer.mv64e.mtb.EcogCoding;
import dev.pcvolkmer.mv64e.mtb.EcogCodingCode;
import dev.pcvolkmer.mv64e.mtb.PerformanceStatus;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * Common interface for mappers that map ECOG performance status data
 *
 * @author Paul-Christian Volkmer
 * @since 0.9
 */
public interface EcogMapper {

  @Nullable PerformanceStatus getById(int id);

  /**
   * Maps a string value to an ECOG coding
   *
   * @param value the string value to map
   * @return the ECOG coding or null if the value is invalid
   */
  @Nullable
  default EcogCoding getEcogCoding(@Nullable final String value) {
    if (null == value
        || !Arrays.stream(EcogCodingCode.values())
            .map(EcogCodingCode::toValue)
            .collect(Collectors.toSet())
            .contains(value)) {
      return null;
    }

    var resultBuilder = EcogCoding.builder().system("ECOG-Performance-Status");

    try {
      resultBuilder.code(EcogCodingCode.forValue(value));
      resultBuilder.display(String.format("ECOG %s", value));
    } catch (IOException e) {
      throw new IllegalStateException("No valid code found");
    }

    return resultBuilder.build();
  }
}
