# Beispielanwendung zum Mapping des DNPM-Datenmodells 2.1 in ein FHIR-Bundle

Diese Beispielanwendung ermöglicht das Mapping des DNPM-Datenmodells 2.1 in ein FHIR-Bundle.

## Anwendung

Die Jar-Datei enthält alle Abhängigkeiten und lässt sich mit folgendem Befehl ausführen:

```
java -jar <dateiname>.jar --filename <file> 
```

## Umsetzung

### MII-MTB – Bereich: Behandlungsepisode

| FHIR-Profil                         | Status | 
|-------------------------------------|--------|
| Diagnose - Diagnose Primärtumor     | ⛅      |
| Diagnose - Tumorausbreitung         | ⛅ (1)  |
| Diagnose - WHO-Grad ZNS             | ⛅ (2)  |
| Diagnose - Oncotree                 | ⛅      |
| Probe - Tumorzellgehalt             | ⛅      |
| Therapieplan - CarePlan             | ☔ (3)  |
| Therapieplan - Therapieempfehlung   | ⛅      |
| Therapieplan - Studieneinschluss    | ⛅      |
| Therapieplan - Weitere Empfehlungen | ☔ (4)  |

1. Wird aktuell aus `otherClassifications` entnommen und ignoriert, wenn nur TNM angegeben ist.
2. Eingefügt nur, wenn DNPM-System `dnpm-dip/mtb/who-grading-cns-tumors` gegeben.
3. Aktuell nur mit Wirkstoffempfehlungen, Studieneinschlussempfehlungen und humangenetischen Beratungen
4. Aktuell nur humangenetische Beratung

### MII-MTB – Bereich: NGS-Bericht

| FHIR-Profil                      | Status           | MII Förderkriterien |
|----------------------------------|------------------|---------------------|
| Variante - Einfache Variante     | ⛅                | Verpflichtend       |
| Variante - Copy Number Variant   | ⛅                | Verpflichtend       |
| Variante - DNA-Fusion            | -                | Verpflichtend       |
| Variante - RNA-Fusion            | -                | Verpflichtend       |
| Gen. Implikation - diagnostisch  | ⛅                |                     |
| Gen. Implikation - therapeutisch | -                |                     |
| Mol Biomarker - TMB              | ⛅                | Verpflichtend       |
| Mol Biomarker - MSI              | ⛅                | Verpflichtend       |
| Mol Biomarker - HRD-Score        | ⛅                | Verpflichtend       |
| Mol Biomarker - BRCAness         | ⛅                | Verpflichtend       |
| RNA-Seq                          | Nicht vorgesehen | Verpflichtend       |

### MII-MTB – Bereich: Molekular-Pathologie-Befund

| FHIR-Profil                    | Status | 
|--------------------------------|--------|
| DiagnosticReport               | -      |
| Immunhistochemie - Observation | -      |
| P-Immunhistochemie             | -      |
| Immunhistochemie MMR MSI       | -      |
| Immunhistochemie PDL1          | -      |

### MII-MTB – Bereich: FollowUp

| FHIR-Profil            | Status | 
|------------------------|--------|
| Clinical Impression    | -      |
| Systemische Therapie   | -      |
| Antrag Kostenübernahme | -      |

### MII-Onco - Bereich: Diagnosen

| FHIR-Profil                          | Status | MII Förderkriterien |
|--------------------------------------|--------|---------------------|
| Diagnose: Condition                  | ⛅ (1)  | Verpflichtend       |
| Frühere Tumorerkrankungen: Condition | -      |                     |
| Extension: ICD-O-3 Morphologie       | -      |                     |
| Erstdiagnose Evidenz: List           | -      |                     |

1. Aktuell ohne Morphologie. Verfikationsstatus ist im § MV 64e immer gesichert.

### MII-Onco - Bereich: TNM-Klassifikation

| FHIR-Profil                   | Status               | MII Förderkriterien |
|-------------------------------|----------------------|---------------------|
| TNM-Kategorie-T: Observation  | ⛅                    | Verpflichtend       |
| TNM-Kategorie-N: Observation  | ⛅                    | Verpflichtend       |
| TNM-Kategorie-M: Observation  | ⛅                    | Verpflichtend       |
| Extension: TNM-Prefix(c/p)    | ⛅ (1)                |                     |
| TNM-Symbol-a: Observation     | Nicht vorgesehen (2) |                     |
| TNM-Symbol-m: Observation     | Nicht vorgesehen (2) |                     |
| TNM-Kategorie-L: Observation  | Nicht vorgesehen (2) |                     |
| TNM-Kategorie-Pn: Observation | Nicht vorgesehen (2) |                     |
| TNM-Symbol-r: Observation     | Nicht vorgesehen (2) |                     |
| TNM-Kategorie-S: Observation  | Nicht vorgesehen (2) |                     |
| TNM-Kategorie-V: Observation  | Nicht vorgesehen (2) |                     |
| TNM-Symbol-y: Observation     | Nicht vorgesehen (2) |                     |

1. Jeweils in T, N und M enthalten
2. DNPM-Datenmodell enthält nicht alle erforderlichen Angaben

### MII-Onco - Bereich: Fernmetastasen

| FHIR-Profil                 | Status               | MII Förderkriterien |
|-----------------------------|----------------------|---------------------|
| Fernmetastasen: Observation | Nicht vorgesehen (1) | Verpflichtend       |

1. DNPM-Datenmodell enthält nicht alle erforderlichen Angaben

### MII-Onco - Bereich: Allgemeiner Leistungszustand

| FHIR-Profil                       | Status |
|-----------------------------------|--------|
| Allgemeiner Leistungszustand ECOG | ⛅      |

## Weitere Hinweise

Diese Software enthält eine Oncotree-Ontologie-Tabelle
aus https://github.com/cBioPortal/oncotree/blob/master/scripts/ontology_to_ontology_mapping_tool/ontology_mappings.txt.
Die Quelle ist unter der CC-BY-4.0 license veröffentlicht worden.