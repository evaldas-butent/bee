package com.butent.bee.server.utils;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Logger;

public class FileUtils {
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  private static int defaultBufferSize = 4096;
  private static Charset defaultCharset = UTF_8;
  private static char extensionSeparatorChar = '.';
  private static String extensionSeparator = Character.toString(extensionSeparatorChar);

  private static Logger logger = Logger.getLogger(FileUtils.class.getName());

  public static String addExtension(String name, String ext) {
    Assert.notEmpty(name);
    Assert.isTrue(isValidExtension(ext));

    if (name.endsWith(extensionSeparator)) {
      return name + ext;
    } else {
      return name + extensionSeparatorChar + ext;
    }
  }

  public static void closeQuietly(Reader rdr) {
    if (rdr == null) {
      return;
    }

    try {
      rdr.close();
    } catch (IOException ex) {
      LogUtils.warning(logger, ex);
    }
  }

  public static void closeQuietly(Writer wrt) {
    if (wrt == null) {
      return;
    }

    try {
      wrt.close();
    } catch (IOException ex) {
      LogUtils.severe(logger, ex);
    }
  }
  
  public static String defaultExtension(String name, String ext) {
    Assert.notEmpty(name);
    Assert.isTrue(isValidExtension(ext));

    if (hasExtension(name)) {
      return name;
    } else {
      return addExtension(name, ext);
    }
  }

  public static String fileToString(File fl) {
    return fileToString(fl, defaultCharset);
  }

  public static String fileToString(File fl, Charset cs) {
    Assert.notNull(fl);
    if (!fl.exists()) {
      LogUtils.warning(logger, fl.getAbsolutePath(), "file not found");
      return null;
    }

    InputStreamReader fr = null;
    StringBuilder sb = new StringBuilder();

    int size = defaultBufferSize;
    char[] arr = new char[size];
    int len;

    try {
      fr = new InputStreamReader(new FileInputStream(fl), cs);
      do {
        len = fr.read(arr, 0, size);
        if (len > 0) {
          sb.append(arr, 0, len);
        }
      } while (len > 0);
    } catch (IOException ex) {
      LogUtils.error(logger, ex, fl.getAbsolutePath());
    }

    closeQuietly(fr);

    return sb.toString();
  }

  public static String fileToString(String fileName) {
    return fileToString(fileName, defaultCharset);
  }

  public static String fileToString(String fileName, Charset cs) {
    Assert.notEmpty(fileName);
    File fl = new File(fileName.trim());
    return fileToString(fl, cs);
  }

  public static String forceExtension(String name, String ext) {
    Assert.notEmpty(name);
    Assert.isTrue(isValidExtension(ext));

    String old = getExtension(name);
    if (ext.equals(old)) {
      return name;
    } else if (BeeUtils.isEmpty(old)) {
      return addExtension(name, ext);
    } else {
      return addExtension(removeExtension(name), ext);
    }
  }

  public static String getCanonicalPath(File fl) {
    Assert.notNull(fl);
    String path;

    try {
      path = fl.getCanonicalPath();
    } catch (IOException ex) {
      LogUtils.error(logger, ex);
      path = BeeConst.STRING_EMPTY;
    }

    return path;
  }

  public static List<ExtendedProperty> getCharsets() {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();
    PropertyUtils.addExtended(lst, "Default Charset", Charset.defaultCharset());

    SortedMap<String, Charset> charsets = Charset.availableCharsets();
    PropertyUtils.addExtended(lst, "Available Charsets", "Cnt", charsets.size());

    int i = 0;
    for (String key : charsets.keySet()) {
      i++;
      Charset cs = charsets.get(key);

      PropertyUtils.addChildren(lst,
          BeeUtils.concat(1, BeeUtils.progress(i, charsets.size()), key),
          "Name", cs.name(), "Aliases", BeeUtils.transformCollection(cs.aliases()),
          "Can Encode", cs.canEncode(), "Display Name", cs.displayName(),
          "Registered", cs.isRegistered());
    }
    return lst;
  }

  public static String getExtension(String name) {
    Assert.notEmpty(name);
    String ext = BeeUtils.getSuffix(name, extensionSeparatorChar);
    if (isValidExtension(ext)) {
      return ext;
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public static List<Property> getFileInfo(File fl) {
    Assert.notNull(fl);

    List<Property> lst = new ArrayList<Property>();
    if (!fl.exists()) {
      PropertyUtils.addProperty(lst, "Exists", false);
      return lst;
    }

    PropertyUtils.addProperties(lst, "Can Execute", fl.canExecute(), "Can Read",
        fl.canRead(), "CanWrite", fl.canWrite(), "Absolute Path",
        fl.getAbsolutePath(), "Canonical Path", getCanonicalPath(fl),
        "Free Space", fl.getFreeSpace(), "Name", fl.getName(), "Parent",
        fl.getParent(), "Path", fl.getPath(), "Total Space",
        fl.getTotalSpace(), "Usable Space", fl.getUsableSpace(), "Absolute",
        fl.isAbsolute(), "Directory", fl.isDirectory(), "File", fl.isFile(),
        "Hidden", fl.isHidden(), "Last Modified",
        new BeeDate(fl.lastModified()), "Length", fl.length(), "URI",
        fl.toURI());
    return lst;
  }

  public static FileReader getFileReader(String fileName) {
    Assert.notEmpty(fileName);
    File fl = new File(fileName.trim());

    if (!fl.exists()) {
      LogUtils.warning(logger, fileName, "file not found");
      return null;
    }

    FileReader fr = null;

    try {
      fr = new FileReader(fl);
    } catch (IOException ex) {
      LogUtils.error(logger, ex, fileName);
    }

    return fr;
  }

  public static String[] getFiles(File dir) {
    return getFiles(dir, null);
  }

  public static String[] getFiles(File dir, FilenameFilter filter) {
    Assert.notNull(dir);
    return (filter == null) ? dir.list() : dir.list(filter);
  }

  public static List<Property> getRootsInfo() {
    List<Property> lst = new ArrayList<Property>();

    File[] roots = File.listRoots();
    int n = BeeUtils.length(roots);
    if (n <= 0) {
      PropertyUtils.addProperty(lst, "Roots", BeeUtils.bracket(n));
      return lst;
    }

    for (int i = 0; i < n; i++) {
      PropertyUtils.addProperty(lst, "Root", BeeUtils.progress(i + 1, n));
      lst.addAll(getFileInfo(roots[i]));
    }

    return lst;
  }

  public static boolean hasExtension(String name) {
    return !BeeUtils.isEmpty(getExtension(name));
  }

  public static boolean isFile(String name) {
    Assert.notEmpty(name);
    return new File(name).exists();
  }

  public static boolean isInputFile(File fl) {
    if (fl == null) {
      return false;
    } else {
      return fl.exists() && fl.isFile() && fl.length() > 0 && fl.canRead();
    }
  }

  public static boolean isInputFile(String fileName) {
    Assert.notEmpty(fileName);
    File fl = new File(fileName);

    return isInputFile(fl);
  }

  public static boolean isValidExtension(String ext) {
    if (BeeUtils.isEmpty(ext)) {
      return false;
    }
    boolean ok = true;

    for (int i = 0; i < ext.length(); i++) {
      if (!Character.isLetterOrDigit(ext.charAt(i))) {
        ok = false;
        break;
      }
    }

    return ok;
  }

  public static Charset normalizeCharset(String name) {
    if (BeeUtils.isEmpty(name)) {
      return defaultCharset;
    }
    Charset cs = null;

    try {
      cs = Charset.forName(name);
    } catch (Exception ex) {
      LogUtils.warning(logger, ex, name);
      cs = null;
    }

    return BeeUtils.nvl(cs, defaultCharset);
  }

  public static String removeExtension(String name) {
    Assert.notEmpty(name);
    if (hasExtension(name)) {
      return name.substring(0, name.lastIndexOf(extensionSeparatorChar));
    } else {
      return name;
    }
  }

  public static boolean toFile(CharSequence src, String dst) {
    return toFile(src, dst, defaultCharset);
  }
  
  public static boolean toFile(CharSequence src, String dst, Charset cs) {
    Assert.notEmpty(src);
    Assert.notEmpty(dst);

    OutputStreamWriter fw = null;
    boolean ok;

    try {
      fw = new OutputStreamWriter(new FileOutputStream(dst), cs);
      fw.append(src);
      ok = true;
    } catch (IOException ex) {
      LogUtils.error(logger, ex, dst);
      ok = false;
    }

    closeQuietly(fw);
    return ok;
  }
  
}
