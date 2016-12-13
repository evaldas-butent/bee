package com.butent.bee.shared.time;

public interface HasYearMonth {

  default int getCentury() {
    return getYear() / 100 + 1;
  }

  JustDate getDate();

  int getMonth();

  default int getQuarter() {
    return (getMonth() - 1) / 3 + 1;
  }

  int getYear();

  void setMonth(int month);

  void setYear(int year);
}
