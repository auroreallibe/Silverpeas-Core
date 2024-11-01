/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
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
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.tree.model;

import java.io.Serializable;

import org.apache.jackrabbit.guava.common.base.Objects;
import org.silverpeas.core.WAPrimaryKey;

@SuppressWarnings("deprecation")
public class TreeNodePK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = -2135967099552497544L;

  public TreeNodePK(String id) {
    super(id);
  }

  public TreeNodePK(WAPrimaryKey pk) {
    super(pk.getId(), pk);
  }

  @Override
  public String getRootTableName() {
    return "Tree";
  }

  @Override
  public String getTableName() {
    return "SB_Tree_Tree";
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof TreeNodePK)) {
      return false;
    }
    return (id.equals(((TreeNodePK) other).getId()))
        && (space.equals(((TreeNodePK) other).getSpace()))
        && (componentName.equals(((TreeNodePK) other).getComponentName()));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, space, componentName);
  }

}
