package com.butent.bee.egg.server.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.SubProp;

public class BeeClass {
  public static <T> List<SubProp> getClassInfo(Class<T> cls) {
    Assert.notNull(cls);

    List<SubProp> lst = new ArrayList<SubProp>();

    PropUtils.addPropSub(lst, false, "Package",
        transformPackage(cls.getPackage()), "Name", cls.getName(),
        "Simple Name", cls.getSimpleName(), "Canonical Name",
        cls.getCanonicalName(), "Component Type",
        transformClass(cls.getComponentType()), "Declaring Class",
        transformClass(cls.getDeclaringClass()), "Enclosing Class",
        transformClass(cls.getEnclosingClass()), "Enclosing Constructor",
        transformConstructor(cls.getEnclosingConstructor()),
        "Enclosing Method", transformMethod(cls.getEnclosingMethod()),
        "Superclass", transformClass(cls.getSuperclass()),
        "Generic Superclass", cls.getGenericSuperclass());

    PropUtils.addSub(lst, "Modifiers", cls.getModifiers(),
        Modifier.toString(cls.getModifiers()));

    PropUtils.addPropSub(lst, false, "Is Annotation", cls.isAnnotation(),
        "Is Anonymous Class", cls.isAnonymousClass(), "Is Array",
        cls.isArray(), "Is Enum", cls.isEnum(), "Is Interface",
        cls.isInterface(), "Is Local Class", cls.isLocalClass(),
        "Is Member Class", cls.isMemberClass(), "Is Primitive",
        cls.isPrimitive(), "Is Synthetic", cls.isSynthetic(), "Class Loader",
        cls.getClassLoader(), "Desired Assertion Status",
        cls.desiredAssertionStatus(), "Hash Code", cls.hashCode(), "To String",
        cls.toString());

    // very long "Protection Domain", cls.getProtectionDomain(),

    Annotation[] annArr = getDeclaredAnnotations(cls);
    if (!BeeUtils.isEmpty(annArr))
      for (Annotation ann : annArr)
        PropUtils.addSub(lst, "Declared Annotation", transformAnnotation(ann));

    annArr = getAnnotations(cls);
    if (!BeeUtils.isEmpty(annArr))
      for (Annotation ann : annArr)
        PropUtils.addSub(lst, "Annotation", transformAnnotation(ann));

    Class<?>[] clArr = getDeclaredClasses(cls);
    if (!BeeUtils.isEmpty(clArr))
      for (Class<?> z : clArr)
        PropUtils.addSub(lst, "Declared Class", transformClass(z));

    clArr = getClasses(cls);
    if (!BeeUtils.isEmpty(clArr))
      for (Class<?> z : clArr)
        PropUtils.addSub(lst, "Class", transformClass(z));

    Constructor<?>[] constrArr = getDeclaredConstructors(cls);
    if (!BeeUtils.isEmpty(constrArr))
      for (Constructor<?> constr : constrArr)
        PropUtils.addSub(lst, "Declared Constructor",
            transformConstructor(constr));

    constrArr = getConstructors(cls);
    if (!BeeUtils.isEmpty(constrArr))
      for (Constructor<?> constr : constrArr)
        PropUtils.addSub(lst, "Constructor", transformConstructor(constr));

    Method[] methArr = getDeclaredMethods(cls);
    if (!BeeUtils.isEmpty(methArr))
      for (Method meth : methArr)
        PropUtils.addSub(lst, "Declared Method", transformMethod(meth));

    methArr = getMethods(cls);
    if (!BeeUtils.isEmpty(methArr))
      for (Method meth : methArr)
        PropUtils.addSub(lst, "Method", transformMethod(meth));

    Field[] fldArr = getDeclaredFields(cls);
    if (!BeeUtils.isEmpty(fldArr))
      for (Field fld : fldArr)
        PropUtils.addSub(lst, "Declared Field", transformField(fld));

    fldArr = getDeclaredFields(cls);
    if (!BeeUtils.isEmpty(fldArr))
      for (Field fld : fldArr)
        PropUtils.addSub(lst, "Field", transformField(fld));

    T[] enumArr = getEnumConstants(cls);
    if (!BeeUtils.isEmpty(enumArr))
      for (T en : enumArr)
        PropUtils.addSub(lst, "Enum Constant", en);

    Type[] tpArr = getGenericInterfaces(cls);
    if (!BeeUtils.isEmpty(tpArr))
      for (Type tp : tpArr)
        PropUtils.addSub(lst, "Generic Interface", tp);

    clArr = getInterfaces(cls);
    if (!BeeUtils.isEmpty(clArr))
      for (Class<?> z : clArr)
        PropUtils.addSub(lst, "Interface", transformClass(z));

    Object[] objArr = getSigners(cls);
    if (!BeeUtils.isEmpty(objArr))
      for (Object obj : objArr)
        PropUtils.addSub(lst, "Signer", obj);

    TypeVariable<Class<T>>[] tpParArr = getTypeParameters(cls);
    if (!BeeUtils.isEmpty(tpParArr))
      for (TypeVariable<Class<T>> tpPar : tpParArr)
        PropUtils.addSub(lst, "Type Parameter", tpPar.getGenericDeclaration(),
            tpPar.getName());

    return lst;
  }

  public static Annotation[] getAnnotations(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getAnnotations();
  }

  public static Class<?>[] getClasses(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getClasses();
  }

  public static Constructor<?>[] getConstructors(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getConstructors();
  }

  public static Annotation[] getDeclaredAnnotations(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getDeclaredAnnotations();
  }

  public static Class<?>[] getDeclaredClasses(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getDeclaredClasses();
  }

  public static Constructor<?>[] getDeclaredConstructors(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getDeclaredConstructors();
  }

  public static Field[] getDeclaredFields(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getDeclaredFields();
  }

  public static Method[] getDeclaredMethods(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getDeclaredMethods();
  }

  public static <T> T[] getEnumConstants(Class<T> cls) {
    Assert.notNull(cls);
    return cls.getEnumConstants();
  }

  public static Field[] getFields(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getFields();
  }

  public static Method[] getMethods(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getMethods();
  }

  public static Type[] getGenericInterfaces(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getGenericInterfaces();
  }

  public static Class<?>[] getInterfaces(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getInterfaces();
  }

  public static Object[] getSigners(Class<?> cls) {
    Assert.notNull(cls);
    return cls.getSigners();
  }

  public static <T> TypeVariable<Class<T>>[] getTypeParameters(Class<T> cls) {
    Assert.notNull(cls);
    return cls.getTypeParameters();
  }

  public static String transformAnnotation(Annotation ann) {
    if (ann == null)
      return null;
    else
      return ann.toString();
  }

  private static String transformClass(Class<?> cls) {
    if (cls == null)
      return null;
    else
      return cls.getName();
  }

  private static String transformConstructor(Constructor<?> constr) {
    if (constr == null)
      return null;
    else
      return constr.toGenericString();
  }

  private static String transformField(Field fld) {
    if (fld == null)
      return null;
    else
      return fld.toGenericString();
  }

  private static String transformMethod(Method meth) {
    if (meth == null)
      return null;
    else
      return meth.toGenericString();
  }

  private static String transformPackage(Package pck) {
    if (pck == null)
      return null;
    else
      return pck.getName();
  }

}
