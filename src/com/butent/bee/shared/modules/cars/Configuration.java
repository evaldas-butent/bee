package com.butent.bee.shared.modules.cars;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Configuration implements BeeSerializable {

  public static final class DataInfo implements BeeSerializable {
    private String price;
    private String description;
    private Map<String, String> criteria = new LinkedHashMap<>();

    private DataInfo() {
    }

    @Override
    public void deserialize(String s) {
      String[] dataInfo = Codec.beeDeserializeCollection(s);
      this.price = dataInfo[0];
      this.description = dataInfo[1];
      setCriteria(Codec.deserializeLinkedHashMap(ArrayUtils.getQuietly(dataInfo, 2)));
    }

    public Map<String, String> getCriteria() {
      return criteria;
    }

    public String getDescription() {
      return description;
    }

    public String getPrice() {
      return price;
    }

    public static DataInfo of(String prc, String descr, String crit) {
      DataInfo info = new DataInfo();
      info.price = prc;
      info.description = BeeUtils.isEmpty(descr) ? null : descr.replace("\n", "<br>");
      info.setCriteria(Codec.deserializeLinkedHashMap(crit));
      return info;
    }

    public static DataInfo restore(String s) {
      if (BeeUtils.isEmpty(s)) {
        return null;
      }
      DataInfo info = new DataInfo();
      info.deserialize(s);
      return info;
    }

    @Override
    public String serialize() {
      return Codec.beeSerialize(new Object[] {price, description, criteria});
    }

    public DataInfo setCriteria(Map<String, String> newCriteria) {
      this.criteria.clear();

      if (!BeeUtils.isEmpty(newCriteria)) {
        this.criteria.putAll(newCriteria);
      }
      return this;
    }

    public void setPrice(String price) {
      this.price = price;
    }
  }

  public static final String DEFAULT_PRICE = BeeUtils.toString(BeeConst.UNDEF);

  private enum Serial {
    ROW_DIMENSIONS, COL_DIMENSIONS, DATA, RELATIONS, RESTRICTIONS
  }

  private final Map<Bundle, Pair<DataInfo, Boolean>> data = new HashMap<>();
  private final Map<Option, Pair<DataInfo, Map<String, DataInfo>>> relations = new TreeMap<>();
  private final Map<Option, Map<Option, Boolean>> restrictions = new HashMap<>();

  private final List<Dimension> rowDimensions = new ArrayList<>();
  private final List<Dimension> colDimensions = new ArrayList<>();

  public void addBundle(Bundle bundle) {
    if (!data.containsKey(bundle)) {
      List<Dimension> allDimensions = new ArrayList<>(rowDimensions);
      allDimensions.addAll(colDimensions);
      List<Option> orphans = processOptions(bundle, allDimensions, null);

      for (Option option : orphans) {
        addDimension(option.getDimension(), null);
      }
      data.put(bundle, null);
    }
  }

  public void addDimension(Dimension dimension, Integer ordinal) {
    Integer idx = ordinal;
    List<Dimension> dimensions = BeeUtils.isNegative(idx) ? colDimensions : rowDimensions;

    if (BeeUtils.isNegative(idx)) {
      idx = Math.abs(idx) - 1;
    }
    if (idx != null && BeeUtils.isIndex(dimensions, idx)) {
      dimensions.add(idx, dimension);
    } else {
      dimensions.add(dimension);
    }
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case ROW_DIMENSIONS:
        case COL_DIMENSIONS:
          List<Dimension> dimensionList = Objects.equals(member, Serial.ROW_DIMENSIONS)
              ? rowDimensions : colDimensions;
          dimensionList.clear();
          String[] dimData = Codec.beeDeserializeCollection(value);

          if (!ArrayUtils.isEmpty(dimData)) {
            for (String d : dimData) {
              dimensionList.add(Dimension.restore(d));
            }
          }
          break;

        case DATA:
          data.clear();
          Codec.deserializeHashMap(value).forEach((key, val) -> {
            Pair<String, String> pair = Pair.restore(val);
            data.put(Bundle.restore(key),
                Pair.of(DataInfo.restore(pair.getA()), BeeUtils.toBoolean(pair.getB())));
          });
          break;

        case RELATIONS:
          relations.clear();
          Codec.deserializeHashMap(value).forEach((key, val) -> {
            Pair<String, String> pair = Pair.restore(val);
            Map<String, DataInfo> map = new HashMap<>();

            Codec.deserializeHashMap(pair.getB())
                .forEach((key2, val2) -> map.put(key2, DataInfo.restore(val2)));

            relations.put(Option.restore(key), Pair.of(DataInfo.restore(pair.getA()), map));
          });
          break;

        case RESTRICTIONS:
          restrictions.clear();
          Codec.deserializeLinkedHashMap(value).forEach((key, val) -> {
            Map<Option, Boolean> map = new HashMap<>();

            Codec.deserializeHashMap(val)
                .forEach((key2, val2) -> map.put(Option.restore(key2), BeeUtils.toBoolean(val2)));

            restrictions.put(Option.restore(key), map);
          });
          break;
      }
    }
  }

  public Collection<Bundle> getAllBundles() {
    return data.keySet();
  }

  public List<Dimension> getAllDimensions() {
    List<Dimension> allDimensions = new ArrayList<>(getRowDimensions());
    allDimensions.addAll(getColDimensions());
    return allDimensions;
  }

  public Map<String, String> getBundleCriteria(Bundle bundle) {
    Pair<DataInfo, Boolean> pair = data.get(bundle);
    return pair != null && pair.getA() != null ? pair.getA().getCriteria() : null;
  }

  public String getBundleDescription(Bundle bundle) {
    Pair<DataInfo, Boolean> pair = data.get(bundle);
    return pair != null && pair.getA() != null ? pair.getA().getDescription() : null;
  }

  public String getBundlePrice(Bundle bundle) {
    Pair<DataInfo, Boolean> pair = data.get(bundle);
    return pair != null && pair.getA() != null ? pair.getA().getPrice() : null;
  }

  public Collection<Bundle> getBundles() {
    return getAllBundles().stream().filter(bundle -> getBundlePrice(bundle) != null)
        .collect(Collectors.toSet());
  }

  public boolean isBundleBlocked(Bundle bundle) {
    Pair<DataInfo, Boolean> pair = data.get(bundle);
    return pair != null && BeeUtils.unbox(pair.getB());
  }

  public List<Dimension> getColDimensions() {
    return colDimensions;
  }

  public Set<Option> getDeniedOptions(Option option) {
    Set<Option> set = new HashSet<>();

    if (hasRestrictions(option)) {
      for (Map.Entry<Option, Boolean> entry : getRestrictions(option).entrySet()) {
        if (entry.getValue()) {
          set.add(entry.getKey());
        }
      }
    }
    return set;
  }

  public List<Bundle> getMetrics(List<Dimension> dimensions) {
    Set<Bundle> bundles = new HashSet<>();

    for (Bundle bundle : data.keySet()) {
      List<Option> options = new ArrayList<>();

      processOptions(bundle, dimensions, (dimension, option) -> {
        if (option != null) {
          options.add(option);
        }
        return true;
      });
      if (!options.isEmpty()) {
        bundles.add(new Bundle(options));
      }
    }
    List<Bundle> metrics = new ArrayList<>(bundles);

    metrics.sort((b1, b2) -> {
      List<Option> l1 = new ArrayList<>();
      List<Option> l2 = new ArrayList<>();
      processOptions(b1, dimensions, (dimension, option) -> l1.add(option));
      processOptions(b2, dimensions, (dimension, option) -> l2.add(option));

      for (int i = 0; i < dimensions.size(); i++) {
        Option o1 = l1.get(i);
        Option o2 = l2.get(i);

        if (!Objects.equals(o1, o2)) {
          return o1 == null ? -1 : o1.compareTo(o2);
        }
      }
      return 0;
    });
    return metrics;
  }

  public Map<String, String> getOptionCriteria(Option option) {
    Pair<DataInfo, Map<String, DataInfo>> pair = relations.get(option);
    return pair != null && pair.getA() != null ? pair.getA().getCriteria() : null;
  }

  public String getOptionDescription(Option option) {
    Pair<DataInfo, Map<String, DataInfo>> pair = relations.get(option);
    return pair != null && pair.getA() != null ? pair.getA().getDescription() : null;
  }

  public String getOptionPrice(Option option) {
    Pair<DataInfo, Map<String, DataInfo>> pair = relations.get(option);
    return pair != null && pair.getA() != null ? pair.getA().getPrice() : null;
  }

  public Collection<Option> getOptions() {
    return relations.keySet();
  }

  public Map<String, String> getRelationCriteria(Option option, Bundle bundle) {
    Pair<DataInfo, Map<String, DataInfo>> pair = relations.get(option);
    return pair != null && pair.getB().get(bundle.getKey()) != null
        ? pair.getB().get(bundle.getKey()).getCriteria() : null;
  }

  public String getRelationDescription(Option option, Bundle bundle) {
    Pair<DataInfo, Map<String, DataInfo>> pair = relations.get(option);
    return pair != null && pair.getB().get(bundle.getKey()) != null
        ? pair.getB().get(bundle.getKey()).getDescription() : null;
  }

  public String getRelationPrice(Option option, Bundle bundle) {
    Pair<DataInfo, Map<String, DataInfo>> pair = relations.get(option);
    return pair != null && pair.getB().get(bundle.getKey()) != null
        ? pair.getB().get(bundle.getKey()).getPrice() : null;
  }

  public Set<Option> getRequiredOptions(Option option) {
    Set<Option> set = new HashSet<>();

    if (hasRestrictions(option)) {
      for (Map.Entry<Option, Boolean> entry : getRestrictions(option).entrySet()) {
        if (!entry.getValue()) {
          set.add(entry.getKey());
        }
      }
    }
    return set;
  }

  public Map<Option, Boolean> getRestrictions(Option option) {
    if (!restrictions.containsKey(option)) {
      restrictions.put(option, new HashMap<>());
    }
    return restrictions.get(option);
  }

  public List<Dimension> getRowDimensions() {
    return rowDimensions;
  }

  public boolean hasRelation(Option option, Bundle bundle) {
    Pair<DataInfo, Map<String, DataInfo>> pair = relations.get(option);
    return pair != null && pair.getB().containsKey(bundle.getKey());
  }

  public boolean hasRelations(Bundle bundle) {
    for (Pair<DataInfo, Map<String, DataInfo>> pair : relations.values()) {
      if (pair.getB().containsKey(bundle.getKey())) {
        return true;
      }
    }
    return false;
  }

  public boolean hasRestrictions(Option option) {
    return !BeeUtils.isEmpty(restrictions.get(option));
  }

  public boolean isDefault(Option option, Bundle bundle) {
    return DEFAULT_PRICE.equals(getRelationPrice(option, bundle));
  }

  public boolean isEmpty() {
    return data.isEmpty();
  }

  public static List<Option> processOptions(Bundle bundle, Collection<Dimension> dimensions,
      BiFunction<Dimension, Option, Boolean> consumer) {
    List<Option> options = new ArrayList<>();

    if (bundle != null) {
      options.addAll(bundle.getOptions());
    }
    for (Dimension dimension : dimensions) {
      boolean found = false;

      for (Iterator<Option> it = options.iterator(); it.hasNext(); ) {
        Option option = it.next();
        found = Objects.equals(option.getDimension(), dimension);

        if (found) {
          if (consumer != null && !BeeUtils.unbox(consumer.apply(dimension, option))) {
            return options;
          }
          it.remove();
          break;
        }
      }
      if (!found && consumer != null && !BeeUtils.unbox(consumer.apply(dimension, null))) {
        break;
      }
    }
    return options;
  }

  public Set<Bundle> removeBundles(Set<Bundle> bundles) {
    Set<Bundle> realBundles = new HashSet<>();

    for (Bundle bundle : bundles) {
      if (Objects.nonNull(data.remove(bundle))) {
        realBundles.add(bundle);
        removeRelations(bundle);
      }
    }
    return realBundles;
  }

  public Set<Bundle> removeBundlesByDimension(Dimension dimension) {
    Set<Bundle> realBundles = new HashSet<>();

    for (Iterator<Map.Entry<Bundle, Pair<DataInfo, Boolean>>> iterator = data.entrySet().iterator();
         iterator.hasNext(); ) {
      Map.Entry<Bundle, Pair<DataInfo, Boolean>> entry = iterator.next();
      Bundle bundle = entry.getKey();

      for (Option option : bundle.getOptions()) {
        if (Objects.equals(option.getDimension(), dimension)) {
          if (Objects.nonNull(entry.getValue()) && Objects.nonNull(entry.getValue().getA())) {
            realBundles.add(bundle);
          }
          removeRelations(bundle);
          iterator.remove();
          break;
        }
      }
    }
    return realBundles;
  }

  public void removeOption(Option option) {
    relations.remove(option);
    restrictions.remove(option);
  }

  public void removeRelation(Option option, Bundle bundle) {
    Pair<DataInfo, Map<String, DataInfo>> pair = relations.get(option);

    if (pair != null) {
      pair.getB().remove(bundle.getKey());
    }
  }

  public void removeRestriction(Option option, Option relatedOption) {
    Boolean oldValue = getRestrictions(option).remove(relatedOption);

    if (BeeUtils.unbox(oldValue)) {
      getRestrictions(relatedOption).remove(option);
    }
  }

  public static Configuration restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    Configuration conf = new Configuration();
    conf.deserialize(s);
    return conf;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case ROW_DIMENSIONS:
          arr[i++] = rowDimensions;
          break;

        case COL_DIMENSIONS:
          arr[i++] = colDimensions;
          break;

        case DATA:
          arr[i++] = data;
          break;

        case RELATIONS:
          arr[i++] = relations;
          break;

        case RESTRICTIONS:
          arr[i++] = restrictions;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setBundleInfo(Bundle bundle, DataInfo info, Boolean blocked) {
    addBundle(bundle);
    data.put(bundle, Objects.isNull(info) ? null : Pair.of(info, blocked));

    if (Objects.isNull(data.get(bundle))) {
      removeRelations(bundle);
    }
  }

  public void setOptionInfo(Option option, DataInfo info) {
    addOption(option);
    relations.get(option).setA(info);
  }

  public void setRelationInfo(Option option, Bundle bundle, DataInfo info) {
    addOption(option);
    relations.get(option).getB().put(bundle.getKey(), info);
  }

  public void setRestriction(Option option, Option relatedOption, boolean denied) {
    Boolean oldValue = getRestrictions(option).put(relatedOption, denied);

    if (denied) {
      getRestrictions(relatedOption).put(option, denied);
    } else if (BeeUtils.unbox(oldValue)) {
      getRestrictions(relatedOption).remove(option);
    }
  }

  private void addOption(Option option) {
    if (!relations.containsKey(option)) {
      relations.put(option, Pair.of(null, new HashMap<>()));
    }
  }

  private void removeRelations(Bundle bundle) {
    for (Pair<DataInfo, Map<String, DataInfo>> pair : relations.values()) {
      pair.getB().remove(bundle.getKey());
    }
  }
}
