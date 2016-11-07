package com.butent.bee.shared.modules.cars;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Objects;

public class Option implements BeeSerializable, Comparable<Option> {

  private enum Serial {
    ID, NAME, DIMENSION, CODE, DESCRIPTION, PHOTO
  }

  private long id;
  private String name;
  private Dimension dimension;
  private String code;
  private String description;
  private Long photo;

  public Option(long id, String name, Dimension dimension) {
    this.id = id;
    this.name = Assert.notEmpty(name);
    this.dimension = Assert.notNull(dimension);
  }

  private Option() {
  }

  @Override
  public int compareTo(Option o) {
    int order = o == null ? 1 : getDimension().compareTo(o.getDimension());
    return order == 0 ? name.compareTo(o.name) : order;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case ID:
          this.id = BeeUtils.toLong(value);
          break;
        case NAME:
          this.name = value;
          break;
        case DIMENSION:
          this.dimension = Dimension.restore(value);
          break;
        case CODE:
          this.code = value;
          break;
        case DESCRIPTION:
          this.description = value;
          break;
        case PHOTO:
          this.photo = BeeUtils.toLongOrNull(value);
          break;
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return id == ((Option) o).id;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public Dimension getDimension() {
    return dimension;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Long getPhoto() {
    return photo;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public static Option restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    Option option = new Option();
    option.deserialize(s);
    return option;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case ID:
          arr[i++] = id;
          break;
        case NAME:
          arr[i++] = name;
          break;
        case DIMENSION:
          arr[i++] = dimension;
          break;
        case CODE:
          arr[i++] = code;
          break;
        case DESCRIPTION:
          arr[i++] = description;
          break;
        case PHOTO:
          arr[i++] = photo;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public Option setCode(String c) {
    this.code = c;
    return this;
  }

  public Option setDescription(String descr) {
    this.description = descr;
    return this;
  }

  public Option setPhoto(Long ph) {
    this.photo = ph;
    return this;
  }

  @Override
  public String toString() {
    return BeeUtils.joinWords(code, name);
  }
}
