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
package org.silverpeas.core.web.authentication.credentials;

import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

/**
 * A wrapper of the settings on the registration of a new user.
 *
 * @author mmoquillon
 */
public class RegistrationSettings {

  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.authentication.settings.authenticationSettings");
  private static final RegistrationSettings instance = new RegistrationSettings();

  public static RegistrationSettings getSettings() {
    return instance;
  }

  /**
   * Is the self registration capability is enabled? With this functionality, a user can register
   * himself either by filling directly a registration form or from its social account (twitter,
   * ...)
   *
   * @return true if a user can create an account in Silverpeas. False otherwise.
   */
  public boolean isUserSelfRegistrationEnabled() {
    return settings.getBoolean("newRegistrationEnabled", false);
  }

  /**
   * In case of self registration, define domain id where the account will be created
   *
   * @return specified domain id. "0" otherwise.
   */
  public String userSelfRegistrationDomainId() {
    return settings.getString("justRegisteredDomainId", "0");
  }

  /**
   * Is the group synchronization enabled when new user registration is enabled.
   * @return true if enabled, false otherwise.
   */
  public boolean isGroupSynchronizationEnabled() {
    return isUserSelfRegistrationEnabled() &&
        settings.getBoolean("registrationSynchroGroupEnabled", true);
  }
}
