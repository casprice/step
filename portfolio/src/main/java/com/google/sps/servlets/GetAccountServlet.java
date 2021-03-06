package com.google.sps.servlets;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Credentials;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/get-account")
public class GetAccountServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    Credentials userCredentials;
    String nickname = "Anonymous";
    String authenticationUrl;
    boolean isLoggedIn = userService.isUserLoggedIn();
    Gson gson = new Gson();
    response.setContentType("application/json;");
    
    // If user is not logged in, send back default Anonymous credentials.
    if (!isLoggedIn) {
      authenticationUrl = userService.createLoginURL("/comments.html");
      userCredentials = new Credentials(nickname, authenticationUrl, isLoggedIn);
      response.getWriter().println(gson.toJson(userCredentials));
      return;
    }

    // If user is logged in, send back their credentials.
    String email = userService.getCurrentUser().getEmail();
    nickname = email.substring(0, email.indexOf("@"));
    authenticationUrl = userService.createLogoutURL("/comments.html");
    userCredentials = new Credentials(nickname, authenticationUrl, isLoggedIn);
    response.getWriter().println(gson.toJson(userCredentials));
  }
}