package com.butent.bee.server.modules.mail;

import javax.mail.MessagingException;

public class FolderOutOfSyncException extends MessagingException {
  public FolderOutOfSyncException(String message) {
    super(message);
  }
}
