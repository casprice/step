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

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comments data */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {

  private ArrayList<String> comments = new ArrayList<String>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    String json = new Gson().toJson(comments);

    // Send the JSON as the response
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retrieve commenter name or assign name as Anonymous
    String name = getRequestParam(request, "custom");

    if (name.equals("")) {
      name = "Anonymous";
    }

    // Create comment from input name and comment body
    comments.add(name + " said: " + getRequestParam(request, "text-input"));
    
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
