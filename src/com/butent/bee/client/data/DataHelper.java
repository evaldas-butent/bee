package com.butent.bee.client.data;

public class DataHelper {

  private static int defaultAsyncThreshold = 100;
  private static int defaultSearchThreshold = 2;

  private static int maxInitialRowSetSize = 50;
  
  public static int getDefaultAsyncThreshold() {
    return defaultAsyncThreshold;
  }

  public static int getDefaultSearchThreshold() {
    return defaultSearchThreshold;
  }

  public static int getMaxInitialRowSetSize() {
    return maxInitialRowSetSize;
  }

  private DataHelper() {
    super();
  }
}
