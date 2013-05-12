package com.butent.bee.client.utils;

import com.google.common.base.Objects;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

import elemental.html.File;

public class NewFileInfo implements HasInfo, HasCaption {

  private final File file;

  private final String name;
  private String type;
  private final long size;
  private final DateTime lastModified;

  private DateTime fileDate = null;
  private String fileVersion = null;

  private String caption = null;
  private String description = null;

  public NewFileInfo(File file) {
    this.file = Assert.notNull(file);

    this.name = file.getName();
    this.type = file.getType();
    this.size = BeeUtils.toLong(file.getSize());

    double millis = FileUtils.getLastModifiedMillis(file);
    this.lastModified = (millis > 0) ? new DateTime(BeeUtils.toLong(millis)) : null;
  }

  public NewFileInfo(String name, long size, DateTime lastModified) {
    Assert.notEmpty(name);
    this.file = null;
    this.name = name;
    this.size = size;
    this.lastModified = lastModified;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof NewFileInfo)) {
      return false;
    } else {
      NewFileInfo other = (NewFileInfo) obj;
      return Objects.equal(name, other.name)
          && size == other.size
          && Objects.equal(lastModified, other.lastModified);
    }
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public String getDescription() {
    return description;
  }

  public File getFile() {
    return file;
  }

  public DateTime getFileDate() {
    return fileDate;
  }

  public String getFileVersion() {
    return fileVersion;
  }

  @Override
  public List<Property> getInfo() {
    return PropertyUtils.createProperties("Name", getName(),
        "Type", getType(),
        "Size", getSize(),
        "Last Modified", getLastModified(),
        "File Date", getFileDate(),
        "File Version", getFileVersion(),
        "Caption", getCaption(),
        "Description", getDescription());
  }

  public DateTime getLastModified() {
    return lastModified;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + (int) (size ^ (size >>> 32));
    return result;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setFileDate(DateTime fileDate) {
    this.fileDate = fileDate;
  }

  public void setFileVersion(String fileVersion) {
    this.fileVersion = fileVersion;
  }

  public void setType(String type) {
    this.type = type;
  }
}
