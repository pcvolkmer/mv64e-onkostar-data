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

package dev.pcvolkmer.onco.datamapper.fhir.ngs;

import dev.pcvolkmer.mv64e.model.Chromosome;
import dev.pcvolkmer.mv64e.model.ClinVarCoding;
import dev.pcvolkmer.onco.datamapper.fhir.ObservationMapper;
import org.hl7.fhir.r4.model.Coding;

public abstract class AbstractNgsMapper<T> extends ObservationMapper<T> {

  protected Coding mapChromosome(Chromosome chromosome) {
    final var result = new Coding().setSystem("http://loinc.org");

    switch (chromosome) {
      case CHR1:
        result.setCode("LA21254-0").setDisplay("Chromosome 1");
        break;
      case CHR2:
        result.setCode("LA21255-7").setDisplay("Chromosome 2");
        break;
      case CHR3:
        result.setCode("LA21256-5").setDisplay("Chromosome 3");
        break;
      case CHR4:
        result.setCode("LA21257-3").setDisplay("Chromosome 4");
        break;
      case CHR5:
        result.setCode("LA21258-1").setDisplay("Chromosome 5");
        break;
      case CHR6:
        result.setCode("LA21259-9").setDisplay("Chromosome 6");
        break;
      case CHR7:
        result.setCode("LA21260-7").setDisplay("Chromosome 7");
        break;
      case CHR8:
        result.setCode("LA21261-5").setDisplay("Chromosome 8");
        break;
      case CHR9:
        result.setCode("LA21262-3").setDisplay("Chromosome 9");
        break;
      case CHR10:
        result.setCode("LA21263-1").setDisplay("Chromosome 10");
        break;
      case CHR11:
        result.setCode("LA21264-9").setDisplay("Chromosome 11");
        break;
      case CHR12:
        result.setCode("LA21265-6").setDisplay("Chromosome 12");
        break;
      case CHR13:
        result.setCode("LA21266-4").setDisplay("Chromosome 13");
        break;
      case CHR14:
        result.setCode("LA21267-2").setDisplay("Chromosome 14");
        break;
      case CHR15:
        result.setCode("LA21268-0").setDisplay("Chromosome 15");
        break;
      case CHR16:
        result.setCode("LA21269-8").setDisplay("Chromosome 16");
        break;
      case CHR17:
        result.setCode("LA21270-6").setDisplay("Chromosome 17");
        break;
      case CHR18:
        result.setCode("LA21271-4").setDisplay("Chromosome 18");
        break;
      case CHR19:
        result.setCode("LA21272-2").setDisplay("Chromosome 19");
        break;
      case CHR20:
        result.setCode("LA21273-0").setDisplay("Chromosome 20");
        break;
      case CHR21:
        result.setCode("LA21274-8").setDisplay("Chromosome 21");
        break;
      case CHR22:
        result.setCode("LA21275-5").setDisplay("Chromosome 22");
        break;
      case CHR_X:
        result.setCode("LA21276-3").setDisplay("Chromosome X");
        break;
      case CHR_Y:
        result.setCode("LA21277-1").setDisplay("Chromosome Y");
        break;
    }

    return result;
  }

  protected Coding mapClinVarInterpretation(ClinVarCoding.CodeEnum code) {
    final var result = new Coding().setSystem("https://www.ncbi.nlm.nih.gov/clinvar");

    switch (code) {
      case _1:
        result.setCode("Benign").setDisplay("Benign");
        break;
      case _2:
        result.setCode("Likely benign").setDisplay("Likely benign");
        break;
      case _3:
        result.setCode("VUS").setDisplay("VUS");
        break;
      case _4:
        result.setCode("Likely pathogenic").setDisplay("Likely pathogenic");
        break;
      case _5:
        result.setCode("Pathogenic").setDisplay("Pathogenic");
        break;
    }

    return result;
  }
}
