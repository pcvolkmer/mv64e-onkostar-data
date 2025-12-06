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

package dev.pcvolkmer.mv64e.datamapper.mapper;

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.MolekulargenMsiCatalogue;
import dev.pcvolkmer.mv64e.mtb.Msi;
import dev.pcvolkmer.mv64e.mtb.MsiInterpretationCoding;
import dev.pcvolkmer.mv64e.mtb.MsiMethodCoding;
import dev.pcvolkmer.mv64e.mtb.MsiMethodCodingCode;
import dev.pcvolkmer.mv64e.mtb.Reference;
import org.jspecify.annotations.Nullable;

/**
 * Mapper class to load and map prozedur data from database table 'dk_molekluargenetik'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class MolekulargenetikMsiDataMapper extends AbstractSubformDataMapper<Msi> {

  public MolekulargenetikMsiDataMapper(final MolekulargenMsiCatalogue molekulargenMsiCatalogue) {
    super(molekulargenMsiCatalogue);
  }

  /**
   * Loads and maps Prozedur related by database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded Procedure
   */
  @Nullable
  @Override
  public Msi getById(final int id) {
    return this.map(catalogue.getById(id));
  }

  @Nullable
  @Override
  protected Msi map(ResultSet resultSet) {
    var builder = Msi.builder();

    if (!"MSI".equals(resultSet.getString("komplexerbiomarker"))) {
      return null;
    }

    builder
        .id(resultSet.getString("id"))
        .patient(resultSet.getPatientReference())
        .method(getMethodCode(resultSet))
        .specimen(
            Reference.builder()
                .id(resultSet.getString("hauptprozedur_id"))
                .type("Specimen")
                .build())
        // In Onkostar nur für "Sequenzierung" bzw "BIOINFORMATIC" als Prozentwert angegeben => "0"
        // als Fallback?
        .value(getSeqProzentwert(resultSet));

    return builder.build();
  }

  @Nullable
  private MsiInterpretationCoding gInterpretationCoding(final ResultSet resultSet) {
    // ToDo. Aktuell nicht dokumentierbar für bioinformatischen MSI-Bestimmung (hier
    // nur Wert, aber keine Interpretation möglich)
    return null;
  }

  private MsiMethodCoding getMethodCode(final ResultSet resultSet) {
    var builder = MsiMethodCoding.builder().system("dnpm-dip/mtb/msi/method");

    var analysemethoden = resultSet.getMerkmalList("analysemethoden");

    // Achtung: Immer nur eine Methode wird betrachtet! In Onkostar sind gleichzeitig mehrere
    // Angaben möglich!
    if (analysemethoden == null) {
      return null;
    } else if (analysemethoden.contains("S")) {
      builder.code(MsiMethodCodingCode.BIOINFORMATIC);
      builder.display(MsiMethodCodingCode.BIOINFORMATIC.toString());
    } else if (analysemethoden.contains("P")) {
      builder.code(MsiMethodCodingCode.PCR);
      builder.display(MsiMethodCodingCode.PCR.toString());
    } else if (analysemethoden.contains("I")) {
      builder.code(MsiMethodCodingCode.IHC);
      builder.display(MsiMethodCodingCode.IHC.toString());
    } else {
      return null;
    }

    return builder.build();
  }

  private double getSeqProzentwert(final ResultSet resultSet) {
    var analysemethoden = resultSet.getMerkmalList("analysemethoden");

    // Achtung: Immer nur eine Methode wird betrachtet! In Onkostar sind gleichzeitig mehrere
    // Angaben möglich!
    var seqprozentwert = resultSet.getDouble("seqprozentwert");
    if (null != seqprozentwert && null != analysemethoden && analysemethoden.contains("S")) {
      return seqprozentwert;
    }

    return 0;
  }
}
