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
import dev.pcvolkmer.mv64e.mtb.MtbDiagnosis;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Reference;

public abstract class DnpmToFhirMapper<T> {

  protected abstract String getPatientId(T item);

  protected Reference getPatientReference(T item) {
    return new Reference()
        .setReference(
            String.format(
                "Patient?identifier=https://fhir.diz.uni-marburg.de/sid/patient-id|%s",
                this.getPatientId(item)));
  }

  protected abstract String getRequestUrl(MtbDiagnosis diagnose);

  protected abstract String getSystem();

  public static Bundle map(Mtb mtb) {
    final var bundle = new Bundle();

    bundle.setType(Bundle.BundleType.TRANSACTION);

    final var diagnoseMapper = new DiagnoseMapper();
    mtb.getDiagnoses().forEach(item -> diagnoseMapper.addToBundle(bundle, item));

    return bundle;
  }
}
