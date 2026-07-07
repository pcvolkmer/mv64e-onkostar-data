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

import org.hl7.fhir.r4.model.ServiceRequest;

public abstract class ServiceRequestMapper<S> extends DnpmToFhirMapper<S, ServiceRequest> {

  @Override
  protected String getRequestUrl(S item) {
    return String.format("ServiceRequest?identifier=%s|%s", this.getSystem(), this.getId(item));
  }

  @Override
  public String getSystem() {
    return "https://fhir.diz.uni-marburg.de/sid/service-request-id";
  }
}
