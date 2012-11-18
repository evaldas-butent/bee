package com.butent.bee.client.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.view.HasGridView;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormImpl;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.ui.EditorType;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;

import java.util.List;
import java.util.Map;

/**
 * Creates and handles user interface forms.
 */

public class FormFactory {

  public interface FormCallback extends WidgetCallback, ReadyForInsertEvent.Handler, HasGridView {

    void afterAction(Action action, FormPresenter presenter);

    void afterCreate(FormView form);

    void afterCreateEditableWidget(EditableWidget editableWidget);

    void afterRefresh(FormView form, IsRow row);

    boolean beforeAction(Action action, FormPresenter presenter);

    void beforeRefresh(FormView form, IsRow row);

    FormView getFormView();

    FormCallback getInstance();

    AbstractCellRenderer getRenderer(WidgetDescription widgetDescription);

    BeeRowSet getRowSet();

    boolean hasFooter(int rowCount);

    void onSetActiveRow(IsRow row);

    void onShow(FormPresenter presenter);

    void onStart(FormView form);

    boolean onStartEdit(FormView form, IsRow row, Scheduler.ScheduledCommand focusCommand);

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

    void onSuccess(WidgetDescription result, IdentifiableWidget widget);
  }

  private static final BeeLogger logger = LogUtils.getLogger(FormFactory.class);

  public static final String TAG_FORM = "Form";

  private static final String ATTR_TYPE = "type";

  private static final Map<String, FormDescription> descriptionCache = Maps.newHashMap();
  private static final Map<String, Pair<FormCallback, Integer>> formCallbacks = Maps.newHashMap();

  public static void clearDescriptionCache() {
    descriptionCache.clear();
  }

  public static IdentifiableWidget createForm(FormDescription formDescription, String viewName,
      List<BeeColumn> columns, WidgetDescriptionCallback widgetDescriptionCallback,
      FormCallback formCallback) {
    Assert.notNull(formDescription);
    Assert.notNull(widgetDescriptionCallback);

    return createWidget(formDescription.getFormElement(), viewName, columns,
        widgetDescriptionCallback, formCallback, "createForm:");
  }

  public static FormDescription createFormDescription(String formName,
      Map<String, String> formAttributes, FormWidget rootWidget,
      Map<String, String> rootAttributes) {
    Assert.notNull(formName);

    Document doc = XMLParser.createDocument();
    Element formElement = doc.createElement(TAG_FORM);

    formElement.setAttribute(UiConstants.ATTR_NAME, formName.trim());
    XmlUtils.setAttributes(formElement, formAttributes);

    if (rootWidget != null) {
      Element rootElement = doc.createElement(rootWidget.getTagName());
      XmlUtils.setAttributes(rootElement, rootAttributes);
      formElement.appendChild(rootElement);
    }
    return new FormDescription(formElement);
  }

  public static void createFormView(final String formName, final String viewName,
      final List<BeeColumn> columns, final boolean addStyle, final FormCallback formCallback,
      final FormViewCallback viewCallback) {
    Assert.notEmpty(formName);
    Assert.notNull(viewCallback);

    getFormDescription(formName, new Callback<FormDescription>() {
      @Override
      public void onSuccess(FormDescription result) {
        FormView view = new FormImpl(formName);
        view.create(result, viewName, columns, addStyle, formCallback);
        viewCallback.onSuccess(result, view);
      }
    });
  }

  public static void createFormView(String formName, String viewName, List<BeeColumn> columns,
      boolean addStyle, FormViewCallback viewCallback) {
    createFormView(formName, viewName, columns, addStyle, getFormCallback(formName), viewCallback);
  }

  public static IdentifiableWidget createWidget(Element parent, String viewName,
      List<BeeColumn> columns, WidgetDescriptionCallback widgetDescriptionCallback,
      WidgetCallback widgetCallback, String messagePrefix) {

    Assert.notNull(parent);
    Assert.notNull(widgetDescriptionCallback, "createWidget: widgetDescriptionCallback is null");

    List<Element> children = XmlUtils.getChildrenElements(parent);
    if (BeeUtils.isEmpty(children)) {
      logger.severe(messagePrefix, "element has no children");
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
      logger.severe(messagePrefix, "root widget not found");
      return null;
    }
    if (count > 1) {
      logger.severe(messagePrefix, "element has", count, "root widgets");
      return null;
    }

    IdentifiableWidget widget = formWidget.create(root, viewName, columns,
        widgetDescriptionCallback, widgetCallback);
    if (widget == null) {
      logger.severe(messagePrefix, "cannot create root widget", formWidget);
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
    Pair<FormCallback, Integer> pair = formCallbacks.get(getFormKey(formName));
    if (pair == null || pair.getA() == null) {
      return null;
    }

    pair.setB(pair.getB() + 1);
    if (pair.getB() > 1) {
      return pair.getA().getInstance();
    } else {
      return pair.getA();
    }
  }

  public static FormDescription getFormDescription(String formName) {
    return descriptionCache.get(getFormKey(Assert.notEmpty(formName)));
  }

  public static List<Property> getInfo() {
    List<Property> info = Lists.newArrayList();

    info.add(new Property("Registered Callbacks", BeeUtils.bracket(formCallbacks.size())));
    for (Map.Entry<String, Pair<FormCallback, Integer>> entry : formCallbacks.entrySet()) {
      info.add(new Property(entry.getKey(), BeeUtils.toString(entry.getValue().getB())));
    }

    info.add(new Property("Description Cache", BeeUtils.bracket(descriptionCache.size())));
    for (Map.Entry<String, FormDescription> entry : descriptionCache.entrySet()) {
      info.add(new Property(entry.getKey(),
          BeeUtils.toString(entry.getValue().getFormElement().toString().length())));
    }
    return info;
  }

  public static String getSupplierKey(String formName, FormCallback formCallback) {
    String key = (formCallback == null) ? null : formCallback.getSupplierKey();
    if (BeeUtils.isEmpty(key)) {
      Assert.notEmpty(formName);
      key = "form_" + BeeUtils.normalize(formName);
    }
    return key;
  }

  public static FormWidget getWidgetType(BeeColumn column) {
    Assert.notNull(column);

    if (column.isForeign()) {
      return FormWidget.DATA_SELECTOR;
    }

    FormWidget widgetType;

    switch (column.getType()) {
      case BOOLEAN:
        widgetType = FormWidget.CHECK_BOX;
        break;
      case DATE:
        widgetType = FormWidget.INPUT_DATE;
        break;
      case DATETIME:
        widgetType = FormWidget.INPUT_DATE_TIME;
        break;
      case DECIMAL:
        widgetType = FormWidget.INPUT_DECIMAL;
        break;
      case INTEGER:
        widgetType = FormWidget.INPUT_INTEGER;
        break;
      case LONG:
        widgetType = FormWidget.INPUT_LONG;
        break;
      case NUMBER:
        widgetType = FormWidget.INPUT_DOUBLE;
        break;
      case TEXT:
        widgetType = column.isText() ? FormWidget.INPUT_AREA : FormWidget.INPUT_TEXT;
        break;
      case TIMEOFDAY:
        widgetType = FormWidget.INPUT_TEXT;
        break;
      default:
        Assert.untouchable();
        widgetType = null;
    }
    return widgetType;
  }

  public static void openForm(FormDescription formDescription, FormCallback formCallback,
      PresenterCallback presenterCallback) {

    String viewName = formDescription.getViewName();
    if (BeeUtils.isEmpty(viewName)) {
      showForm(formDescription, formCallback, presenterCallback);
      return;
    }

    if (formCallback != null) {
      BeeRowSet rowSet = formCallback.getRowSet();
      if (rowSet != null) {
        showForm(formDescription, viewName, rowSet.getNumberOfRows(), rowSet,
            Provider.Type.LOCAL, CachingPolicy.NONE, formCallback, presenterCallback);
        return;
      }
    }

    DataInfo dataInfo = Data.getDataInfo(viewName);
    if (dataInfo != null) {
      getInitialRowSet(viewName, dataInfo.getRowCount(), formDescription, formCallback,
          presenterCallback);
    }
  }

  public static void openForm(String formName) {
    openForm(formName, getFormCallback(formName));
  }

  public static void openForm(final String formName, final FormCallback formCallback) {

    String supplierKey = getSupplierKey(formName, formCallback);
    if (!WidgetFactory.hasSupplier(supplierKey)) {

      WidgetSupplier supplier = new WidgetSupplier() {
        @Override
        public void create(final Callback<IdentifiableWidget> callback) {

          final PresenterCallback presenterCallback = new PresenterCallback() {
            @Override
            public void onCreate(Presenter presenter) {
              callback.onSuccess(presenter.getWidget());
            }
          };

          Callback<FormDescription> descriptionCallback = new Callback<FormDescription>() {
            @Override
            public void onSuccess(FormDescription result) {
              openForm(result, formCallback, presenterCallback);
            }
          };

          getFormDescription(formName, descriptionCallback);
        }
      };

      WidgetFactory.registerSupplier(supplierKey, supplier);
    }

    getFormDescription(formName, new Callback<FormDescription>() {
      @Override
      public void onSuccess(FormDescription result) {
        openForm(result, formCallback, PresenterCallback.SHOW_IN_ACTIVE_PANEL);
      }
    });
  }

  public static FormDescription parseFormDescription(String xml) {
    Assert.notEmpty(xml);

    Document xmlDoc = XmlUtils.parse(xml);
    if (xmlDoc == null) {
      return null;
    }
    Element formElement = xmlDoc.getDocumentElement();
    if (formElement == null) {
      logger.severe("xml form element not found");
      return null;
    }

    return new FormDescription(formElement);
  }

  public static void putFormDescription(String formName, FormDescription formDescription) {
    descriptionCache.put(getFormKey(Assert.notEmpty(formName)), Assert.notNull(formDescription));
  }

  public static void registerFormCallback(String formName, FormCallback callback) {
    Assert.notEmpty(formName);
    formCallbacks.put(getFormKey(formName), Pair.of(callback, 0));
  }

  private static void getFormDescription(final String formName,
      final Callback<FormDescription> callback) {
    Assert.notEmpty(formName);
    Assert.notNull(callback);

    final String key = getFormKey(formName);
    if (descriptionCache.containsKey(key)) {
      callback.onSuccess(descriptionCache.get(key));
      return;
    }

    BeeKeeper.getRpc().sendText(Service.GET_FORM, BeeUtils.trim(formName), new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(String.class)) {
          FormDescription fd = parseFormDescription((String) response.getResponse());
          if (fd == null) {
            callback.onFailure("form", formName, "decription not created");
          } else {
            if (fd.cacheDescription()) {
              descriptionCache.put(key, fd);
            }
            callback.onSuccess(fd);
          }
        } else {
          callback.onFailure("get form description", formName, "response not a string");
        }
      }
    });
  }

  private static String getFormKey(String formName) {
    return formName.trim().toLowerCase();
  }

  private static void getInitialRowSet(final String viewName, final int rowCount,
      final FormDescription formDescription, final FormCallback callback,
      final PresenterCallback presenterCallback) {

    int limit = formDescription.getAsyncThreshold();

    final Provider.Type providerType;
    final CachingPolicy cachingPolicy;

    if (rowCount >= limit) {
      providerType = Provider.Type.ASYNC;
      cachingPolicy = CachingPolicy.FULL;

      if (rowCount <= DataUtils.getMaxInitialRowSetSize()) {
        limit = BeeConst.UNDEF;
      } else {
        limit = DataUtils.getMaxInitialRowSetSize();
      }

    } else {
      providerType = Provider.Type.CACHED;
      cachingPolicy = CachingPolicy.NONE;
      limit = BeeConst.UNDEF;
    }

    Queries.getRowSet(viewName, null, null, null, 0, limit, cachingPolicy,
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(final BeeRowSet rowSet) {
            int rc = Math.max(rowCount, rowSet.getNumberOfRows());
            showForm(formDescription, viewName, rc, rowSet, providerType, cachingPolicy,
                callback, presenterCallback);
          }
        });
  }

  private static void showForm(FormDescription formDescription, FormCallback callback,
      PresenterCallback presenterCallback) {
    showForm(formDescription, null, BeeConst.UNDEF, null, Provider.Type.CACHED,
        CachingPolicy.NONE, callback, presenterCallback);
  }

  private static void showForm(FormDescription formDescription, String viewName, int rowCount,
      BeeRowSet rowSet, Provider.Type providerType, CachingPolicy cachingPolicy,
      FormCallback callback, PresenterCallback presenterCallback) {

    FormPresenter presenter = new FormPresenter(formDescription, viewName, rowCount, rowSet,
        providerType, cachingPolicy, callback);

    if (callback != null) {
      callback.onShow(presenter);
    }
    if (presenterCallback != null) {
      presenterCallback.onCreate(presenter);
    }
  }

  private FormFactory() {
  }
}
