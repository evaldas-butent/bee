package com.butent.bee.client.datepicker;

import com.google.gwt.user.client.ui.Composite;

abstract class Component extends Composite {

  private DatePicker datePicker;

  protected Model getModel() {
    return datePicker.getModel();
  }

  protected void addMonths(int numMonths) {
    getModel().shiftCurrentMonth(numMonths);
    getDatePicker().refreshAll();
  }

  protected DatePicker getDatePicker() {
    return datePicker;
  }

  protected abstract void refresh();

  protected void refreshAll() {
    getDatePicker().refreshAll();
  }

  protected abstract void setUp();

  void setDatePicker(DatePicker datePicker) {
    this.datePicker = datePicker;
  }
}
