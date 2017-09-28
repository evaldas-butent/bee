package com.butent.bee.server.rest;

import com.butent.bee.shared.modules.administration.AdministrationConstants;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath(AdministrationConstants.FILE_URL)
public class FileServiceEntryPoint extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<>();

    classes.add(AuthenticationFilter.class);
    classes.add(FileServiceApplication.class);

    return classes;
  }
}
