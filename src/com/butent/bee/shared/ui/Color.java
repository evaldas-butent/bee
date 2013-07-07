package com.butent.bee.shared.ui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.Map;

public class Color implements BeeSerializable {

  private static final String HEX_PREFIX = "#";

  private static final Map<String, String> names = Maps.newHashMap();

  private static final Splitter rgbSplitter =
      Splitter.on(BeeConst.CHAR_COMMA).omitEmptyStrings().trimResults();
  private static final Splitter hslSplitter =
      Splitter.on(BeeConst.CHAR_COMMA).omitEmptyStrings().trimResults();

  static {
    initNames();
  }

  public static Map<String, String> getNames() {
    return names;
  }

  public static String normalize(String value) {
    if (BeeUtils.isEmpty(value)) {
      return null;
    }

    String key = BeeUtils.remove(value.trim(), BeeConst.CHAR_SPACE).toLowerCase();
    if (names.containsKey(key)) {
      return names.get(key);

    } else if (key.startsWith(HEX_PREFIX)) {
      return BeeUtils.isHexString(key.substring(1)) ? normalizeHex(key.substring(1)) : null;

    } else if (key.startsWith("rgb")) {
      List<Integer> rgb = Lists.newArrayList();

      for (String s : rgbSplitter.split(key)) {
        int v = Math.max(extract(s), 0);

        if (s.contains(BeeConst.STRING_PERCENT)) {
          rgb.add(BeeUtils.round(255 * Math.min(v, 100) / 100.0));
        } else {
          rgb.add(Math.min(v, 255));
        }
      }

      if (rgb.size() >= 3) {
        return normalize(rgb.get(0), rgb.get(1), rgb.get(2));
      } else {
        return null;
      }

    } else if (key.startsWith("hsl")) {
      List<Integer> hsl = Lists.newArrayList();

      for (String s : hslSplitter.split(key)) {
        int v = extract(s);
        if (hsl.isEmpty()) {
          hsl.add((v % 360 + 360) % 360);
        } else {
          hsl.add(BeeUtils.clamp(v, 0, 100));
        }
      }

      if (hsl.size() >= 3) {
        int[] arr = hslToRgb(hsl.get(0) / 360.0, hsl.get(1) / 100.0, hsl.get(2) / 100.0);
        return normalize(arr[0], arr[1], arr[2]);
      } else {
        return null;
      }

    } else if (BeeUtils.isHexString(key)) {
      return normalizeHex(key);

    } else {
      return null;
    }
  }

  public static Color restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    
    Color color = new Color();
    color.deserialize(s);

    return color;
  }
  
  public static boolean validate(String value) {
    return !BeeUtils.isEmpty(normalize(value));
  }

  private static int extract(String input) {
    return BeeUtils.val(input, true);
  }

  private static int[] hslToRgb(double hue, double sat, double light) {
    double m2 = (light <= 0.5) ? light * (sat + 1) : light + sat - light * sat;
    double m1 = light * 2 - m2;

    double r = hueToRgb(m1, m2, hue + 1.0 / 3);
    double g = hueToRgb(m1, m2, hue);
    double b = hueToRgb(m1, m2, hue - 1.0 / 3);

    return new int[] {BeeUtils.round(r * 255), BeeUtils.round(g * 255), BeeUtils.round(b * 255)};
  }

  private static double hueToRgb(double m1, double m2, double z) {
    double x = (z < 0) ? z + 1 : (z > 1) ? z - 1 : z;

    if (x * 6 < 1) {
      return m1 + (m2 - m1) * x * 6;
    } else if (x * 2 < 1) {
      return m2;
    } else if (x * 3 < 2) {
      return m1 + (m2 - m1) * (2.0 / 3 - x) * 6;
    } else {
      return m1;
    }
  }

  private static void initNames() {
    if (!names.isEmpty()) {
      names.clear();
    }

    names.put("aliceblue", "#F0F8FF");
    names.put("antiquewhite", "#FAEBD7");
    names.put("aqua", "#00FFFF");
    names.put("aquamarine", "#7FFFD4");
    names.put("azure", "#F0FFFF");
    names.put("beige", "#F5F5DC");
    names.put("bisque", "#FFE4C4");
    names.put("black", "#000000");
    names.put("blanchedalmond", "#FFEBCD");
    names.put("blue", "#0000FF");
    names.put("blueviolet", "#8A2BE2");
    names.put("brown", "#A52A2A");
    names.put("burlywood", "#DEB887");
    names.put("cadetblue", "#5F9EA0");
    names.put("chartreuse", "#7FFF00");
    names.put("chocolate", "#D2691E");
    names.put("coral", "#FF7F50");
    names.put("cornflowerblue", "#6495ED");
    names.put("cornsilk", "#FFF8DC");
    names.put("crimson", "#DC143C");
    names.put("cyan", "#00FFFF");
    names.put("darkblue", "#00008B");
    names.put("darkcyan", "#008B8B");
    names.put("darkgoldenrod", "#B8860B");
    names.put("darkgray", "#A9A9A9");
    names.put("darkgreen", "#006400");
    names.put("darkgrey", "#A9A9A9");
    names.put("darkkhaki", "#BDB76B");
    names.put("darkmagenta", "#8B008B");
    names.put("darkolivegreen", "#556B2F");
    names.put("darkorange", "#FF8C00");
    names.put("darkorchid", "#9932CC");
    names.put("darkred", "#8B0000");
    names.put("darksalmon", "#E9967A");
    names.put("darkseagreen", "#8FBC8F");
    names.put("darkslateblue", "#483D8B");
    names.put("darkslategray", "#2F4F4F");
    names.put("darkslategrey", "#2F4F4F");
    names.put("darkturquoise", "#00CED1");
    names.put("darkviolet", "#9400D3");
    names.put("deeppink", "#FF1493");
    names.put("deepskyblue", "#00BFFF");
    names.put("dimgray", "#696969");
    names.put("dimgrey", "#696969");
    names.put("dodgerblue", "#1E90FF");
    names.put("firebrick", "#B22222");
    names.put("floralwhite", "#FFFAF0");
    names.put("forestgreen", "#228B22");
    names.put("fuchsia", "#FF00FF");
    names.put("gainsboro", "#DCDCDC");
    names.put("ghostwhite", "#F8F8FF");
    names.put("gold", "#FFD700");
    names.put("goldenrod", "#DAA520");
    names.put("gray", "#808080");
    names.put("green", "#008000");
    names.put("greenyellow", "#ADFF2F");
    names.put("grey", "#808080");
    names.put("honeydew", "#F0FFF0");
    names.put("hotpink", "#FF69B4");
    names.put("indianred", "#CD5C5C");
    names.put("indigo", "#4B0082");
    names.put("ivory", "#FFFFF0");
    names.put("khaki", "#F0E68C");
    names.put("lavender", "#E6E6FA");
    names.put("lavenderblush", "#FFF0F5");
    names.put("lawngreen", "#7CFC00");
    names.put("lemonchiffon", "#FFFACD");
    names.put("lightblue", "#ADD8E6");
    names.put("lightcoral", "#F08080");
    names.put("lightcyan", "#E0FFFF");
    names.put("lightgoldenrodyellow", "#FAFAD2");
    names.put("lightgray", "#D3D3D3");
    names.put("lightgreen", "#90EE90");
    names.put("lightgrey", "#D3D3D3");
    names.put("lightpink", "#FFB6C1");
    names.put("lightsalmon", "#FFA07A");
    names.put("lightseagreen", "#20B2AA");
    names.put("lightskyblue", "#87CEFA");
    names.put("lightslategray", "#778899");
    names.put("lightslategrey", "#778899");
    names.put("lightsteelblue", "#B0C4DE");
    names.put("lightyellow", "#FFFFE0");
    names.put("lime", "#00FF00");
    names.put("limegreen", "#32CD32");
    names.put("linen", "#FAF0E6");
    names.put("magenta", "#FF00FF");
    names.put("maroon", "#800000");
    names.put("mediumaquamarine", "#66CDAA");
    names.put("mediumblue", "#0000CD");
    names.put("mediumorchid", "#BA55D3");
    names.put("mediumpurple", "#9370DB");
    names.put("mediumseagreen", "#3CB371");
    names.put("mediumslateblue", "#7B68EE");
    names.put("mediumspringgreen", "#00FA9A");
    names.put("mediumturquoise", "#48D1CC");
    names.put("mediumvioletred", "#C71585");
    names.put("midnightblue", "#191970");
    names.put("mintcream", "#F5FFFA");
    names.put("mistyrose", "#FFE4E1");
    names.put("moccasin", "#FFE4B5");
    names.put("navajowhite", "#FFDEAD");
    names.put("navy", "#000080");
    names.put("oldlace", "#FDF5E6");
    names.put("olive", "#808000");
    names.put("olivedrab", "#6B8E23");
    names.put("orange", "#FFA500");
    names.put("orangered", "#FF4500");
    names.put("orchid", "#DA70D6");
    names.put("palegoldenrod", "#EEE8AA");
    names.put("palegreen", "#98FB98");
    names.put("paleturquoise", "#AFEEEE");
    names.put("palevioletred", "#DB7093");
    names.put("papayawhip", "#FFEFD5");
    names.put("peachpuff", "#FFDAB9");
    names.put("peru", "#CD853F");
    names.put("pink", "#FFC0CB");
    names.put("plum", "#DDA0DD");
    names.put("powderblue", "#B0E0E6");
    names.put("purple", "#800080");
    names.put("red", "#FF0000");
    names.put("rosybrown", "#BC8F8F");
    names.put("royalblue", "#4169E1");
    names.put("saddlebrown", "#8B4513");
    names.put("salmon", "#FA8072");
    names.put("sandybrown", "#F4A460");
    names.put("seagreen", "#2E8B57");
    names.put("seashell", "#FFF5EE");
    names.put("sienna", "#A0522D");
    names.put("silver", "#C0C0C0");
    names.put("skyblue", "#87CEEB");
    names.put("slateblue", "#6A5ACD");
    names.put("slategray", "#708090");
    names.put("slategrey", "#708090");
    names.put("snow", "#FFFAFA");
    names.put("springgreen", "#00FF7F");
    names.put("steelblue", "#4682B4");
    names.put("tan", "#D2B48C");
    names.put("teal", "#008080");
    names.put("thistle", "#D8BFD8");
    names.put("tomato", "#FF6347");
    names.put("turquoise", "#40E0D0");
    names.put("violet", "#EE82EE");
    names.put("wheat", "#F5DEB3");
    names.put("white", "#FFFFFF");
    names.put("whitesmoke", "#F5F5F5");
    names.put("yellow", "#FFFF00");
    names.put("yellowgreen", "#9ACD32");
  }

  private static String normalize(int r, int g, int b) {
    return HEX_PREFIX + toHex(r) + toHex(g) + toHex(b);
  }

  private static String normalizeHex(String hex) {
    if (hex.length() == 3) {
      String r = hex.substring(0, 1).toUpperCase();
      String g = hex.substring(1, 2).toUpperCase();
      String b = hex.substring(2, 3).toUpperCase();

      return HEX_PREFIX + r + r + g + g + b + b;

    } else if (hex.length() == 6) {
      return HEX_PREFIX + hex.toUpperCase();

    } else {
      return null;
    }
  }

  private static String toHex(int value) {
    String hex = Integer.toHexString(value);
    return (hex.length() == 1) ? BeeConst.STRING_ZERO + hex : hex;
  }
  
  private long id;
  
  private String background;
  private String foreground;

  public Color(long id, String background, String foreground) {
    super();
    this.id = id;
    this.background = background;
    this.foreground = foreground;
  }

  private Color() {
    super();
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 3);
    
    setId(BeeUtils.toLong(arr[0]));
    setBackground(arr[1]);
    setForeground(arr[2]);
  }

  public String getBackground() {
    return background;
  }

  public String getForeground() {
    return foreground;
  }

  public long getId() {
    return id;
  }

  @Override
  public String serialize() {
    List<String> values = Lists.newArrayList(BeeUtils.toString(getId()), getBackground(),
        getForeground());
    return Codec.beeSerialize(values);
  }

  private void setBackground(String background) {
    this.background = background;
  }

  private void setForeground(String foreground) {
    this.foreground = foreground;
  }

  private void setId(long id) {
    this.id = id;
  }
}
