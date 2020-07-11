// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class FindMeetingQuery {
  /**
   * Returns a collection of time ranges that do not conflict with the meeting request 
   * in terms of event times and attendees. If there are optional attendees and one or 
   * more time slots exists such that both mandatory and optional attendees may attend, 
   * those time slots are returned. Otherwise, just the time slots that fit the mandatory 
   * attendees are returned.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return new ArrayList<TimeRange>();
    }
    
    // Build list of events that conflict with the mandatory requested attendees.
    ArrayList<Event> attendedEvents = 
        getEventsWithAttendees(events, request.getAttendees());

    // Build list of events that conflict only with the optional attendees.
    ArrayList<Event> attendedOptionalEvents = 
        getEventsWithAttendees(events, request.getOptionalAttendees());

    // Return with the full day if none of the events conflict with the requested attendees.
    if (attendedEvents.isEmpty() && attendedOptionalEvents.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // Separately get the time ranges of events on the calendars of the mandatory and 
    // optional attendees.
    ArrayList<TimeRange> eventTimeRanges = getEventTimeRanges(attendedEvents);
    ArrayList<TimeRange> optionalEventTimeRanges = 
                                           getEventTimeRanges(attendedOptionalEvents);

    // If we only have mandatory attendees or only have optional attendees, find and 
    // return that group's available time ranges.
    if (eventTimeRanges.isEmpty()) {
      return getAvailableTimeRanges(optionalEventTimeRanges, request.getDuration());
    }

    ArrayList<TimeRange> availableTimeRanges = 
        getAvailableTimeRanges(eventTimeRanges, request.getDuration());
    
    if (optionalEventTimeRanges.isEmpty()) {
      return availableTimeRanges;
    }

    // If we have both mandatory and optional attendees, combine the events so that
    // optional events are only retained if they don't overlap with mandatory events.
    combineTimeRanges(eventTimeRanges, optionalEventTimeRanges);

    // Find and return the combined time ranges only if there are still time ranges left.
    ArrayList<TimeRange> availableCombinedTimeRanges = 
        getAvailableTimeRanges(eventTimeRanges, request.getDuration());
    
    if (availableCombinedTimeRanges.isEmpty()) {
      // Adding optional events got rid of all possible time slots, so we want to 
      // return the available time ranges from before we combined mandatory/optional.
      return availableTimeRanges;
    }

    return availableCombinedTimeRanges;
  }

  /**
   * Build a list of events that contain the given attendees.
   */
  private ArrayList<Event> getEventsWithAttendees(Collection<Event> events,
                                                  Collection<String> attendees) {
    ArrayList<Event> attendedEvents = new ArrayList<Event>();
    for (Event event : events) {
      for (String person : event.getAttendees()) {
        if (attendees.contains(person)) {
          attendedEvents.add(event);
          break;
        }
      }
    }

    return attendedEvents;
  }

  /**
   * Returns the time ranges that a list of events take up, merging any time ranges 
   * that overlap.
   */
  private ArrayList<TimeRange> getEventTimeRanges(ArrayList<Event> attendedEvents) {
    // If there are no events, there are no time ranges to get.
    if (attendedEvents.isEmpty()) {
      return new ArrayList<TimeRange>();
    }

    ArrayList<TimeRange> eventTimeRanges = new ArrayList<TimeRange>();
    eventTimeRanges.add(attendedEvents.get(0).getWhen());

    // Iterate through each event, adding merged time ranges to eventTimeRanges. 
    for (Event event : attendedEvents) {
      TimeRange currentTimeRange = event.getWhen();
      TimeRange previousTimeRange = eventTimeRanges.get(eventTimeRanges.size() - 1);

      if (previousTimeRange.end() < currentTimeRange.start()) {
        // The event starts after the previous one ends, so add its time range to the stack.
        eventTimeRanges.add(currentTimeRange);
      } else if (previousTimeRange.end() < currentTimeRange.end()) {
        // The event ends after the previous one, so merge their time ranges.
        eventTimeRanges.remove(eventTimeRanges.size() - 1);
        eventTimeRanges.add(TimeRange.fromStartEnd(previousTimeRange.start(), 
                                                   currentTimeRange.end(), 
                                                   false));
      }
    }

    return eventTimeRanges;
  }

  /**
   * Combine the time ranges of mandatory and optional events, keeping only the optional
   * events that do not overlap with mandatory events.
   */
  private void combineTimeRanges(ArrayList<TimeRange> mandatory, ArrayList<TimeRange> optional) {
    int initialSize = mandatory.size();
    
    for (TimeRange optionalTimeRange : optional) {
      boolean overlap = false;
      for (int i = 0; i < initialSize; i++) {
        if (optionalTimeRange.overlaps(mandatory.get(i))) {
          // One overlap means we can't use this time range.
          overlap = true;
          break;
        }
      }

      if (!overlap) {
        // Only added if there was no overlap.
        mandatory.add(optionalTimeRange);
      }
    }
  }

  /**
   * Given a list of unavailable time ranges, return the list of time ranges that are 
   * available throughout the day and fit the duration of a requested meeting.
   */
  private ArrayList<TimeRange> getAvailableTimeRanges(ArrayList<TimeRange> takenTimeRanges,
                                                      long duration) {
    Collections.sort(takenTimeRanges, TimeRange.ORDER_BY_START);
    
    ArrayList<TimeRange> availableTimeRanges = new ArrayList<TimeRange>();
    int startTime = TimeRange.START_OF_DAY;
    int endTime;
    
    for (int i = 0; i < takenTimeRanges.size(); i++) {
      // Assign the next time range's end time to be this event's start time.
      endTime = takenTimeRanges.get(i).start();

      // Check that the time range to be added fits the requested duration.
      if (endTime - startTime >= duration) {
        availableTimeRanges.add(TimeRange.fromStartEnd(startTime, endTime, false));
      }
      
      // Assign the next time range's start time to be this event's end time.
      startTime = takenTimeRanges.get(i).end();
    }

    // Add the remaining portion of the day, if its duration is long enough.
    if (TimeRange.END_OF_DAY - startTime >= duration) {
      availableTimeRanges.add(TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true));
    }

    return availableTimeRanges;
  }
}
