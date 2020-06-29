package com.google.sps.data;

/** A user-inputted comment. */
public final class Comment {

  private final long id;
  private final String name;
  private final String body;
  private final long timestamp;

  public Comment(long id, String name, String body, long timestamp) {
    this.id = id;
    this.name = name;
    this.body = body;
    this.timestamp = timestamp;
  }
}