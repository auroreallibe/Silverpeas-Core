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
package org.silverpeas.core.admin.user;

import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import java.util.Date;
import java.util.List;

/**
 * Indexation of the users in Silverpeas. It uses the indexer and searcher API to provide a way to
 * index and to search in a transparently way the users.
 */
@Technical
@Bean
public class UserIndexation {

  public static final String COMPONENT_ID = "users";
  public static final String OBJECT_TYPE = "UserFull";

  private UserIndexation() {

  }

  /**
   * Indexes the specified user. If no user exist with the specified unique identifier, nothing is
   * done.
   *
   * @param userId the unique identifier of the user to index.
   */
  public void indexUser(String userId) {
    try {
      UserFull user = UserFull.getById(userId);
      if (user != null) {
        FullIndexEntry indexEntry = new FullIndexEntry(new IndexEntryKey(COMPONENT_ID,
            OBJECT_TYPE, userId));
        indexEntry.setLastModificationDate(new Date());
        indexEntry.setTitle(user.getDisplayedName());
        if (! user.hasSensitiveData()) {
          indexEntry.setPreview(user.getEmailAddress());
        }
        indexEntry.addTextContent(StringUtil.normalizeByRemovingAccent(user.getDisplayedName()));

        // index some useful information
        indexEntry.addField("FirstName", user.getFirstName());
        indexEntry.addField("LastName", user.getLastName());
        indexEntry.addField("DomainId", user.getDomainId());
        indexEntry.addField("AccessLevel", user.getAccessLevel().code());

        // index extra information
        String[] propertyNames = user.getPropertiesNames();
        StringBuilder extraValues = new StringBuilder(50);
        for (String propertyName : propertyNames) {
          DomainProperty property = user.getProperty(propertyName);
          if (! property.isSensitive()) {
            String extraValue = user.getValue(propertyName);
            indexEntry.addField(propertyName, extraValue);
            extraValues.append(extraValue);
            extraValues.append(" ");
          }
        }
        indexEntry.addTextContent(extraValues.toString());

        //index data from directory templates
        setTemplatesDataIntoIndex(userId, indexEntry);

        IndexEngineProxy.addIndexEntry(indexEntry);
      }
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
  }

  private void setTemplatesDataIntoIndex(String userId, FullIndexEntry indexEntry) {
    PublicationTemplateManager manager = PublicationTemplateManager.getInstance();
    List<PublicationTemplate> templates = manager.getDirectoryTemplates();
    for (PublicationTemplate template : templates) {
      manager.setDataIntoIndex(template.getFileName(), UserDetail.USER_COMPONENT, userId,
          indexEntry);
    }
  }

  public void unindexUser(String userId) {
    FullIndexEntry indexEntry = new FullIndexEntry(new IndexEntryKey(COMPONENT_ID, OBJECT_TYPE,
        userId));
    IndexEngineProxy.removeIndexEntry(indexEntry.getPK());
  }
}
