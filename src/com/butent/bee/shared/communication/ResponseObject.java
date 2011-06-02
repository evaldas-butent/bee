package com.butent.bee.shared.communication;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

/**
 * Enables to use bundles of response messages in one object.
 */

public class ResponseObject {

  public static ResponseObject error(Object... err) {
    return new ResponseObject().addError(err);
  }

  public static ResponseObject info(Object... obj) {
    return new ResponseObject().addInfo(obj);
  }

  public static ResponseObject response(Object response) {
    return new ResponseObject().setResponse(response);
  }

  public static ResponseObject warning(Object... obj) {
    return new ResponseObject().addWarning(obj);
  }

  private Collection<ResponseMessage> messages = Lists.newArrayList();
  private Object response = null;
  private String type = null;

  public ResponseObject addError(Object... err) {
    messages.add(new ResponseMessage(Level.SEVERE, BeeUtils.concat(1, err)));
    return this;
  }

  public ResponseObject addError(Throwable err) {
    addError(err.toString());
    return this;
  }

  public ResponseObject addInfo(Object... obj) {
    messages.add(new ResponseMessage(Level.INFO, BeeUtils.concat(1, obj)));
    return this;
  }

  public ResponseObject addWarning(Object... obj) {
    messages.add(new ResponseMessage(Level.WARNING, BeeUtils.concat(1, obj)));
    return this;
  }

  public String[] getErrors() {
    return getMessageArray(Level.SEVERE);
  }

  public Collection<ResponseMessage> getMessages() {
    return messages;
  }

  public String[] getNotifications() {
    return getMessageArray(Level.INFO);
  }

  public Object getResponse() {
    return response;
  }

  public String getType() {
    return type;
  }

  public String[] getWarnings() {
    return getMessageArray(Level.WARNING);
  }

  public boolean hasErrors() {
    return hasMessages(Level.SEVERE);
  }

  public boolean hasNotifications() {
    return hasMessages(Level.INFO);
  }

  public boolean hasWarnings() {
    return hasMessages(Level.WARNING);
  }

  public ResponseObject setResponse(Object response) {
    this.response = response;
    return this;
  }

  public ResponseObject setType(Class<?> clazz) {
    Assert.notNull(clazz);
    return setType(BeeUtils.getClassName(clazz));
  }
  
  public ResponseObject setType(String type) {
    this.type = type;
    return this;
  }

  private String[] getMessageArray(Level lvl) {
    List<String> msgs = Lists.newArrayList();

    for (ResponseMessage message : messages) {
      if (BeeUtils.equals(message.getLevel(), lvl)) {
        msgs.add(message.getMessage());
      }
    }
    return msgs.toArray(new String[0]);
  }

  private boolean hasMessages(Level lvl) {
    for (ResponseMessage message : messages) {
      if (BeeUtils.equals(message.getLevel(), lvl)) {
        return true;
      }
    }
    return false;
  }
}
