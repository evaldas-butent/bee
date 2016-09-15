#!/bin/bash

java -cp lib/*:war/WEB-INF/lib/guava-18.0.jar:/opt/gwt/*:war/WEB-INF/classes -Xmx2g com.google.gwt.dev.codeserver.CodeServer -noprecompile -src src/ com.butent.bee.Dev
