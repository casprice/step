package com.google.sps.data;

/** A user's credentials. */
public final class Credentials {

  private final String nickname;
  private final String authenticationUrl;
  private final boolean isLoggedIn;

  public Credentials(String nickname, String authenticationUrl, boolean isLoggedIn) {
    this.nickname = nickname;
    this.authenticationUrl = authenticationUrl;
    this.isLoggedIn = isLoggedIn;
  }
}