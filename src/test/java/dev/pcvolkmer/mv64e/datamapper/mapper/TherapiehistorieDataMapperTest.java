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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import dev.pcvolkmer.mv64e.datamapper.datacatalogues.EinzelempfehlungCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.FollowUpCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapielinieCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.TherapieplanCatalogue;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.PropcatColumn;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import dev.pcvolkmer.mv64e.datamapper.test.fuzz.FuzzNullExtension;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, FuzzNullExtension.class})
class TherapiehistorieDataMapperTest {

  KpaTherapielinieDataMapper therapielinieMapper;
  TherapieplanCatalogue therapieplanCatalogue;
  EinzelempfehlungCatalogue einzelempfehlungCatalogue;
  FollowUpCatalogue followUpCatalogue;
  TherapielinieCatalogue therapielinieCatalogue;
  TherapiehistorieDataMapper dataMapper;

  @BeforeEach
  void setUp(
      @Mock KpaTherapielinieDataMapper therapielinieMapper,
      @Mock TherapieplanCatalogue therapieplanCatalogue,
      @Mock EinzelempfehlungCatalogue einzelempfehlungCatalogue,
      @Mock FollowUpCatalogue followUpCatalogue,
      @Mock TherapielinieCatalogue therapielinieCatalogue) {

    this.therapielinieMapper = therapielinieMapper;
    this.therapieplanCatalogue = therapieplanCatalogue;
    this.einzelempfehlungCatalogue = einzelempfehlungCatalogue;
    this.followUpCatalogue = followUpCatalogue;
    this.therapielinieCatalogue = therapielinieCatalogue;

    this.dataMapper =
        new TherapiehistorieDataMapper(
            therapielinieMapper,
            therapieplanCatalogue,
            einzelempfehlungCatalogue,
            followUpCatalogue,
            therapielinieCatalogue);
  }

  @Test
  void testShouldMapFollowUpFormToSystemicTherapy() {

    when(therapieplanCatalogue.getByKpaId(anyInt())).thenReturn(List.of(1001));

    when(einzelempfehlungCatalogue.getAllByParentId(eq(1001)))
        .thenReturn(
            List.of(
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(2001),
                    PropcatColumn.name("empfehlungskategorie").value("systemisch")),
                TestResultSet.withColumns(
                    Column.name(Column.ID).value(2001),
                    PropcatColumn.name("empfehlungskategorie").value("op"))));

    when(followUpCatalogue.getByRecommendationId(eq(2001))).thenReturn(List.of(3001, 3002));

    when(therapielinieCatalogue.getAllByParentId(eq(3001)))
        .thenReturn(
            List.of(
                TestResultSet.withColumns(Column.name(Column.ID).value(4001)),
                TestResultSet.withColumns(Column.name(Column.ID).value(4002))));

    when(therapielinieCatalogue.getAllByParentId(eq(3002)))
        .thenReturn(List.of(TestResultSet.withColumns(Column.name(Column.ID).value(4003))));

    this.dataMapper.getById(1);

    verify(therapielinieMapper, times(3)).getById(anyInt());
  }
}
