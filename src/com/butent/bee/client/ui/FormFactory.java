package com.butent.bee.client.ui;

import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormImpl;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.ui.EditorType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

/**
 * Creates and handles user interface forms.
 */

public class FormFactory {

  public interface FormCallback extends WidgetCallback {

    void afterAction(Action action, FormPresenter presenter);

    void afterCreate(FormView form);

    void afterCreateEditableWidget(EditableWidget editableWidget);

    void afterRefresh(FormView form, IsRow row);

    boolean beforeAction(Action action, FormPresenter presenter);

    void beforeRefresh(FormView form, IsRow row);
    
    FormView getFormView();
    
    FormCallback getInstance();
    
    BeeRowSet getRowSet();

    boolean hasFooter(int rowCount);

    boolean onLoad(Element formElement);

    boolean onPrepareForInsert(FormView form, DataView dataView, IsRow row);

    void onSetActiveRow(IsRow row);

    void onShow(FormPresenter presenter);

    void onStart(FormView form);

    void onStartEdit(FormView form, IsRow row);

    void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow);
  
    void setFormView(FormView form);
  }

  public abstract static class FormViewCallback {
    public void onFailure(String... reason) {
      BeeKeeper.getScreen().notifyWarning(reason);
    }
    
    public abstract void onSuccess(FormDescription formDescription, FormView result);
  }

  public interface WidgetDescriptionCallback {
    WidgetDescription getLastWidgetDescription();
    
    void onFailure(Object... messages);
    
    void onSuccess(WidgetDescription result);
  }

  private static final String ATTR_TYPE = "type";

  private static final Map<String, FormCallback> formCallbacks = Maps.newHashMap();

  public static Widget createForm(FormDescription formDescription, List<BeeColumn> columns,
      WidgetDescriptionCallback widgetDescriptionCallback, FormCallback formCallback) {
    Assert.notNull(formDescription);
    Assert.notNull(widgetDescriptionCallback);

    return createWidget(formDescription.getFormElement(), formDescription.getViewName(), columns,
        widgetDescriptionCallback, formCallback, "createForm:");
  }

  public static void createFormView(final String name, final String viewName, 
      final List<BeeColumn> columns, final FormCallback formCallback,
      final FormViewCallback viewCallback, final boolean addStyle) {
    Assert.notEmpty(name);
    Assert.notNull(viewCallback);

    getForm(name, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(String.class)) {
          FormDescription fd = getFormDescription((String) response.getResponse(), formCallback);
          if (fd == null) {
            viewCallback.onFailure("form", name, "decription not created");
          } else {
            if (!BeeUtils.isEmpty(viewName)) {
              fd.setViewName(viewName);
            }
            FormView view = new FormImpl(name);
            view.create(fd, columns, formCallback, addStyle);
            viewCallback.onSuccess(fd, view);
          }
        } else {
          viewCallback.onFailure("get form", name, "response not a string");
        }
      }
    });
  }

  public static void createFormView(String name, String viewName, List<BeeColumn> columns,
      FormViewCallback viewCallback, boolean addStyle) {
    createFormView(name, viewName, columns, getFormCallback(name), viewCallback, addStyle);
  }

  public static Widget createWidget(Element parent, String viewName, List<BeeColumn> columns,
      WidgetDescriptionCallback widgetDescriptionCallback, WidgetCallback widgetCallback,
      String messagePrefix) {
    Assert.notNull(parent);
    Assert.notNull(widgetDescriptionCallback, "createWidget: widgetDescriptionCallback is null");

    List<Element> children = XmlUtils.getChildrenElements(parent);
    if (BeeUtils.isEmpty(children)) {
      BeeKeeper.getLog().severe(messagePrefix, "element has no children");
      return null;
    }

    Element root = null;
    FormWidget formWidget = null;
    int count = 0;

    for (Element child : children) {
      FormWidget fw = FormWidget.getByTagName(XmlUtils.getLocalName(child));
      if (fw != null) {
        root = child;
        formWidget = fw;
        count++;
      }
    }

    if (count <= 0) {
      BeeKeeper.getLog().severe(messagePrefix, "root widget not found");
      return null;
    }
    if (count > 1) {
      BeeKeeper.getLog().severe(messagePrefix, "element has", count, "root widgets");
      return null;
    }

    Widget widget = formWidget.create(root, viewName, columns, widgetDescriptionCallback,
        widgetCallback);
    if (widget == null) {
      BeeKeeper.getLog().severe(messagePrefix, "cannot create root widget", formWidget);
    }
    return widget;
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
    editor.setAttributes(XmlUtils.getAttributes(element, false));

    List<String> items = XmlUtils.getChildrenText(element, HasItems.TAG_ITEM);
    if (!BeeUtils.isEmpty(items)) {
      editor.setItems(items);
    }
    return editor;
  }

  public static FormCallback getFormCallback(String formName) {
    Assert.notEmpty(formName);
    FormCallback callback = formCallbacks.get(BeeUtils.normalize(formName));
    return getInstance(callback);
  }

  public static FormCallback getInstance(FormCallback callback) {
    if (callback != null) {
      FormCallback instance = callback.getInstance();
      if (instance != null) {
        return instance;
      }
    }
    return callback;
  }

  public static void openForm(String name) {
    openForm(name, getFormCallback(name));
  }

  public static void openForm(String name, final FormCallback formCallback) {
    getForm(name, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(String.class)) {
          parseForm((String) response.getResponse(), formCallback);
        }
      }
    });
  }

  public static void parseForm(String xml) {
    parseForm(xml, null);
  }

  public static void parseForm(String xml, FormCallback callback) {
    FormDescription formDescription = getFormDescription(xml, callback);
    if (formDescription == null) {
      return;
    }

    String viewName = formDescription.getViewName();
    if (BeeUtils.isEmpty(viewName)) {
      showForm(formDescription, callback);
      return;
    }
    
    if (callback != null) {
      BeeRowSet rowSet = callback.getRowSet();
      if (rowSet != null) {
        showForm(formDescription, viewName, rowSet.getNumberOfRows(), rowSet, 
            Provider.Type.LOCAL, callback);
        return;
      }
    }
    
    DataInfo dataInfo = Global.getDataInfo(viewName);
    if (dataInfo != null) {
      getInitialRowSet(viewName, dataInfo.getRowCount(), formDescription, callback);
    }
  }
  
  public static void registerFormCallback(String formName, FormCallback callback) {
    Assert.notEmpty(formName);
    formCallbacks.put(BeeUtils.normalize(formName), callback);
  }

  private static void getForm(String name, ResponseCallback responseCallback) {
    Assert.notEmpty(name);
    Assert.notNull(responseCallback);

    BeeKeeper.getRpc().sendText(Service.GET_FORM, BeeUtils.trim(name), responseCallback);
  }

  private static FormDescription getFormDescription(String xml, FormCallback callback) {
    Assert.notEmpty(xml);

    Document xmlDoc = XmlUtils.parse(xml);
    if (xmlDoc == null) {
      return null;
    }
    Element formElement = xmlDoc.getDocumentElement();
    if (formElement == null) {
      BeeKeeper.getLog().severe("xml form element not found");
      return null;
    }

    if (callback != null && !callback.onLoad(formElement)) {
      return null;
    }

    return new FormDescription(formElement);
  }

  private static void getInitialRowSet(final String viewName, final int rowCount,
      final FormDescription formDescription, final FormCallback callback) {
    int limit = formDescription.getAsyncThreshold();

    final Provider.Type providerType;
    if (rowCount >= limit) {
      providerType = Provider.Type.ASYNC;
      if (rowCount <= DataUtils.getMaxInitialRowSetSize()) {
        limit = BeeConst.UNDEF;
      } else {
        limit = DataUtils.getMaxInitialRowSetSize();
      }
    } else {
      providerType = Provider.Type.CACHED;
      limit = BeeConst.UNDEF;
    }

    Queries.getRowSet(viewName, null, null, null, 0, limit, CachingPolicy.FULL,
        new Queries.RowSetCallback() {
          public void onSuccess(final BeeRowSet rowSet) {
            int rc = Math.max(rowCount, rowSet.getNumberOfRows());
            showForm(formDescription, viewName, rc, rowSet, providerType, callback);
          }
        });
  }

  private static void showForm(FormDescription formDescription, FormCallback callback) {
    showForm(formDescription, null, BeeConst.UNDEF, null, Provider.Type.CACHED, callback);
  }

  private static void showForm(FormDescription formDescription, String viewName, int rowCount,
      BeeRowSet rowSet, Provider.Type providerType, FormCallback callback) {
    FormPresenter presenter = new FormPresenter(formDescription, viewName, rowCount, rowSet,
        providerType, callback);
    if (callback != null) {
      callback.onShow(presenter);
    }
    BeeKeeper.getScreen().updateActivePanel(presenter.getWidget());
  }

  private FormFactory() {
  }
}
