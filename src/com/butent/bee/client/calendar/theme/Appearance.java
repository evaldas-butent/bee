package com.butent.bee.client.calendar.theme;

import com.butent.bee.client.calendar.ThemeAppointmentStyle;

public class Appearance implements ThemeAppointmentStyle {

  protected String selectedBorder;

  protected String selectedBackground;
  protected String selectedBackgroundHeader;
  protected String selectedBackgroundFooter;

  protected String selectedText;
  protected String selectedHeaderText;
  
  protected String border;
  
  protected String background;
  protected String backgroundHeader;
  protected String backgroundFooter;
  
  protected String text;
  protected String headerText;

  public Appearance(String border, String background) {
    super();

    this.border = background;
    this.selectedBorder = border;

    this.text = "#FFFFFF";
    this.selectedText = text;

    this.headerText = text;
    this.selectedHeaderText = text;

    this.background = background;
    this.selectedBackground = background;

    this.backgroundHeader = border;
    this.selectedBackgroundHeader = border;
  }

  public String getBackground() {
    return background;
  }

  public String getBackgroundFooter() {
    return backgroundFooter;
  }

  public String getBackgroundHeader() {
    return backgroundHeader;
  }

  public String getBorder() {
    return border;
  }

  public String getHeaderText() {
    return headerText;
  }

  public String getSelectedBackground() {
    return selectedBackground;
  }

  public String getSelectedBackgroundFooter() {
    return selectedBackgroundFooter;
  }

  public String getSelectedBackgroundHeader() {
    return selectedBackgroundHeader;
  }

  public String getSelectedBorder() {
    return selectedBorder;
  }

  public String getSelectedHeaderText() {
    return selectedHeaderText;
  }

  public String getSelectedText() {
    return selectedText;
  }

  public String getText() {
    return text;
  }

  public void setBackground(String background) {
    this.background = background;
  }

  public void setBackgroundFooter(String backgroundFooter) {
    this.backgroundFooter = backgroundFooter;
  }

  public void setBackgroundHeader(String backgroundHeader) {
    this.backgroundHeader = backgroundHeader;
  }

  public void setBorder(String border) {
    this.border = border;
  }

  public void setHeaderText(String headerText) {
    this.headerText = headerText;
  }

  public void setSelectedBackground(String selectedBackground) {
    this.selectedBackground = selectedBackground;
  }

  public void setSelectedBackgroundFooter(String selectedBackgroundFooter) {
    this.selectedBackgroundFooter = selectedBackgroundFooter;
  }

  public void setSelectedBackgroundHeader(String selectedBackgroundHeader) {
    this.selectedBackgroundHeader = selectedBackgroundHeader;
  }

  public void setSelectedBorder(String selectedBorder) {
    this.selectedBorder = selectedBorder;
  }

  public void setSelectedHeaderText(String selectedHeaderText) {
    this.selectedHeaderText = selectedHeaderText;
  }

  public void setSelectedText(String selectedText) {
    this.selectedText = selectedText;
  }

  public void setText(String text) {
    this.text = text;
  }
}
