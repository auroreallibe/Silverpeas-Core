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
/*
 * SilverpeasCalendar.java
 *
 * Created on 11 juin 2001, 14:38
 */

package org.silverpeas.core.web.util.viewgenerator.html.calendar;

import java.util.Date;
import java.util.List;

import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;
import org.silverpeas.core.web.util.viewgenerator.html.monthcalendar.Event;

/**
 * @author groccia
 * @version
 */
public interface Calendar extends SimpleGraphicElement {
  public void setEvents(List<Event> events);

  public void addEvent(Event event);

  public void setWeekDayStyle(String value);

  public void setMonthDayStyle(String value);

  public void setMonthVisible(boolean value);

  public void setNavigationBar(boolean value);

  public void setShortName(boolean value);

  public void setNonSelectableDays(List<Date> nonSelectableDays);

  public void setEmptyDayNonSelectable(boolean nonSelectable);

  @Override
  public String print();

}
