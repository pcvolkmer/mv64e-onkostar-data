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

package dev.pcvolkmer.onco.datamapper.mapper;

import dev.pcvolkmer.mv64e.mtb.Msi;
import dev.pcvolkmer.mv64e.mtb.MsiMethodCoding;
import dev.pcvolkmer.mv64e.mtb.MsiMethodCodingCode;
import dev.pcvolkmer.mv64e.mtb.Reference;
import dev.pcvolkmer.onco.datamapper.ResultSet;
import dev.pcvolkmer.onco.datamapper.datacatalogues.MolekulargenMsiCatalogue;

/**
 * Mapper class to load and map prozedur data from database table 'dk_molekluargenetik'
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class KpaMolekulargenetikMsiDataMapper extends AbstractSubformDataMapper<Msi> {

  public KpaMolekulargenetikMsiDataMapper(final MolekulargenMsiCatalogue molekulargenMsiCatalogue) {
    super(molekulargenMsiCatalogue);
  }

  /**
   * Loads and maps Prozedur related by database id
   *
   * @param id The database id of the procedure data set
   * @return The loaded Procedure
   */
  @Override
  public Msi getById(final int id) {
    return this.map(catalogue.getById(id));
  }

  @Override
  protected Msi map(ResultSet resultSet) {
    var builder = Msi.builder();

    if (!resultSet.getString("komplexerbiomarker").equals("MSI")) {
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
        // Aktuell nicht in Onkostar vorhanden!
        // .interpretation()
        // In Onkostar nur für "Sequenzierung" bzw "BIOINFORMATIC" als Prozentwert angegeben => "0"
        // als Fallback?
        .value(getSeqProzentwert(resultSet));

    return builder.build();
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
    if (analysemethoden != null && analysemethoden.contains("S")) {
      return resultSet.getDouble("seqprozentwert");
    }

    return 0;
  }
}
