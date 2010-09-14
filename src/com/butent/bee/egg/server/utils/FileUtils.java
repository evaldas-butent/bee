package com.butent.bee.egg.server.utils;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.LogUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Logger;

public class FileUtils {
  private static final int DEFAULT_BUFFER_SIZE = 4096;

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

  public static String fileToString(String fileName) {
    Assert.notEmpty(fileName);
    File fl = new File(fileName.trim());

    if (!fl.exists()) {
      LogUtils.warning(logger, fileName, "file not found");
      return null;
    }

    FileReader fr = null;
    StringBuilder sb = new StringBuilder();

    int size = DEFAULT_BUFFER_SIZE;
    char[] arr = new char[size];
    int len;

    try {
      fr = new FileReader(fl);
      do {
        len = fr.read(arr, 0, size);
        if (len > 0) {
          sb.append(arr, 0, len);
        }
      } while (len > 0);
    } catch (IOException ex) {
      LogUtils.error(logger, ex, fileName);
    }

    closeQuietly(fr);

    return sb.toString();
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

  public static boolean isInputFile(File fl) {
    if (fl == null) {
      return false;
    } else {
      return fl.exists() && fl.isFile() && fl.length() > 0;
    }
  }

  public static boolean isInputFile(String fileName) {
    Assert.notEmpty(fileName);
    File fl = new File(fileName);

    return fl.exists() && fl.isFile() && fl.length() > 0;
  }

}
