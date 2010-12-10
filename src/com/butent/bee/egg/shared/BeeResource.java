package com.butent.bee.egg.shared;

import com.butent.bee.egg.shared.communication.CommUtils;
import com.butent.bee.egg.shared.communication.ContentType;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.Codec;

public class BeeResource implements BeeSerializable {
  private String name = null;
  private String uri = null;
  private boolean readOnly = false;

  private String content = null;
  private ContentType type = null;

  public BeeResource() {
  }
  
  public BeeResource(String src) {
    deserialize(src);
  }

  public BeeResource(String uri, ContentType type) {
    this(uri, null, type, false);
  }

  public BeeResource(String uri, String content) {
    this(uri, content, null, false);
  }
  
  public BeeResource(String uri, String content, boolean readOnly) {
    this(uri, content, null, readOnly);
  }

  public BeeResource(String uri, String content, ContentType type) {
    this(uri, content, type, false);
  }

  public BeeResource(String uri, String content, ContentType type, boolean readOnly) {
    this.uri = uri;
    this.content = content;
    this.type = type;
    this.readOnly = readOnly;
  }
  
  public void deserialize(String src) {
    Assert.notNull(src);
    
    Pair<Integer, Integer> scan;
    int len, start = 0;
    
    for (int i = 0; i < 5; i++) {
      scan = Codec.deserializeLength(src, start);
      len = scan.getA();
      start += scan.getB();
      
      if (len <= 0) {
        continue;
      }
      String v = src.substring(start, start + len);
      
      switch (i) {
        case 0:
          setName(v);
          break;
        case 1:
          setUri(v);
          break;
        case 2:
          setType(CommUtils.getContentType(v));
          break;
        case 3:
          setReadOnly(BeeUtils.toBoolean(v));
          break;
        case 4:
          setContent(v);
          break;
      }
      
      start += len;
    }
  }

  public String getContent() {
    return content;
  }

  public String getName() {
    return name;
  }

  public ContentType getType() {
    return type;
  }

  public String getUri() {
    return uri;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public String serialize() {
    int[] arr = new int[]{
        BeeUtils.length(name), BeeUtils.length(uri),
        BeeUtils.length(BeeUtils.transform(type)),
        BeeUtils.length(BeeUtils.toString(readOnly)),
        BeeUtils.length(content)};

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < arr.length; i++) {
      sb.append(Codec.serializeLength(arr[i]));
      if (arr[i] <= 0) {
        continue;
      }

      switch (i) {
        case 0:
          sb.append(name);
          break;
        case 1:
          sb.append(uri);
          break;
        case 2:
          sb.append(BeeUtils.transform(type));
          break;
        case 3:
          sb.append(BeeUtils.toString(readOnly));
          break;
        case 4:
          sb.append(content);
          break;
      }
    }

    return sb.toString();
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setType(ContentType type) {
    this.type = type;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}
