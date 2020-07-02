package com.google.sps.data;

/** A user's credentials. */
public final class Credentials {

  private final String nickname;
  private final String authUrl;
  private final boolean loggedIn;

  public Credentials(String nickname, String authUrl, boolean loggedIn) {
    this.nickname = nickname;
    this.authUrl = authUrl;
    this.loggedIn = loggedIn;
  }
}