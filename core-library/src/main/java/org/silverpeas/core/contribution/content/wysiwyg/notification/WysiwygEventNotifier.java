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
package org.silverpeas.core.contribution.content.wysiwyg.notification;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.notification.system.CDIResourceEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;

/**
 * A notifier of events about a WYSIWYG content. Currently, the notifications are sent
 * asynchronously.
 * @author mmoquillon
 */
@Bean
public class WysiwygEventNotifier extends CDIResourceEventNotifier<WysiwygContent, WysiwygEvent> {

  private WysiwygEventNotifier() {
  }

  @Override
  protected WysiwygEvent createResourceEventFrom(final ResourceEvent.Type type,
      final WysiwygContent... resource) {
    return new WysiwygEvent(type, resource);
  }
}
