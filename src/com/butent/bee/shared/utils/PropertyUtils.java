package com.butent.bee.shared.utils;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PropertyUtils {
  public static final List<Property> EMPTY_PROPERTIES_LIST = new ArrayList<Property>();
  public static final List<ExtendedProperty> EMPTY_EXTENDED_LIST = 
    new ArrayList<ExtendedProperty>();

  public static int addChildren(Collection<ExtendedProperty> lst, String root, Object... x) {
    Assert.notNull(lst);
    Assert.notEmpty(root);

    int c = x.length;
    Assert.isTrue(c >= 2);

    int r = 0;

    for (int i = 0; i < c - 1; i += 2) {
      if (addExtended(lst, root, x[i], x[i + 1])) {
        r++;
      }
    }
    return r;
  }

  public static boolean addExtended(Collection<ExtendedProperty> lst, String nm, Object v) {
    return addExtended(lst, nm, null, v);
  }

  public static boolean addExtended(Collection<ExtendedProperty> lst, String nm, Object sub,
      Object v) {
    Assert.notNull(lst);
    if (validName(nm) && validValue(v)) {
      lst.add(new ExtendedProperty(nm, transformSub(sub), transformValue(v)));
      return true;
    } else {
      return false;
    }
  }

  public static int addProperties(Collection<ExtendedProperty> lst, boolean subMd, Object... x) {
    Assert.notNull(lst);
    int c = x.length;
    int r = 0;
    boolean ok;

    int step = subMd ? 3 : 2;
    if (c < step) {
      return r;
    }

    for (int i = 0; i <= c - step; i += step) {
      if (!(x[i] instanceof String)) {
        continue;
      }

      if (subMd) {
        ok = addExtended(lst, (String) x[i], x[i + 1], x[i + 2]);
      } else {
        ok = addExtended(lst, (String) x[i], null, x[i + 1]);
      }
      if (ok) {
        r++;
      }
    }
    return r;
  }

  public static int addProperties(Collection<Property> lst, Object... x) {
    Assert.notNull(lst);
    int c = x.length;
    Assert.parameterCount(c + 1, 3);
    int r = 0;

    if (c < 2) {
      return r;
    }

    for (int i = 0; i < c - 1; i += 2) {
      if (x[i] instanceof String && validName((String) x[i]) && validValue(x[i + 1])) {
        lst.add(new Property((String) x[i], transformValue(x[i + 1])));
        r++;
      }
    }
    return r;
  }

  public static boolean addProperty(Collection<Property> lst, String nm, Object v) {
    Assert.notNull(lst);
    if (validName(nm) && validValue(v)) {
      lst.add(new Property(nm, transformValue(v)));
      return true;
    } else {
      return false;
    }
  }
  
  public static int addSplit(Collection<ExtendedProperty> lst, String nm, Object sub,
      String v, String sep) {
    Assert.notNull(lst);
    int r = 0;
    if (validName(nm) || BeeUtils.isEmpty(v)) {
      return r;
    }

    String z;
    if (sep != null && sep.length() > 0) {
      z = sep;
    } else {
      z = BeeConst.STRING_COMMA;
    }

    if (v.contains(z)) {
      String[] arr = v.split(z);
      if (arr.length > 1 && addExtended(lst, nm, sub, BeeUtils.bracket(arr.length))) {
        r++;
      }

      for (int i = 0; i < arr.length; i++) {
        if (addExtended(lst, nm, sub, arr[i])) {
          r++;
        }
      }
    } else if (addExtended(lst, nm, sub, v)) {
      r++;
    }
    return r;
  }

  public static void appendChildrenToExtended(Collection<ExtendedProperty> dst, String root,
      Collection<Property> src) {
    Assert.notNull(dst);
    Assert.notEmpty(root);

    if (src != null && !src.isEmpty()) {
      Property el;

      for (Iterator<Property> it = src.iterator(); it.hasNext();) {
        el = it.next();
        addExtended(dst, root, el.getName(), el.getValue());
      }
    }
  }

  public static void appendChildrenToProperties(Collection<Property> dst, String root,
      Collection<Property> src) {
    Assert.notNull(dst);
    if (src != null && !src.isEmpty()) {
      if (BeeUtils.isEmpty(root)) {
        dst.addAll(src);
      } else {
        Property el;

        for (Iterator<Property> it = src.iterator(); it.hasNext();) {
          el = it.next();
          addProperty(dst, BeeUtils.concat(1, root, el.getName()), el.getValue());
        }
      }
    }
  }

  public static void appendExtended(Collection<ExtendedProperty> dst, 
      Collection<ExtendedProperty> src) {
    Assert.notNull(dst);
    if (src != null && !src.isEmpty()) {
      dst.addAll(src);
    }
  }

  public static void appendWithPrefix(Collection<ExtendedProperty> dst, String prefix,
      Collection<ExtendedProperty> src) {
    Assert.notNull(dst);
    Assert.notEmpty(prefix);

    if (src != null && !src.isEmpty()) {
      ExtendedProperty el;

      for (Iterator<ExtendedProperty> it = src.iterator(); it.hasNext();) {
        el = new ExtendedProperty(it.next());
        el.setName(BeeUtils.concat(1, prefix, el.getName()));

        dst.add(el);
      }
    }
  }
  
  public static List<Property> createProperties(Object... obj) {
    List<Property> lst = new ArrayList<Property>();
    if (obj.length > 0) {
      addProperties(lst, obj);
    }
    return lst;
  }

  public static List<Property> createProperties(String prefix, String[] values) {
    Assert.notEmpty(values);
    List<Property> lst = new ArrayList<Property>();
    
    int n = values.length;
    String name = BeeUtils.isEmpty(prefix) ? BeeConst.STRING_EMPTY :
      prefix.trim() + BeeConst.STRING_SPACE;
    
    for (int i = 0; i < n; i++) {
      addProperty(lst, name + BeeUtils.progress(i + 1, n), values[i]);
    }
    return lst;
  }

  public static String[][] extendedToArray(List<ExtendedProperty> lst) {
    Assert.notEmpty(lst);

    int r = lst.size();
    String[][] arr = new String[r][4];

    for (int i = 0; i < r; i++) {
      ExtendedProperty el = lst.get(i);

      arr[i][0] = el.getName();
      arr[i][1] = el.getSub();
      arr[i][2] = el.getValue();
      arr[i][3] = el.getDate().toLog();
    }
    return arr;
  }

  public static String[][] propertiesToArray(List<Property> lst) {
    Assert.notEmpty(lst);

    int r = lst.size();
    String[][] arr = new String[r][2];

    for (int i = 0; i < r; i++) {
      Property el = lst.get(i);

      arr[i][0] = el.getName();
      arr[i][1] = el.getValue();
    }
    return arr;
  }

  private static String transformString(String v) {
    if (v.isEmpty()) {
      return v;
    } else if (v.trim().isEmpty()) {
      if (v.length() <= 10) {
        return "[hex] " + Codec.toHex(v.toCharArray());
      } else {
        return "[whitespace] " + v.length();
      }
    } else {
      return v;
    }
  }

  private static String transformSub(Object s) {
    return transformValue(s);
  }

  private static String transformValue(Object v) {
    if (v == null) {
      return BeeConst.STRING_EMPTY;
    } else if (v instanceof String) {
      return transformString((String) v);
    } else {
      return BeeUtils.transform(v);
    }
  }

  private static boolean validName(String nm) {
    return (nm != null && !nm.isEmpty());
  }

  private static boolean validValue(Object v) {
    if (v == null) {
      return false;
    } else if (v instanceof String) {
      return !((String) v).isEmpty();
    } else {
      return true;
    }
  }
  
  private PropertyUtils() {
  }
}
