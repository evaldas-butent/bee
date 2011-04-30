package com.butent.bee.server;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.io.Filter;
import com.butent.bee.server.io.NameUtils;
import com.butent.bee.server.io.NameUtils.Component;
import com.butent.bee.server.io.WildcardFilter;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.PropertyUtils;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Contains essential server configuration parameters like directory structure or files blacklist.
 */

public class Config {
  private static Logger logger = Logger.getLogger(Config.class.getName());

  public static final File WAR_DIR;
  public static final File SOURCE_DIR;
  public static final File CONFIG_DIR;
  public static final File USER_DIR;

  private static Properties properties;

  private static final Splitter VALUE_SPLITTER =
      Splitter.on(BeeConst.CHAR_COMMA).trimResults().omitEmptyStrings();

  private static List<Filter> fileBlacklist = null;
  private static List<String> textExtensions = null;

  static {
    Class<?> z = Config.class;
    String path = z.getResource(z.getSimpleName() + ".class").getPath();

    int idx = path.indexOf("/" + z.getName().replace('.', '/') + ".class");
    String w;
    if (idx > 0) {
      w = path.substring(0, path.substring(0, idx).lastIndexOf('/'));
    } else {
      w = path.substring(0, path.indexOf("/classes/"));
    }
    File dir = new File(w);

    WAR_DIR = dir.getParentFile();
    SOURCE_DIR = new File(WAR_DIR.getParentFile(), "src");

    CONFIG_DIR = new File(dir, "config");
    USER_DIR = new File(dir, "user");

    properties = loadProperties("server.properties");
  }

  public static String getConfigPath(String resource) {
    Assert.notEmpty(resource);

    if (FileUtils.isInputFile(CONFIG_DIR, resource)) {
      return new File(CONFIG_DIR, resource).getPath();
    }
    return null;
  }

  public static List<Filter> getFileBlacklist() {
    if (fileBlacklist == null) {
      fileBlacklist = Lists.newArrayList();
      for (String expr : getList("FileBlacklist")) {
        fileBlacklist.add(new WildcardFilter(expr, Component.NAME));
      }
    }
    return fileBlacklist;
  }

  public static List<ExtendedProperty> getInfo() {
    List<ExtendedProperty> lst = Lists.newArrayList();
    PropertyUtils.addProperties(lst, true,
        "War dir", WAR_DIR.isDirectory(), WAR_DIR.getAbsolutePath(),
        "Source dir", SOURCE_DIR.isDirectory(), SOURCE_DIR.getAbsolutePath(),
        "Config dir", CONFIG_DIR.isDirectory(), CONFIG_DIR.getAbsolutePath(),
        "User dir", USER_DIR.isDirectory(), USER_DIR.getAbsolutePath());

    if (fileBlacklist == null) {
      PropertyUtils.addExtended(lst, "File Blacklist", "not initialized");
    } else {
      int cnt = fileBlacklist.size();
      PropertyUtils.addExtended(lst, "File Blacklist", cnt);
      for (int i = 0; i < cnt; i++) {
        PropertyUtils.addExtended(lst, "blacklist pattern", BeeUtils.progress(i + 1, cnt),
            fileBlacklist.get(i));
      }
    }

    if (textExtensions == null) {
      PropertyUtils.addExtended(lst, "Text extensions", "not initialized");
    } else {
      int cnt = textExtensions.size();
      PropertyUtils.addExtended(lst, "Text extensions", cnt);
      for (int i = 0; i < cnt; i++) {
        PropertyUtils.addExtended(lst, "text extension", BeeUtils.progress(i + 1, cnt),
            textExtensions.get(i));
      }
    }

    Set<String> keys = properties.stringPropertyNames();
    int size = keys.size();
    PropertyUtils.addExtended(lst, "Properties", size);

    int idx = 0;
    for (String key : keys) {
      PropertyUtils.addExtended(lst,
          key, BeeUtils.progress(++idx, size), properties.getProperty(key));
    }
    return lst;
  }

  public static List<String> getList(String key) {
    String values = getProperty(key);
    if (BeeUtils.isEmpty(values)) {
      return Lists.newArrayList();
    }
    return Lists.newArrayList(VALUE_SPLITTER.split(values));
  }

  public static String getPath(String resource) {
    Assert.notEmpty(resource);

    String path = getUserPath(resource);
    if (BeeUtils.isEmpty(path)) {
      path = getConfigPath(resource);
    }

    if (BeeUtils.isEmpty(path)) {
      LogUtils.warning(logger, resource, "resource not found");
    }
    return path;
  }

  public static String getProperty(String key) {
    Assert.notEmpty(key);
    return properties.getProperty(key);
  }

  public static List<String> getTextExtensions() {
    if (textExtensions == null) {
      textExtensions = getList("TextExtensions");
    }
    return textExtensions;
  }

  public static String getUserPath(String resource) {
    Assert.notEmpty(resource);

    if (FileUtils.isInputFile(USER_DIR, resource)) {
      return new File(USER_DIR, resource).getPath();
    }
    return null;
  }

  public static boolean isText(File file) {
    if (file == null) {
      return false;
    }
    String ext = NameUtils.getExtension(file.getName());
    if (BeeUtils.isEmpty(ext)) {
      return false;
    }

    boolean ok = false;
    for (String x : getTextExtensions()) {
      if (BeeUtils.same(x, ext)) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  public static boolean isVisible(File file) {
    if (file == null) {
      return false;
    }

    boolean ok = true;
    for (Filter filter : getFileBlacklist()) {
      if (filter.accept(file) && filter.accept(file, file.getName())) {
        ok = false;
        break;
      }
    }
    return ok;
  }

  private static int getSize(Properties props) {
    if (props == null) {
      return 0;
    }
    return props.stringPropertyNames().size();
  }

  private static boolean isEmpty(Properties props) {
    return getSize(props) <= 0;
  }

  private static Properties loadProperties(String name) {
    Properties def = readProperties(new File(CONFIG_DIR, name));
    Properties usr = readProperties(new File(USER_DIR, name));

    Properties result;
    if (isEmpty(def)) {
      result = new Properties();
    } else {
      result = new Properties(def);
    }

    if (!isEmpty(usr)) {
      for (String key : usr.stringPropertyNames()) {
        result.setProperty(key, usr.getProperty(key));
      }
    }
    return result;
  }

  private static Properties readProperties(File file) {
    if (!FileUtils.isInputFile(file)) {
      return null;
    }
    Properties props = new Properties();
    FileUtils.loadProperties(props, file);
    return props;
  }

  private Config() {
  }
}
