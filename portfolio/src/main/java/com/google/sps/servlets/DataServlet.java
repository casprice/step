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
@WebServlet("/get-comments")
public class DataServlet extends HttpServlet {

  private int maxComments = 1;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String maxCommentsString = getRequestParam(request, "max-comments");
    System.out.println("maxComments is " + maxComments);
    
    if (!maxCommentsString.equals("")) {
      maxComments = Integer.parseInt(maxCommentsString);
    }

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

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retrieve commenter name or assign name as Anonymous
    String name = getRequestParam(request, "custom");
    String body = getRequestParam(request, "text-input");
    long timestamp = System.currentTimeMillis();   

    if (name.equals("")) {
      name = "Anonymous";
    } 

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("body", body);
    commentEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    
    response.sendRedirect("/comments.html");
  }

  private String getRequestParam(HttpServletRequest request, String inputName) {
    String input = request.getParameter(inputName);

    if (input != null) {
      return input;
    }

    return "";
  }
}
