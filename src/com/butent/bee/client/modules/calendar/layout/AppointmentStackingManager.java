package com.butent.bee.client.modules.calendar.layout;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.client.modules.calendar.Appointment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppointmentStackingManager {

  private final Map<Integer, List<AppointmentLayoutDescription>> layers = Maps.newHashMap();

  private final int maxLayer;

  private int highestLayer = 0;
  
  public AppointmentStackingManager(int maxLayer) {
    this.maxLayer = maxLayer;
  }

  public void assignLayer(AppointmentLayoutDescription description) {
    boolean layerAssigned = false;
    int layer = 0;

    do {
      initLayer(layer);
      layerAssigned = assignLayer(layer, description);
      layer++;
    } while (!layerAssigned);
  }
  
  public void assignLayer(int fromWeekDay, int toWeekDay, Appointment appointment) {
    assignLayer(new AppointmentLayoutDescription(fromWeekDay, toWeekDay, appointment));
  }

  public int countOverLimit(int day) {
    int count = 0;
    for (int layer = 0; layer <= highestLayer; layer++) {
      List<AppointmentLayoutDescription> descriptions = layers.get(layer);
      if (descriptions != null) {
        for (AppointmentLayoutDescription description : descriptions) {
          if (layer > maxLayer && description.overlaps(day, day)) {
            count++;
          }
        }
      }
    }
    return count;
  }

  public List<AppointmentLayoutDescription> getDescriptionsInLayer(int layerIndex) {
    return layers.get(layerIndex);
  }

  public List<Appointment> getOverLimit(int day) {
    List<Appointment> result = Lists.newArrayList();

    for (int layer = 0; layer <= highestLayer; layer++) {
      List<AppointmentLayoutDescription> descriptions = layers.get(layer);
      if (descriptions != null) {
        for (AppointmentLayoutDescription description : descriptions) {
          if (layer > maxLayer && description.overlaps(day, day)) {
            result.add(description.getAppointment());
          }
        }
      }
    }
    return result;
  }

  public int lowestLayerIndex(int day) {
    return nextLowestLayerIndex(day, 0);
  }
  
  public int nextLowestLayerIndex(int day, int fromLayer) {
    boolean layerFound = false;
    int currentlyInspectedLayer = fromLayer;

    do {
      if (isLayerAllocated(currentlyInspectedLayer)) {
        if (overlaps(layers.get(currentlyInspectedLayer), day, day)) {
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

  private boolean assignLayer(int layer, AppointmentLayoutDescription description) {
    List<AppointmentLayoutDescription> layerDescriptions = layers.get(layer);

    boolean assigned = false;
    if (!overlaps(layerDescriptions, description.getWeekStartDay(), description.getWeekEndDay())) {
      highestLayer = Math.max(highestLayer, layer);

      if (layer > maxLayer && description.spansMoreThanADay()) {
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
      layers.put(layerIndex, new ArrayList<AppointmentLayoutDescription>());
    }
  }

  private boolean isLayerAllocated(int layerIndex) {
    return layers.get(layerIndex) != null;
  }

  private boolean overlaps(List<AppointmentLayoutDescription> descriptions, int start, int end) {
    if (descriptions != null) {
      for (AppointmentLayoutDescription description : descriptions) {
        if (description.overlaps(start, end)) {
          return true;
        }
      }
    }
    return false;
  }
}
