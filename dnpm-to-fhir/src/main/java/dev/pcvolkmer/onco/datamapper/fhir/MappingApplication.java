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

package dev.pcvolkmer.onco.datamapper.fhir;

import dev.pcvolkmer.mv64e.model.Converter;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.io.IOUtils;

public class MappingApplication {

  public static void main(String[] args) throws Exception {
    final var parsedCliArgs = DefaultParser.builder().get().parse(getCliOptions(), args);

    if (parsedCliArgs.hasOption("help")) {
      HelpFormatter.builder()
          .setShowSince(false)
          .get()
          .printHelp("java -jar <dateiname>.jar", "", getCliOptions(), "", true);
      return;
    }

    if (parsedCliArgs.hasOption("filename")) {
      var inputFile = Path.of(parsedCliArgs.getOptionValue("filename")).toFile();

      if (!inputFile.exists()) {
        System.err.println("Datei existiert nicht: " + inputFile.getAbsolutePath());
        return;
      }

      var fis = new FileInputStream(inputFile);
      var dnpmJson = IOUtils.toString(fis, StandardCharsets.UTF_8);
      var dnpmData = Converter.fromJsonString(dnpmJson);

      var json = Converter.toJsonString(dnpmData);
      var writer =
          new PrintWriter(
              Path.of(parsedCliArgs.getOptionValue("filename").replaceAll(".json", ".fhir.json"))
                  .toFile());
      writer.println(json);
      writer.close();
    } else {
      System.err.println("Keine Datei angegeben. Verwenden Sie --filename <dateiname>.json");
    }
  }

  private static Options getCliOptions() {
    Options options = new Options();
    options.addOption(Option.builder().longOpt("filename").hasArg().desc("Datei").get());
    return options;
  }
}
