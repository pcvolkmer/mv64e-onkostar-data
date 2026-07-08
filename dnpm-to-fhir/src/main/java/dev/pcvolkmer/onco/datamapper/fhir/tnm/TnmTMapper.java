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

package dev.pcvolkmer.onco.datamapper.fhir.tnm;

import org.hl7.fhir.r4.model.*;

public class TnmTMapper extends AbstractTnmMapper {
  @Override
  protected String idSuffix() {
    return "tnmt";
  }

  @Override
  protected String getProfile() {
    return "https://www.medizininformatik-initiative.de/fhir/ext/modul-onko/StructureDefinition/mii-pr-onko-tnm-t-kategorie";
  }

  @Override
  protected Coding getExtensionCoding() {
    return new Coding()
        .setSystem("http://snomed.info/sct")
        .setCode("399504009")
        .setDisplay("cT category (observable entity)");
  }
}
