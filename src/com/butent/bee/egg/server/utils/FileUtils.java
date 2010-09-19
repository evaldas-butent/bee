package com.butent.bee.egg.server.utils;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;
import com.butent.bee.egg.shared.utils.SubProp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Logger;

public class FileUtils {
  private static final int DEFAULT_BUFFER_SIZE = 4096;
  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  private static Logger logger = Logger.getLogger(FileUtils.class.getName());

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

  public static String fileToString(File fl) {
    return fileToString(fl, DEFAULT_CHARSET);
  }

  public static String fileToString(File fl, Charset cs) {
    Assert.notNull(fl);
    if (!fl.exists()) {
      LogUtils.warning(logger, fl.getAbsolutePath(), "file not found");
      return null;
    }

    InputStreamReader fr = null;
    StringBuilder sb = new StringBuilder();

    int size = DEFAULT_BUFFER_SIZE;
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
    return fileToString(fileName, DEFAULT_CHARSET);
  }

  public static String fileToString(String fileName, Charset cs) {
    Assert.notEmpty(fileName);
    File fl = new File(fileName.trim());
    return fileToString(fl, cs);
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

  public static List<SubProp> getCharsets() {
    List<SubProp> lst = new ArrayList<SubProp>();
    PropUtils.addSub(lst, "Default Charset", Charset.defaultCharset());

    SortedMap<String, Charset> charsets = Charset.availableCharsets();
    PropUtils.addSub(lst, "Available Charsets", "Cnt", charsets.size());

    int i = 0;
    for (String key : charsets.keySet()) {
      i++;
      Charset cs = charsets.get(key);

      PropUtils.addRoot(lst,
          BeeUtils.concat(1, BeeUtils.progress(i, charsets.size()), key),
          "Name", cs.name(), "Aliases",
          BeeUtils.transformCollection(cs.aliases()), "Can Encode",
          cs.canEncode(), "Display Name", cs.displayName(), "Registered",
          cs.isRegistered());
    }

    return lst;
  }

  public static List<StringProp> getFileInfo(File fl) {
    Assert.notNull(fl);

    List<StringProp> lst = new ArrayList<StringProp>();
    if (!fl.exists()) {
      PropUtils.addString(lst, "Exists", false);
      return lst;
    }

    PropUtils.addString(lst, "Can Execute", fl.canExecute(), "Can Read",
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

  public static List<StringProp> getRootsInfo() {
    List<StringProp> lst = new ArrayList<StringProp>();

    File[] roots = File.listRoots();
    int n = BeeUtils.length(roots);
    if (n <= 0) {
      PropUtils.addString(lst, "Roots", BeeUtils.bracket(n));
      return lst;
    }

    for (int i = 0; i < n; i++) {
      PropUtils.addString(lst, "Root", BeeUtils.progress(i + 1, n));
      lst.addAll(getFileInfo(roots[i]));
    }

    return lst;
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

  public static Charset normalizeCharset(String name) {
    if (BeeUtils.isEmpty(name)) {
      return DEFAULT_CHARSET;
    }
    Charset cs = null;

    try {
      cs = Charset.forName(name);
    } catch (Exception ex) {
      LogUtils.warning(logger, ex, name);
      cs = null;
    }

    return BeeUtils.nvl(cs, DEFAULT_CHARSET);
  }

}
