/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.Set;

/**
 * A plannable object is a object that can be planned in a calendar and that can be serialized
 * on a data source. A plannable object is defined in the timeline by a start date time and
 * by an end date time with split-minute accuracy. According to the type of the plannable
 * object, the end date time can be undefined. When a plannable object is created, the temporal
 * type of the event's end must be the same temporal type that the event's start, and the
 * plannable object should convert them into a date time with split-minute accuracy when
 * accessing them.
 * @author mmoquillon
 */
public interface Plannable extends Serializable {

  /**
   * Gets the unique identifier of this plannable object.
   * @return
   */
  String getId();

  /**
   * If this plannable object is on all days, then gets the start date.<br/>
   * Otherwise gets the date and the time in UTC/Greenwich at which this plannable object starts
   * on the timeline.
   * @return a temporal instance ({@link LocalDate} if an all day plannable or
   * {@link OffsetDateTime}) otherwise.
   */
  Temporal getStartDate();

  /**
   * If this plannable object is on all days, then gets the end date.<br/>
   * Otherwise gets the date and the time in UTC/Greenwich at which this plannable object ends
   * on the timeline.
   * Gets the date and the time in UTC/Greenwich at which this plannable object ends on the
   * timeline.<br/>
   * According to the type of the plannable object,
   * the end date time can be undefined; in this case, it must be indicated as such in the
   * implemented method's documentation.
   * @return a temporal instance ({@link LocalDate} if an all day plannable or
   * {@link OffsetDateTime}) otherwise.
   */
  Temporal getEndDate();

  /**
   * Does this plannable object extend over all the day(s)? In the case it is on all the day(s)
   * from the start date to the end date, the time in the date time returned by the methods
   * {@link Plannable#getStartDate()} and {@link Plannable#getEndDate()} is meaningless and
   * shouldn't be taken into account.
   * @return true if this plannable object extend over all the day(s) between its start date and
   * its end date.
   */
  boolean isOnAllDay();

  /**
   * Gets the title of this plannable object. A title is a short resume or the subject of the
   * plannable object.
   * @return a short text about the reason of this plannable object.
   */
  String getTitle();

  /**
   * Sets a title to this plannable object. A title is a short resume or the subject of the
   * plannable object.
   * @param title a short text about the reason of this plannable object.
   */
  void setTitle(String title);

  /**
   * Saves this plannable object into the specified calendar. This will add this plannable object
   * into the given calendar and it will have hence an unique identifier that will uniquely
   * identify it among all others plannable objects in the calendar. If this was already
   * planned in a calender, nothing is done.
   * @param aCalendar
   * @return itself.
   */
  Plannable planOn(final Calendar aCalendar);

  /**
   * Is this planned in a given calendar?
   * @return true if this event is planned in a calendar, false otherwise.
   */
  boolean isPlanned();

  /**
   * Deletes this planned object from the calendar it belongs to. If it was not planned (aka saved)
   * in a given calendar, then nothing is done.
   */
  ModificationResult delete();

  /**
   * Updates this planned object in the underlying calendar it belongs to. If it was not planned
   * (aka saved) in a given calendar, then nothing is done.
   */
  ModificationResult update();
}
