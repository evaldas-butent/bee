package com.butent.bee.server.modules.mail;

import javax.mail.MessagingException;

@SuppressWarnings("serial")
public class ConnectionFailureException extends MessagingException {
  public ConnectionFailureException(String message) {
    super(message);
  }
}
