package com.butent.bee.client.utils;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class FileUtils {
  
  public static class FileInfo {
    private final JavaScriptObject file;
    private final String name;
    private final String type;
    private final long size;
    private final DateTime lastModifiedDate;

    private FileInfo(JavaScriptObject file, String name, String type, long size,
        DateTime lastModifiedDate) {
      this.file = file;
      this.name = name;
      this.type = type;
      this.size = size;
      this.lastModifiedDate = lastModifiedDate;
    }

    public JavaScriptObject getFile() {
      return file;
    }

    public DateTime getLastModifiedDate() {
      return lastModifiedDate;
    }

    public String getName() {
      return name;
    }

    public long getSize() {
      return size;
    }

    public String getType() {
      return type;
    }
  }

  public static List<FileInfo> getFileInfo(JavaScriptObject obj) {
    Assert.notNull(obj);
    List<FileInfo> result = Lists.newArrayList();
    
    JsArrayMixed arr = getFileInfoArray(obj);
    if (arr == null || arr.length() <= 0) {
      return result;
    }
    
    int c = 5;
    for (int i = 0; i < arr.length() / c; i++) {
      result.add(new FileInfo(arr.getObject(i * c),
          arr.getString(i * c + 1),
          arr.getString(i * c + 2),
          BeeUtils.toLong(arr.getString(i * c + 3)),
          DateTime.restore(arr.getString(i * c + 4))));
    }
    return result;
  }
  
  public static List<FileInfo> getFileInfo(UIObject uiObj) {
    Assert.notNull(uiObj);
    return getFileInfo(uiObj.getElement());
  }

  private static native JsArrayMixed getFileInfoArray(JavaScriptObject obj) /*-{
    var arr = new Array();
    if (obj.files) {
      var files = obj.files;

      for (var i = 0; i < files.length; i++) {
        var file = files[i];
        if (file) {
          arr.push(file, String(file.name), String(file.type), String(file.size),
              String(file.lastModifiedDate.getTime()));
        }
      }
    }
    return arr;
  }-*/;

  private FileUtils() {
  }
}
