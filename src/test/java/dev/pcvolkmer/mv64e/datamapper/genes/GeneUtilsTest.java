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

package dev.pcvolkmer.mv64e.datamapper.genes;

import static org.assertj.core.api.Assertions.assertThat;

import dev.pcvolkmer.mv64e.mtb.Chromosome;
import org.junit.jupiter.api.Test;

class GeneUtilsTest {

  @Test
  void findByHgncId() {
    var actual = GeneUtils.findByHgncId("HGNC:1097");

    assertThat(actual)
        .isPresent()
        .hasValueSatisfying(
            gene -> {
              assertThat(gene.getHgncId()).isEqualTo("HGNC:1097");
              assertThat(gene.getEnsemblId()).isEqualTo("ENSG00000157764");
              assertThat(gene.getSymbol()).isEqualTo("BRAF");
              assertThat(gene.getName()).isEqualTo("B-Raf proto-oncogene, serine/threonine kinase");
              assertThat(gene.getChromosome()).isEqualTo("7q34");
              assertThat(gene.getSingleChromosomeInPropertyForm()).hasValue(Chromosome.CHR7);
            });
  }

  @Test
  void findByHgncSymbol() {
    var actual = GeneUtils.findBySymbol("ABCD1");

    assertThat(actual)
        .isPresent()
        .hasValueSatisfying(
            gene -> {
              assertThat(gene.getHgncId()).isEqualTo("HGNC:61");
              assertThat(gene.getEnsemblId()).isEqualTo("ENSG00000101986");
              assertThat(gene.getSymbol()).isEqualTo("ABCD1");
              assertThat(gene.getName()).isEqualTo("ATP binding cassette subfamily D member 1");
              assertThat(gene.getChromosome()).isEqualTo("Xq28");
              assertThat(gene.getSingleChromosomeInPropertyForm()).hasValue(Chromosome.CHR_X);
            });
  }

  @Test
  void findByHgncSymbolContainingWhitespaces() {
    var actual = GeneUtils.findBySymbol("BRCA 2");

    assertThat(actual)
        .isPresent()
        .hasValueSatisfying(
            gene -> {
              assertThat(gene.getHgncId()).isEqualTo("HGNC:1101");
              assertThat(gene.getEnsemblId()).isEqualTo("ENSG00000139618");
              assertThat(gene.getSymbol()).isEqualTo("BRCA2");
              assertThat(gene.getName()).isEqualTo("BRCA2 DNA repair associated");
              assertThat(gene.getChromosome()).isEqualTo("13q13.1");
              assertThat(gene.getSingleChromosomeInPropertyForm()).hasValue(Chromosome.CHR13);
            });
  }
}
