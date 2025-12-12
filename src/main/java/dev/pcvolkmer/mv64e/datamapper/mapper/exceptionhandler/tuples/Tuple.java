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

package dev.pcvolkmer.mv64e.datamapper.mapper.exceptionhandler.tuples;

public abstract class Tuple {

  protected Tuple() {
    // No content
  }

  public static <A, B> Tuple2<A, B> from(A value1, B value2) {
    return new Tuple2<>(value1, value2);
  }

  public static <A, B, C> Tuple3<A, B, C> from(A value1, B value2, C value3) {
    return new Tuple3<>(value1, value2, value3);
  }

  public static <A, B, C, D> Tuple4<A, B, C, D> from(A value1, B value2, C value3, D value4) {
    return new Tuple4<>(value1, value2, value3, value4);
  }

  public static <A, B, C, D, E> Tuple5<A, B, C, D, E> from(
      A value1, B value2, C value3, D value4, E value5) {
    return new Tuple5<>(value1, value2, value3, value4, value5);
  }
}
