package com.butent.bee.shared.communication;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to transport data with messages between various layers.
 */
public class ResponseObject implements BeeSerializable {

  /**
   * Contains a list of serializable members of response object.
   */

  private enum Serial {
    MESSAGES, RESPONSE_TYPE, RESPONSE
  }

  public static ResponseObject error(Object... err) {
    return new ResponseObject().addError(err);
  }

  public static ResponseObject error(Throwable err) {
    return new ResponseObject().addError(err);
  }

  public static ResponseObject info(Object... obj) {
    return new ResponseObject().addInfo(obj);
  }

  public static ResponseObject response(Object response) {
    return new ResponseObject().setResponse(response);
  }

  public static ResponseObject restore(String s) {
    ResponseObject response = new ResponseObject();
    response.deserialize(s);
    return response;
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

  @Override
  public void deserialize(String s) {
    Serial[] members = Serial.values();
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case MESSAGES:
          messages.clear();
          String[] cArr = Codec.beeDeserializeCollection(value);

          if (!BeeUtils.isEmpty(cArr)) {
            for (String msg : cArr) {
              messages.add(new ResponseMessage(msg, true));
            }
          }
          break;

        case RESPONSE_TYPE:
          this.type = value;
          break;

        case RESPONSE:
          this.response = value;
          break;
      }
    }
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

  @SuppressWarnings("unchecked")
  public <T> T getResponse(T def, Logger logger) {
    Assert.notNull(def);
    T res = def;

    if (!hasErrors()) {
      if (hasResponse(def.getClass())) {
        res = (T) getResponse();

      } else if (logger != null) {
        LogUtils.warning(logger,
            "Requested response type:", BeeUtils.bracket(def.getClass()),
            "got:", BeeUtils.bracket(getResponse().getClass()));
      }
    }
    if (logger != null) {
      for (ResponseMessage message : getMessages()) {
        Level lvl = message.getLevel();
        String msg = message.getMessage();

        if (lvl == Level.SEVERE) {
          LogUtils.severe(logger, msg);
        } else if (lvl == Level.WARNING) {
          LogUtils.warning(logger, msg);
        } else {
          LogUtils.info(logger, msg);
        }
      }
    }
    return res;
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

  public boolean hasMessages() {
    return !messages.isEmpty();
  }

  public boolean hasNotifications() {
    return hasMessages(Level.INFO);
  }

  public boolean hasResponse() {
    return hasResponse(null);
  }

  public boolean hasResponse(Class<?> clazz) {
    boolean ok = response != null;

    if (ok && clazz != null) {
      ok = BeeUtils.same(getType(), NameUtils.getClassName(clazz));
    }
    return ok;
  }

  public boolean hasWarnings() {
    return hasMessages(Level.WARNING);
  }

  public boolean isEmpty() {
    return !hasMessages(null) && !hasResponse();
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case MESSAGES:
          arr[i++] = messages;
          break;

        case RESPONSE_TYPE:
          arr[i++] = type;
          break;

        case RESPONSE:
          arr[i++] = response;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public ResponseObject setResponse(Object response) {
    if (response != null) {
      setType(response.getClass());
    }
    this.response = response;
    return this;
  }

  public ResponseObject setType(Class<?> clazz) {
    Assert.notNull(clazz);
    return setType(NameUtils.getClassName(clazz));
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
      if (lvl == null || BeeUtils.equals(message.getLevel(), lvl)) {
        return true;
      }
    }
    return false;
  }
}
