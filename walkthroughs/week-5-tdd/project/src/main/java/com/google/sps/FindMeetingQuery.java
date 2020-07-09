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
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public final class FindMeetingQuery {
  /**
   * Takes a collection of Events and a meeting request, and returns a collection of 
   * time ranges that do not conflict with the meeting request in terms of event 
   * times and attendees.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> eventTimeRanges = new ArrayList<TimeRange>();
    ArrayList<TimeRange> availableTimeRanges = new ArrayList<TimeRange>();
    
    // Handle request that lasts longer than a day.
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    
    // Build collections of events that conflict with the requested attendees and the
    // optional attendees.
    ArrayList<Event> attendedEvents = 
        getEventsWithAttendees(request.getAttendees(), events);
    ArrayList<Event> attendedOptionalEvents = 
        getEventsWithAttendees(request.getOptionalAttendees(), events);

    // Return with the full day if none of the events conflict with the requested attendees.
    if (attendedEvents.size() == 0 && attendedOptionalEvents.size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    
    eventTimeRanges = getEventTimeRanges(attendedEvents);

    availableTimeRanges = getAvailableTimeRanges(eventTimeRanges, request.getDuration());
    
    return availableTimeRanges;
  }

  /**
   * Build a list of events that contain the given attendees.
   */
  private ArrayList<Event> getEventsWithAttendees(Collection<String> attendees, 
                                                  Collection<Event> events) {
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
   * Given a list of events, merge any events that overlap and return the time
   * ranges of the newly merged events.
   */
  private ArrayList<TimeRange> getEventTimeRanges(ArrayList<Event> attendedEvents) {
    ArrayList<TimeRange> eventTimeRanges = new ArrayList<TimeRange>();
    if (attendedEvents.size() == 0) {
      return eventTimeRanges;
    }

    eventTimeRanges.add(attendedEvents.get(0).getWhen());

    // Get the time ranges 
    for (Event event : attendedEvents) {
      TimeRange currentTimeRange = event.getWhen();
      TimeRange previousTimeRange = eventTimeRanges.get(eventTimeRanges.size() - 1);
      
      // If the event doesn't overlap the previous one, add its time range to the stack.
      if (previousTimeRange.end() < currentTimeRange.start()) {
        eventTimeRanges.add(currentTimeRange);
      }

      // If two events overlap in time ranges, merge their time ranges.
      else if (previousTimeRange.end() < currentTimeRange.end()) {
        eventTimeRanges.remove(eventTimeRanges.size() - 1);
        eventTimeRanges.add(TimeRange.fromStartEnd(previousTimeRange.start(), 
                                                    currentTimeRange.end(), 
                                                    false));
      }
    }

    return eventTimeRanges;
  }

  /**
   * Given a list of unavailable time ranges, return the list of time ranges that are 
   * available throughout the day and fit the duration of a requested meeting.
   */
  private ArrayList<TimeRange> getAvailableTimeRanges(ArrayList<TimeRange> takenTimeRanges,
                                                      long duration) {
    ArrayList<TimeRange> availableTimeRanges = new ArrayList<TimeRange>();
    
    // Get the available time ranges from the merged events.
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
