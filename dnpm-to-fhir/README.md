# Beispielanwendung zum Mapping des DNPM-Datenmodells 2.1 in ein FHIR-Bundle

Diese Beispielanwendung ermöglicht das Mapping des DNPM-Datenmodells 2.1 in ein FHIR-Bundle.

## Anwendung

Die Jar-Datei enthält alle Abhängigkeiten und lässt sich mit folgendem Befehl ausführen:

```
java -jar <dateiname>.jar --filename <file> 
```

## Umsetzung

### MII-MTB – Bereich: Behandlungsepisode

| FHIR-Profil                         | Status           | 
|-------------------------------------|------------------|
| Diagnose - Diagnose Primärtumor     | ⛅                |
| Diagnose - Tumorausbreitung         | ⛅ (1)            |
| Diagnose - WHO-Grad ZNS             | ⛅ (2)            |
| Diagnose - Oncotree                 | Nicht vorgesehen |
| Probe - Tumorzellgehalt             | ⛅                |
| Therapieplan - CarePlan             | ☔ (3)            |
| Therapieplan - Therapieempfehlung   | ⛅                |
| Therapieplan - Studieneinschluss    | ⛅                |
| Therapieplan - Weitere Empfehlungen | ☔ (4)            |

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
| Gen. Implikation - genetisch     | -                |                     |
| Gen. Implikation - therapeutisch | -                |                     |
| Mol Biomarker - Observation      | -                | Verpflichtend       |
| Mol Biomarker - TMB              | -                | Verpflichtend       |
| Mol Biomarker - MSI              | -                | Verpflichtend       |
| Mol Biomarker - HRD-Score        | -                | Verpflichtend       |
| Mol Biomarker - BRCAness         | -                | Verpflichtend       |
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

| FHIR-Profil                     | Status           | MII Förderkriterien |
|---------------------------------|------------------|---------------------|
| TNM-Klassifikation: Observation | -                |                     |
| TNM-Kategorie-T: Observation    | -                | Verpflichtend       |
| TNM-Kategorie-N: Observation    | -                | Verpflichtend       |
| TNM-Kategorie-M: Observation    | -                | Verpflichtend       |
| Extension: TNM-Prefix(c/p)      | -                |                     |
| TNM-Symbol-a: Observation       | Nicht vorgesehen |                     |
| TNM-Symbol-m: Observation       | Nicht vorgesehen |                     |
| TNM-Kategorie-L: Observation    | Nicht vorgesehen |                     |
| TNM-Kategorie-Pn: Observation   | Nicht vorgesehen |                     |
| TNM-Symbol-r: Observation       | Nicht vorgesehen |                     |
| TNM-Kategorie-S: Observation    | Nicht vorgesehen |                     |
| TNM-Kategorie-V: Observation    | Nicht vorgesehen |                     |
| TNM-Symbol-y: Observation       | Nicht vorgesehen |                     |

### MII-Onco - Bereich: Fernmetastasen

| FHIR-Profil                 | Status | MII Förderkriterien |
|-----------------------------|--------|---------------------|
| Fernmetastasen: Observation | -      | Verpflichtend       |

### MII-Onco - Bereich: Allgemeiner Leistungszustand

| FHIR-Profil                       | Status |
|-----------------------------------|--------|
| Allgemeiner Leistungszustand ECOG | ⛅      |