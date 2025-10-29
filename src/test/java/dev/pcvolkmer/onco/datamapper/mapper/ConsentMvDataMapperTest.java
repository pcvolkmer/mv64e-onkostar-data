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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;

import dev.pcvolkmer.mv64e.mtb.ConsentProvision;
import dev.pcvolkmer.mv64e.mtb.ModelProjectConsent;
import dev.pcvolkmer.mv64e.mtb.ModelProjectConsentPurpose;
import dev.pcvolkmer.mv64e.mtb.Provision;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.ConsentMvCatalogue;
import dev.pcvolkmer.onco.datamapper.datacatalogues.ConsentMvVerlaufCatalogue;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
                    ResultSet.from(
                        Map.of(
                            "id", "1",
                            "date",
                                new java.sql.Date(
                                    Date.from(Instant.parse("2025-07-11T12:00:00Z")).getTime()),
                            "version", "01",
                            "sequencing", "permit",
                            "caseidentification", "deny",
                            "reidentification", "deny")),
                    ResultSet.from(
                        Map.of(
                            "id", "1",
                            "date",
                                new java.sql.Date(
                                    Date.from(Instant.parse("2025-07-12T12:00:00Z")).getTime()),
                            "version", "02",
                            "sequencing", "permit",
                            "caseidentification", "permit"
                            // no new value for reidentification!
                            ))))
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
}
