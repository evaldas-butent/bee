package com.butent.bee.client.modules.orders;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.orders.Bundle;
import com.butent.bee.shared.modules.orders.Configuration;
import com.butent.bee.shared.modules.orders.Dimension;
import com.butent.bee.shared.modules.orders.Option;
import com.butent.bee.shared.modules.orders.Specification;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class SpecificationBuilder implements InputCallback {

  private static final class Branch {
    private final Long id;
    private final Option option;
    private final List<Branch> childs = new ArrayList<>();
    private Branch parent;
    private Configuration configuration;

    private Branch(Long id, Option option) {
      this.id = id;
      this.option = option;
    }

    private void addChild(Branch child) {
      child.parent = this;
      childs.add(child);
    }

    private List<Branch> getChilds() {
      return childs;
    }

    private Configuration getConfiguration() {
      return configuration;
    }

    private Long getId() {
      return id;
    }

    private Option getOption() {
      return option;
    }

    private Branch getParent() {
      return parent;
    }

    private void setConfiguration(Configuration configuration) {
      this.configuration = configuration;
    }
  }

  public static final String STYLE_PREFIX = "bee-spec";
  public static final String STYLE_OPTIONS = STYLE_PREFIX + "-options";
  private static final String STYLE_BOX = STYLE_PREFIX + "-box";
  public static final String STYLE_THUMBNAIL = STYLE_PREFIX + "-thumbnail";
  private static final String STYLE_SELECTABLE = STYLE_PREFIX + "-selectable";
  private static final String STYLE_DESCRIPTION = STYLE_PREFIX + "-description";

  private final Specification template;
  private final Consumer<Specification> callback;
  private final Flow container = new Flow(STYLE_PREFIX);

  private Branch currentBranch;
  private final Specification specification = new Specification();

  public SpecificationBuilder(Specification template, Consumer<Specification> callback) {
    this.template = template;
    this.callback = Assert.notNull(callback);

    Queries.getRowSet(TBL_CONF_PRICELIST, null, Filter.or(Filter.isNull(COL_VALID_UNTIL),
        Filter.isMore(COL_VALID_UNTIL, new DateValue(TimeUtils.today()))),
        new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            Branch tree = new Branch(null, null);
            Multimap<Long, Branch> hierarchy = LinkedHashMultimap.create();

            for (int i = 0; i < result.getNumberOfRows(); i++) {
              Option option = new Option(result.getLong(i, COL_OPTION),
                  result.getString(i, COL_OPTION_NAME),
                  new Dimension(result.getLong(i, COL_GROUP), result.getString(i, COL_GROUP_NAME)))
                  .setPhoto(result.getLong(i, ClassifierConstants.COL_PHOTO));

              hierarchy.put(result.getLong(i, COL_BRANCH),
                  new Branch(result.getRow(i).getId(), option));
            }
            fillTree(tree, hierarchy);
            setBranch(tree);
          }
        });
    StyleUtils.setWidth(container, BeeKeeper.getScreen().getWidth() * 0.7, CssUnit.PX);
    StyleUtils.setHeight(container, BeeKeeper.getScreen().getHeight() * 0.8, CssUnit.PX);

    Global.inputWidget(Localized.dictionary().specification(), container, this);
  }

  @Override
  public String getErrorMessage() {
    Multimap<Dimension, Option> allOptions = getAvailableOptions();

    for (Dimension dimension : allOptions.keySet()) {
      if (dimension.isRequired()) {
        boolean ok = false;

        for (Option option : allOptions.get(dimension)) {
          if (specification.getOptions().contains(option)) {
            ok = true;
            break;
          }
        }
        if (!ok) {
          return BeeUtils.join(": ", dimension.getName(), Localized.dictionary().valueRequired());
        }
      }
    }
    return null;
  }

  @Override
  public void onClose(CloseCallback closeCallback) {
    if (specification.getBundle() != null) {
      Global.confirm(Localized.dictionary().actionClose(), closeCallback::onClose);
    } else {
      closeCallback.onClose();
    }
  }

  @Override
  public void onSuccess() {
    if (specification.getBundle() != null) {
      List<String> selectedOptions = new ArrayList<>();
      Dimension dimension = null;

      for (Option option : specification.getOptions()) {
        if (!option.getDimension().isRequired()) {
          if (!Objects.equals(option.getDimension(), dimension)) {
            dimension = option.getDimension();
            selectedOptions.add("<b>" + dimension + ":</b>");
          }
          selectedOptions.add(option.toString());
        }
      }
      specification.setDescription(BeeUtils.join("<br><br>"
              + Localized.dictionary().additionalServices() + ":<br>",
          specification.getDescription(), BeeUtils.join("<br>", selectedOptions)));

      ParameterList args = OrdersKeeper.createSvcArgs(SVC_SAVE_OBJECT);
      args.addDataItem(COL_OBJECT, Codec.beeSerialize(specification));

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(BeeKeeper.getScreen());

          if (!response.hasErrors()) {
            specification.setId(response.getResponseAsLong());
            callback.accept(specification);
          }
        }
      });
    }
  }

  private static Widget buildThumbnail(Collection<Option> choices, Consumer<Integer> onChoice,
      Widget... widgets) {
    Flow thumbnail = new Flow(STYLE_THUMBNAIL);
    String styleActive = STYLE_THUMBNAIL + "-active";

    if (!ArrayUtils.isEmpty(widgets)) {
      for (Widget widget : widgets) {
        if (widget != null) {
          thumbnail.add(widget);
        }
      }
    }
    if (onChoice != null) {
      if (BeeUtils.isEmpty(choices)) {
        thumbnail.addClickHandler(clickEvent -> onChoice.accept(BeeConst.UNDEF));
        thumbnail.addStyleName(styleActive);

      } else if (choices.size() > 1 || thumbnail.isEmpty()) {
        Holder<Dimension> dimension = Holder.absent();

        for (Option option : choices) {
          if (option != null) {
            if (dimension.isNull()) {
              dimension.set(option.getDimension());
            } else if (!Objects.equals(dimension.get(), option.getDimension())) {
              dimension.clear();
              break;
            }
          }
        }
        thumbnail.addClickHandler(clickEvent -> {
          Flow box = new Flow(STYLE_BOX);
          int i = 0;

          for (Option option : choices) {
            int idx = i++;

            box.add(buildThumbnail(null, index -> {
              UiHelper.getParentPopup(box).close();
              onChoice.accept(idx);
            }, getCaptionWidgets(option, dimension.isNull())));
          }
          Global.showModalWidget(dimension.isNull() ? null : dimension.get().getName(), box,
              thumbnail.getElement());
        });
        if (thumbnail.isEmpty()) {
          if (dimension.isNotNull()) {
            thumbnail.add(new Label(dimension.get().getName()));
          }
          thumbnail.add(new FaLabel(FontAwesome.QUESTION));
        }
        thumbnail.addStyleName(styleActive);
      }
    }
    return thumbnail;
  }

  private void collectRestrictions(Multimap<Dimension, Option> allOptions, Option option,
      boolean on, Map<Option, Boolean> options) throws BeeException {

    if (!allOptions.containsValue(option) || Objects.equals(options.get(option), on)) {
      return;
    }
    if (options.containsKey(option)) {
      if (options.get(option) != null) {
        throw new BeeException(option.toString());
      } else if (on) {
        options.put(option, true);
        return;
      }
    } else if (!on) {
      options.put(option, false);
      return;
    }
    options.put(option, on);
    Configuration configuration = currentBranch.getConfiguration();

    if (on) {
      if (option.getDimension().isRequired()) {
        for (Option opt : allOptions.get(option.getDimension())) {
          if (!Objects.equals(opt, option)) {
            collectRestrictions(allOptions, opt, false, options);
          }
        }
      }
      for (Option opt : configuration.getDeniedOptions(option)) {
        collectRestrictions(allOptions, opt, false, options);
      }
      for (Option opt : configuration.getRequiredOptions(option)) {
        collectRestrictions(allOptions, opt, true, options);
      }
    } else {
      for (Option opt : options.keySet()) {
        if (configuration.getRequiredOptions(opt).contains(option)) {
          collectRestrictions(allOptions, opt, false, options);
        }
      }
    }
  }

  private static void fillTree(Branch parent, Multimap<Long, Branch> hierarchy) {
    if (hierarchy.containsKey(parent.getId())) {
      for (Branch branch : hierarchy.get(parent.getId())) {
        fillTree(branch, hierarchy);
        parent.addChild(branch);
      }
    }
  }

  private Multimap<Dimension, Option> getAvailableOptions() {
    Multimap<Dimension, Option> options = TreeMultimap.create();

    if (specification.getBundle() != null) {
      Configuration configuration = currentBranch.getConfiguration();

      for (Option option : configuration.getOptions()) {
        if (configuration.hasRelation(option, specification.getBundle())) {
          options.put(option.getDimension(), option);
        }
      }
    }
    return options;
  }

  private static Widget[] getCaptionWidgets(Option option, boolean showDimension) {
    if (option != null) {
      return new Widget[] {
          showDimension ? new Label(option.getDimension().getName()) : null,
          DataUtils.isId(option.getPhoto()) ? new Image(FileUtils.getUrl(option.getPhoto()))
              : null, new Label(option.toString())};
    } else {
      return new Widget[] {new FaLabel(FontAwesome.MINUS)};
    }
  }

  private Integer normPrice(Option option) {
    Configuration configuration = currentBranch.getConfiguration();
    Integer price = BeeUtils.toInt(BeeUtils.notEmpty(configuration.getRelationPrice(option,
        specification.getBundle()), configuration.getOptionPrice(option)));

    if (BeeUtils.isNegative(price)) {
      price = 0;
    }
    return price;
  }

  private void refresh() {
    if (currentBranch.getConfiguration() == null) {
      if (DataUtils.isId(currentBranch.getId())) {
        ParameterList args = OrdersKeeper.createSvcArgs(SVC_GET_CONFIGURATION);
        args.addDataItem(COL_BRANCH, currentBranch.getId());

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            response.notify(BeeKeeper.getScreen());

            if (!response.hasErrors()) {
              currentBranch.setConfiguration(Configuration.restore(response.getResponseAsString()));
              refresh();
            }
          }
        });
        return;
      } else {
        currentBranch.setConfiguration(new Configuration());
      }
    }
    int scroll = 0;

    for (Widget widget : container) {
      if (widget.getElement().hasClassName(STYLE_OPTIONS)) {
        scroll = widget.getElement().getScrollTop();
      }
    }
    container.clear();
    Flow header = new Flow(StyleUtils.NAME_FLEX_BOX_HORIZONTAL);
    Flow boxes = new Flow(StyleUtils.NAME_FLEXIBLE);
    header.add(boxes);
    container.add(header);

    // BRANCHES
    List<Branch> path = new ArrayList<>();
    Branch br = currentBranch;

    while (br.getParent() != null) {
      path.add(0, br);
      br = br.getParent();
    }
    Flow branchBox = new Flow(STYLE_BOX);

    for (Branch branch : path) {
      List<Branch> branches = branch.getParent().getChilds();
      List<Option> choices = new ArrayList<>();

      for (Branch bra : branches) {
        choices.add(bra.getOption());
      }
      branchBox.add(buildThumbnail(choices, index -> setBranch(branches.get(index)),
          getCaptionWidgets(branch.getOption(), true)));
    }
    if (!BeeUtils.isEmpty(currentBranch.getChilds())) {
      List<Branch> branches = currentBranch.getChilds();

      if (branches.size() == 1 && currentBranch.getConfiguration().isEmpty()) {
        setBranch(BeeUtils.peek(branches));
        return;
      } else {
        List<Option> choices = new ArrayList<>();

        for (Branch bra : branches) {
          choices.add(bra.getOption());
        }
        branchBox.add(buildThumbnail(choices, index -> setBranch(branches.get(index))));
      }
    }
    boxes.add(branchBox);

    // BUNDLES
    Flow bundleBox = new Flow(STYLE_BOX);
    Configuration configuration = currentBranch.getConfiguration();

    List<Dimension> allDimensions = configuration.getAllDimensions();
    List<Bundle> bundles = configuration.getMetrics(allDimensions);
    Set<Option> options = new HashSet<>();
    Holder<Boolean> stop = Holder.of(false);
    Holder<Boolean> proceed = Holder.of(!configuration.isEmpty());

    Configuration.processOptions(specification.getBundle(), allDimensions, (dimension, option) -> {
      Map<Option, Bundle> choices = new LinkedHashMap<>();

      for (Iterator<Bundle> iterator = bundles.iterator(); iterator.hasNext(); ) {
        Configuration.processOptions(iterator.next(), Collections.singleton(dimension),
            (dim, opt) -> {
              if (!Objects.equals(option, opt)) {
                iterator.remove();
              }
              if (!choices.containsKey(opt)) {
                Set<Option> tmp = new HashSet<>(options);

                if (opt != null) {
                  tmp.add(opt);
                }
                choices.put(opt, BeeUtils.isEmpty(tmp) ? null : new Bundle(tmp));
              }
              return true;
            });
      }
      if (option != null) {
        options.add(option);

        bundleBox.add(buildThumbnail(choices.keySet(), index ->
                setBundle(choices.get(new ArrayList<>(choices.keySet()).get(index))),
            getCaptionWidgets(option, true)));

      } else if (!BeeUtils.isEmpty(choices)) {
        boolean hasEmptyChoices = choices.containsKey(null);

        if (!hasEmptyChoices && choices.size() == 1) {
          setBundle(BeeUtils.peek(choices.values()));
          stop.set(true);
        } else {
          bundleBox.add(buildThumbnail(choices.keySet(), index ->
                  setBundle(choices.get(new ArrayList<>(choices.keySet()).get(index))),
              new Label(dimension.getName()),
              new FaLabel(hasEmptyChoices ? FontAwesome.MINUS : FontAwesome.QUESTION)));
        }
        proceed.set(hasEmptyChoices);
        return hasEmptyChoices;
      }
      return true;
    });
    if (stop.get()) {
      return;
    }
    boxes.add(bundleBox);

    if (!proceed.get()) {
      return;
    }
    header.add(specification.renderSummary(true));
    Flow subContainer = new Flow(STYLE_OPTIONS);
    container.add(subContainer);

    // OPTIONS
    Multimap<Dimension, Option> allOptions = getAvailableOptions();
    Flow optionBox = new Flow(STYLE_BOX);
    HtmlTable selectable = new HtmlTable(STYLE_SELECTABLE);

    for (Dimension dimension : allOptions.keySet()) {
      List<Option> opts = new ArrayList<>(allOptions.get(dimension));

      if (dimension.isRequired()) {
        Option option = null;
        Option def = null;

        for (Option opt : opts) {
          if (specification.getOptions().contains(opt)) {
            option = opt;
            break;
          } else if (def == null && configuration.isDefault(opt, specification.getBundle())
              && !configuration.hasRestrictions(opt)) {
            def = opt;
          }
        }
        if (option == null && def != null) {
          option = def;
          specification.addOption(option, normPrice(option));
        }
        optionBox.add(buildThumbnail(opts, index -> toggleOption(allOptions, opts.get(index), true),
            option != null ? getCaptionWidgets(option, true) : null));
      } else {
        int rowSelectable = BeeConst.UNDEF;

        for (Option option : opts) {
          if (!configuration.isDefault(option, specification.getBundle())) {
            CheckBox check = new CheckBox();
            check.setChecked(specification.getOptions().contains(option));
            check.addValueChangeHandler(valueChangeEvent ->
                toggleOption(allOptions, option, check.isChecked()));

            if (BeeConst.isUndef(rowSelectable)) {
              rowSelectable = selectable.getRowCount();
              selectable.setText(rowSelectable, 0, dimension.getName());
              selectable.getCellFormatter().setColSpan(rowSelectable, 0, 4);
            }
            rowSelectable++;
            selectable.setWidget(rowSelectable, 0, check);
            selectable.setText(rowSelectable, 1, option.getCode());
            selectable.setText(rowSelectable, 2, option.getName());
            selectable.setText(rowSelectable, 3, BeeUtils.toString(normPrice(option)));
          }
        }
      }
    }
    if (!BeeUtils.isEmpty(specification.getDescription())) {
      CustomDiv defaults = new CustomDiv(STYLE_DESCRIPTION);
      defaults.setHtml(specification.getDescription());
      subContainer.add(defaults);
    }
    subContainer.add(optionBox);
    subContainer.add(selectable);
    subContainer.getElement().setScrollTop(scroll);
  }

  private void setBranch(Branch branch) {
    List<Option> branchOptions = new ArrayList<>();
    Branch br = branch;

    while (br.getParent() != null) {
      branchOptions.add(0, br.getOption());
      br = br.getParent();
    }
    currentBranch = branch;
    specification.setBranchOptions(branch.getId(), branchOptions);
    setBundle(null);
  }

  private void setBundle(Bundle bundle) {
    List<String> defaults = new ArrayList<>();

    if (bundle != null) {
      Configuration configuration = currentBranch.getConfiguration();
      specification.setBundle(bundle, BeeUtils.toIntOrNull(configuration.getBundlePrice(bundle)));
      Dimension dimension = null;

      for (Option option : getAvailableOptions().values()) {
        if (!option.getDimension().isRequired() && configuration.isDefault(option, bundle)) {
          if (!Objects.equals(option.getDimension(), dimension)) {
            dimension = option.getDimension();
            defaults.add("<b>" + dimension + ":</b>");
          }
          defaults.add(option.toString());
        }
      }
    } else {
      specification.setBundle(null, null);
    }
    specification.setDescription(BeeUtils.join("<br>", defaults));
    specification.getOptions().clear();
    refresh();
  }

  private void toggleOption(Multimap<Dimension, Option> allOptions, Option option, boolean on) {
    Map<Option, Boolean> toggle = new TreeMap<>();

    for (Option opt : specification.getOptions()) {
      toggle.put(opt, null);
    }
    ConfirmationCallback confirmationCallback = new ConfirmationCallback() {
      @Override
      public void onCancel() {
        refresh();
      }

      @Override
      public void onConfirm() {
        for (Option opt : toggle.keySet()) {
          Boolean action = toggle.get(opt);

          if (action != null) {
            if (action) {
              specification.addOption(opt, normPrice(opt));
            } else {
              specification.getOptions().remove(opt);
            }
          }
        }
        refresh();
      }
    };
    try {
      collectRestrictions(allOptions, option, on, toggle);
      List<String> msgs = new ArrayList<>();

      for (Option opt : toggle.keySet()) {
        Boolean action = toggle.get(opt);

        if (action != null && !Objects.equals(opt, option)
            && !Objects.equals(specification.getOptions().contains(opt), action)) {

          msgs.add(BeeUtils.joinWords(action ? "+" : "-", opt));
        }
      }
      if (BeeUtils.isEmpty(msgs) || msgs.size() == 1 && option.getDimension().isRequired()) {
        confirmationCallback.onConfirm();
      } else {
        Global.confirm(option.toString(), Icon.QUESTION, msgs, confirmationCallback);
      }
    } catch (BeeException e) {
      Global.showError(option.toString(), Collections.singletonList(e.getMessage()));
      confirmationCallback.onCancel();
    }
  }
}
