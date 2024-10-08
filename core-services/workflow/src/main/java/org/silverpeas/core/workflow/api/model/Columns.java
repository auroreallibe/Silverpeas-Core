/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.model;

import java.util.Iterator;
import java.util.List;

/**
 * Interface describing a representation of the &lt;columns&gt; element of a Process Model.
 */
public interface Columns {

  /**
   * Get the referenced Column objects as a list
   * @return
   */
  List<Column> getColumnList();

  /**
   * Get the role for which the list of items must be returned
   * @return role name
   */
  String getRoleName();

  /**
   * Set the role for which the list of items must be returned
   * @param roleName role name
   */
  void setRoleName(String roleName);

  /**
   * Get the column referencing the given item
   * @param strItemName the name of the item
   * @return a Column object
   */
  Column getColumn(String strItemName);

  /**
   * Iterate through the Column objects
   * @return an iterator
   */
  Iterator<Column> iterateColumn();

  /**
   * Add an column to the collection
   * @param column to be added
   */
  void addColumn(Column column);

  /**
   * Create an Column
   * @return an object implementing Column
   */
  Column createColumn();

}