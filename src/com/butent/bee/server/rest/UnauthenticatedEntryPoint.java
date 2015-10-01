package com.butent.bee.server.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("hop")
public class UnauthenticatedEntryPoint extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<>();

    classes.add(HopWorker.class);

    return classes;
  }
}
