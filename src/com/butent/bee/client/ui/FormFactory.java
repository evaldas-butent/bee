package com.butent.bee.client.ui;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.ui.Widget;
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
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.ui.EditorType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class FormFactory {

  private static final String ATTR_TYPE = "type";

  private static final String TAG_ITEM = "item";
  
  public interface WidgetCallback extends Callback<WidgetDescription, String[]> {
  }
  
  public static Widget createForm(FormDescription formDescription, WidgetCallback callback) {
    Assert.notNull(formDescription);
    Assert.notNull(callback);

    List<Element> children = XmlUtils.getChildrenElements(formDescription.getFormElement());
    if (BeeUtils.isEmpty(children)) {
      BeeKeeper.getLog().severe("createForm: form element has no children");
      return null;
    }
    
    Element root = null;
    FormWidget formWidget = null;
    int count = 0;

    for (Element child : children) {
      FormWidget fw = FormWidget.getByTagName(child.getTagName());
      if (fw != null) {
        root = child;
        formWidget = fw;
        count++;
      }
    }
    
    if (count <= 0) {
      BeeKeeper.getLog().severe("createForm: root widget not found");
      return null;
    }
    if (count > 1) {
      BeeKeeper.getLog().severe("createForm: form element has", count, "root widgets");
      return null;
    }
    
    Widget form = formWidget.create(root, callback);
    if (form == null) {
      BeeKeeper.getLog().severe("createForm: cannot create root widget", formWidget);
    }
    return form;
  }

  public static EditorDescription getEditorDescription(Element element) {
    Assert.notNull(element);

    String typeCode = element.getAttribute(ATTR_TYPE);
    if (BeeUtils.isEmpty(typeCode)) {
      return null;
    }
    EditorType editorType = EditorType.getByTypeCode(typeCode);
    if (editorType == null) {
      return null;
    }

    EditorDescription editor = new EditorDescription(editorType);
    editor.setAttributes(XmlUtils.getAttributes(element));
    
    List<String> items = XmlUtils.getChildrenText(element, TAG_ITEM);
    if (!BeeUtils.isEmpty(items)) {
      editor.setItems(items);
    }
    return editor;
  }
  
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
