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

import dev.pcvolkmer.mv64e.model.PatientRecord;
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
import dev.pcvolkmer.onco.datamapper.fhir.tnm.TnmMMapper;
import dev.pcvolkmer.onco.datamapper.fhir.tnm.TnmNMapper;
import dev.pcvolkmer.onco.datamapper.fhir.tnm.TnmTMapper;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public abstract class DnpmToFhirMapper<S, D extends Resource> implements Mapper<S, D> {

  private final MessageDigest md5Digest;

  protected DnpmToFhirMapper() {
    try {
      md5Digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

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
    final var resource = map(item);
    if (null != resource) {
      bundle
          .addEntry()
          .setResource(resource)
          .setFullUrl(this.fullUrlUrn(getRequestUrl(item)))
          .getRequest()
          .setMethod(Bundle.HTTPVerb.PUT)
          .setUrl(getRequestUrl(item));
    }
  }

  public String fullUrlUrn(String requestUrl) {
    final var digest = this.md5Digest.digest(requestUrl.getBytes(StandardCharsets.UTF_8));
    ByteBuffer bb = ByteBuffer.wrap(digest);
    long mostSigBits = bb.getLong();
    long leastSigBits = bb.getLong();

    final var uuid = new UUID(mostSigBits, leastSigBits);
    return String.format("urn:uuid:%s", uuid);
  }

  protected abstract String getId(S item);

  protected abstract String getRequestUrl(S item);

  public static Bundle mapToBundle(PatientRecord patientRecord) {
    final var bundle = new Bundle();

    bundle.setType(Bundle.BundleType.TRANSACTION);

    final var diagnoses = patientRecord.getDiagnoses();
    if (null != diagnoses) {
      final var mtbDiagnoseMapper = new MtbDiagnoseMapper();
      diagnoses.forEach(item -> mtbDiagnoseMapper.addToBundle(bundle, item));

      final var oncoDiagnoseMapper = new OncoDiagnoseMapper();
      diagnoses.forEach(item -> oncoDiagnoseMapper.addToBundle(bundle, item));

      final var tumorausbreitungMapper = new TumorausbreitungMapper();
      diagnoses.forEach(item -> tumorausbreitungMapper.addManyToBundle(bundle, item));

      final var tnmtMapper = new TnmTMapper();
      diagnoses.forEach(item -> tnmtMapper.addManyToBundle(bundle, item));

      final var tnmnMapper = new TnmNMapper();
      diagnoses.forEach(item -> tnmnMapper.addManyToBundle(bundle, item));

      final var tnmmMapper = new TnmMMapper();
      diagnoses.forEach(item -> tnmmMapper.addManyToBundle(bundle, item));
    }

    final var carePlans = patientRecord.getCarePlans();
    if (null != carePlans) {
      final var therapieplanMapper = new TherapieplanMapper();
      carePlans.forEach(item -> therapieplanMapper.addToBundle(bundle, item));

      final var humangenetischeBeratungMapper = new HumangenetischeBeratungMapper();
      carePlans.forEach(item -> humangenetischeBeratungMapper.addToBundle(bundle, item));

      final var therapieempfehlungMapper = new TherapieempfehlungMapper();
      carePlans.stream()
          .filter(item -> item.getMedicationRecommendations() != null)
          .flatMap(item -> item.getMedicationRecommendations().stream())
          .forEach(item -> therapieempfehlungMapper.addToBundle(bundle, item));

      final var studieneinschlussMapper = new StudieneinschlussMapper();
      carePlans.stream()
          .filter(item -> item.getStudyEnrollmentRecommendations() != null)
          .flatMap(item -> item.getStudyEnrollmentRecommendations().stream())
          .forEach(item -> studieneinschlussMapper.addToBundle(bundle, item));
    }

    final var histologieReports = patientRecord.getHistologyReports();
    if (null != histologieReports) {
      final var tumorzellgehaltMapper = new TumorzellgehaltMapper();
      histologieReports.forEach(item -> tumorzellgehaltMapper.addToBundle(bundle, item));
    }

    final var performanceStatus = patientRecord.getPerformanceStatus();
    if (null != performanceStatus) {
      final var ecogMapper = new EcogMapper();
      performanceStatus.forEach(item -> ecogMapper.addToBundle(bundle, item));
    }

    final var ngsReports = patientRecord.getNgsReports();
    if (null != ngsReports) {

      final var einfacheVarianteMapper = new EinfacheVarianteMapper();
      ngsReports.stream()
          .filter(item -> item.getResults().getSimpleVariants() != null)
          .flatMap(item -> item.getResults().getSimpleVariants().stream())
          .forEach(item -> einfacheVarianteMapper.addToBundle(bundle, item));

      final var cnvMapper = new CnvMapper();
      ngsReports.stream()
          .filter(item -> item.getResults().getCopyNumberVariants() != null)
          .flatMap(item -> item.getResults().getCopyNumberVariants().stream())
          .forEach(item -> cnvMapper.addToBundle(bundle, item));

      final var hrdScoreMapper = new HrdScoreMapper();
      ngsReports.stream()
          .filter(item -> item.getResults().getHrdScore() != null)
          .map(item -> item.getResults().getHrdScore())
          .forEach(item -> hrdScoreMapper.addToBundle(bundle, item));

      final var brcanessMapper = new BrcanessMapper();
      ngsReports.stream()
          .filter(item -> item.getResults().getBrcaness() != null)
          .map(item -> item.getResults().getBrcaness())
          .forEach(item -> brcanessMapper.addToBundle(bundle, item));

      final var tmbMapper = new TmbMapper();
      ngsReports.stream()
          .filter(item -> item.getResults().getTmb() != null)
          .map(item -> item.getResults().getTmb())
          .forEach(item -> tmbMapper.addToBundle(bundle, item));
    }

    final var msiFindings = patientRecord.getMsiFindings();
    if (null != msiFindings) {
      final var msiMapper = new MsiMapper();
      msiFindings.forEach(item -> msiMapper.addToBundle(bundle, item));
    }

    return bundle;
  }
}
