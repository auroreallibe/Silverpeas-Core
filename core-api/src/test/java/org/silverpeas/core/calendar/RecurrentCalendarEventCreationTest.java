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

import org.junit.Test;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TimeUnit;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.silverpeas.core.calendar.Recurrence.NO_RECURRENCE_COUNT;

/**
 * A recurrent event is created on a period of time that is recurred regularly on the timeline
 * according to a given recurrence (or frequency). The recurrence can have some exceptions
 * explicitly defined. The event is effective only when it is added in a given calendar which will
 * be in charge of the computation of the occurrences of the event according to its recurrence
 * rules (this will be covered by another unit test).
 * @author mmoquillon
 */
public class RecurrentCalendarEventCreationTest {

  private static final LocalDate today = LocalDate.now();
  private static final OffsetDateTime now = OffsetDateTime.now();
  private static final OffsetDateTime after2Hours = now.plusHours(2);
  private static final String EVENT_TITLE = "an event title";
  private static final String EVENT_DESCRIPTION = "a short event description";

  @Test
  public void createADailyEvent() {
    CalendarEvent event = anAllDayEvent().recur(Recurrence.every(2, TimeUnit.DAY));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(2, TimeUnit.DAY)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createAWeeklyEvent() {
    CalendarEvent event = anAllDayEvent().recur(Recurrence.every(1, TimeUnit.WEEK));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.WEEK)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createAMonthlyEvent() {
    CalendarEvent event = anAllDayEvent().recur(Recurrence.every(3, TimeUnit.MONTH));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(3, TimeUnit.MONTH)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createAYearlyEvent() {
    CalendarEvent event = anAllDayEvent().recur(Recurrence.every(1, TimeUnit.YEAR));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.YEAR)));
    assertDefaultValuesOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createAHourlyEvent() {
    aTimelyEvent().recur(Recurrence.every(2, TimeUnit.HOUR));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createAHourlyAllDayEvent() {
    anAllDayEvent().recur(Recurrence.every(1, TimeUnit.HOUR));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createAMinutelyEvent() {
    aTimelyEvent().recur(Recurrence.every(30, TimeUnit.MINUTE));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createASecondlyEvent() {
    aTimelyEvent().recur(Recurrence.every(65, TimeUnit.SECOND));
  }

  @Test
  public void createARecurringEventWithExceptionDates() {
    CalendarEvent event = aTimelyEvent().recur(Recurrence.every(1, TimeUnit.WEEK)
        .excludeEventOccurrencesStartingAt(today.plusWeeks(2), today.plusWeeks(5)));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.WEEK)));
    assertThat(event.getRecurrence().getExceptionDates(),
        hasItems(today.plusWeeks(2).atStartOfDay().atOffset(ZoneOffset.UTC),
            today.plusWeeks(5).atStartOfDay().atOffset(ZoneOffset.UTC)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createARecurringEventWithExceptionDateTimes() {
    CalendarEvent event = aTimelyEvent().recur(Recurrence.every(1, TimeUnit.WEEK)
        .excludeEventOccurrencesStartingAt(now.plusWeeks(2), now.plusWeeks(5)));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.WEEK)));
    assertThat(event.getRecurrence().getExceptionDates(),
        hasItems(now.plusWeeks(2).withOffsetSameInstant(ZoneOffset.UTC),
            now.plusWeeks(5).withOffsetSameInstant(ZoneOffset.UTC)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createAHourlyEventOnSpecificDays() {
    aTimelyEvent().recur(Recurrence.every(3, TimeUnit.HOUR));
  }

  @Test(expected = IllegalStateException.class)
  public void createADailyEventOnSpecificDays() {
    CalendarEvent event = aTimelyEvent().recur(
        Recurrence.every(1, TimeUnit.DAY).on(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.DAY)));
    assertThat(event.getRecurrence().getDaysOfWeek(),
        hasItems(DayOfWeekOccurrence.all(DayOfWeek.MONDAY),
            DayOfWeekOccurrence.all(DayOfWeek.FRIDAY)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createAWeeklyEventOnSpecificDays() {
    CalendarEvent event = aTimelyEvent().recur(
        Recurrence.every(1, TimeUnit.WEEK).on(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.WEEK)));
    assertThat(event.getRecurrence().getDaysOfWeek(),
        hasItems(DayOfWeekOccurrence.nth(1, DayOfWeek.MONDAY),
            DayOfWeekOccurrence.nth(1, DayOfWeek.FRIDAY)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createAMonthlyEventOnAllSpecificDays() {
    CalendarEvent event = aTimelyEvent().recur(Recurrence.every(1, TimeUnit.MONTH)
        .on(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.MONTH)));
    assertThat(event.getRecurrence().getDaysOfWeek(),
        hasItems(DayOfWeekOccurrence.all(DayOfWeek.TUESDAY),
            DayOfWeekOccurrence.all(DayOfWeek.THURSDAY)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createAMonthlyEventOnSpecificDayOccurrences() {
    CalendarEvent event = aTimelyEvent().recur(Recurrence.every(1, TimeUnit.MONTH)
        .on(DayOfWeekOccurrence.nth(2, DayOfWeek.TUESDAY),
            DayOfWeekOccurrence.nth(3, DayOfWeek.THURSDAY)));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.MONTH)));
    assertThat(event.getRecurrence().getDaysOfWeek(),
        hasItems(DayOfWeekOccurrence.nth(2, DayOfWeek.TUESDAY),
            DayOfWeekOccurrence.nth(3, DayOfWeek.THURSDAY)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createAYearlyEventOnAllSpecificDays() {
    CalendarEvent event = aTimelyEvent().recur(Recurrence.every(1, TimeUnit.YEAR)
        .on(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.YEAR)));
    assertThat(event.getRecurrence().getDaysOfWeek(),
        hasItems(DayOfWeekOccurrence.all(DayOfWeek.TUESDAY),
            DayOfWeekOccurrence.all(DayOfWeek.THURSDAY)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createAYearlyEventOnSpecificDayOccurrences() {
    CalendarEvent event = aTimelyEvent().recur(Recurrence.every(1, TimeUnit.YEAR)
        .on(DayOfWeekOccurrence.nth(2, DayOfWeek.TUESDAY),
            DayOfWeekOccurrence.nth(3, DayOfWeek.THURSDAY)));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.YEAR)));
    assertThat(event.getRecurrence().getDaysOfWeek(),
        hasItems(DayOfWeekOccurrence.nth(2, DayOfWeek.TUESDAY),
            DayOfWeekOccurrence.nth(3, DayOfWeek.THURSDAY)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createAWeeklyEventOnTheFirstDayOccurrence() {
    CalendarEvent event = aTimelyEvent().recur(Recurrence.every(1, TimeUnit.WEEK)
        .on(DayOfWeekOccurrence.nth(1, DayOfWeek.TUESDAY)));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.WEEK)));
    assertThat(event.getRecurrence().getDaysOfWeek(),
        hasItems(DayOfWeekOccurrence.nth(1, DayOfWeek.TUESDAY)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createAWeeklyEventOnAnotherThanFirstDayOccurrence() {
    aTimelyEvent().recur(Recurrence.every(1, TimeUnit.WEEK)
        .on(DayOfWeekOccurrence.nth(2, DayOfWeek.TUESDAY)));
  }

  @Test
  public void createARecurringEndlessEvent() {
    CalendarEvent event = anAllDayEvent().recur(Recurrence.every(1, TimeUnit.WEEK));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.WEEK)));
    assertThat(event.getRecurrence().getEndDate().isPresent(), is(false));
    assertThat(event.getRecurrence().getRecurrenceCount(), is(NO_RECURRENCE_COUNT));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createARecurringEventEndingAtGivenDate() {
    CalendarEvent event = anAllDayEvent().recur(
        Recurrence.every(1, TimeUnit.WEEK).upTo(today.plusWeeks(4)));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.WEEK)));
    assertThat(event.getRecurrence().getEndDate().isPresent(), is(true));
    assertThat(event.getRecurrence().getRecurrenceCount(), is(NO_RECURRENCE_COUNT));
    assertThat(event.getRecurrence().getEndDate().get(),
        is(today.plusWeeks(4).atStartOfDay().atOffset(ZoneOffset.UTC)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createARecurringEventEndingAtGivenDateTime() {
    CalendarEvent event = anAllDayEvent().recur(
        Recurrence.every(1, TimeUnit.WEEK).upTo(now.plusWeeks(4)));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.WEEK)));
    assertThat(event.getRecurrence().getEndDate().isPresent(), is(true));
    assertThat(event.getRecurrence().getRecurrenceCount(), is(NO_RECURRENCE_COUNT));
    assertThat(event.getRecurrence().getEndDate().get(),
        is(now.plusWeeks(4).withOffsetSameInstant(ZoneOffset.UTC)));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  @Test
  public void createARecurringEventEndingAfterGivenOccurrencesCount() {
    CalendarEvent event = anAllDayEvent().recur(
        Recurrence.every(1, TimeUnit.WEEK).upTo(10));
    assertThat(event.getRecurrence().getFrequency(), is(RecurrencePeriod.every(1, TimeUnit.WEEK)));
    assertThat(event.getRecurrence().getEndDate().isPresent(), is(false));
    assertThat(event.getRecurrence().getRecurrenceCount(), is(10));
    assertDefaultValuesOf(event);
    assertEventTimePeriodOf(event);
    assertTitleAndDescriptionOf(event);
  }

  private CalendarEvent anAllDayEvent() {
    return CalendarEvent.on(today).withTitle(EVENT_TITLE).withDescription(EVENT_DESCRIPTION);
  }

  private CalendarEvent aTimelyEvent() {
    return CalendarEvent.on(Period.between(now, after2Hours))
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION);
  }

  private void assertEventTimePeriodOf(CalendarEvent event) {
    if (event.isOnAllDay()) {
      assertThat(event.getStartDate(), is(today));
      assertThat(event.getEndDate(), is(today));
    } else {
      assertThat(event.getStartDate(), is(now.withOffsetSameInstant(ZoneOffset.UTC)));
      assertThat(event.getEndDate(), is(after2Hours.withOffsetSameInstant(ZoneOffset.UTC)));
    }
  }

  private void assertDefaultValuesOf(CalendarEvent event) {
    assertThat(event.getVisibilityLevel(), is(VisibilityLevel.PUBLIC));
    assertThat(event.getAttendees().isEmpty(), is(true));
    assertThat(event.getCategories().isEmpty(), is(true));
  }

  private void assertTitleAndDescriptionOf(CalendarEvent event) {
    assertThat(event.getTitle(), is(EVENT_TITLE));
    assertThat(event.getDescription(), is(EVENT_DESCRIPTION));
  }
}
