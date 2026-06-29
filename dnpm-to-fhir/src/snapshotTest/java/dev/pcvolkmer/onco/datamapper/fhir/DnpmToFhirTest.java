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

import static org.assertj.core.api.Assertions.assertThat;

import ca.uhn.fhir.context.FhirContext;
import java.util.List;
import org.approvaltests.Approvals;
import org.hl7.fhir.instance.model.api.IBaseResource;

public abstract class DnpmToFhirTest {

  /**
   * Verifies given Fhir resource matches approved source file
   *
   * @param resource The Fhir resource to be verified
   * @param sourceFile The approved source file
   */
  protected static void verify(IBaseResource resource, String sourceFile) {
    var fhirParser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
    var fhirJson = fhirParser.encodeResourceToString(resource);
    Approvals.verify(
        fhirJson, Approvals.NAMES.withParameters(sourceFile).forFile().withExtension(".fhir.json"));
  }

  /**
   * Verifies given Fhir resources match approved source file
   *
   * @param resources The Fhir resources to be verified
   * @param sourceFile The approved source file
   */
  protected static <T extends IBaseResource> void verifyAll(List<T> resources, String sourceFile) {
    for (int i = 0; i < resources.size(); i++) {
      assertThat(resources.get(i)).isNotNull();
      var fhirParser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
      var fhirJson = fhirParser.encodeResourceToString(resources.get(i));
      Approvals.verify(
          fhirJson,
          Approvals.NAMES
              .withParameters(sourceFile, String.format("index_%d", i))
              .forFile()
              .withExtension(".fhir.json"));
    }
  }
}
