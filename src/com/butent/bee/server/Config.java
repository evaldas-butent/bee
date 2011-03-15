package com.butent.bee.server;

import com.butent.bee.server.utils.FileUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {
  private static Logger logger = Logger.getLogger(Config.class.getName());

  public static final String RESOURCE_PATH;
  public static final String CONFIG_DIR;
  public static final String USER_DIR;

  private static Properties properties;

  static {
    Class<?> z = Config.class;
    String path = z.getResource(z.getSimpleName() + ".class").getPath();

    int idx = path.indexOf("/" + z.getName().replace('.', '/') + ".class");
    if (idx > 0) {
      RESOURCE_PATH = path.substring(0, path.substring(0, idx).lastIndexOf('/') + 1);
    } else {
      RESOURCE_PATH = path.substring(0, path.indexOf("/classes/") + 1);
    }

    CONFIG_DIR = RESOURCE_PATH + "config/";
    USER_DIR = RESOURCE_PATH + "user/";

    properties = loadProperties("server.properties");
  }

  public static List<Property> getInfo() {
    List<Property> lst = PropertyUtils.createProperties("Resource path", RESOURCE_PATH,
        "Config dir", CONFIG_DIR, "User dir", USER_DIR, "Properties", getSize(properties));

    for (String key : properties.stringPropertyNames()) {
      lst.add(new Property(key, properties.getProperty(key)));
    }
    return lst;
  }

  public static String getPath(String resource) {
    Assert.notEmpty(resource);

    if (FileUtils.isInputFile(USER_DIR + resource)) {
      return USER_DIR + resource;
    }
    if (FileUtils.isInputFile(CONFIG_DIR + resource)) {
      return CONFIG_DIR + resource;
    }

    LogUtils.warning(logger, resource, "resource not found");
    return null;
  }

  public static String getProperty(String key) {
    Assert.notEmpty(key);
    return properties.getProperty(key);
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
    Properties def = readProperties(CONFIG_DIR + name);
    Properties usr = readProperties(USER_DIR + name);

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

  private static Properties readProperties(String fileName) {
    if (!FileUtils.isInputFile(fileName)) {
      return null;
    }
    Properties props = new Properties();

    try {
      InputStreamReader inp = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
      props.load(inp);
      inp.close();
      LogUtils.infoNow(logger, props.size(), "properties loaded from", fileName);
    } catch (IOException ex) {
      LogUtils.error(logger, ex, fileName);
    }
    return props;
  }
}
