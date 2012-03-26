package com.butent.bee.client.calendar.theme;

import com.google.common.collect.Maps;

import com.butent.bee.client.calendar.AppointmentStyle;

import java.util.Map;

public class DefaultTheme {

  public static final Appearance BLUE = new Appearance("#2952A3", "#668CD9");
  public static final Appearance RED = new Appearance("#A32929", "#D96666");
  public static final Appearance PINK = new Appearance("#B1365F", "#E67399");
  public static final Appearance PURPLE = new Appearance("#7A367A", "#B373B3");
  public static final Appearance DARK_PURPLE = new Appearance("#5229A3", "#8C66D9");
  public static final Appearance STEELE_BLUE = new Appearance("#29527A", "#29527A");
  public static final Appearance LIGHT_BLUE = new Appearance("#1B887A", "#59BFB3");
  public static final Appearance TEAL = new Appearance("#28754E", "#65AD89");
  public static final Appearance LIGHT_TEAL = new Appearance("#4A716C", "#85AAA5");
  public static final Appearance GREEN = new Appearance("#0D7813", "#4CB052");
  public static final Appearance LIGHT_GREEN = new Appearance("#528800", "#8CBF40");
  public static final Appearance YELLOW_GREEN = new Appearance("#88880E", "#BFBF4D");
  public static final Appearance YELLOW = new Appearance("#AB8B00", "#E0C240");
  public static final Appearance ORANGE = new Appearance("#BE6D00", "#F2A640");
  public static final Appearance RED_ORANGE = new Appearance("#B1440E", "#E6804D");
  public static final Appearance LIGHT_BROWN = new Appearance("#865A5A", "#BE9494");
  public static final Appearance LIGHT_PURPLE = new Appearance("#705770", "#A992A9");
  public static final Appearance GREY = new Appearance("#4E5D6C", "#8997A5");
  public static final Appearance BLUE_GREY = new Appearance("#5A6986", "#94A2bE");
  public static final Appearance YELLOW_GREY = new Appearance("#6E6E41", "#A7A77D");
  public static final Appearance BROWN = new Appearance("#8D6F47", "#C4A883");
  
  public static final Appearance DEFAULT = BLUE;

  public static final Map<AppointmentStyle, Appearance> STYLES = Maps.newHashMap();

  static {
    STYLES.put(AppointmentStyle.BLUE, BLUE);
    STYLES.put(AppointmentStyle.BLUE_GREY, BLUE_GREY);
    STYLES.put(AppointmentStyle.BROWN, BROWN);
    STYLES.put(AppointmentStyle.DARK_PURPLE, DARK_PURPLE);
    STYLES.put(AppointmentStyle.GREEN, GREEN);
    STYLES.put(AppointmentStyle.GREY, GREY);
    STYLES.put(AppointmentStyle.LIGHT_BLUE, LIGHT_BLUE);
    STYLES.put(AppointmentStyle.LIGHT_BROWN, LIGHT_BROWN);
    STYLES.put(AppointmentStyle.LIGHT_GREEN, LIGHT_GREEN);
    STYLES.put(AppointmentStyle.LIGHT_PURPLE, LIGHT_PURPLE);
    STYLES.put(AppointmentStyle.LIGHT_TEAL, LIGHT_TEAL);
    STYLES.put(AppointmentStyle.ORANGE, ORANGE);
    STYLES.put(AppointmentStyle.PINK, PINK);
    STYLES.put(AppointmentStyle.PURPLE, PURPLE);
    STYLES.put(AppointmentStyle.RED, RED);
    STYLES.put(AppointmentStyle.RED_ORANGE, RED_ORANGE);
    STYLES.put(AppointmentStyle.STEELE_BLUE, STEELE_BLUE);
    STYLES.put(AppointmentStyle.TEAL, TEAL);
    STYLES.put(AppointmentStyle.YELLOW, YELLOW);
    STYLES.put(AppointmentStyle.YELLOW_GREEN, YELLOW_GREEN);
    STYLES.put(AppointmentStyle.YELLOW_GREY, YELLOW_GREY);
    STYLES.put(AppointmentStyle.DEFAULT, DEFAULT);
  }

  private DefaultTheme() {
  }
}
