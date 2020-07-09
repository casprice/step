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
    // Handle request that lasts longer than a day.
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    
    // Builds collection of events that conflict with the requested attendees.
    ArrayList<Event> existingEvents = new ArrayList<Event>();
    for (Event event : events) {
      for (String attendee : event.getAttendees()) {
        if (request.getAttendees().contains(attendee)) {
          existingEvents.add(event);
          break;
        }
      }
    }

    // Return with the full day if none of the events conflict with the requested attendees.
    if (existingEvents.size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    
    // Create stack of the given events, merging those that overlap.
    ArrayList<TimeRange> eventTimeRanges = new ArrayList<TimeRange>();
    Iterator<Event> iterator = existingEvents.iterator();
    eventTimeRanges.add(iterator.next().getWhen());

    while (iterator.hasNext()) {
      TimeRange currentTimeRange = iterator.next().getWhen();
      TimeRange previousTimeRange = eventTimeRanges.get(eventTimeRanges.size() - 1);
      
      // If the event doesn't overlap the previous one, add it to the stack.
      if (previousTimeRange.end() < currentTimeRange.start()) {
        eventTimeRanges.add(currentTimeRange);
      }

      // Merge overlapping events.
      else if (previousTimeRange.end() < currentTimeRange.end()) {
        eventTimeRanges.remove(eventTimeRanges.size() - 1);
        eventTimeRanges.add(TimeRange.fromStartEnd(previousTimeRange.start(), 
                                                    currentTimeRange.end(), 
                                                    false));
      }
    }

    // Get the available time ranges from the merged events.
    List<TimeRange> timeRangesList = new ArrayList();
    int eventIndex = 0;
    int startTime = TimeRange.START_OF_DAY;
    int endTime = eventTimeRanges.get(eventIndex).start();
    
    while (endTime != TimeRange.END_OF_DAY) {
      if (endTime - startTime >= request.getDuration()) {
        timeRangesList.add(TimeRange.fromStartEnd(startTime, endTime, false));
      }
      
      startTime = eventTimeRanges.get(eventIndex).end();
      
      if (eventIndex >= eventTimeRanges.size() - 1) {
        break;
      }

      eventIndex++;
      endTime = eventTimeRanges.get(eventIndex).start();
    }

    if (TimeRange.END_OF_DAY - startTime >= request.getDuration()) {
      timeRangesList.add(TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true));
    }

    return timeRangesList;
  }
}
