package com.butent.bee.shared.communication;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

/**
 * Used to tansport data with messages between various layers.
 */

public class ResponseObject implements BeeSerializable {

  private enum SerializationMember {
    MESSAGES, RESPONSE_TYPE, RESPONSE
  }

  public static ResponseObject error(Object... err) {
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
    SerializationMember[] members = SerializationMember.values();
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMember member = members[i];
      String value = arr[i];

      switch (member) {
        case MESSAGES:
          messages.clear();
          String[] cArr = Codec.beeDeserialize(value);

          for (String msg : cArr) {
            messages.add(new ResponseMessage(msg, true));
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

  public ResponseMessage[] getMessages() {
    return messages.toArray(new ResponseMessage[0]);
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

  public boolean hasResponse() {
    return hasResponse(null);
  }

  public boolean hasResponse(Class<?> clazz) {
    boolean ok = response != null;

    if (ok && clazz != null) {
      ok = BeeUtils.same(getType(), BeeUtils.getClassName(clazz));
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
    SerializationMember[] members = SerializationMember.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (SerializationMember member : members) {
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
    return Codec.beeSerializeAll(arr);
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
      if (lvl == null || BeeUtils.equals(message.getLevel(), lvl)) {
        return true;
      }
    }
    return false;
  }
}
