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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests catalog version normalization.
 *
 * @author David Mal (UKW) assisted by GitHub Copilot
 */
class CatalogVersionNormalizerTest {

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @ParameterizedTest
  @CsvSource({
    "2025, 2025",
    "Version 2009, 2009",
    "Version 2010, 2010",
    "Version 2011, 2011",
    "Version 2012, 2012",
    "Version 2013, 2013",
    "Version 2014, 2014",
    "Version 2015, 2015",
    "Version 2016, 2016",
    "Version 2017, 2017",
    "Version 2018, 2018",
    "Version 2019, 2019",
    "Version 2020, 2020",
    "Version 2021, 2021",
    "Version 2022, 2022",
    "Version 2023, 2023",
    "Version 2024, 2024",
    "Version 2025, 2025",
    "Version 2026, 2026",
    "ICD-10-GM 2025, 2025",
    "ICD-10-GM Version 2024, 2024",
    "2025 (BfArM), 2025",
    "'ICD-10-GM (ersetzt 2024), Version 2025', 'ICD-10-GM (ersetzt 2024), Version 2025'",
    "custom, custom"
  })
  void shouldNormalizeIcd10GmVersion(final String input, final String expected) {
    assertThat(CatalogVersionNormalizer.normalizeIcd10GmVersion(input)).isEqualTo(expected);
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void shouldReturnNullForBlankIcd10GmVersion(final String input) {
    assertThat(CatalogVersionNormalizer.normalizeIcd10GmVersion(input)).isNull();
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @ParameterizedTest
  @CsvSource({
    "Erste Revision, Erste Revision",
    "ICD-O-3 Erste Revision, Erste Revision",
    "First Revision, Erste Revision",
    "Revision I, Erste Revision",
    "1. Revision, Erste Revision",
    "Rev. 1, Erste Revision",
    "Version 2013, Erste Revision",
    "2013, Erste Revision",
    "Version 2014, Erste Revision",
    "2014, Erste Revision",
    "Zweite Revision, Zweite Revision",
    "ICD-O-3 Zweite Revision, Zweite Revision",
    "Second Revision, Zweite Revision",
    "Revision II, Zweite Revision",
    "2. Revision, Zweite Revision",
    "Rev. 2, Zweite Revision",
    "Version 2019, Zweite Revision",
    "2019, Zweite Revision",
    "Zweite Revision (ersetzt Version 2014), Zweite Revision (ersetzt Version 2014)",
    "Zweite Revision (ersetzt Erste Revision), Zweite Revision (ersetzt Erste Revision)",
    "Version 2003, 2003",
    "Blue Book, Blue Book"
  })
  void shouldNormalizeIcdO3Version(final String input, final String expected) {
    assertThat(CatalogVersionNormalizer.normalizeIcdO3Version(input)).isEqualTo(expected);
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @Test
  void shouldReturnNullForNullIcdO3Version() {
    assertThat(CatalogVersionNormalizer.normalizeIcdO3Version(null)).isNull();
    assertThat(CatalogVersionNormalizer.normalizeIcdO3Version("  ")).isNull();
  }
}
