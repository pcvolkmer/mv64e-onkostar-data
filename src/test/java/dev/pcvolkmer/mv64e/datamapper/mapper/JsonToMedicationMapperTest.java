/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2025  Paul-Christian Volkmer and David Mal (UKW)
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

import dev.pcvolkmer.mv64e.model.AtcUnregisteredMedicationCoding;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JsonToMedicationMapperTest {

  // See example in:
  // https://github.com/pcvolkmer/onkostar-plugin-dnpm/blob/master/sql/sql-queries.md
  @Test
  void shouldMapTherapielinieJson() {
    var json =
        "[\n"
            + "    {\"system\":\"other\",\"code\":\"Gemcitabin\",\"substance\":\"Gemcitabin (dFdC)\"},\n"
            + "    {\"system\":\"other\",\"code\":\"Cisplatin\",\"substance\":\"Cisplatin (CDDP)\"}\n"
            + "]";

    var actual = new ArrayList<>(JsonToMedicationMapper.map(json));

    assertThat(actual)
        .hasSize(2)
        .containsAll(
            List.of(
                AtcUnregisteredMedicationCoding.builder()
                    .code("L01XA01")
                    .display("Cisplatin (CDDP)")
                    .version("2026")
                    .system(
                        AtcUnregisteredMedicationCoding.SystemEnum
                            .HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC)
                    .build(),
                AtcUnregisteredMedicationCoding.builder()
                    .code("L01BC05")
                    .display("Gemcitabin (dFdC)")
                    .version("2026")
                    .system(
                        AtcUnregisteredMedicationCoding.SystemEnum
                            .HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC)
                    .build()));
  }

  // See example in:
  // https://github.com/pcvolkmer/onkostar-plugin-dnpm/blob/master/sql/sql-queries.md
  @Test
  void shouldMapEinzelempfehlungJson() {
    var json =
        "[\n"
            + "    {\"code\":\"\",\"name\":\"PARP-Inhibierung\",\"system\":\"UNREGISTERED\"}\n"
            + "]";

    var actual = new ArrayList<>(JsonToMedicationMapper.map(json));

    assertThat(actual).hasSize(1);

    var elem0 = actual.get(0);
    assertThat(elem0.getCode()).isEmpty();
    assertThat(elem0.getDisplay()).isEqualTo("PARP-Inhibierung");
    assertThat(elem0.getSystem()).isEqualTo(AtcUnregisteredMedicationCoding.SystemEnum.UNDEFINED);
    assertThat(elem0.getVersion()).isNull();
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @Test
  void shouldResolveVersionForValidAtcCodeWithoutVersion() {
    var json =
        " [{\"code\":\"L01FX26\",\"name\":\"Mirvetuximab Soravtansine\",\"synonyms\":\"\",\"version\":null,\"system\":\"ATC\"}]";

    var actual = new ArrayList<>(JsonToMedicationMapper.map(json));

    assertThat(actual).hasSize(1);

    var elem0 = actual.get(0);
    assertThat(elem0.getCode()).isEqualTo("L01FX26");
    assertThat(elem0.getDisplay()).isEqualTo("Mirvetuximab Soravtansine");
    assertThat(elem0.getSystem())
        .isEqualTo(AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC);
    assertThat(elem0.getVersion()).isEqualTo("2026");
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @Test
  void shouldPreserveExplicitAtcVersion() {
    var json =
        " [{\"code\":\"L01FX26\",\"name\":\"Mirvetuximab Soravtansine\",\"synonyms\":\"\",\"version\":\"2024\",\"system\":\"ATC\"}]";

    var actual = new ArrayList<>(JsonToMedicationMapper.map(json));

    assertThat(actual).hasSize(1);

    var elem0 = actual.get(0);
    assertThat(elem0.getCode()).isEqualTo("L01FX26");
    assertThat(elem0.getDisplay()).isEqualTo("Mirvetuximab Soravtansine");
    assertThat(elem0.getSystem())
        .isEqualTo(AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC);
    assertThat(elem0.getVersion()).isEqualTo("2024");
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @ParameterizedTest
  @MethodSource("resolvableOnkostarSubstances")
  void shouldResolveOnkostarSubstancesToAtc(
      String rawCode, String rawSubstance, String expectedAtcCode) {
    var json =
        String.format(
            "[{\"system\":\"other\",\"code\":\"%s\",\"substance\":\"%s\"}]", rawCode, rawSubstance);

    var actual = new ArrayList<>(JsonToMedicationMapper.map(json));

    assertThat(actual).hasSize(1);
    var elem0 = actual.get(0);
    assertThat(elem0.getCode()).isEqualTo(expectedAtcCode);
    assertThat(elem0.getSystem())
        .isEqualTo(AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC);
    assertThat(elem0.getVersion()).isEqualTo("2026");
    assertThat(elem0.getDisplay()).isEqualTo(rawSubstance.trim());
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  private static Stream<Arguments> resolvableOnkostarSubstances() {
    return Stream.of(
        Arguments.of("Alectinib", "Alectinib", "L01ED03"),
        Arguments.of("Amsacrin (", "Amsacrin (AMSA, m-AMSA)", "L01XX01"),
        Arguments.of("Asparagina", "Asparaginase (L-ASP)", "L01XX02"),
        Arguments.of("Axicabtage", "Axicabtagen ciloleucel (CAR-T-Zellen)", "L01XL03"),
        Arguments.of("Bevacizuma", "Bevacizumab", "L01FG01"),
        Arguments.of("Brexucabtagen", "Brexucabtagen autoleucel (CAR-T-Zellen)", "L01XL06"),
        Arguments.of("Carboplati", "Carboplatin (CBCDA)", "L01XA02"),
        Arguments.of(
            "Ciltacabtagen autoleucel", "Ciltacabtagen autoleucel (CAR-T-Zellen)", "L01XL05"),
        Arguments.of("Cisplatin ", "Cisplatin (CDDP)", "L01XA01"),
        Arguments.of("Cladribin ", "Cladribin (2-CDA)", "L01BB04"),
        Arguments.of("Cytarabin ", "Cytarabin (AraC)", "L01BC01"),
        Arguments.of("Dacarbazin", "Dacarbazin (DTIC)", "L01AX04"),
        Arguments.of("Docetaxel", "Docetaxel", "L01CD02"),
        Arguments.of("Dox lip np", "Doxorubicin liposomal nicht-pegyliert", "L01DB01"),
        Arguments.of("Dox lip p", "Doxorubicin liposomal pegyliert", "L01DB01"),
        Arguments.of("DoxAdmLip", "Doxorubicin (ADM) liposomal pegyliert", "L01DB01"),
        Arguments.of("Doxorubici", "Doxorubicin (o.n.A.)", "L01DB01"),
        Arguments.of("Durvalumab", "Durvalumab", "L01FF03"),
        Arguments.of("Entrectinib", "Entrectinib", "L01EX14"),
        Arguments.of("Epirubicin", "Epirubicin (EPI)", "L01DB03"),
        Arguments.of("Etoposid", "Etoposid (o.n.A.)", "L01CB01"),
        Arguments.of("Gemcitabin", "Gemcitabin (dFdC)", "L01BC05"),
        Arguments.of("Gemtuzumab", "Gemtuzumab-Ozogamicin (Konjugat)", "L01FX02"),
        Arguments.of("Idarubicin", "Idarubicin (IDA)", "L01DB06"),
        Arguments.of("Idecabtagen vicleucel", "Idecabtagen vicleucel (CAR-T-Zellen)", "L01XL07"),
        Arguments.of("Ipilimumab", "Ipilimumab", "L01FX04"),
        Arguments.of("Irinotecan", "Irinotecan (CPT-11)", "L01CE02"),
        Arguments.of("Irinotecan-pegyliert", "nal-Irinotecan", "L01CE02"),
        Arguments.of(
            "Lisocabtagen-Maraleucel", "Lisocabtagen-Maraleucel (CAR-T-Zellen)", "L01XL08"),
        Arguments.of("Medroxypro", "Medroxyprogesteron (Medroxyprogesteronacetat, MPA)", "L02AB02"),
        Arguments.of("Melphalan ", "Melphalan (MPL)", "L01AA03"),
        Arguments.of("Mercaptopu", "Mercaptopurin (6-MP, Purinethol)", "L01BB02"),
        Arguments.of("Mitomycin ", "Mitomycin (MMC)", "L01DC03"),
        Arguments.of("Nivolumab", "Nivolumab", "L01FF01"),
        Arguments.of("PaclitaONA", "Paclitaxel (o.n.A.)", "L01CD01"),
        Arguments.of("nab-Paclit", "nab-Paclitaxel", "L01CD01"),
        Arguments.of("lip-Paclit", "liposomal verkapseltes Paclitaxel", "L01CD01"),
        Arguments.of("Paclitaxel", "Paclitaxel poliglumex", "L01CD03"),
        Arguments.of("Pembrolizu", "Pembrolizumab", "L01FF02"),
        Arguments.of("Pemetrexed", "Pemetrexed", "L01BA04"),
        Arguments.of("Pentostati", "Pentostatin (DCF)", "L01XX08"),
        Arguments.of("Ramuciruma", "Ramucirumab", "L01FG02"),
        Arguments.of("Tioguanin ", "Tioguanin (6-TG)", "L01BB03"),
        Arguments.of("Tisagenlec", "Tisagen lecleucel (CAR-T-Zellen)", "L01XL04"),
        Arguments.of("TrastDerux", "Trastuzumab-Deruxtecan (T-DXd)", "L01FD04"),
        Arguments.of("Vinorelbin", "Vinorelbin", "L01CA04"));
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @Test
  void shouldResolveSubstanceByTrimmedCodeWhenDisplayNameIsAbsent() {
    var json = "[{\"system\":\"other\",\"code\":\"Cisplatin \"}]";

    var actual = new ArrayList<>(JsonToMedicationMapper.map(json));

    assertThat(actual).hasSize(1);
    var elem0 = actual.get(0);
    assertThat(elem0.getCode()).isEqualTo("L01XA01");
    assertThat(elem0.getDisplay()).isEqualTo("Cisplatin");
    assertThat(elem0.getSystem())
        .isEqualTo(AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC);
    assertThat(elem0.getVersion()).isEqualTo("2026");
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @Test
  void shouldPreserveFormulationTextAsDisplayWhenResolvedFromCodeWithoutName() {
    var json =
        "[{\"system\":\"other\",\"code\":\"nab-Paclitaxel\"},{\"system\":\"other\",\"code\":\"Paclitaxel\"}]";

    var actual = new ArrayList<>(JsonToMedicationMapper.map(json));

    assertThat(actual)
        .hasSize(2)
        .containsExactlyInAnyOrder(
            AtcUnregisteredMedicationCoding.builder()
                .code("L01CD01")
                .display("nab-Paclitaxel")
                .version("2026")
                .system(
                    AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC)
                .build(),
            AtcUnregisteredMedicationCoding.builder()
                .code("L01CD01")
                .display("Paclitaxel")
                .version("2026")
                .system(
                    AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC)
                .build());
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @Test
  void shouldResolveLowercaseAtcCodeAndTreatUnicodeWhitespaceVersionAsBlank() {
    var json =
        "[{\"code\":\"l01fx26\",\"name\":\"Mirvetuximab Soravtansine\",\"version\":\"\\u2003\",\"system\":\"ATC\"}]";

    var actual = new ArrayList<>(JsonToMedicationMapper.map(json));

    assertThat(actual).hasSize(1);
    var elem0 = actual.get(0);
    assertThat(elem0.getCode()).isEqualTo("L01FX26");
    assertThat(elem0.getVersion()).isEqualTo("2026");
    assertThat(elem0.getSystem())
        .isEqualTo(AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC);
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @Test
  void
      shouldNotOverwriteExplicitUnknownAtcCodeViaSubstanceNameAndUseCatalogVersionForOtherSystem() {
    var json =
        "["
            + "{\"code\":\"L01ZZ99\",\"name\":\"Bevacizumab\",\"version\":null,\"system\":\"ATC\"},"
            + "{\"code\":\"L01FX26\",\"name\":\"Mirvetuximab Soravtansine\",\"version\":\"1\",\"system\":\"other\"}"
            + "]";

    var actual = new ArrayList<>(JsonToMedicationMapper.map(json));

    assertThat(actual)
        .hasSize(2)
        .containsExactlyInAnyOrder(
            AtcUnregisteredMedicationCoding.builder()
                .code("L01ZZ99")
                .display("Bevacizumab")
                .system(AtcUnregisteredMedicationCoding.SystemEnum.UNDEFINED)
                .build(),
            AtcUnregisteredMedicationCoding.builder()
                .code("L01FX26")
                .display("Mirvetuximab Soravtansine")
                .version("2026")
                .system(
                    AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC)
                .build());
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @ParameterizedTest
  @MethodSource("unresolvableMedications")
  void shouldKeepUndefinedAndTrimCodeForAmbiguousOrUnknownMedications(
      String rawCode, String rawName, String expectedCode) {
    var json =
        String.format("[{\"system\":\"other\",\"code\":\"%s\",\"name\":\"%s\"}]", rawCode, rawName);

    var actual = new ArrayList<>(JsonToMedicationMapper.map(json));

    assertThat(actual).hasSize(1);
    var elem0 = actual.get(0);
    assertThat(elem0.getCode()).isEqualTo(expectedCode);
    assertThat(elem0.getSystem()).isEqualTo(AtcUnregisteredMedicationCoding.SystemEnum.UNDEFINED);
    assertThat(elem0.getVersion()).isNull();
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  private static Stream<Arguments> unresolvableMedications() {
    return Stream.of(
        Arguments.of("Mistelkrau", "Mistelkraut", "Mistelkrau"),
        Arguments.of("Kombi", "Kombinationen", "Kombi"),
        Arguments.of("Dexamethas", "Dexamethason", "Dexamethas"),
        Arguments.of("IfnPeg", "Interferon alfa-2a (pegyliert)", "IfnPeg"),
        Arguments.of("BevaIvt", "Bevacizumab (intravitreale Anwendung)", "BevaIvt"),
        Arguments.of("TrastTDXd", "Trastuzumab (T-DXd)", "TrastTDXd"),
        Arguments.of("TrastKonj", "Trastuzumab (Konjugat)", "TrastKonj"),
        Arguments.of("L01XX99", "Unbekannter ATC-Code", "L01XX99"),
        Arguments.of(" CustomCode ", "Unbekannte Substanz", "CustomCode"));
  }

  /**
   * @author David Mal (UKW) assisted by GitHub Copilot
   */
  @Test
  void shouldPreserveSourceVersionForUnresolvableMedications() {
    var json =
        "["
            + "{\"system\":\"other\",\"code\":\"LocalCode\",\"name\":\"Unbekannt\",\"version\":\"7\"},"
            + "{\"system\":\"other\",\"code\":\"LocalCode\",\"name\":\"Unbekannt\",\"version\":\"8\"}"
            + "]";

    var actual = new ArrayList<>(JsonToMedicationMapper.map(json));

    assertThat(actual)
        .hasSize(2)
        .containsExactlyInAnyOrder(
            AtcUnregisteredMedicationCoding.builder()
                .code("LocalCode")
                .display("Unbekannt")
                .version("7")
                .system(AtcUnregisteredMedicationCoding.SystemEnum.UNDEFINED)
                .build(),
            AtcUnregisteredMedicationCoding.builder()
                .code("LocalCode")
                .display("Unbekannt")
                .version("8")
                .system(AtcUnregisteredMedicationCoding.SystemEnum.UNDEFINED)
                .build());
  }
}
