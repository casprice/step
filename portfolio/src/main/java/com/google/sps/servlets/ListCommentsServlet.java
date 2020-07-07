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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comments data */
@WebServlet("/list-comments")
public class ListCommentsServlet extends HttpServlet {

  private int maxComments = 1;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    maxComments = getMaxComments(request);

    Query query = new Query("Comment").addSort("timestamp", SortDirection.ASCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      if (comments.size() == maxComments) {
        break;
      }

      Comment comment = new Comment(entity.getKey().getId(), 
                                    (String) entity.getProperty("name"),
                                    (String) entity.getProperty("body"),
                                    (long) entity.getProperty("timestamp"));
      comments.add(comment);
    }

    Gson gson = new Gson();

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  private int getMaxComments(HttpServletRequest request) {
    // Get the input from the form.
    String maxCommentsString = request.getParameter("max-comments");

    // Convert the input into an int (default is 5).
    int maxCommentsInt = 5;

    try {
      maxCommentsInt = Integer.parseInt(maxCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + maxCommentsString);
    }
    
    return maxCommentsInt;
  }
}
