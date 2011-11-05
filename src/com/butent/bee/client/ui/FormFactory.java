package com.butent.bee.client.ui;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcUtils;
import com.butent.bee.client.data.DataInfoProvider.DataInfoCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.view.form.FormImpl;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeFileUpload;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.ui.EditorType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

/**
 * Creates and handles user interface forms.
 */

public class FormFactory {

  public interface FormCallback {
    void afterCreateWidget(String name, Widget widget);

    boolean beforeCreateWidget(String name, Element description);
    
    boolean onLoad(Element formElement);
    
    boolean onPrepareForInsert(FormView form, IsRow row);
    
    void onShow(FormPresenter presenter);
    
    void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow);
  }

  public interface FormViewCallback extends Callback<FormView, String[]> {
  }
  
  public interface WidgetCallback extends Callback<WidgetDescription, String[]> {
  }

  private static final String ATTR_TYPE = "type";
  
  private static final String TAG_ITEM = "item";

  private static final Map<String, FormCallback> formCallbacks = Maps.newHashMap();
  
  public static Widget createForm(FormDescription formDescription, List<BeeColumn> columns,
      WidgetCallback widgetCallback, FormCallback formCallback) {
    Assert.notNull(formDescription);
    Assert.notNull(widgetCallback);

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

    Widget form = formWidget.create(root, columns, widgetCallback, formCallback);
    if (form == null) {
      BeeKeeper.getLog().severe("createForm: cannot create root widget", formWidget);
    }
    return form;
  }

  public static void createFormView(final String name, final List<BeeColumn> columns,
      final FormCallback formCallback, final FormViewCallback viewCallback) {
    Assert.notEmpty(name);
    Assert.notNull(viewCallback);
    
    getForm(name, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(String.class)) {
          FormDescription fd = getFormDescription((String) response.getResponse(), formCallback);
          if (fd == null) {
            viewCallback.onFailure(new String[]{"form", name, "decription not created"});
          } else {
            FormView view = new FormImpl();
            view.create(fd, columns, formCallback);
            viewCallback.onSuccess(view);
          }
        } else {
          viewCallback.onFailure(new String[]{"get form", name, "response not a string"});
        }
      }
    });
  }
  
  public static void createFormView(String name, List<BeeColumn> columns,
      FormViewCallback viewCallback) {
    createFormView(name, columns, getFormCallback(name), viewCallback);
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

  public static void importForm(final String name) {
    if (!BeeUtils.isEmpty(name)) {
      Global.setVarValue(Service.VAR_FORM_NAME, BeeUtils.trim(name));
    }

    final FormPanel formPanel = new FormPanel();
    formPanel.setAction(GWT.getModuleBaseURL() + Service.UPLOAD_URL);

    formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
    formPanel.setMethod(FormPanel.METHOD_POST);

    FlexTable container = new FlexTable();
    formPanel.setWidget(container);

    container.setCellSpacing(10);

    int row = 0;
    container.setWidget(row, 0, new BeeLabel("Form Name"));
    final InputText inputName = new InputText(Global.getVar(Service.VAR_FORM_NAME));
    inputName.setName(Service.VAR_FORM_NAME);
    container.setWidget(row, 1, inputName);

    row++;
    container.setWidget(row, 0, new BeeLabel("Design File"));
    final BeeFileUpload upload = new BeeFileUpload();
    upload.setName(Service.VAR_FILE_NAME);
    container.setWidget(row, 1, upload);

    row++;
    container.setWidget(row, 0, new Hidden(Service.NAME_SERVICE, Service.IMPORT_FORM));

    BeeButton submit = new BeeButton("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
        formPanel.submit();
      }
    });

    row++;
    container.setWidget(row, 0, submit);
    container.getCellFormatter().setHorizontalAlignment(row, 0,
        HasHorizontalAlignment.ALIGN_CENTER);
    container.getFlexCellFormatter().setColSpan(row, 0, 2);

    final DialogBox dialog = new DialogBox();

    formPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
      public void onSubmit(FormPanel.SubmitEvent event) {
        if (BeeUtils.isEmpty(inputName.getValue())) {
          Global.showError("Form name not specified");
          event.cancel();
        } else if (BeeUtils.isEmpty(upload.getFilename())) {
          Global.showError("Select design file");
          event.cancel();
        }
      }
    });

    formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
      public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
        ResponseObject response = CommUtils.getFormResonse(event.getResults());
        RpcUtils.dispatchMessages(response);
        if (response.hasResponse(String.class)) {
          parseForm((String) response.getResponse());
          dialog.hide();
        } else {
          BeeKeeper.getLog().warning("unknown response type", response.getType());
        }
      }
    });

    dialog.setText("Import Form Design");
    dialog.setAnimationEnabled(true);

    dialog.setWidget(formPanel);
    dialog.center();

    inputName.setFocus(true);
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

  public static void parseForm(String xml, final FormCallback callback) {
    final FormDescription formDescription = getFormDescription(xml, callback);
    if (formDescription == null) {
      return;
    }

    final String viewName = formDescription.getViewName();
    if (BeeUtils.isEmpty(viewName)) {
      showForm(formDescription, callback);
      return;
    }

    Global.getDataInfo(viewName, new DataInfoCallback() {
      @Override
      public void onSuccess(DataInfo result) {
        getInitialRowSet(viewName, result.getRowCount(), formDescription, callback);
      }
    });
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
  
  private static FormCallback getFormCallback(String formName) {
    Assert.notEmpty(formName);
    return formCallbacks.get(BeeUtils.normalize(formName));
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

    final boolean async;
    if (rowCount >= limit) {
      async = true;
      if (rowCount <= DataUtils.getMaxInitialRowSetSize()) {
        limit = -1;
      } else {
        limit = DataUtils.getMaxInitialRowSetSize();
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
            showForm(formDescription, viewName, rowCount, rowSet, async, callback);
          }
        });
  }

  private static void showForm(FormDescription formDescription, FormCallback callback) {
    showForm(formDescription, null, BeeConst.UNDEF, null, false, callback);
  }

  private static void showForm(FormDescription formDescription, String viewName, int rowCount,
      BeeRowSet rowSet, boolean async, FormCallback callback) {
    FormPresenter presenter = new FormPresenter(formDescription, viewName, rowCount, rowSet,
        async, callback);
    if (callback != null) {
      callback.onShow(presenter);
    }
    BeeKeeper.getScreen().updateActivePanel(presenter.getWidget());
  }

  private FormFactory() {
  }
}
