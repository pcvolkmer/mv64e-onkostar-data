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

package dev.pcvolkmer.mv64e.datamapper.mapper.exceptionhandler;

import static dev.pcvolkmer.mv64e.datamapper.mapper.exceptionhandler.TryAndLog.tryAndLog;
import static dev.pcvolkmer.mv64e.datamapper.mapper.exceptionhandler.TryAndLog.tryAndLogWithResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.pcvolkmer.mv64e.datamapper.exceptions.IgnorableMappingException;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class TryAndLogTest {

  Logger logger;
  ListAppender<ILoggingEvent> listAppender;

  @BeforeEach
  void setUp() {
    this.logger = (Logger) LoggerFactory.getLogger(TryAndLogTest.class);
    this.listAppender = new ListAppender<>();
    this.logger.addAppender(this.listAppender);
    this.listAppender.start();
  }

  @Test
  void testShouldNotThrowExceptionOnTryAndLog() {
    var actual =
        tryAndLogWithResult(
                () -> {
                  throw new IgnorableMappingException("Test");
                  // Should not be thrown beyond this
                },
                this.logger)
            .ok();

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
  void testShouldStopAtFirstException() {
    var values = new ArrayList<Integer>();

    var actual =
        tryAndLogWithResult(
                () -> {
                  values.add(1);
                  throw new IgnorableMappingException("Test1");
                  // Should not be thrown beyond this
                },
                this.logger)
            .andTryWithResult(
                it -> {
                  values.add(2);
                  throw new RuntimeException("Test2");
                  // Should not be reached
                })
            .ok();

    assertThat(actual).isEmpty();
    assertThat(values)
        .satisfies(
            it -> {
              assertThat(it).hasSize(1);
              assertThat(it.get(0)).isEqualTo(1);
            });
    assertThat(listAppender.list)
        .satisfies(
            it -> {
              assertThat(it).hasSize(1);
              assertThat(it.get(0).getFormattedMessage()).isEqualTo("Test1");
              assertThat(it.get(0).getLevel()).isEqualTo(Level.ERROR);
            });
  }

  @Test
  void testShouldCatchExceptionAndHaveNoResult() {
    var catchedExceptions = new ArrayList<Exception>();

    var actual =
        tryAndLogWithResult(() -> 1, this.logger)
            .andTryWithResult(
                value -> {
                  throw new IgnorableMappingException("Test");
                  // Should not be thrown beyond this
                })
            .elseCatch(catchedExceptions::add)
            .ok();

    assertThat(actual).isEmpty();
    assertThat(catchedExceptions).hasSize(1);
  }

  @Test
  void testShouldNotTryAfterException() {
    var catchedExceptions = new ArrayList<Exception>();

    tryAndLogWithResult(() -> 1, this.logger)
        .andTryWithResult(
            value -> {
              throw new IgnorableMappingException("Test");
              // Should not be thrown beyond this
            })
        .andTryWithResult(value -> fail("Should not be called"))
        .andTry(value -> fail("Should not be called"))
        .elseCatch(catchedExceptions::add);

    assertThat(catchedExceptions).hasSize(1);
  }

  @Test
  void testShouldTryWithoutException() {
    var values = new ArrayList<Integer>();

    tryAndLogWithResult(() -> 1, this.logger)
        .andTryWithResult(value -> value + 1)
        .andTry(values::add)
        .elseThrow();

    assertThat(values)
        .satisfies(
            it -> {
              assertThat(it).hasSize(1);
              assertThat(it.get(0)).isEqualTo(2);
            });
  }

  @Test
  void testShouldThrowExceptionAndHaveNoResult() {
    var actual =
        assertThrows(
            IgnorableMappingException.class,
            () ->
                tryAndLogWithResult(() -> 1, this.logger)
                    .andTryWithResult(
                        value -> {
                          throw new IgnorableMappingException("Test");
                          // Should not be thrown beyond this
                        })
                    .elseThrow()
                    .ok());

    assertThat(actual)
        .satisfies(
            ex -> {
              assertThat(ex).isInstanceOf(IgnorableMappingException.class);
              assertThat(ex.getMessage()).isEqualTo("Test");
            });
  }

  @Test
  void testShouldCatchException() {
    var catchedExceptions = new ArrayList<Exception>();

    tryAndLog(
            () -> {
              throw new IgnorableMappingException("Test");
              // Should not be thrown beyond this
            },
            this.logger)
        .elseCatch(catchedExceptions::add);

    assertThat(catchedExceptions).hasSize(1);
  }

  @Test
  void testShouldThrowException() {
    var actual =
        assertThrows(
            IgnorableMappingException.class,
            () ->
                tryAndLog(
                        () -> {
                          throw new IgnorableMappingException("Test");
                          // Should not be thrown beyond this
                        },
                        this.logger)
                    .elseThrow());

    assertThat(actual)
        .satisfies(
            ex -> {
              assertThat(ex).isInstanceOf(IgnorableMappingException.class);
              assertThat(ex.getMessage()).isEqualTo("Test");
            });
  }

  @Test
  void testShouldBeCleanAfterCatchWithoutValue() {
    var actual =
        tryAndLogWithResult(() -> 1, this.logger)
            .andTryWithResult(
                value -> {
                  throw new IgnorableMappingException("Test");
                  // Should not be thrown beyond this
                })
            .elseCatch(ex -> {})
            .isClean();

    assertThat(actual).isTrue();
  }

  @Test
  void testShouldBeCleanAfterCatch() {
    var actual =
        tryAndLog(
                () -> {
                  throw new IgnorableMappingException("Test");
                },
                this.logger)
            .elseCatch(ex -> {})
            .isClean();

    assertThat(actual).isTrue();
  }

  @Test
  void testShouldReturnValueOptionalOnTryAndLog() {
    var actual = tryAndLogWithResult(() -> "Test", this.logger).ok();

    assertThat(actual).get().isEqualTo("Test");
    assertThat(listAppender.list).isEmpty();
  }

  @Test
  void testShouldReturnValueOnTryAndLog() {
    var actual = tryAndLogWithResult(() -> "Test", this.logger).okOrNull();

    assertThat(actual).isEqualTo("Test");
    assertThat(listAppender.list).isEmpty();
  }
}
