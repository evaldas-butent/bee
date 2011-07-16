package com.butent.bee.client.ui;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.DataHelper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;

public class FormFactory {
  
  public static void getForm(String name) {
    Assert.notEmpty(name);
    
    BeeKeeper.getRpc().sendText(Service.GET_FORM, BeeUtils.trim(name), new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(String.class)) {
          openForm((String) response.getResponse());
        }
      }
    });
  }
  
  public static void openForm(String xml) {
    Assert.notEmpty(xml);
    
    Document xmlDoc = XmlUtils.parse(xml);
    if (xmlDoc == null) {
      return;
    }
    Element formElement = xmlDoc.getDocumentElement();
    if (formElement == null) {
      BeeKeeper.getLog().severe("xml form element not found");
      return;
    }
    
    final FormDescription formDescription = new FormDescription(formElement);
    final String viewName = formDescription.getViewName();
    if (BeeUtils.isEmpty(viewName)) {
      showForm(formDescription);
      return;
    }  

    DataInfo dataInfo = Global.getDataExplorer().getDataInfo(viewName);
    if (dataInfo != null) {
      getInitialRowSet(viewName, dataInfo.getRowCount(), formDescription);
      return;
    }  

    Queries.getRowCount(viewName, new Queries.IntCallback() {
        public void onFailure(String[] reason) {
        }
        public void onSuccess(Integer result) {
          getInitialRowSet(viewName, result, formDescription);
        }
      });
  }

  private static void getInitialRowSet(final String viewName, final int rowCount,
      final FormDescription formDescription) {
    int limit = formDescription.getAsyncThreshold();

    final boolean async;
    if (rowCount >= limit) {
      async = true;
      if (rowCount <= DataHelper.getMaxInitialRowSetSize()) {
        limit = -1;
      } else {
        limit = DataHelper.getMaxInitialRowSetSize();
      }
    } else {
      async = false;
      limit = -1;
    }

    Queries.getRowSet(viewName, null, null, null, 0, limit, CachingPolicy.FULL,
        new Queries.RowSetCallback() {
          public void onFailure(String[] reason) {
          }

          public void onSuccess(final BeeRowSet rowSet) {
            showForm(formDescription, viewName, rowCount, rowSet, async);
          }
        });
  }

  private static void showForm(FormDescription formDescription) {
    showForm(formDescription, null, BeeConst.UNDEF, null, false);
  }
  
  private static void showForm(FormDescription formDescription, String viewName, int rowCount,
      BeeRowSet rowSet, boolean async) {
    FormPresenter presenter = new FormPresenter(formDescription, viewName, rowCount, rowSet, async);
    BeeKeeper.getScreen().updateActivePanel(presenter.getWidget());
  }
  
  private FormFactory() {
  }
}
