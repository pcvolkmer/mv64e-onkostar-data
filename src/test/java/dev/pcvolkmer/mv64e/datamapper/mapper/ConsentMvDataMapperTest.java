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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.ConsentMvCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.ConsentMvVerlaufCatalogue;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.PropcatColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullExtension;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullTest;
import dev.pcvolkmer.mv64e.mtb.ConsentProvision;
import dev.pcvolkmer.mv64e.mtb.ModelProjectConsent;
import dev.pcvolkmer.mv64e.mtb.ModelProjectConsentPurpose;
import dev.pcvolkmer.mv64e.mtb.Provision;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
class ConsentMvDataMapperTest {

  ConsentMvCatalogue catalogue;
  ConsentMvVerlaufCatalogue consentMvVerlaufCatalogue;
  ConsentMvDataMapper dataMapper;

  @BeforeEach
  void setUp(
      @Mock ConsentMvCatalogue catalogue,
      @Mock ConsentMvVerlaufCatalogue consentMvVerlaufCatalogue) {
    this.catalogue = catalogue;
    this.consentMvVerlaufCatalogue = consentMvVerlaufCatalogue;
    this.dataMapper = new ConsentMvDataMapper(catalogue, consentMvVerlaufCatalogue);
  }

  @Test
  void shouldCreateDataMapper(@Mock DataSource dataSource) {
    assertThat(MtbDataMapper.create(dataSource)).isNotNull();
  }

  @Test
  void shouldCreateConsent() {
    doAnswer(
            invocationOnMock ->
                List.of(
                    TestResultSet.withColumns(
                        Column.name("id").value(1),
                        DateColumn.name("date").value("2025-07-11"),
                        Column.name("version").value("01"),
                        PropcatColumn.name("sequencing").value("permit"),
                        PropcatColumn.name("caseidentification").value("deny"),
                        PropcatColumn.name("reidentification").value("deny")),
                    TestResultSet.withColumns(
                        Column.name("id").value(1),
                        DateColumn.name("date").value("2025-07-12"),
                        Column.name("version").value("02"),
                        PropcatColumn.name("sequencing").value("permit"),
                        PropcatColumn.name("caseidentification").value("permit"))))
        .when(consentMvVerlaufCatalogue)
        .getAllByParentId(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(ModelProjectConsent.class);
    // Intentionally left blank/null
    assertThat(actual.getDate()).isNull();
    assertThat(actual.getVersion()).isEqualTo("02");
    assertThat(actual.getProvisions()).hasSize(3);
    assertThat(actual.getProvisions())
        .containsAll(
            List.of(
                Provision.builder()
                    .date(Date.from(Instant.parse("2025-07-12T00:00:00Z")))
                    .purpose(ModelProjectConsentPurpose.SEQUENCING)
                    .type(ConsentProvision.PERMIT)
                    .build(),
                Provision.builder()
                    .date(Date.from(Instant.parse("2025-07-12T00:00:00Z")))
                    .purpose(ModelProjectConsentPurpose.CASE_IDENTIFICATION)
                    .type(ConsentProvision.PERMIT)
                    .build(),
                Provision.builder()
                    .date(Date.from(Instant.parse("2025-07-11T00:00:00Z")))
                    .purpose(ModelProjectConsentPurpose.REIDENTIFICATION)
                    .type(ConsentProvision.DENY)
                    .build()));
  }

  @FuzzNullTest(initMethod = "testData")
  void shouldNotThrowNPEInFuzzyTest(ResultSet resultSet) {
    doAnswer(
            invocationOnMock ->
                List.of(
                    resultSet,
                    TestResultSet.withColumns(
                        Column.name("id").value(1),
                        DateColumn.name("date").value("2025-07-12"),
                        Column.name("version").value("02"),
                        PropcatColumn.name("sequencing").value("permit"),
                        PropcatColumn.name("caseidentification").value("permit"))))
        .when(consentMvVerlaufCatalogue)
        .getAllByParentId(anyInt());

    this.dataMapper.getById(1);
  }

  static ResultSet testData() {
    return TestResultSet.withColumns(
        Column.name("id").value(1),
        DateColumn.name("date").value("2025-07-11"),
        Column.name("version").value("01"),
        PropcatColumn.name("sequencing").value("permit"),
        PropcatColumn.name("caseidentification").value("deny"),
        PropcatColumn.name("reidentification").value("deny"));
  }
}
