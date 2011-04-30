package com.butent.bee.server.communication;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * Enables server response buffer's contents management.
 */

public class ResponseBuffer {
  private char[] separator;
  private StringBuilder buffer = new StringBuilder();
  private int count = 0;

  private int columnCount = 0;

  private List<ResponseMessage> messages = new ArrayList<ResponseMessage>();
  private List<BeeResource> parts = new ArrayList<BeeResource>();

  private ContentType contentType;

  private String mediaType = null;
  private String characterEncoding = null;

  public ResponseBuffer() {
    setDefaultSeparator();
  }

  public ResponseBuffer(char sep) {
    this.separator = new char[] {sep};
  }

  public ResponseBuffer(ContentType contentType) {
    this.contentType = contentType;
  }

  public ResponseBuffer(String sep) {
    if (BeeUtils.isHexString(sep)) {
      setHexSeparator(sep);
    } else {
      setDefaultSeparator();
    }
  }

  public void add(CharSequence s) {
    if (s != null && s.length() > 0) {
      checkSeparator(s);
      buffer.append(s);
    }

    addSeparator();
  }

  public void add(Collection<?> lst) {
    if (BeeUtils.isEmpty(lst)) {
      addSeparator();
    } else {
      for (Iterator<?> it = lst.iterator(); it.hasNext();) {
        add(it.next());
      }
    }
  }

  public void add(Object x) {
    add(BeeUtils.transform(x));
  }

  public void add(Object... lst) {
    if (lst.length == 0) {
      addSeparator();
    } else {
      for (int i = 0; i < lst.length; i++) {
        add(lst[i]);
      }
    }
  }

  public void addBinary(CharSequence s) {
    if (s == null || s.length() <= 0) {
      return;
    }

    buffer.append(s);
    count++;

    setContentType(ContentType.BINARY);
  }

  public void addColumn(BeeColumn col) {
    Assert.notEmpty(col);

    add(col.serialize());
    setColumnCount(getColumnCount() + 1);
  }

  public void addColumns(BeeColumn... cols) {
    Assert.isPositive(cols.length);

    for (BeeColumn col : cols) {
      addColumn(col);
    }
  }

  public void addError(Throwable err) {
    messages.add(new ResponseMessage(Level.SEVERE, err.toString()));
  }

  public void addErrors(List<? extends Throwable> lst) {
    for (Throwable err : lst) {
      addError(err);
    }
  }

  public void addExtended(ExtendedProperty el) {
    Assert.notEmpty(el);

    add(el.getName());
    add(el.getSub());
    add(el.getValue());
    add(el.getDate().toTimeString());
  }

  public void addExtendedProperties(Collection<ExtendedProperty> lst, String... cap) {
    Assert.notEmpty(lst);
    addExtendedPropertiesColumns(cap);

    for (ExtendedProperty el : lst) {
      addExtended(el);
    }
  }

  public void addExtendedPropertiesColumns(String... cap) {
    int c = cap.length;
    String nm;

    for (int i = 0; i < ExtendedProperty.COLUMN_COUNT; i++) {
      if (c > i && !BeeUtils.isEmpty(cap[i])) {
        nm = cap[i].trim();
      } else {
        nm = ExtendedProperty.COLUMN_HEADERS[i];
      }
      addColumn(new BeeColumn(nm));
    }
  }

  public void addLine(Object... obj) {
    if (obj.length > 0) {
      add(BeeUtils.concat(1, obj));
    }
  }

  public void addMessage(Object... obj) {
    messages.add(new ResponseMessage(BeeUtils.concat(1, obj)));
  }

  public void addNow(Object... obj) {
    messages.add(new ResponseMessage(true, BeeUtils.concat(1, obj)));
  }

  public void addOff(Object... obj) {
    messages.add(new ResponseMessage(Level.OFF, BeeUtils.concat(1, obj)));
  }

  public void addPart(String uri, String content) {
    addPart(uri, content, null, false);
  }

  public void addPart(String uri, String content, ContentType type) {
    addPart(uri, content, type, false);
  }

  public void addPart(String uri, String content, ContentType type, boolean readOnly) {
    Assert.notNull(content);
    parts.add(new BeeResource(uri, content, type, readOnly));
    setContentType(ContentType.MULTIPART);
  }

  public void addProperties(Collection<Property> lst, String... cap) {
    Assert.notEmpty(lst);
    if (getColumnCount() == 0) {
      addPropertiesColumns(cap);
    }

    for (Property el : lst) {
      addProperty(el);
    }
  }

  public void addPropertiesColumns(String... cap) {
    int c = (cap == null) ? 0 : cap.length;
    String nm;

    for (int i = 0; i < Property.HEADER_COUNT + 1; i++) {
      if (c > i && !BeeUtils.isEmpty(cap[i])) {
        nm = cap[i].trim();
      } else if (i < Property.HEADER_COUNT) {
        nm = Property.HEADERS[i];
      } else {
        nm = "Date";
      }
      addColumn(new BeeColumn(nm));
    }
  }

  public void addProperty(Property el) {
    Assert.notEmpty(el);

    add(el.getName());
    add(el.getValue());
    add(new DateTime().toTimeString());
  }

  public void addResource(String content) {
    addResource(null, content, null, true);
  }

  public void addResource(String content, ContentType type) {
    addResource(null, content, type, true);
  }

  public void addResource(String uri, String content) {
    addResource(uri, content, null, false);
  }

  public void addResource(String uri, String content, ContentType type) {
    addResource(uri, content, type, false);
  }

  public void addResource(String uri, String content, ContentType type, boolean readOnly) {
    Assert.notNull(content);
    buffer.append(new BeeResource(uri, content, type, readOnly).serialize());
    count++;
    setContentType(ContentType.RESOURCE);
  }

  public void addSeparator() {
    buffer.append(separator);
    count++;
  }

  public void addSevere(Object... obj) {
    messages.add(new ResponseMessage(Level.SEVERE, BeeUtils.concat(1, obj)));
  }

  public void addWarning(Object... obj) {
    messages.add(new ResponseMessage(Level.WARNING, BeeUtils.concat(1, obj)));
  }

  public void addWarning(Throwable err) {
    messages.add(new ResponseMessage(Level.WARNING, err.toString()));
  }

  public void addWarnings(List<?> lst) {
    for (Object w : lst) {
      addWarning(w);
    }
  }

  public void appendExtended(Collection<ExtendedProperty> lst) {
    Assert.notEmpty(lst);
    for (ExtendedProperty el : lst) {
      addExtended(el);
    }
  }

  public void appendProperties(String root, Collection<Property> lst) {
    Assert.notEmpty(root);
    Assert.notEmpty(lst);

    for (Property el : lst) {
      add(root);
      add(el.getName());
      add(el.getValue());
      add(new DateTime().toTimeString());
    }
  }

  public void build(Object... obj) {
    if (obj == null || obj.length == 0) {
      return;
    }
    for (Object z : obj) {
      add(z);
    }
  }

  public void clearData() {
    setBuffer(new StringBuilder());
    setCount(0);
    setColumnCount(0);
    setDefaultSeparator();
  }

  public StringBuilder getBuffer() {
    return buffer;
  }

  public String getCharacterEncoding() {
    return characterEncoding;
  }

  public int getColumnCount() {
    return columnCount;
  }

  public ContentType getContentType() {
    return contentType;
  }

  public int getCount() {
    return count;
  }

  public String getHexSeparator() {
    return Codec.toHex(getSeparator());
  }

  public String getMediaType() {
    return mediaType;
  }

  public ResponseMessage getMessage(int i) {
    return messages.get(i);
  }

  public int getMessageCount() {
    return messages.size();
  }

  public List<ResponseMessage> getMessages() {
    return messages;
  }

  public int getPartCount() {
    return parts.size();
  }

  public List<BeeResource> getParts() {
    return parts;
  }

  public char[] getSeparator() {
    return separator;
  }

  public int getSize() {
    return buffer.length();
  }

  public String getString() {
    return buffer.toString();
  }

  public boolean isDefaultSeparator() {
    return (separator != null && separator.length == 1 && separator[0] == CommUtils.DEFAULT_INFORMATION_SEPARATOR);
  }

  public String now() {
    return new DateTime().toTimeString();
  }

  public void setBuffer(StringBuilder buffer) {
    this.buffer = buffer;
  }

  public void setCharacterEncoding(String characterEncoding) {
    this.characterEncoding = characterEncoding;
  }

  public void setColumnCount(int columnCount) {
    this.columnCount = columnCount;
  }

  public void setContentType(ContentType contentType) {
    this.contentType = contentType;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public void setHexSeparator(String sep) {
    if (BeeUtils.isHexString(sep)) {
      this.separator = BeeUtils.fromHex(sep);
    }
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public void setMessages(List<ResponseMessage> messages) {
    this.messages = messages;
  }

  public void setParts(List<BeeResource> parts) {
    this.parts = parts;
  }

  public void setSeparator(char[] separator) {
    this.separator = separator;
  }

  private void checkSeparator(CharSequence s) {
    if (!containsSeparator(s, separator)) {
      return;
    }

    char[] newSep = nextSeparator(s);
    updateSeparator(newSep);
  }

  private boolean containsSeparator(CharSequence src, char[] sep) {
    boolean ok = false;
    if (src == null || src.length() == 0 || sep == null || sep.length == 0) {
      return ok;
    }

    int lenSrc = src.length();
    int lenSep = sep.length;

    if (lenSep == 1) {
      for (int i = 0; i < lenSrc; i++) {
        if (src.charAt(i) == sep[0]) {
          ok = true;
          break;
        }
      }
    } else if (lenSrc >= lenSep) {
      for (int i = 0; i <= lenSrc - lenSep; i++) {
        for (int j = 0; j < lenSep; j++) {
          if (src.charAt(i + j) == sep[j]) {
            ok = true;
          } else {
            ok = false;
            break;
          }
        }
        if (ok) {
          break;
        }
      }
    }

    return ok;
  }

  private char[] nextSeparator(CharSequence s) {
    if (separator == null || separator.length == 0) {
      return null;
    }

    int n = separator.length;
    char[] newSep = new char[n];
    System.arraycopy(separator, 0, newSep, 0, n);

    while (containsSeparator(buffer, newSep) || containsSeparator(s, newSep)) {
      if (newSep[n - 1] < Character.MAX_VALUE) {
        newSep[n - 1]++;
      } else {
        char[] arr = new char[n + 1];
        System.arraycopy(newSep, 0, arr, 0, n);
        arr[n] = Character.MIN_VALUE;
        newSep = arr;
      }
    }

    return newSep;
  }

  private void setDefaultSeparator() {
    separator = new char[] {CommUtils.DEFAULT_INFORMATION_SEPARATOR};
  }

  private void updateSeparator(char[] newSep) {
    if (newSep == null || newSep.length == 0) {
      return;
    }

    if (count > 0 && separator != null && separator.length > 0) {
      if (separator.length == 1 && newSep.length == 1) {
        for (int i = 0; i < buffer.length(); i++) {
          if (buffer.charAt(i) == separator[0]) {
            buffer.setCharAt(i, newSep[0]);
          }
        }
      } else {
        String s = buffer.toString().replace(new String(separator),
            new String(newSep));
        setBuffer(new StringBuilder(s));
      }
    }

    setSeparator(newSep);
  }

}
