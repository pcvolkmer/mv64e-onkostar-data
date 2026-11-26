/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026  Paul-Christian Volkmer and David Mal (UKW)
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

import java.util.Locale;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Normalizes Onkostar property catalogue version descriptions to DNPM-compatible Coding versions.
 *
 * @author David Mal (UKW) assisted by GitHub Copilot
 */
@NullMarked
final class CatalogVersionNormalizer {

  private static final Pattern YEAR_PATTERN =
      Pattern.compile("(?:^|\\D)((?:19|20)\\d{2})(?:\\D|$)");
  private static final Pattern FIRST_REV_PATTERN =
      Pattern.compile(
          "erste\\s+revision|\\bfirst\\s+revision\\b|\\b1\\.\\s*rev(?:ision)?\\b|\\brev\\.?\\s*1\\b|\\brevision\\s+i\\b");
  private static final Pattern SECOND_REV_PATTERN =
      Pattern.compile(
          "zweite\\s+revision|\\bsecond\\s+revision\\b|\\b2\\.\\s*rev(?:ision)?\\b|\\brev\\.?\\s*2\\b|\\brevision\\s+ii\\b");

  private CatalogVersionNormalizer() {
    // intentionally left empty
  }

  /**
   * Normalizes Onkostar ICD-10-GM version descriptions (e.g. {@code "Version 2025"} or {@code
   * "ICD-10-GM 2025"}) to the 4-digit catalog year expected by DNPM ({@code "2025"}). Returns the
   * trimmed input unchanged if zero or multiple distinct years are present.
   *
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @Nullable
  static String normalizeIcd10GmVersion(@Nullable final String versionDescription) {
    if (null == versionDescription || versionDescription.isBlank()) {
      return null;
    }

    final var trimmed = versionDescription.trim();
    final var year = extractSingleYear(trimmed);
    return null != year ? year : trimmed;
  }

  /**
   * Normalizes Onkostar ICD-O-3 version descriptions to the ClaML catalog version keys expected by
   * DNPM ({@code "Erste Revision"} / {@code "Zweite Revision"}).
   *
   * <p>In Onkostar, German ICD-O-3 1. Revision (2014) is commonly stored as {@code "Version 2013"}
   * and 2. Revision as {@code "Version 2019"}. Ambiguous or contradictory strings are returned
   * trimmed without guessing.
   *
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @Nullable
  static String normalizeIcdO3Version(@Nullable final String versionDescription) {
    if (null == versionDescription || versionDescription.isBlank()) {
      return null;
    }

    final var trimmed = versionDescription.trim();
    final var lower = trimmed.toLowerCase(Locale.ROOT);
    final var year = extractSingleYear(trimmed);

    final boolean matchesFirstRev =
        FIRST_REV_PATTERN.matcher(lower).find() || "2013".equals(year) || "2014".equals(year);
    final boolean matchesSecondRev =
        SECOND_REV_PATTERN.matcher(lower).find() || "2019".equals(year);

    if (matchesFirstRev && matchesSecondRev) {
      return trimmed;
    }
    if (matchesFirstRev) {
      return "Erste Revision";
    }
    if (matchesSecondRev) {
      return "Zweite Revision";
    }

    return null != year ? year : trimmed;
  }

  /**
   * Extracts a single distinct 4-digit year from {@code value}, or {@code null} if none or multiple
   * distinct years are present.
   *
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @Nullable
  private static String extractSingleYear(final String value) {
    final var matcher = YEAR_PATTERN.matcher(value);
    String found = null;
    int pos = 0;
    while (matcher.find(pos)) {
      final var candidate = matcher.group(1);
      if (null == found) {
        found = candidate;
      } else if (!found.equals(candidate)) {
        return null;
      }
      pos = matcher.end(1);
    }
    return found;
  }
}
