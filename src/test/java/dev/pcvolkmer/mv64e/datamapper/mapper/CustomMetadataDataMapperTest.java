package dev.pcvolkmer.mv64e.datamapper.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import dev.pcvolkmer.mv64e.datamapper.datacatalogues.KpaCatalogue;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.PatientCatalogue;
import dev.pcvolkmer.mv64e.datamapper.test.Column;
import dev.pcvolkmer.mv64e.datamapper.test.TestResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomMetadataDataMapperTest {

  CustomMetadataDataMapper dataMapper;

  KpaCatalogue kpaCatalogue;
  PatientCatalogue patientCatalogue;

  @BeforeEach
  void setUp(@Mock KpaCatalogue kpaCatalogue, @Mock PatientCatalogue patientCatalogue) {
    this.kpaCatalogue = kpaCatalogue;
    this.patientCatalogue = patientCatalogue;

    this.dataMapper = new CustomMetadataDataMapper(kpaCatalogue, patientCatalogue);
  }

  @Test
  void shouldReturnCustomMetadataWhenKpaAndPatientDataFound() {
    when(kpaCatalogue.getById(anyInt()))
        .thenReturn(
            TestResultSet.withColumns(
                Column.name(Column.ID).value(1),
                Column.name(Column.PATIENT_ID).value(42),
                Column.name("fallnummermv").value("1600012345")));

    when(patientCatalogue.getById(eq(42)))
        .thenReturn(
            TestResultSet.withColumns(
                Column.name(Column.ID).value(42),
                Column.name("krankenkassennummer").value("105313145")));

    var actual = this.dataMapper.getById(1);
    assertThat(actual).isNotNull();
    assertThat(actual.getFallnummer()).isEqualTo("1600012345");
    assertThat(actual.getIk()).isEqualTo("105313145");
  }
}
