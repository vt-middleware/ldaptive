/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.ssl.SslConfig;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for objects that extends {@link AbstractConfig}.
 *
 * @author  Middleware Services
 */
public class ConfigTest
{

  /** Map of default primitive type values. */
  private static final Map<Class<?>, Object> PRIMITIVE_TYPES = new HashMap<>();

  static {
    // Initialize primitive type values
    PRIMITIVE_TYPES.put(int.class, 0);
    PRIMITIVE_TYPES.put(long.class, 0);
    PRIMITIVE_TYPES.put(boolean.class, false);
  }


  /**
   * Config test data.
   *
   * @return  test data
   */
  @DataProvider(name = "configs")
  public Object[][] createConfigs()
  {
    return
      new Object[][] {
        new Object[] {new ConnectionConfig(), },
        new Object[] {new SaslConfig(), },
        new Object[] {new SslConfig(), },
      };
  }


  /**
   * @param  config  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "config", dataProvider = "configs")
  public void testImmutable(
    // CheckStyle:IllegalType OFF
    final AbstractConfig config)
    // CheckStyle:IllegalType ON
    throws Exception
  {
    config.makeImmutable();
    for (Method method : config.getClass().getMethods()) {
      if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
        try {
          final Class<?> type = method.getParameterTypes()[0];
          if (type.isPrimitive()) {
            method.invoke(config, PRIMITIVE_TYPES.get(type));
          } else {
            method.invoke(config, new Object[] {null});
          }
          Assert.fail("Should have thrown IllegalStateException for " + method);
        } catch (Exception e) {
          Assert.assertEquals(e.getCause().getClass(), IllegalStateException.class);
        }
      }
    }
  }


  /**
   * @param  config  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "config", dataProvider = "configs")
  public void testArrayContainsNull(
    // CheckStyle:IllegalType OFF
    final AbstractConfig config)
  // CheckStyle:IllegalType ON
    throws Exception
  {
    for (Method method : config.getClass().getMethods()) {
      if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
        try {
          final Class<?> type = method.getParameterTypes()[0];
          if (type.isArray()) {
            method.invoke(config, new Object[] {Array.newInstance(type.getComponentType(), 1)});
            Assert.fail("Should have thrown IllegalArgumentException for " + method);
          }
        } catch (Exception e) {
          Assert.assertEquals(e.getCause().getClass(), IllegalArgumentException.class);
        }
      }
    }
  }
}
