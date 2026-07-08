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

package dev.pcvolkmer.onco.datamapper.fhir;

import dev.pcvolkmer.mv64e.mtb.Mtb;
import dev.pcvolkmer.onco.datamapper.fhir.biomarker.BrcanessMapper;
import dev.pcvolkmer.onco.datamapper.fhir.biomarker.HrdScoreMapper;
import dev.pcvolkmer.onco.datamapper.fhir.biomarker.MsiMapper;
import dev.pcvolkmer.onco.datamapper.fhir.biomarker.TmbMapper;
import dev.pcvolkmer.onco.datamapper.fhir.careplan.HumangenetischeBeratungMapper;
import dev.pcvolkmer.onco.datamapper.fhir.careplan.StudieneinschlussMapper;
import dev.pcvolkmer.onco.datamapper.fhir.careplan.TherapieempfehlungMapper;
import dev.pcvolkmer.onco.datamapper.fhir.careplan.TherapieplanMapper;
import dev.pcvolkmer.onco.datamapper.fhir.diagnosis.MtbDiagnoseMapper;
import dev.pcvolkmer.onco.datamapper.fhir.diagnosis.OncoDiagnoseMapper;
import dev.pcvolkmer.onco.datamapper.fhir.diagnosis.TumorausbreitungMapper;
import dev.pcvolkmer.onco.datamapper.fhir.diagnosis.TumorzellgehaltMapper;
import dev.pcvolkmer.onco.datamapper.fhir.ngs.CnvMapper;
import dev.pcvolkmer.onco.datamapper.fhir.ngs.EinfacheVarianteMapper;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public abstract class DnpmToFhirMapper<S, D extends Resource> implements Mapper<S, D> {

  protected abstract String getPatientId(S item);

  protected Reference getPatientReference(S item) {
    return new Reference()
        .setReference(
            String.format(
                "Patient?identifier=https://fhir.diz.uni-marburg.de/sid/patient-id|%s",
                this.getPatientId(item)));
  }

  public Reference getReference(S item) {
    return new Reference().setReference(this.getRequestUrl(item));
  }

  @Override
  public void addToBundle(Bundle bundle, S item) {
    bundle
        .addEntry()
        .setResource(map(item))
        .getRequest()
        .setMethod(Bundle.HTTPVerb.PUT)
        .setUrl(getRequestUrl(item));
  }

  protected abstract String getId(S item);

  protected abstract String getRequestUrl(S item);

  public static Bundle mapToBundle(Mtb mtb) {
    final var bundle = new Bundle();

    bundle.setType(Bundle.BundleType.TRANSACTION);

    final var mtbDiagnoseMapper = new MtbDiagnoseMapper();
    mtb.getDiagnoses().forEach(item -> mtbDiagnoseMapper.addToBundle(bundle, item));

    final var oncoDiagnoseMapper = new OncoDiagnoseMapper();
    mtb.getDiagnoses().forEach(item -> oncoDiagnoseMapper.addToBundle(bundle, item));

    final var therapieplanMapper = new TherapieplanMapper();
    mtb.getCarePlans().forEach(item -> therapieplanMapper.addToBundle(bundle, item));

    final var humangenetischeBeratungMapper = new HumangenetischeBeratungMapper();
    mtb.getCarePlans().forEach(item -> humangenetischeBeratungMapper.addToBundle(bundle, item));

    final var therapieempfehlungMapper = new TherapieempfehlungMapper();
    mtb.getCarePlans().stream()
        .filter(item -> item.getMedicationRecommendations() != null)
        .flatMap(item -> item.getMedicationRecommendations().stream())
        .forEach(item -> therapieempfehlungMapper.addToBundle(bundle, item));

    final var studieneinschlussMapper = new StudieneinschlussMapper();
    mtb.getCarePlans().stream()
        .filter(item -> item.getStudyEnrollmentRecommendations() != null)
        .flatMap(item -> item.getStudyEnrollmentRecommendations().stream())
        .forEach(item -> studieneinschlussMapper.addToBundle(bundle, item));

    final var tumorausbreitungMapper = new TumorausbreitungMapper();
    mtb.getDiagnoses().forEach(item -> tumorausbreitungMapper.addManyToBundle(bundle, item));

    final var tumorzellgehaltMapper = new TumorzellgehaltMapper();
    mtb.getHistologyReports().forEach(item -> tumorzellgehaltMapper.addToBundle(bundle, item));

    final var ecogMapper = new EcogMapper();
    mtb.getPerformanceStatus().forEach(item -> ecogMapper.addToBundle(bundle, item));

    final var einfacheVarianteMapper = new EinfacheVarianteMapper();
    mtb.getNgsReports().stream()
        .flatMap(item -> item.getResults().getSimpleVariants().stream())
        .forEach(item -> einfacheVarianteMapper.addToBundle(bundle, item));

    final var cnvMapper = new CnvMapper();
    mtb.getNgsReports().stream()
        .filter(item -> item.getResults().getCopyNumberVariants() != null)
        .flatMap(item -> item.getResults().getCopyNumberVariants().stream())
        .forEach(item -> cnvMapper.addToBundle(bundle, item));

    final var hrdScoreMapper = new HrdScoreMapper();
    mtb.getNgsReports().stream()
        .filter(item -> item.getResults().getHrdScore() != null)
        .map(item -> item.getResults().getHrdScore())
        .forEach(item -> hrdScoreMapper.addToBundle(bundle, item));

    final var msiMapper = new MsiMapper();
    mtb.getMsiFindings().forEach(item -> msiMapper.addToBundle(bundle, item));

    final var brcanessMapper = new BrcanessMapper();
    mtb.getNgsReports().stream()
        .filter(item -> item.getResults().getBrcaness() != null)
        .map(item -> item.getResults().getBrcaness())
        .forEach(item -> brcanessMapper.addToBundle(bundle, item));

    final var tmbMapper = new TmbMapper();
    mtb.getNgsReports().stream()
        .filter(item -> item.getResults().getTmb() != null)
        .map(item -> item.getResults().getTmb())
        .forEach(item -> tmbMapper.addToBundle(bundle, item));

    return bundle;
  }
}
