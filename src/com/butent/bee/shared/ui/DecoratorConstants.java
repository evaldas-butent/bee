package com.butent.bee.shared.ui;

public class DecoratorConstants {

  public static final String DIRECTORY = "decorators";
  public static final String SCHEMA = "decorator.xsd";
  public static final String NAMESPACE = "http://www.butent.com/decorator";
  
  public static final String TAG_DECORATORS = "Decorators"; 

  public static final String TAG_ABSTRACT = "abstract"; 
  public static final String TAG_DECORATOR = "decorator"; 

  public static final String TAG_REQUIRED_PARAM = "requiredParam"; 
  public static final String TAG_OPTIONAL_PARAM = "optionalParam"; 
  public static final String TAG_CONST = "const"; 
  public static final String TAG_STYLE = "style"; 
  public static final String TAG_LIFECYCLE = "lifecycle"; 
  public static final String TAG_HANDLER = "handler"; 
  public static final String TAG_TEMPLATE = "template"; 

  public static final String TAG_CREATED = "created";
  public static final String TAG_INSERTED = "inserted";
  public static final String TAG_REMOVED = "removed";

  public static final String TAG_CONTENT = "content";
  
  public static final String ATTR_ID = "id"; 
  public static final String ATTR_EXTENDS = "extends"; 

  public static final String ATTR_NAME = "name"; 

  public static final String ATTR_EVENT = "event";
  
  public static final String ATTR_APPLY_AUTHOR_STYLES = "apply-author-styles"; 
 
  public static final String SUBSTITUTE_PREFIX = "{";
  public static final String SUBSTITUTE_SUFFIX = "}";
  
  private DecoratorConstants() {
  }
}
