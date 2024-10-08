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
package org.silverpeas.core.notification.message;

import org.silverpeas.kernel.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

/**
 * This manager provides tools to register and restitute volatile messages (info, success or error)
 * to the user (on screen).
 * <p>
 * It works for now with Thread Cache Service, and several steps have to be performed :
 * - use the initialize function in order to render accessible message tools provided
 * - user message tools in treatment when necessary
 * - use the destroy function in order to clear all message data attached to the thread
 * <p>
 * A typical initialization/destruction in a the service method of a HttpServlet :
 * protected void service(HttpHttpServletResponse response) {
 * <code>
 * MessageManager.initialize();
 * try {
 * ...
 * } finally {
 * MessageManager.destroy();
 * ...
 * }
 * }
 * </code>
 * <p>
 * A typical use anywhere in treatments :
 * <code>
 * if ([test of functionnal information is not ok]) {
 * MessageMessager.addError(bundle.getMessage("err.label", params));
 * }
 * </code>
 * <p>
 * @author Yohann Chastagnier
 */
public class MessageManager {

  private static SimpleCache applicationCache =
      CacheAccessorProvider.getApplicationCacheAccessor().getCache();

  public static String initialize() {
    String registeredKey = applicationCache.add(new MessageContainer());
    CacheAccessorProvider.getThreadCacheAccessor()
        .getCache()
        .put(MessageManager.class, registeredKey);
    return registeredKey;
  }

  /**
   * Clear out the thread cache the registered key referenced.
   */
  public static void destroy() {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().remove(MessageManager.class);
  }

  public static void addListener(MessageListener listener) {
    addListener(getRegistredKey(), listener);
  }

  protected static void addListener(String registredKey, MessageListener listener) {
    MessageContainer container = getMessageContainer(registredKey);
    if (container != null) {
      container.addListener(listener);
    }
  }

  public static void setLanguage(final String language) {
    setLanguage(getRegistredKey(), language);
  }

  public static String getLanguage() {
    return getLanguage(getRegistredKey());
  }

  protected static void setLanguage(String registredKey, String language) {
    MessageContainer container = getMessageContainer(registredKey);
    if (container != null) {
      container.setLanguage(language);
    }
  }

  protected static String getLanguage(String registredKey) {
    MessageContainer container = getMessageContainer(registredKey);
    if (container != null) {
      return container.getLanguage();
    }
    return DisplayI18NHelper.getDefaultLanguage();
  }

  public static void clear(String registredKey) {
    try {
      applicationCache.remove(registredKey);
    } catch (NullPointerException e) {
      // the MessageManager was already cleared!
      SilverLogger.getLogger(MessageManager.class).silent(e);
    }
  }

  public static String getRegistredKey() {
    return CacheAccessorProvider.getThreadCacheAccessor()
        .getCache()
        .get(MessageManager.class, String.class);
  }

  /**
   * Gets the localization bundle with the given base name and for the root locale.
   * @param bundleBaseName the localization bundle base name.
   * @return a localization bundle.
   */
  public static LocalizationBundle getLocalizationBundle(String bundleBaseName) {
    return getLocalizationBundle(getRegistredKey(), bundleBaseName, null);
  }

  /**
   * Gets from the message container the localization bundle with the specified bundle base name
   * and for the given language.
   * @param messageContainerName the name of the message container.
   * @param bundleBaseName the base name of the localization bundle.
   * @param language the language for which the bundle is asked.
   * @return a localization bundle.
   */
  protected static LocalizationBundle getLocalizationBundle(String messageContainerName,
      String bundleBaseName, String language) {
    MessageContainer container = getMessageContainer(messageContainerName);

    // If null, manager has not been initialized -> ERROR is traced
    if (container == null) {
      SilverLogger.getLogger(MessageManager.class).error("ResourceLocator : " + bundleBaseName);
      return null;
    }

    return container.getLocalizationBundle(bundleBaseName,
        StringUtil.isDefined(language) ? language : container.getLanguage());
  }


  public static MessageContainer getMessageContainer(String registredKey) {
    try {
      return applicationCache.get(registredKey, MessageContainer.class);
    } catch (NullPointerException e) {
      SilverLogger.getLogger(MessageManager.class)
          .silent(e)
          .debug("No Message Container registered with key ''{0}''!", registredKey);
      return null;
    }
  }

  /**
   * Add an error message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message message to add
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  public static Message addError(String message) {
    return addError(getRegistredKey(), message);
  }

  /**
   * Add an error message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param registredKey the key
   * @param message message to add
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  protected static Message addError(String registredKey, String message) {
    return addMessage(registredKey, new ErrorMessage(message));
  }

  /**
   * Add an severe message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message message to add
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  public static Message addSevere(String message) {
    return addSevere(getRegistredKey(), message);
  }

  /**
   * Add an severe message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param registredKey the key
   * @param message message to add
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  protected static Message addSevere(String registredKey, String message) {
    return addMessage(registredKey, new SevereMessage(message));
  }

  /**
   * Add an warning message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message message to add
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  public static Message addWarning(String message) {
    return addWarning(getRegistredKey(), message);
  }

  /**
   * Add an warning message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param registredKey the key
   * @param message message to add
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  protected static Message addWarning(String registredKey, String message) {
    return addMessage(registredKey, new WarningMessage(message));
  }

  /**
   * Add a success message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message message to add
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  public static Message addSuccess(String message) {
    return addSuccess(getRegistredKey(), message);
  }

  /**
   * Add a success message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param registredKey the key
   * @param message message to add
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  protected static Message addSuccess(String registredKey, String message) {
    return addMessage(registredKey, new SuccessMessage(message));
  }

  /**
   * Add an info message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message message to add
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  public static Message addInfo(String message) {
    return addInfo(getRegistredKey(), message);
  }

  /**
   * Add an info message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param registredKey the key
   * @param message message to add
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  protected static Message addInfo(String registredKey, String message) {
    return addMessage(registredKey, new InfoMessage(message));
  }

  /**
   * Centralization
   * @param registredKey the key mapped with the message
   * @param message message to add
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  private static Message addMessage(String registredKey, Message message) {
    MessageContainer container = getMessageContainer(registredKey);

    // If null, manager has not been initialized -> ERROR is traced
    if (container == null) {
      SilverLogger.getLogger(MessageManager.class)
          .error("Type : " + message.getType() + ", Message : " + message.getContent());
    } else {
      container.addMessage(message);
    }
    return message;
  }
}
