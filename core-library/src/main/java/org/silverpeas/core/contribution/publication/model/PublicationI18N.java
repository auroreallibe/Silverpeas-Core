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
package org.silverpeas.core.contribution.publication.model;

import org.silverpeas.core.i18n.BeanTranslation;

import java.io.Serializable;

public class PublicationI18N extends BeanTranslation implements Serializable {

  private static final long serialVersionUID = -3608883875752659027L;

  private String keywords = null;

  public PublicationI18N() {
  }

  protected PublicationI18N(final PublicationI18N translation) {
    super(translation);
  }

  public PublicationI18N(PublicationDetail publi) {
    this(publi.getLanguage(), publi.getName(), publi.getDescription(), publi.getKeywords());

    if (publi.getTranslationId() != null) {
      super.setId(publi.getTranslationId());
    }
    super.setObjectId(publi.getPK().getId());
  }

  public PublicationI18N(String lang, String name, String description,
      String keywords) {
    super(lang, name, description);
    this.keywords = keywords;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected PublicationI18N copy() {
    final PublicationI18N copy = super.copy();
    copy.keywords = keywords;
    return copy;
  }
}
