package com.butent.bee.client.calendar.monthview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppointmentStackingManager {

  private int highestLayer = 0;

  private HashMap<Integer, List<AppointmentLayoutDescription>> layeredDescriptions =
      new HashMap<Integer, List<AppointmentLayoutDescription>>();

  private int layerOverflowLimit = Integer.MAX_VALUE;

  public boolean areThereAppointmentsOn(int day) {
    boolean thereAre = false;
    for (int layersIndex = 0; layersIndex <= highestLayer; layersIndex++) {
      List<AppointmentLayoutDescription> layerDescriptions = layeredDescriptions.get(layersIndex);
      if (overlapsWithDescriptionInLayer(layerDescriptions, day, day)) {
        thereAre = true;
        break;
      }
    }
    return thereAre;
  }

  public void assignLayer(AppointmentLayoutDescription description) {
    boolean layerAssigned;
    int currentlyInspectedLayer = 0;
    do {
      initLayer(currentlyInspectedLayer);
      layerAssigned = assignLayer(currentlyInspectedLayer, description);
      currentlyInspectedLayer++;
    } while (!layerAssigned);
  }

  public List<AppointmentLayoutDescription> getDescriptionsInLayer(int layerIndex) {
    return layeredDescriptions.get(layerIndex);
  }

  public int lowestLayerIndex(int day) {
    return (nextLowestLayerIndex(day, 0));
  }

  public int multidayAppointmentsOverLimitOn(int day) {
    int count = 0;
    for (int layer = 0; layer <= highestLayer; layer++) {
      List<AppointmentLayoutDescription> descriptions = layeredDescriptions.get(layer);
      if (descriptions != null) {
        for (AppointmentLayoutDescription description : descriptions) {
          if (layer > layerOverflowLimit && description.overlapsWithRange(day, day)) {
            count++;
          }
        }
      }
    }
    return count;
  }

  public int nextLowestLayerIndex(int day, int fromLayer) {
    boolean layerFound = false;
    int currentlyInspectedLayer = fromLayer;
    do {
      if (isLayerAllocated(currentlyInspectedLayer)) {
        if (overlapsWithDescriptionInLayer(layeredDescriptions.get(currentlyInspectedLayer), day,
            day)) {
          currentlyInspectedLayer++;
        } else {
          layerFound = true;
        }
      } else {
        layerFound = true;
      }

    } while (!layerFound);
    return currentlyInspectedLayer;
  }

  public void setLayerOverflowLimit(int layerOverflowLimit) {
    this.layerOverflowLimit = layerOverflowLimit;
  }

  @Override
  public String toString() {
    StringBuilder managerState = new StringBuilder();
    for (int i = 0; i <= highestLayer; i++) {
      List<AppointmentLayoutDescription> descriptions = this.getDescriptionsInLayer(i);
      if (descriptions == null) {
        continue;
      }
      for (AppointmentLayoutDescription desc : descriptions) {
        managerState.append("[").append(i).append("]");
        for (int before = 0; before < desc.getWeekStartDay(); before++) {
          managerState.append("_");
        }
        for (int dur = desc.getWeekStartDay(); dur <= desc.getWeekEndDay(); dur++) {
          managerState.append("X");
        }
        for (int after = desc.getWeekEndDay(); after < 6; after++) {
          managerState.append("_");
        }
        managerState.append(" ->").append(desc).append("\n");
      }
    }
    return managerState.toString();
  }

  private boolean assignLayer(int layer, AppointmentLayoutDescription description) {
    List<AppointmentLayoutDescription> layerDescriptions = layeredDescriptions.get(layer);

    boolean assigned = false;
    if (!overlapsWithDescriptionInLayer(layerDescriptions, description.getWeekStartDay(),
        description.getWeekEndDay())) {
      highestLayer = Math.max(highestLayer, layer);
      if (layer > layerOverflowLimit && description.spansMoreThanADay()) {
        AppointmentLayoutDescription split = description.split();
        layerDescriptions.add(description);
        assignLayer(split);
      } else {
        layerDescriptions.add(description);
      }

      assigned = true;
    }
    return assigned;
  }

  private void initLayer(int layerIndex) {
    if (!isLayerAllocated(layerIndex)) {
      layeredDescriptions.put(layerIndex, new ArrayList<AppointmentLayoutDescription>());
    }
  }

  private boolean isLayerAllocated(int layerIndex) {
    return layeredDescriptions.get(layerIndex) != null;
  }

  private boolean overlapsWithDescriptionInLayer(
      List<AppointmentLayoutDescription> layerDescriptions, int start, int end) {
    if (layerDescriptions != null) {
      for (AppointmentLayoutDescription description : layerDescriptions) {
        if (description.overlapsWithRange(start, end)) {
          return true;
        }
      }
    }
    return false;
  }
}
