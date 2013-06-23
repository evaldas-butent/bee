package com.butent.bee.shared.modules.ec;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class EcCarModel implements BeeSerializable {

  private enum Serial {
    MODEL_ID, MODEL_NAME, MANUFACTURER, PRODUCED_FROM, PRODUCED_TO
  }
  
  public static EcCarModel restore(String s) {
    EcCarModel carModel = new EcCarModel();
    carModel.deserialize(s);
    return carModel;
  }
  
  private int modelId;
  private String modelName;
  private String manufacturer;

  private Integer producedFrom;
  private Integer producedTo;
  
  public EcCarModel(SimpleRow row) {
    this.modelId = row.getInt(COL_TCD_MODEL_ID);
    this.modelName = row.getValue(COL_TCD_MODEL_NAME);
    this.manufacturer = row.getValue(COL_TCD_MANUFACTURER);
    
    this.producedFrom = row.getInt(COL_TCD_PRODUCED_FROM);
    this.producedTo = row.getInt(COL_TCD_PRODUCED_TO);
  }

  private EcCarModel() {
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
        case MANUFACTURER:
          setManufacturer(value);
          break;

        case MODEL_ID:
          setModelId(BeeUtils.toInt(value));
          break;

        case MODEL_NAME:
          setModelName(value);
          break;

        case PRODUCED_FROM:
          setProducedFrom(BeeUtils.toIntOrNull(value));
          break;

        case PRODUCED_TO:
          setProducedTo(BeeUtils.toIntOrNull(value));
          break;
      }
    }
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public int getModelId() {
    return modelId;
  }

  public String getModelName() {
    return modelName;
  }

  public Integer getProducedFrom() {
    return producedFrom;
  }

  public Integer getProducedTo() {
    return producedTo;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case MANUFACTURER:
          arr[i++] = getManufacturer();
          break;

        case MODEL_ID:
          arr[i++] = getModelId();
          break;

        case MODEL_NAME:
          arr[i++] = getModelName();
          break;

        case PRODUCED_FROM:
          arr[i++] = getProducedFrom();
          break;

        case PRODUCED_TO:
          arr[i++] = getProducedTo();
          break;
      }
    }

    return Codec.beeSerialize(arr);
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public void setModelId(int modelId) {
    this.modelId = modelId;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public void setProducedFrom(Integer producedFrom) {
    this.producedFrom = producedFrom;
  }
  
  public void setProducedTo(Integer producedTo) {
    this.producedTo = producedTo;
  }
}
