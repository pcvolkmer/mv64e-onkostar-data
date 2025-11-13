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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pcvolkmer.mv64e.mtb.GeneAlterationReference;
import dev.pcvolkmer.mv64e.mtb.Reference;
import dev.pcvolkmer.onco.datamapper.exceptions.DataAccessException;
import dev.pcvolkmer.onco.datamapper.genes.GeneUtils;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

/**
 * Maps JSON strings used in Einzelempfehlung MolAltVariante
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
@NullMarked
public class JsonToMolAltVarianteMapper {

  private JsonToMolAltVarianteMapper() {
    // intentionally left empty
  }

  public static List<GeneAlterationReference> map(@Nullable String studyJson) {
    if (studyJson == null) {
      return List.of();
    }
    try {
      return new ObjectMapper()
          .readValue(studyJson, new TypeReference<List<MolAltVariante>>() {}).stream()
              .map(
                  variante -> {
                    var resultBuilder = GeneAlterationReference.builder();
                    GeneUtils.findBySymbol(variante.getGen())
                        .ifPresent(
                            gene ->
                                resultBuilder
                                    .gene(GeneUtils.toCoding(gene))
                                    .variant(
                                        Reference.builder()
                                            .id(variante.id)
                                            .type("Variant")
                                            .build()));
                    return resultBuilder.build();
                  })
              .collect(Collectors.toList());
    } catch (Exception e) {
      throw new DataAccessException(String.format("Cannot map gene alteration for %s", studyJson));
    }
  }

  @NullUnmarked
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class MolAltVariante {
    private String id;
    private String gen;

    public String getId() {
      return id;
    }

    public String getGen() {
      return gen;
    }
  }
}
