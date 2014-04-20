package com.butent.bee.shared.export;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.css.values.FontStyle;
import com.butent.bee.shared.css.values.FontWeight;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XFont implements BeeSerializable {

  private enum Serial {
    COLOR, NAME, FACTOR, WEIGHT, STYLE
  }

  public static XFont bold() {
    XFont font = new XFont();
    font.setWeight(FontWeight.BOLD);
    return font;
  }

  public static XFont boldItalic() {
    XFont font = bold();
    font.setStyle(FontStyle.ITALIC);
    return font;
  }

  public static XFont italic() {
    XFont font = new XFont();
    font.setStyle(FontStyle.ITALIC);
    return font;
  }

  public static XFont restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    } else {
      XFont font = new XFont();
      font.deserialize(s);
      return font;
    }
  }

  private String name;
  private Double factor;
  private FontWeight weight;
  private FontStyle style;

  private String color;

  public XFont() {
    super();
  }

  public XFont copy() {
    return restore(serialize());
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      String value = arr[i];
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      switch (members[i]) {
        case COLOR:
          setColor(value);
          break;
        case FACTOR:
          setFactor(BeeUtils.toDoubleOrNull(value));
          break;
        case NAME:
          setName(value);
          break;
        case STYLE:
          setStyle(Codec.unpack(FontStyle.class, value));
          break;
        case WEIGHT:
          setWeight(Codec.unpack(FontWeight.class, value));
          break;
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (getClass() != obj.getClass()) {
      return false;
    }

    XFont other = (XFont) obj;

    return Objects.equals(color, other.color)
        && Objects.equals(factor, other.factor)
        && Objects.equals(name, other.name)
        && style == other.style
        && weight == other.weight;
  }

  public String getColor() {
    return color;
  }

  public Double getFactor() {
    return factor;
  }

  public String getName() {
    return name;
  }

  public FontStyle getStyle() {
    return style;
  }

  public FontWeight getWeight() {
    return weight;
  }

  @Override
  public int hashCode() {
    return Objects.hash(color, factor, name, style, weight);
  }

  public XFont merge(XFont other) {
    XFont result = copy();
    if (other == null) {
      return result;
    }

    if (BeeUtils.isEmpty(getColor()) && !BeeUtils.isEmpty(other.getColor())) {
      result.setColor(other.getColor());
    }

    if (BeeUtils.isEmpty(getName()) && !BeeUtils.isEmpty(other.getName())) {
      result.setName(other.getName());
    }
    if (!BeeUtils.isPositive(getFactor()) && BeeUtils.isPositive(other.getFactor())) {
      result.setFactor(other.getFactor());
    }
    if (getWeight() == null && other.getWeight() != null) {
      result.setWeight(other.getWeight());
    }
    if (getStyle() == null && other.getStyle() != null) {
      result.setStyle(other.getStyle());
    }

    return result;
  }

  @Override
  public String serialize() {
    List<String> values = new ArrayList<>();

    for (Serial member : Serial.values()) {
      switch (member) {
        case COLOR:
          values.add(getColor());
          break;

        case FACTOR:
          values.add(BeeUtils.isDouble(getFactor()) ? BeeUtils.toString(getFactor()) : null);
          break;
        case NAME:
          values.add(getName());
          break;
        case STYLE:
          values.add(Codec.pack(getStyle()));
          break;
        case WEIGHT:
          values.add(Codec.pack(getWeight()));
          break;
      }
    }

    return Codec.beeSerialize(values);
  }

  public void setColor(String color) {
    this.color = color;
  }

  public void setFactor(Double factor) {
    this.factor = factor;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setStyle(FontStyle style) {
    this.style = style;
  }

  public void setWeight(FontWeight weight) {
    this.weight = weight;
  }
}
