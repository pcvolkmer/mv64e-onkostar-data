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

import dev.pcvolkmer.mv64e.datamapper.ResultSet;
import dev.pcvolkmer.mv64e.datamapper.datacatalogues.AbstractSubformDataCatalogue;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Abstract common implementation for all subform data mappers
 *
 * @since 0.1
 * @author Paul-Christian Volkmer
 * @param <T> The destination type
 */
public abstract class AbstractSubformDataMapper<T> implements SubformDataMapper<T> {

  protected final AbstractSubformDataCatalogue catalogue;

  protected AbstractSubformDataMapper(AbstractSubformDataCatalogue catalogue) {
    this.catalogue = catalogue;
  }

  /**
   * Loads a data set from database and maps it into destination data type
   *
   * @param parentId The database id of the parent procedure data set
   * @return The data set to be loaded
   */
  @NullMarked
  @Override
  public List<T> getByParentId(final int parentId) {
    return catalogue.getAllByParentId(parentId).stream()
        .map(this::map)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Maps a single result set into destination object
   *
   * @param resultSet The result set to start from
   * @return The destination object
   */
  @Nullable
  protected abstract T map(ResultSet resultSet);
}
