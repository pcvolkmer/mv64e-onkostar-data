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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pcvolkmer.mv64e.datamapper.exceptions.DataAccessException;
import dev.pcvolkmer.mv64e.mtb.StudyReference;
import dev.pcvolkmer.mv64e.mtb.StudySystem;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * Maps JSON strings used in form into DNPM study
 *
 * @author Paul-Christian Volkmer
 * @since 0.1
 */
public class JsonToStudyMapper {

  private JsonToStudyMapper() {
    // intentionally left empty
  }

  public static List<StudyReference> map(String studyJson) {
    if (studyJson == null) {
      return List.of();
    }
    try {
      return new ObjectMapper()
          .readValue(studyJson, new TypeReference<List<Studie>>() {}).stream()
              .map(
                  studie ->
                      StudyReference.builder()
                          .id(studie.getId())
                          .system(getStudySystem(studie.getSystem()))
                          .type("Study")
                          .display(
                              studie.getStudy()) // Datenmodell v2.1: Über den "display"-Wert an der
                          // Referenz kann der Studien-Name gesetzt werden.
                          .build())
              .collect(Collectors.toList());
    } catch (Exception e) {
      throw new DataAccessException(String.format("Cannot map medication for %s", studyJson));
    }
  }

  @Nullable
  private static StudySystem getStudySystem(@Nullable String code) {
    if (code == null) return null;

    // possible values from DNPM Datamodel
    switch (code) {
      case "NCT":
        return StudySystem.NCT;
      case "EudraCT": // Additional value from Onkostar Property Catalogue
      case "Eudra-CT":
        return StudySystem.EUDRA_CT;
      case "DRKS":
        return StudySystem.DRKS;
      case "EUDAMED":
        return StudySystem.EUDAMED;

      // Or try to map from Enum values
      default:
        try {
          return StudySystem.valueOf(code);
        } catch (IllegalArgumentException e) {
          return null;
        }
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class Studie {
    @JsonAlias("nct")
    private String id;

    private String system;

    @JsonAlias("studie")
    private String study;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getStudy() {
      return this.study;
    }

    public void setStudy(String study) {
      this.study = study;
    }

    public String getSystem() {
      return system;
    }

    public void setSystem(String system) {
      this.system = system;
    }
  }
}
