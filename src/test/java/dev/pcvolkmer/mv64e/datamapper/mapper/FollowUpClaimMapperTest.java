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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.FollowUpCatalogue;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.DateColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullExtension;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullTest;
import dev.pcvolkmer.mv64e.mtb.Claim;
import dev.pcvolkmer.mv64e.mtb.ClaimStageCoding;
import dev.pcvolkmer.mv64e.mtb.ClaimStageCodingCode;
import dev.pcvolkmer.mv64e.mtb.Reference;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
class FollowUpClaimMapperTest {

  FollowUpCatalogue catalogue;
  FollowUpClaimMapper dataMapper;

  @BeforeEach
  void setUp(@Mock FollowUpCatalogue catalogue) {
    this.catalogue = catalogue;
    this.dataMapper = new FollowUpClaimMapper(catalogue);
  }

  @Test
  void shouldCreateDataMapper(@Mock DataSource dataSource) {
    assertThat(MtbDataMapper.create(dataSource)).isNotNull();
  }

  @Test
  void shouldCreateFollowUp() {
    doAnswer(
            invocationOnMock ->
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(11),
                    Column.name(Column.PATIENT_ID).value(42),
                    Column.name(Column.PATIENTEN_ID).value(2000000042),
                    Column.name("linktherapieempfehlung").value(1),
                    DateColumn.name("ausstellungsdatumantrag").value("2026-04-01"),
                    Column.name("antragsstadium").value("initial-claim")))
        .when(catalogue)
        .getById(anyInt());

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isInstanceOf(Claim.class);
    assertThat(actual.getPatient())
        .isEqualTo(Reference.builder().id("2000000042").type("Patient").build());
    assertThat(actual.getRecommendation()).isEqualTo(Reference.builder().id("1").build());
    assertThat(actual.getStage())
        .isEqualTo(
            ClaimStageCoding.builder()
                .code(ClaimStageCodingCode.INITIAL_CLAIM)
                .display("initial-claim")
                .system("dnpm-dip/mtb/claim/stage")
                .build());
  }

  @FuzzNullTest(
      initMethod = "fuzzInitData",
      excludeColumns = {Column.PATIENTEN_ID})
  void fuzzTestNullColumns(final ResultSet resultSet) {
    when(catalogue.getById(anyInt())).thenReturn(resultSet);
    var actual = this.dataMapper.getById(1);
    assertThat(actual).isNotNull();
  }

  static ResultSet fuzzInitData() {
    return TestResultSet.withColumns(
        Column.name(Column.ID).value(11),
        Column.name(Column.PATIENT_ID).value(42),
        Column.name(Column.PATIENTEN_ID).value(2000000042),
        Column.name("linktherapieempfehlung").value(1),
        DateColumn.name("datumfollowup").value("2026-04-01"),
        DateColumn.name("datumletzterkontakt").value("2026-01-01"),
        Column.name("losttofollowup").value(1));
  }
}
