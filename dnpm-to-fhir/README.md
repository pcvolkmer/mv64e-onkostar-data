# Beispielanwendung zum Mapping des DNPM-Datenmodells 2.1 in ein FHIR-Bundle

Diese Beispielanwendung ermöglicht das Mapping des DNPM-Datenmodells 2.1 in ein FHIR-Bundle.

## Anwendung

Die Jar-Datei enthält alle Abhängigkeiten und lässt sich mit folgendem Befehl ausführen:

```
java -jar <dateiname>.jar --filename <file> 
```

## Umsetzung

### MII-MTB – Bereich Behandlungsepisode

| FHIR-Profil                         | Status           | 
|-------------------------------------|------------------|
| Diagnose - Diagnose Primärtumor     | ⛅                |
| Diagnose - Tumorausbreitung         | ⛅ (1)            |
| Diagnose - WHO-Grad ZNS             | ⛅ (2)            |
| Diagnose - Oncotree                 | Nicht vorgesehen |
| Probe - Tumorzellgehalt             | -                |
| Therapieplan - CarePlan             | -                |
| Therapieplan - Therapieempfehlung   | -                |
| Therapieplan - Studieneinschluss    | -                |
| Therapieplan - Weitere Empfehlungen | -                |

1. Wird aktuell aus `otherClassifications` entnommen und ignoriert, wenn nur TNM angegeben ist.
2. Eingefügt nur, wenn DNPM-System `dnpm-dip/mtb/who-grading-cns-tumors` gegeben.

### MII-MTB – Bereich NGS-Bericht

| FHIR-Profil                       | Status | 
|-----------------------------------|--------|
| NGS Bericht - Einfache Variante   | ⛅      |
| NGS Bericht - Copy Number Variant | ⛅      |

### MII-MTB – Bereich Molekular-Pathologie-Befund

*Nicht umgesetzt*

### MII-MTB – Bereich FollowUp

*Nicht umgesetzt*

### MII-Onco - Bereich Allgemeiner Leistungszustand

| FHIR-Profil                       | Status | 
|-----------------------------------|--------|
| Allgemeiner Leistungszustand ECOG | ⛅      |