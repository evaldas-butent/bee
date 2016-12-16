package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResults implements BeeSerializable {

  public static AnalysisResults restore(String s) {
    AnalysisResults ar = new AnalysisResults();
    ar.deserialize(s);
    return ar;
  }

  private final List<AnalysisValue> values = new ArrayList<>();

  public AnalysisResults() {
  }

  public void add(AnalysisValue value) {
    if (value != null) {
      values.add(value);
    }
  }

  public List<AnalysisValue> getValues() {
    return values;
  }

  public boolean isEmpty() {
    return values.isEmpty();
  }

  @Override
  public void deserialize(String s) {
    if (!values.isEmpty()) {
      values.clear();
    }

    String[] arr = Codec.beeDeserializeCollection(s);
    if (arr != null) {
      for (String vs : arr) {
        add(AnalysisValue.restore(vs));
      }
    }
  }

  @Override
  public String serialize() {
    return Codec.beeSerialize(values);
  }
}
