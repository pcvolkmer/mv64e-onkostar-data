package dev.pcvolkmer.onco.datamapper.mapper;

import dev.pcvolkmer.mv64e.mtb.FamilyMemberHistory;
import dev.pcvolkmer.mv64e.mtb.FamilyMemberHistoryRelationshipTypeCoding;
import dev.pcvolkmer.mv64e.mtb.FamilyMemberHistoryRelationshipTypeCodingCode;
import dev.pcvolkmer.mv64e.mtb.Reference;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.VerwandteCatalogue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KpaVerwandteDataMapperTest {

    VerwandteCatalogue catalogue;

    KpaVerwandteDataMapper dataMapper;

    @BeforeEach
    void setUp(@Mock VerwandteCatalogue catalogue) {
        this.catalogue = catalogue;
        this.dataMapper = new KpaVerwandteDataMapper(catalogue);
    }

    @Test
    void shouldMapResultSet(@Mock ResultSet resultSet) {
        var testData = Map.of(
                "id", "1",
                "patient_id", "42",
                "verwandtschaftsgrad", "EXT"
        );

        doAnswer(invocationOnMock -> {
            var columnName = invocationOnMock.getArgument(0, String.class);
            return testData.get(columnName);
        }).when(resultSet).getString(anyString());

        when(resultSet.getId()).thenReturn(1);

        doAnswer(invocationOnMock -> List.of(resultSet))
                .when(catalogue)
                .getAllByParentId(anyInt());

        var actualList = this.dataMapper.getByParentId(1);
        assertThat(actualList).hasSize(1);

        var actual = actualList.get(0);
        assertThat(actual).isInstanceOf(FamilyMemberHistory.class);
        assertThat(actual.getId()).isEqualTo("1");
        assertThat(actual.getPatient())
                .isEqualTo(Reference.builder()
                        .id("42")
                        .type("Patient")
                        .build()
                );
        assertThat(actual.getRelationship())
                .isEqualTo(
                        FamilyMemberHistoryRelationshipTypeCoding.builder()
                                .code(FamilyMemberHistoryRelationshipTypeCodingCode.EXT)
                                .display("Verwandter weiteren Grades")
                                .system("dnpm-dip/mtb/family-meber-history/relationship-type")
                                .build()
                );
    }

}
