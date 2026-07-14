package dev.pcvolkmer.onco.datamapper.fhir.ngs;

import dev.pcvolkmer.mv64e.model.Snv;
import java.util.Objects;
import org.hl7.fhir.r4.model.*;

public class DiagnostischeImplikationMapper extends AbstractNgsMapper<Snv> {

  private final EinfacheVarianteMapper einfacheVarianteMapper;

  public DiagnostischeImplikationMapper(EinfacheVarianteMapper einfacheVarianteMapper) {
    this.einfacheVarianteMapper =
        Objects.requireNonNull(einfacheVarianteMapper, "EinfacheVarianteMapper must not be null");
  }

  public boolean supports(Snv sourceItem) {
    return sourceItem.getInterpretation() != null;
  }

  @Override
  protected String getPatientId(Snv item) {
    return item.getPatient().getId();
  }

  @Override
  protected String getId(Snv item) {
    return String.format("%s_ngsdi", item.getId());
  }

  @Override
  public Observation map(Snv sourceItem) {
    final var interpretation = sourceItem.getInterpretation();

    if (interpretation == null) {
      throw new IllegalArgumentException(
          "Diagnostic Implication cannot be set without interpretation for clinical significance.");
    }

    var result = new Observation();

    result.addIdentifier().setSystem(this.getSystem()).setValue(this.getId(sourceItem));

    result.setMeta(
        new Meta()
            .setSource(this.fhirMetaSource)
            .addProfile(
                "https://www.medizininformatik-initiative.de/fhir/ext/modul-mtb/StructureDefinition/mii-pr-mtb-diagnostische-implikation"));

    result.setStatus(Observation.ObservationStatus.FINAL);

    result
        .addCategory()
        .addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
        .setCode("laboratory")
        .setDisplay("Laboratory");

    result
        .addCategory()
        .addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/v2-0074")
        .setCode("GE");

    result.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://hl7.org/fhir/uv/genomics-reporting/CodeSystem/tbd-codes-cs")
                    .setCode("diagnostic-implication")));

    // Klinische Signifikanz
    result.addComponent(
        new Observation.ObservationComponentComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(new Coding().setCode("53037-8").setSystem("http://loinc.org")))
            .setValue(
                new CodeableConcept()
                    .addCoding(mapClinVarInterpretation(interpretation.getCode()))));

    result.setSubject(this.getPatientReference(sourceItem));
    result.addDerivedFrom(this.einfacheVarianteMapper.getReference(sourceItem));

    return result;
  }
}
