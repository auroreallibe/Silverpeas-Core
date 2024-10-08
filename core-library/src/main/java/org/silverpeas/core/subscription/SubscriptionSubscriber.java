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
package org.silverpeas.core.subscription;

import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.service.SubscribeRuntimeException;

/**
 * User: Yohann Chastagnier
 * Date: 20/02/13
 */
public interface SubscriptionSubscriber {

  /**
   * Gets the identifier of the subscriber of a subscription
   * @return the unique identifier of either a user or a group of users.
   */
  String getId();

  /**
   * Gets the type of the subscriber of a subscription. It can be either a user or a group of users.
   * @return the subscriber type.
   */
  SubscriberType getType();

  /**
   * This method checks the subscriber integrity.
   * @throws SubscribeRuntimeException if not valid.
   */
  void checkValid() throws SubscribeRuntimeException;
}
