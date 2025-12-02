/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2025  Paul-Christian Volkmer
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
 *
 */

package dev.pcvolkmer.mv64e.datamapper;

/**
 * Custom metadata containing additional information
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class CustomMetadata {

  private final String fallnummer;
  private final String kvnr;

  public CustomMetadata(String fallnummer, String kvnr) {
    this.fallnummer = fallnummer;
    this.kvnr = kvnr;
  }

  public String getFallnummer() {
    return fallnummer;
  }

  public String getKvnr() {
    return kvnr;
  }
}
