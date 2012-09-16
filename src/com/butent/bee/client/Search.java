package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.ModuleManager;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.CustomWidget;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.event.RowTransformEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class Search {

  private static final String STYLE_PANEL = "bee-MainSearchContainer";
  private static final String STYLE_INPUT = "bee-MainSearchBox";

  private static final String STYLE_OPTIONS_CONTAINER = "bee-MainSearchOptionsContainer";
  private static final String STYLE_OPTIONS = "bee-MainSearchOptions";
  private static final String STYLE_SUBMIT_CONTAINER = "bee-MainSearchSubmitContainer";
  private static final String STYLE_SUBMIT = "bee-MainSearchSubmit";

  private static final String STYLE_RESULT_CONTAINER = "bee-SearchResultContainer";

  private static final String STYLE_RESULT_HEADER = "bee-SearchResultHeader";
  private static final String STYLE_RESULT_CAPTION = "bee-SearchResultCaption";
  private static final String STYLE_RESULT_MESSAGE = "bee-SearchResultMessage";
  private static final String STYLE_RESULT_CLOSE = "bee-SearchResultClose";

  private static final String STYLE_RESULT_CONTENT = "bee-SearchResultContent";
  private static final String STYLE_RESULT_VIEW = "bee-SearchResultView";
  private static final String STYLE_RESULT_ROW = "bee-SearchResultRow";
  private static final String STYLE_RESULT_MATCH = "bee-SearchResultMatch";

  private Panel panel = null;
  private InputText input = null;

  Search() {
    super();
  }

  Widget ensureWidget() {
    if (getPanel() == null) {
      createPanel();
    }
    return getPanel();
  }

  void focus() {
    if (getInput() != null) {
      getInput().setFocus(true);
    }
  }

  private void createPanel() {
    setPanel(new Flow());
    getPanel().addStyleName(STYLE_PANEL);

    setInput(new InputText());
    DomUtils.setSearch(getInput());
    getInput().addStyleName(STYLE_INPUT);

    getInput().addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          event.preventDefault();
          submit();
        }
      }
    });

    getPanel().add(getInput());

    Simple optionsContainer = new Simple();
    optionsContainer.addStyleName(STYLE_OPTIONS_CONTAINER);

    BeeImage options = new BeeImage(Global.getImages().searchOptions().getSafeUri());
    options.addStyleName(STYLE_OPTIONS);

    optionsContainer.setWidget(options);
    getPanel().add(optionsContainer);

    Simple submitContainer = new Simple();
    submitContainer.addStyleName(STYLE_SUBMIT_CONTAINER);

    BeeImage submit = new BeeImage(Global.getImages().search().getSafeUri());
    submit.addStyleName(STYLE_SUBMIT);

    submit.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        submit();
      }
    });

    submitContainer.setWidget(submit);
    getPanel().add(submitContainer);
  }

  private Widget createRowWidget(final String viewName, final BeeRow row, String text,
      String query) {
    Element element = Document.get().createPElement();

    String nt = text.toLowerCase();
    String nq = query.toLowerCase();

    int ql = query.length();

    int start = 0;
    int end = nt.indexOf(nq);

    while (end >= 0) {
      if (end > start) {
        Element span = Document.get().createSpanElement();
        span.setInnerText(text.substring(start, end));
        element.appendChild(span);
      }

      Element match = Document.get().createSpanElement();
      match.setClassName(STYLE_RESULT_MATCH);
      match.setInnerText(text.substring(end, end + ql));
      element.appendChild(match);

      start = end + ql;
      end = nt.indexOf(nq, start);
    }

    if (start < text.length()) {
      Element span = Document.get().createSpanElement();
      span.setInnerText(text.substring(start));
      element.appendChild(span);
    }

    Widget widget = new CustomWidget(element);
    widget.addStyleName(STYLE_RESULT_ROW);

    Binder.addClickHandler(widget, new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        RowEditor.openRow(viewName, row);
      }
    });

    return widget;
  }

  private InputText getInput() {
    return input;
  }

  private Panel getPanel() {
    return panel;
  }

  private void processResults(String query, List<SearchResult> results) {
    if (results.size() == 1) {
      RowEditor.openRow(results.get(0).getViewName(), results.get(0).getRow());
    } else {
      showResults(query, results);
    }
  }

  private void setInput(InputText input) {
    this.input = input;
  }

  private void setPanel(Panel panel) {
    this.panel = panel;
  }

  private void showResults(String query, List<SearchResult> results) {
    final Complex container = new Complex();
    container.addStyleName(STYLE_RESULT_CONTAINER);

    Flow header = new Flow();
    header.addStyleName(STYLE_RESULT_HEADER);

    InlineLabel caption = new InlineLabel(query);
    caption.addStyleName(STYLE_RESULT_CAPTION);
    header.add(caption);

    InlineLabel message = new InlineLabel(BeeUtils.bracket(results.size()));
    message.addStyleName(STYLE_RESULT_MESSAGE);
    header.add(message);

    BeeImage close = new BeeImage(Global.getImages().silverClose());
    close.addStyleName(STYLE_RESULT_CLOSE);
    close.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        BeeKeeper.getScreen().closeWidget(container);
      }
    });
    header.add(close);

    container.add(header);

    Flow content = new Flow();
    content.addStyleName(STYLE_RESULT_CONTENT);

    String viewName = null;
    DataInfo dataInfo = null;

    for (SearchResult result : results) {
      if (!result.getViewName().equals(viewName)) {
        viewName = result.getViewName();
        dataInfo = Data.getDataInfo(viewName);
        
        String viewCaption = LocaleUtils.maybeLocalize(dataInfo.getCaption());
        BeeLabel label = new BeeLabel(BeeUtils.notEmpty(viewCaption, viewName));
        label.addStyleName(STYLE_RESULT_VIEW);
        content.add(label);
      }

      String text = transformRow(dataInfo, result.getRow());
      Widget widget = createRowWidget(viewName, result.getRow(), text, query);

      content.add(widget);
    }
    container.add(content);

    BeeKeeper.getScreen().updateActivePanel(container);
  }

  private void submit() {
    final String value = getInput().getValue();
    if (!BeeUtils.isEmpty(value)) {
      ParameterList params = BeeKeeper.getRpc().createParameters(Service.SEARCH);
      params.addPositionalHeader(value.trim());

      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          String[] arr = Codec.beeDeserializeCollection((String) response.getResponse());
          final List<SearchResult> results = Lists.newArrayList();

          if (!BeeUtils.isEmpty(arr)) {
            for (String s : arr) {
              SearchResult result = SearchResult.restore(s);
              if (result != null) {
                results.add(result);
              }
            }
          }

          if (results.isEmpty()) {
            BeeKeeper.getScreen().notifyWarning(value, "nieko nerasta");
          } else {
            ModuleManager.maybeInitialize(new Command() {
              @Override
              public void execute() {
                processResults(value, results);
              }
            });
          }
        }
      });
    }
  }

  private String transformRow(DataInfo dataInfo, BeeRow row) {
    RowTransformEvent event = new RowTransformEvent(dataInfo.getViewName(), row);
    BeeKeeper.getBus().fireEvent(event);

    if (BeeUtils.isEmpty(event.getResult())) {
      return DataUtils.join(dataInfo, row, 1);
    } else {
      return event.getResult();
    }
  }
}
