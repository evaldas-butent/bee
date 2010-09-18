package com.butent.bee.egg.shared.data;

import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.egg.client.data.JsData;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.StringProp;
import com.butent.bee.egg.shared.utils.SubProp;

import java.util.List;

public class DataUtils {
  
  @SuppressWarnings("unchecked")
  public static BeeView createView(Object data, Object... columns) {
    Assert.notNull(data);
    int c = columns.length;
    
    BeeView view = null;
    
    if (data instanceof BeeView) {
      view = (BeeView) data;
    
    }  else if (data instanceof String[][]) {
      view = new StringData((String[][]) data);
    } else if (data instanceof JsArrayString) {
      view = new JsData((JsArrayString) data);
    
    } else if (data instanceof List) {
      Object el = ((List<?>) data).get(0);
      
      if (el instanceof SubProp) {
        view = new SubPropData((List<SubProp>) data);
      } else if (el instanceof StringProp) {
        view = new StringPropData((List<StringProp>) data);
      }
    }
    
    Assert.notNull(view);
    
    if (c > 0) {
      BeeColumn[] arr = new BeeColumn[c];
      
      for (int i = 0; i < c; i++) {
        if (columns[i] instanceof BeeColumn) {
          arr[i] = (BeeColumn) columns[i];
        } else {
          arr[i] = new BeeColumn(BeeUtils.ifString(columns[i], BeeUtils.transform(i + 1)));
        }
      }
      
      view.setColumns(arr);
      view.setColumnCount(c);
    }

    return view;
  }
    
}
