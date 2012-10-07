package com.butent.bee.shared.logging;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.logging.Level;

public enum LogLevel {
  ERROR(Level.SEVERE), WARNING(Level.WARNING), INFO(Level.INFO), DEBUG(Level.CONFIG);
  
  public static LogLevel parse(String input) {
    for (LogLevel lvl : LogLevel.values()) {
      if (BeeUtils.inListSame(input, lvl.name(), lvl.getLevel().getName())) {
        return lvl;
      }
    }
    return null;
  }
  
  private final Level level;

  private LogLevel(Level level) {
    this.level = level;
  }

  public Level getLevel() {
    return level;
  }
}