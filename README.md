# Datenextraktion aus den Onkostar DNPM-Formularen

Ziel dieser Library ist die Bereitstellung von Methoden zum Laden von Daten der DNPM-Formulare aus der
Onkostar-Datenbank
und dem Mapping in das DNPM-Datenmodell 2.1.

## Beispiel

Im Verzeichnis [`application`](/application) ist eine Beispielanwendung für den Export anhand der Fallnummer enthalten

### Nutzung in einer eigenen Anwendung

Beispiel zur Konfiguration der Datenbankanbindung, Abruf über eine Fallnummer im Formular DNPM Klinik/Anamnese und
Serialisierung als JSON-String.

```
var datasource = new MariaDbDataSource();
datasource.setUrl("jdbc:mariadb://localhost:3306/onkostar");
datasource.setUser("onkostar");
datasource.setPassword("devpass");

var mtbMapper = MtbDataMapper.create(datasource)
  .filterIncomplete()
  .tumorCellContentMethod(TumorCellContentMethodCodingCode.HISTOLOGIC);

var jsonResult = Converter.toJsonString(
  mtbMapper.getByCaseId("16000123")
);
```

Die Verwendung von `filterIncomplete()` sorgt dafür, dass unvollständige oder nicht referenzierbare Einträge aus
den Therapieplänen oder MSI-Findings entfernt werden und somit Validierungsfehler in DNPM:DIP vermieden werden.

Mit `tumorCellContentMethod(TumorCellContentMethodCodingCode.HISTOLOGIC)` kann die verwendete Methode zur Feststellung
des Tumorzellgehalts angegeben werden.

**Achtung!** Die Methode ist standardmäßig auf `BIOINFORMATIC` gesetzt, wie in
https://ibmi-ut.atlassian.net/wiki/spaces/DAM/pages/698777783/ Zeile 144 gefordert.
Eine Änderung auf eine abweichend tatsächlich verwendete Methode `HISTOLOGIC` würde zu *Fehlern* bei der Validierung
in DNPM:DIP führen, daher wird die Angabe in diesem Fall für den Export ignoriert und resultiert nur in einer *Warnung*.

Alternative Initialisierung:

```
var mtbMapper = MtbDataMapper.create(datasource, true, TumorCellContentMethodCodingCode.HISTOLOGIC);
```

Es ist auch möglich, die Daten anhand der Patienten-ID und dem Tumoridentifikator zu ermitteln.
Hierbei wird das letzte Formular `DNPM Klinik/Anamnese` anhand des Anmeldedatums MTB
ausgewählt und verwendet.

```
...

var jsonResult = Converter.toJsonString(
  mtbMapper.getLatestByPatientIdAndTumorId("2000123456", 1))
);
```

## Status

Das Projekt befindet sich aktuell in einem sehr frühen Entwicklungsstand und kann daher auch bei Status ✅ Probleme
aufweisen.
Um Mithilfe wird gebeten.

| DNPM-Datenmodell 2.1 - Bereich   | Status | Anmerkung                                                                      |
|----------------------------------|--------|--------------------------------------------------------------------------------|
| MV Metadaten                     | ⛅      | MV Consent anhand DNPM-Formular. Kein Broad Consent                            |                    
| Patient                          | ✅      | Verwendet Patienten-ID, nicht Datenbank-ID. Keine Managing Site                |
| Episoden                         | ✅      |                                                                                |
| Diagnosen                        | ✅      | Entsprechend Formularaufbau nur Diagnose der aktuellen Episode                 |
| Verwandten-Diagnosen             | ✅      |                                                                                |
| Systemische Leitlinien-Therapien | ✅      | Siehe auch: https://github.com/dnpm-dip/mtb-model/issues/9                     |
| Leitlinien-Prozeduren            | ✅      | Siehe auch: https://github.com/dnpm-dip/mtb-model/issues/9                     |
| ECOG-Verlauf                     | ✅      |                                                                                |
| Tumor-Proben                     | ⛅      | Best effort: Formular OS.Molekulargenetik erfüllt nicht alle Anforderungen (1) |
| vorherige Molekular-Diagnostik   | ✅      |                                                                                |
| Histologie-Berichte              | ✅      |                                                                                |
| IHC-Berichte                     | -      | Aktuell nicht vorgesehen                                                       |
| MSI-Befunde                      | ⛅      | Best effort: Formular OS.Molekulargenetik erfüllt nicht alle Anforderungen (2) |
| NGS-Berichte                     | ⛅      | Best effort: Formular OS.Molekulargenetik erfüllt nicht alle Anforderungen (2) |
| MTB-Beschlüsse                   | ✅      | Stützende molekulare Alteration(en) für einfache Variante und CNV (3)          |
| Follow-Up Verlauf                | -      | Späterer Zeitpunkt                                                             |
| Antrag Kostenübernahme           | -      | Späterer Zeitpunkt                                                             |
| Antwort Kostenübernahme          | -      | Späterer Zeitpunkt                                                             |
| Therapien                        | -      | Späterer Zeitpunkt                                                             |
| Response Befunde                 | -      | Späterer Zeitpunkt                                                             |

### Hinweise

1. Nicht alle möglichen Ausprägungen in `OS.Molekulargenetik` vorhanden.
2. Aktuell nicht alle Angaben effektiv im Formular `OS.Molekulargenetik` wie gefordert angebbar.
   Hinweise:
   * Tumorzellgehalt-Methode problematisch, wenn auch im NGS-Bericht histologisch festgestellt.
   * Angabe zu MSI-Interpretation fehlt in Formular, ist aber Pflichtangabe - Wird gefiltert.
   * Datenbanktabelle für MSI lautet tatsächlich `dk_molekluargenmsi` [sic!]
3. Implementierung des Mappings von HGNC-Symbol (Gen-Name) zu HGNC-ID über enthaltene Gen-Liste.

## Enthaltene Liste mit Genen

Es ist eine Liste mit über 43000 Genen
von [https://genenames.org](https://www.genenames.org/cgi-bin/download/custom?col=gd_hgnc_id&col=gd_app_sym&col=gd_app_name&col=gd_pub_chrom_map&col=md_ensembl_id&status=Approved&hgnc_dbtag=on&order_by=gd_app_sym_sort&format=text&submit=submit)
enthalten.

Diese Liste der Gene unterliegt der folgenden Lizenz und ist frei
verfügbar: [Creative Commons Public Domain (CC0) License](https://creativecommons.org/public-domain/cc0/).

## Zusätzliche Informationen

Weitere benötigte Informationen können ebenfalls abgerufen werden.

```java
var customMetadataMapper = new CustomMetadataDataMapper(
        KpaCatalogue.create(new JdbcTemplate(datasource)),
        PatientCatalogue.create(new JdbcTemplate(datasource))
);

var additional1 = customMetadataMapper.getByCaseId("16000123");
// oder
var additional2 = customMetadataMapper.getLatestByPatientIdAndTumorId("2000123456", 1);
```

Aktuell wird hier die Fallnummer und Krankenversicherungsnummer abgerufen.

## Fuzz Tests

In Tests sind einige Methoden mit `@FuzzNullTest` annotiert.
Die Implementierung dieser JUnit5-Erweiterung befindet sich in
[`FuzzNullExtension.java`](src/test/java/dev/pcvolkmer/mv64e/datamapper/test/fuzz/FuzzNullExtension.java).

Entsprechende Test-Methoden simulieren den Fall, dass einzelne Spalten in einem ResultSet auf `null` gesetzt sind.
Einzige Ausnahme ist die Spalte `id` mit dem Primary Key des Datensatzes; diese bleibt erhalten.

Dadurch können `NullPointerExceptions` bei der Verarbeitung von `null`-Werten in den Mappings besser erkannt werden.

```java

@ExtendWith(FuzzyNullExtension.class)
class DemoTest {
   // ...

   @FuzzNullTest(initMethod = "testData")
   void shouldNotSetIdToNull(final ResultSet resultSet) {
      assertThat(resultSet.getId()).isEqualTo(1);
   }

   @FuzzNullTest(initMethod = "testData")
   void exampleShouldThrowIgnorableMappingExceptionOnNullColumns(final ResultSet resultSet) {
      // Expect an IgnorableMappingException not NullPointerException!
      var exception = assertThrows(
              IgnorableMappingException.class,
              () -> this.dataMapper.getById(1)
      );
      assertThat(exception.getMessage()).isEqualTo("...");
   }

   static ResultSet testData() {
      return TestResultSet.withColumns(
              Column.name(Column.ID).value(1),
              DateColumn.name("date").value("2025-07-11"),
              Column.name("value").value("Test")
      );
   }
}

```

Durch `@FuzzNullTest(/*...*/, includeColumns = {"date"})` können einzelne Spalten explizit im Null-Fuzzing eingeschlossen 
und andere Spalten dadurch implizit ausgeschlossen werden.

Mit `@FuzzNullTest(/*...*/, excludeColumns = {"value"})` können Spalten explizit ausgeschlossen werden.
Der Einschluss einer Spalte hat Vorrang vor einem Ausschluss.

### Anzahl der Null-Fuzz-Tests

Die Anzahl der Spalten je Test, die maximal auf `null` gesetzt werden, kann durch
`@FuzzNullTest(/*...*/, maxNullColumns = ...)` festgelegt werden.
Der Standardwert ist 1 und sollte nicht kleiner als 1 oder größer als die Anzahl der Spalten im ResultSet sein.
Werte kleiner als 1 werden ignoriert.

**Vorsicht**: Die Anzahl der ausgeführten Tests kann sehr hoch sein und entsprechend lange dauern.

Für $`n`$ = 8 Spalten (exklusive `id`) und `maxNullColumns = 4`  gilt bereits:
$`\sum_{r=1}^{4} {n! \over r!(n-i)!} = 162`$ Tests.
