/*
 * This file is part of mv64e-onkostar-data
 *
 * Copyright (C) 2026  Paul-Christian Volkmer
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

import dev.pcvolkmer.mv64e.model.AtcUnregisteredMedicationCoding;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves medication codes and substance names against the bundled BfArM ATC catalog ({@code
 * atc.csv}).
 */
@NullMarked
final class AtcCatalog {

  private static final Logger logger = LoggerFactory.getLogger(AtcCatalog.class);

  private static final String ATC_RESOURCE = "atc.csv";
  private static final String ATC_VERSION_COLUMN = "Version";
  private static final String ATC_CODE_COLUMN = "ATC-Code";
  private static final String ATC_NAME_COLUMN = "ATC-Bedeutung";

  private static final Pattern ATC_CODE_PATTERN =
      Pattern.compile("^[ABCDGHJLMNPRSV][0-9]{2}(?:[A-Z]{1,2}(?:[0-9]{2})?)?$");
  private static final Pattern ATC_LEVEL5_PATTERN =
      Pattern.compile("^[ABCDGHJLMNPRSV][0-9]{2}[A-Z]{2}[0-9]{2}$");
  private static final Pattern SAFE_TRAILING_PARENS_PATTERN =
      Pattern.compile(
          "\\s*\\((?:o\\.\\s*n\\.\\s*A\\.|NOS|CBCDA|CDDP|dFdC|5-FU|FU|DTIC|CCNU|BCNU|MTX|VP-16|CPT-11|L-OHP|ADM|CTX|IFO|MMC|BLM|VCR|VBL|VDS|L-ASP|2-CDA|AraC|EPI|IDA|MPL|DCF|6-TG|CAR-T-Zellen|AMSA,\\s*m-AMSA|6-MP,\\s*Purinethol|Medroxyprogesteronacetat,\\s*MPA)\\)\\s*$",
          Pattern.CASE_INSENSITIVE);
  private static final Pattern SAFE_CONJUGATE_PARENS_PATTERN =
      Pattern.compile(
          "(?<=(?:deruxtecan))\\s*\\(T-DXd\\)\\s*$|(?<=(?:ozogamicin))\\s*\\(Konjugat\\)\\s*$",
          Pattern.CASE_INSENSITIVE);
  private static final Pattern FORMULATION_AFFIX_PATTERN =
      Pattern.compile(
          "^(?:nab-|nal-|liposomal\\s+verkapseltes\\s+|pegyliertes?\\s+liposomales?\\s+|liposomales?\\s+)|\\s+liposomal(?:\\s+(?:nicht-)?pegyliert)?$",
          Pattern.CASE_INSENSITIVE);
  private static final Pattern NON_ALNUM_PATTERN = Pattern.compile("[\\s-]+");

  private final Map<String, AtcEntry> byCode;
  private final Map<String, List<AtcEntry>> level5ByExactName;
  private final Map<String, List<AtcEntry>> level5ByCollapsedName;

  private static final class Holder {
    private static final AtcCatalog INSTANCE = loadFromClasspath();
  }

  private AtcCatalog(
      final Map<String, AtcEntry> byCode,
      final Map<String, List<AtcEntry>> level5ByExactName,
      final Map<String, List<AtcEntry>> level5ByCollapsedName) {
    this.byCode = byCode;
    this.level5ByExactName = level5ByExactName;
    this.level5ByCollapsedName = level5ByCollapsedName;
  }

  static AtcCatalog instance() {
    return Holder.INSTANCE;
  }

  static boolean isAtcCode(@Nullable final String code) {
    final var normalized = trimToNull(code);
    return null != normalized
        && ATC_CODE_PATTERN.matcher(normalized.toUpperCase(Locale.ROOT)).matches();
  }

  ResolvedMedication resolve(
      @Nullable final String rawCode,
      @Nullable final String rawName,
      @Nullable final String rawSystem,
      @Nullable final String rawVersion) {
    final var code = trimToNull(rawCode);
    final var preservedCode = null != rawCode ? rawCode.strip() : null;
    final var name = trimToNull(rawName);
    final var system = trimToNull(rawSystem);
    final var version = trimToNull(rawVersion);
    final boolean explicitAtcSystem = "ATC".equalsIgnoreCase(system);

    if (explicitAtcSystem && null != version && null != code) {
      return new ResolvedMedication(
          code,
          AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC,
          version,
          name);
    }

    if (null != code) {
      final var upperCode = code.toUpperCase(Locale.ROOT);
      if (ATC_CODE_PATTERN.matcher(upperCode).matches()) {
        final var entry = byCode.get(upperCode);
        if (null != entry) {
          return new ResolvedMedication(
              entry.code,
              AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC,
              entry.version,
              null != name ? name : entry.name);
        }
      }
      // Never overwrite an explicit ATC code via substance-name fallback
      if (explicitAtcSystem) {
        return new ResolvedMedication(
            preservedCode, AtcUnregisteredMedicationCoding.SystemEnum.UNDEFINED, version, name);
      }
    }

    final var resolvedByName = findBySubstanceName(name);
    if (resolvedByName.isPresent()) {
      final var entry = resolvedByName.get();
      return new ResolvedMedication(
          entry.code,
          AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC,
          entry.version,
          null != name ? name : entry.name);
    }

    final var resolvedByCodeText = findBySubstanceName(code);
    if (resolvedByCodeText.isPresent()) {
      final var entry = resolvedByCodeText.get();
      return new ResolvedMedication(
          entry.code,
          AtcUnregisteredMedicationCoding.SystemEnum.HTTP_FHIR_DE_CODE_SYSTEM_BFARM_ATC,
          entry.version,
          null != name ? name : code);
    }

    return new ResolvedMedication(
        preservedCode, AtcUnregisteredMedicationCoding.SystemEnum.UNDEFINED, version, name);
  }

  private Optional<AtcEntry> findBySubstanceName(@Nullable final String rawCandidate) {
    final var candidate = trimToNull(rawCandidate);
    if (null == candidate) {
      return Optional.empty();
    }

    for (final var variant : buildNameVariants(candidate)) {
      final var exactMatches = level5ByExactName.get(variant.toLowerCase(Locale.ROOT));
      final var matches =
          null != exactMatches ? exactMatches : level5ByCollapsedName.get(collapse(variant));
      if (null != matches && !matches.isEmpty()) {
        return disambiguate(matches);
      }
    }

    return Optional.empty();
  }

  private static List<String> buildNameVariants(final String candidate) {
    final var variants = new ArrayList<String>(4);
    addIfNotBlankAndNew(variants, candidate);

    final var withoutParens = stripSafeParens(candidate);
    addIfNotBlankAndNew(variants, withoutParens);

    final var withoutAffix = stripFormulationAffixes(candidate);
    addIfNotBlankAndNew(variants, withoutAffix);

    if (null != withoutParens) {
      addIfNotBlankAndNew(variants, stripFormulationAffixes(withoutParens));
    }
    if (null != withoutAffix) {
      addIfNotBlankAndNew(variants, stripSafeParens(withoutAffix));
    }

    return variants;
  }

  @Nullable
  private static String stripSafeParens(final String value) {
    final var step1 = SAFE_TRAILING_PARENS_PATTERN.matcher(value).replaceFirst("");
    return trimToNull(SAFE_CONJUGATE_PARENS_PATTERN.matcher(step1).replaceFirst(""));
  }

  @Nullable
  private static String stripFormulationAffixes(final String value) {
    return trimToNull(FORMULATION_AFFIX_PATTERN.matcher(value).replaceAll(""));
  }

  private static void addIfNotBlankAndNew(final List<String> list, @Nullable final String value) {
    if (null != value && !list.contains(value)) {
      list.add(value);
    }
  }

  private static Optional<AtcEntry> disambiguate(final List<AtcEntry> matches) {
    if (matches.size() == 1) {
      return Optional.of(matches.get(0));
    }

    // Prefer antineoplastic (L01) or endocrine therapy (L02) when a substance name appears in
    // multiple anatomical groups (e.g. Bevacizumab L01FG01 vs. S01LA08), but never guess when
    // multiple L01/L02 candidates exist.
    final var oncologyMatches = new ArrayList<AtcEntry>();
    for (final var entry : matches) {
      if (entry.code.startsWith("L01") || entry.code.startsWith("L02")) {
        oncologyMatches.add(entry);
      }
    }
    if (oncologyMatches.size() == 1) {
      return Optional.of(oncologyMatches.get(0));
    }

    return Optional.empty();
  }

  private static String collapse(final String value) {
    return NON_ALNUM_PATTERN.matcher(value.toLowerCase(Locale.ROOT)).replaceAll("");
  }

  @Nullable
  private static String trimToNull(@Nullable final String value) {
    if (null == value || value.isBlank()) {
      return null;
    }
    return value.strip();
  }

  private static AtcCatalog loadFromClasspath() {
    final var byCode = new HashMap<String, AtcEntry>();
    final var byExactName = new HashMap<String, List<AtcEntry>>();
    final var byCollapsedName = new HashMap<String, List<AtcEntry>>();

    try (var inputStream =
        Objects.requireNonNull(
            AtcCatalog.class.getClassLoader().getResourceAsStream(ATC_RESOURCE))) {
      final var parser =
          CSVFormat.RFC4180
              .builder()
              .setHeader()
              .setSkipHeaderRecord(true)
              .setDelimiter('\t')
              .setIgnoreEmptyLines(true)
              .build()
              .parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

      for (final var row : parser) {
        if (!row.isMapped(ATC_CODE_COLUMN)
            || !row.isMapped(ATC_NAME_COLUMN)
            || !row.isMapped(ATC_VERSION_COLUMN)) {
          continue;
        }
        final var code = trimToNull(row.get(ATC_CODE_COLUMN));
        final var name = trimToNull(row.get(ATC_NAME_COLUMN));
        final var version = trimToNull(row.get(ATC_VERSION_COLUMN));
        if (null == code || null == name || null == version || !isAtcCode(code)) {
          continue;
        }

        final var entry = new AtcEntry(code.toUpperCase(Locale.ROOT), name, version);
        byCode.putIfAbsent(entry.code, entry);

        if (ATC_LEVEL5_PATTERN.matcher(entry.code).matches()) {
          byExactName
              .computeIfAbsent(entry.name.toLowerCase(Locale.ROOT), ignored -> new ArrayList<>())
              .add(entry);
          byCollapsedName
              .computeIfAbsent(collapse(entry.name), ignored -> new ArrayList<>())
              .add(entry);
        }
      }
    } catch (IOException | RuntimeException e) {
      logger.warn("Could not load BfArM ATC catalog '{}'", ATC_RESOURCE, e);
    }

    return new AtcCatalog(byCode, byExactName, byCollapsedName);
  }

  private static final class AtcEntry {
    private final String code;
    private final String name;
    private final String version;

    private AtcEntry(final String code, final String name, final String version) {
      this.code = code;
      this.name = name;
      this.version = version;
    }
  }

  static final class ResolvedMedication {
    @Nullable private final String code;
    private final AtcUnregisteredMedicationCoding.SystemEnum system;
    @Nullable private final String version;
    @Nullable private final String display;

    private ResolvedMedication(
        @Nullable final String code,
        final AtcUnregisteredMedicationCoding.SystemEnum system,
        @Nullable final String version,
        @Nullable final String display) {
      this.code = code;
      this.system = system;
      this.version = version;
      this.display = display;
    }

    @Nullable String code() {
      return code;
    }

    AtcUnregisteredMedicationCoding.SystemEnum system() {
      return system;
    }

    @Nullable String version() {
      return version;
    }

    @Nullable String display() {
      return display;
    }
  }
}
