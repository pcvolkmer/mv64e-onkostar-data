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

    final var diagnoseMapper = new DiagnoseMapper();
    mtb.getDiagnoses().forEach(item -> diagnoseMapper.addToBundle(bundle, item));

    final var tumorausbreitungMapper = new TumorausbreitungMapper();
    mtb.getDiagnoses().forEach(item -> tumorausbreitungMapper.addManyToBundle(bundle, item));

    final var ecogMapper = new EcogMapper();
    mtb.getPerformanceStatus().forEach(item -> ecogMapper.addToBundle(bundle, item));

    final var einfacheVarianteMapper = new EinfacheVarianteMapper();
    mtb.getNgsReports().stream()
        .flatMap(item -> item.getResults().getSimpleVariants().stream())
        .forEach(item -> einfacheVarianteMapper.addToBundle(bundle, item));

    final var cnvMapper = new CnvMapper();
    mtb.getNgsReports().stream()
        .flatMap(item -> item.getResults().getCopyNumberVariants().stream())
        .forEach(item -> cnvMapper.addToBundle(bundle, item));

    return bundle;
  }
}
