package com.butent.bee.client.view;

import com.butent.bee.client.output.Printable;
import com.butent.bee.shared.data.event.SelectionCountChangeEvent;

public interface FooterView extends View, SelectionCountChangeEvent.Handler, Printable {

  void create(int rowCount, boolean addPaging, boolean showPageSize, boolean addSearch);
  
  int getHeight();
}
