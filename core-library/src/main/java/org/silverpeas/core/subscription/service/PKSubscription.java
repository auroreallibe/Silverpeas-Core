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
package org.silverpeas.core.subscription.service;

import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;

/**
 * A subscription related to a resource behind a Primary PK.
 */
public class PKSubscription<R extends PKSubscriptionResource> extends AbstractSubscription<R> {

  /**
   * PK subscription constructor for which the type of the subscriber is USER and the
   * subscriber is the one that handles the subscription.
   * @param subscriberId id of the subscriber
   * @param subscriptionResource representation of the resource subscription
   */
  protected PKSubscription(final String subscriberId, final R subscriptionResource) {
    super(UserSubscriptionSubscriber.from(subscriberId), subscriptionResource, subscriberId);
  }

  /**
   * PK subscription constructor for a subscriber that handles the subscription too.
   * @param subscriber the subscriber
   * @param subscriptionResource representation of the resource subscription
   * @param creatorId the user id that has handled the subscription
   */
  protected PKSubscription(final SubscriptionSubscriber subscriber, final R subscriptionResource,
      final String creatorId) {
    super(subscriber, subscriptionResource, SubscriptionMethod.UNKNOWN, creatorId, null);
  }
}
