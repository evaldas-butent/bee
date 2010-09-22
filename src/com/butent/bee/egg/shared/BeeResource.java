package com.butent.bee.egg.shared;

import com.butent.bee.egg.shared.BeeService.DATA_TYPE;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeResource implements BeeSerializable {
  private String name = null;
  private String uri = null;

  private String content = null;
  private BeeService.DATA_TYPE type;

  public BeeResource(String uri) {
    this.uri = uri;
  }

  public BeeResource(String uri, DATA_TYPE type) {
    this(uri, null, type);
  }

  public BeeResource(String uri, String content, DATA_TYPE type) {
    this.uri = uri;
    this.content = content;
    this.type = type;
  }

  public void deserialize(String s) {
    Assert.notNull(s);
  }

  public String getContent() {
    return content;
  }

  public String getName() {
    return name;
  }

  public BeeService.DATA_TYPE getType() {
    return type;
  }

  public String getUri() {
    return uri;
  }

  public String serialize() {
    int[] arr = new int[]{
        BeeUtils.length(name), BeeUtils.length(uri),
        BeeUtils.length(BeeService.transform(type)), BeeUtils.length(content)};

    StringBuilder head = new StringBuilder();
    int tot = 0;

    for (int i = 0; i < arr.length; i++) {
      tot += arr[i];
      head.append(BeeUtils.serializeLength(arr[i]));
    }
    
    tot += head.length();

    StringBuilder sb = new StringBuilder();
    sb.append(BeeUtils.serializeLength(tot));
    sb.append(head);
    
    if (arr[0] > 0) {
      sb.append(name);
    }
    if (arr[1] > 0) {
      sb.append(uri);
    }
    if (arr[2] > 0) {
      sb.append(BeeService.transform(type));
    }
    if (arr[3] > 0) {
      sb.append(content);
    }
    
    return sb.toString();
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(BeeService.DATA_TYPE type) {
    this.type = type;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

}
