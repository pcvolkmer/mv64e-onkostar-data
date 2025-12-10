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

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.pcvolkmer.mv64e.datamapper.exceptions.IgnorableMappingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class MapperUtilsTest {

  Logger logger;
  ListAppender<ILoggingEvent> listAppender;

  @BeforeEach
  void setUp() {
    this.logger = (Logger) LoggerFactory.getLogger(MapperUtilsTest.class);
    this.listAppender = new ListAppender<>();
    this.logger.addAppender(this.listAppender);
    this.listAppender.start();
  }

  @Test
  void testShouldNotThrowExceptionOnTryAndReturnOrLog() {
    var actual =
        MapperUtils.tryAndReturnOrLog(
            () -> {
              throw new IgnorableMappingException("Test");
              // Should not be thrown beyond this
            },
            logger);

    assertThat(actual).isEmpty();
    assertThat(listAppender.list)
        .satisfies(
            it -> {
              assertThat(it).hasSize(1);
              assertThat(it.get(0).getFormattedMessage()).isEqualTo("Test");
              assertThat(it.get(0).getLevel()).isEqualTo(Level.ERROR);
            });
  }

  @Test
  void testShouldReturnValueOnTryAndReturnOrLog() {
    var actual = MapperUtils.tryAndReturnOrLog(() -> "Test", logger);

    assertThat(actual).get().isEqualTo("Test");
    assertThat(listAppender.list).isEmpty();
  }
}
