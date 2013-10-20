package com.butent.bee.server.modules.commons;

import com.google.common.collect.Maps;

import com.butent.bee.server.Config;
import com.butent.bee.shared.io.FileNameUtils;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.io.File;
import java.util.Map;

public final class ExtensionIcons {

  private static BeeLogger logger = LogUtils.getLogger(ExtensionIcons.class);
  
  private static final Map<String, String> icons = Maps.newHashMap();
  private static boolean initialized;

  public static String getIcon(String filename) {
    String extension = FileNameUtils.getExtension(filename);
    if (BeeUtils.isEmpty(extension)) {
      return null;
    }
    
    ensureIcons();
    return icons.get(BeeUtils.normalize(extension));
  }
  
  private static synchronized void ensureIcons() {
    if (initialized) {
      return;
    }
    
    File dir = new File(Config.IMAGE_DIR, Paths.FILE_ICON_DIR);
    File[] files = dir.listFiles();
    
    if (files != null) {
      for (File file : files) {
        String name = file.getName();
        String key = FileNameUtils.removeExtension(name);
        
        if (!BeeUtils.same(key, name)) {
          icons.put(BeeUtils.normalize(key), BeeUtils.trim(name));
        }
      }
    }
    
    initialized = true;
    
    logger.info("loaded", icons.size(), "extension icons from", dir.getPath());
  }

  private ExtensionIcons() {
  }
}
