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

  /**
   * Adds children to the specified {@code lst}. {@code root} is the name and {@code x} the sub and
   * value of the new children.
   * 
   * @param lst a collection to add children to
   * @param root a name for the children
   * @param x the children's Sub and Value values coupled by 2
   * @return how many children were added to the collection.
   */
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

  /**
   * Adds an ExtendedProperty to {@code lst} with specified {@code nm} and {@code v} values.
   * 
   * @param lst a collection to add the new ExtendedProperty to
   * @param nm the name for the ExtendedProperty
   * @param v the value for the ExtendedProperty
   * @return true, if the new ExtendedProperty was valid and was added to the collection, otherwise
   *         false.
   */
  public static boolean addExtended(Collection<ExtendedProperty> lst, String nm, Object v) {
    return addExtended(lst, nm, null, v);
  }

  /**
   * Adds an ExtendedProperty to {@code lst} with specified {@code nm}, {@code sub} and {@code v}.
   * values
   * 
   * @param lst a collection to add the new ExtendedProperty to
   * @param nm the name for the ExtendedProperty
   * @param sub the sub for the ExtendedProperty
   * @param v the value for the ExtendedProperty
   * @return true, if the new ExtendedProperty was valid and was added to the collection, otherwise
   *         false.
   */
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

  /**
   * Adds a non empty Property to the specified collection {@code lst}.
   * 
   * @param lst a collection to add the new Property to
   * @param x the name and value added in pairs
   * @return the amount of added Properties.
   */
  public static int addNotEemptyProperties(Collection<Property> lst, Object... x) {
    Assert.notNull(lst);
    int c = x.length;
    Assert.parameterCount(c + 1, 3);
    int r = 0;

    for (int i = 0; i < c - 1; i += 2) {
      if (x[i] instanceof String && validName((String) x[i]) && !BeeUtils.isEmpty(x[i + 1])) {
        lst.add(new Property((String) x[i], transformValue(x[i + 1])));
        r++;
      }
    }
    return r;
  }

  /**
   * Adds valid ExtendedProperties to the specified collection {@code lst}. If {@code subMd} is set
   * to {@code false} only name and value are required. If {@code subMd} is set to {@code true} then
   * name, sub and value are required.
   * 
   * @param lst a collection to add the new ExtendedProperty to
   * @param subMd the value sets if sub is required
   * @param x the ExtendedProperty values in couples or in threes.
   * @return the amount of added ExtendedProperties.
   */
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

  /**
   * Adds valid Properties to the specified collection {@code lst}.
   * 
   * @param lst a collection to add the new Properties to
   * @param x values in pairs of the new Properties to be added
   * @return amount of Properties added to the list.
   */
  public static int addProperties(Collection<Property> lst, Object... x) {
    Assert.notNull(lst);
    int c = x.length;
    Assert.parameterCount(c + 1, 3);
    int r = 0;

    for (int i = 0; i < c - 1; i += 2) {
      if (x[i] instanceof String && validName((String) x[i]) && validValue(x[i + 1])) {
        lst.add(new Property((String) x[i], transformValue(x[i + 1])));
        r++;
      }
    }
    return r;
  }

  /**
   * Adds a valid Property to the collection {@code lst}.
   * 
   * @param lst lst a collection to add the new Property to
   * @param nm name of the Property
   * @param v value of the Property
   * @return true if the Property was succesfully added to the list, otherwise false.
   */
  public static boolean addProperty(Collection<Property> lst, String nm, Object v) {
    Assert.notNull(lst);
    if (validName(nm) && validValue(v)) {
      lst.add(new Property(nm, transformValue(v)));
      return true;
    } else {
      return false;
    }
  }

  /**
   * Adds ExtendedProperties to the specified collection {@code lst}. ExtendedProperties contain a
   * name {@code nm}, sub {@code sub} and values split by separator {@code sep}.
   * <p>
   * E.g {@code nm = "name", sub ="sub", v = "value1, value2", sep = ","} adds two
   * ExtendedProperties containing: {"name","sub","value1"} and { "name","sub","value2"}.
   * 
   * @param lst the collection to add ExtendedProperties to
   * @param nm name of new ExtendedProperties to add
   * @param sub the sub of new ExtendedProperties to add
   * @param v values for splitting and creating ExtendedProperties
   * @param sep the separator used for splitting
   * @return the amount of ExtendedPropertied added to the collection
   */
  public static int addSplit(Collection<ExtendedProperty> lst, String nm, Object sub,
      String v, String sep) {
    Assert.notNull(lst);
    int r = 0;
    if (!validName(nm) || BeeUtils.isEmpty(v)) {
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

  /**
   * Appends {@code src} elements to the collection {@code dst}. {@code root} is used as a name in
   * {@code dst} and the {@code src} name is transformed to sub.
   * 
   * @param dst the destination collection
   * @param root the specified name for new elements
   * @param src the source collection.
   */
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

  /**
   * Appends elements from collection {@code src} to {@code dst}. Adds {@code root} value to the
   * name for each added element.
   * 
   * @param dst the destination collection
   * @param root the specified value to add in the name
   * @param src the source collection
   */
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

  /**
   * Appends all elements from {@code src} to {@code dst}.
   * 
   * @param dst the destination collection
   * @param src the source collection
   */
  public static void appendExtended(Collection<ExtendedProperty> dst,
      Collection<ExtendedProperty> src) {
    Assert.notNull(dst);
    if (src != null && !src.isEmpty()) {
      dst.addAll(src);
    }
  }

  /**
   * Appends all elements from {@code src} to {@code dst} using a {@code prefix} prefix for all
   * added elements.
   * 
   * @param dst the destination collection
   * @param prefix the prefix to add in {@code name} value
   * @param src the source collection
   */
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

  /**
   * Creates a Property list and adds {@code obj}.
   * 
   * @param obj the Objects to add to Properties
   * @return a new list of Properties
   */
  public static List<Property> createProperties(Object... obj) {
    List<Property> lst = new ArrayList<Property>();
    if (obj.length > 0) {
      addProperties(lst, obj);
    }
    return lst;
  }

  /**
   * Creates a new list of Properties with a specified prefix. Property names are represented as the
   * progress.
   * 
   * @param prefix a prefix if specified is used in {@code name} value
   * @param values the values of the Properties
   * @return a new list of Properties
   */
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

  /**
   * Returns an ExtendedProperty list as an array.
   * 
   * @param lst the data list
   * @return a String array of the {@code lst}
   */
  public static String[][] extendedToArray(List<ExtendedProperty> lst) {
    Assert.notEmpty(lst);

    int r = lst.size();
    String[][] arr = new String[r][4];

    for (int i = 0; i < r; i++) {
      ExtendedProperty el = lst.get(i);

      arr[i][0] = el.getName();
      arr[i][1] = el.getSub();
      arr[i][2] = el.getValue();
      arr[i][3] = el.getDate().toTimeString();
    }
    return arr;
  }

  /**
   * Returns a Property list as an array.
   * 
   * @param lst the data list
   * @return a String array of the {@code lst}
   */
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

  /**
   * Transforms a String {@code v}.
   * <p>
   * If {@code v} has leading or trailing whitespaces it ommits them and if {@code v.length() <= 10}
   * returns a Hex representation of {@code v} with "[hex]" tag. If {@code v.length() > 10} returns
   * a "[whitespace]" tag with the length of {@code v}.
   * </p>
   * 
   * @param v the String to transform
   * @return a String representation of the value {@code v}
   */
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

  /**
   * Transforms a sub {@code s}. Transforms using {@link #transformValue(Object)}
   * 
   * @param s the Object to transform
   * @return a String representation of the value {@code s}
   */
  private static String transformSub(Object s) {
    return transformValue(s);
  }

  /**
   * If {@code v} is a Sting value, transforms using {@link #transformString(String)}. If any other
   * Object, transforms it using {@link com.butent.bee.shared.utils.BeeUtils#transform(Object)}
   * 
   * @param v teh String to transform
   * @return a String representation of the value {@code v}
   */
  private static String transformValue(Object v) {
    if (v == null) {
      return BeeConst.STRING_EMPTY;
    } else if (v instanceof String) {
      return transformString((String) v);
    } else {
      return BeeUtils.transform(v);
    }
  }

  /**
   * Checks if {@code nm} is a valid name value.
   * 
   * @param nm the name to check
   * @return true if {@code nm} is not {@code null} or empty, otherwise false
   */
  private static boolean validName(String nm) {
    return (nm != null && !nm.isEmpty());
  }

  /**
   * Checks if {@code v} is a valid value.
   * 
   * @param v the value to check
   * @return true if the value is valid, otherwise false.
   */
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
