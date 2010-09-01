package com.butent.bee.egg.server.lang;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.butent.bee.egg.server.lang.exception.CloneFailedException;

public class ObjectUtils extends com.butent.bee.egg.shared.lang.ObjectUtils {
  /**
   * Clone an object.
   * 
   * @param <T>
   *          the type of the object
   * @param o
   *          the object to clone
   * @return the clone if the object implements {@link Cloneable} otherwise
   *         <code>null</code>
   * @throws CloneFailedException
   *           if the object is cloneable and the clone operation fails
   * @since 3.0
   */
  public static <T> T clone(final T o) {
    if (o instanceof Cloneable) {
      final Object result;
      if (o.getClass().isArray()) {
        final Class<?> componentType = o.getClass().getComponentType();
        if (!componentType.isPrimitive()) {
          result = ((Object[]) o).clone();
        } else {
          int length = Array.getLength(o);
          result = Array.newInstance(componentType, length);
          while (length-- > 0) {
            Array.set(result, length, Array.get(o, length));
          }
        }
      } else {
        try {
          final Method clone = o.getClass().getMethod("clone");
          result = clone.invoke(o);
        } catch (final NoSuchMethodException e) {
          throw new CloneFailedException("Cloneable type "
              + o.getClass().getName() + " has no clone method", e);
        } catch (final IllegalAccessException e) {
          throw new CloneFailedException("Cannot clone Cloneable type "
              + o.getClass().getName(), e);
        } catch (final InvocationTargetException e) {
          throw new CloneFailedException("Exception cloning Cloneable type "
              + o.getClass().getName(), e.getCause());
        }
      }
      @SuppressWarnings("unchecked")
      final T checked = (T) result;
      return checked;
    }

    return null;
  }

  /**
   * Clone an object if possible. This method is similar to
   * {@link #clone(Object)}, but will return the provided instance as the return
   * value instead of <code>null</code> if the instance is not cloneable. This
   * is more convenient if the caller uses different implementations (e.g. of a
   * service) and some of the implementations do not allow concurrent processing
   * or have state. In such cases the implementation can simply provide a proper
   * clone implementation and the caller's code does not have to change.
   * 
   * @param <T>
   *          the type of the object
   * @param o
   *          the object to clone
   * @return the clone if the object implements {@link Cloneable} otherwise the
   *         object itself
   * @throws CloneFailedException
   *           if the object is cloneable and the clone operation fails
   * @since 3.0
   */
  public static <T> T cloneIfPossible(final T o) {
    final T clone = clone(o);
    return clone == null ? o : clone;
  }
}
