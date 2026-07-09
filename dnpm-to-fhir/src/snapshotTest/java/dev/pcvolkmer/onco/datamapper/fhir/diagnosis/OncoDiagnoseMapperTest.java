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

package dev.pcvolkmer.onco.datamapper.fhir.diagnosis;

import dev.pcvolkmer.mv64e.model.Converter;
import dev.pcvolkmer.onco.datamapper.fhir.DnpmToFhirTest;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OncoDiagnoseMapperTest extends DnpmToFhirTest {

  @ParameterizedTest
  @ValueSource(strings = {"diagnosis.json"})
  void shouldMapDiagnose(String filename) throws IOException {
    var inputStream =
        Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(filename));
    var mtb = Converter.fromJsonString(new String(inputStream.readAllBytes()));

    final var diagnoseMapper = new OncoDiagnoseMapper();

    var fhir = mtb.getDiagnoses().stream().map(diagnoseMapper::map).collect(Collectors.toList());

    verifyAll(fhir, filename);
  }
}
