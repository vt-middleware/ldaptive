/*
  $Id: ConfigTest.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.provider.ProviderConfig;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.ssl.SslConfig;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for objects that extends {@link AbstractConfig}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class ConfigTest
{

  /** Map of default primitive type values. */
  private static final Map<Class<?>, Object> PRIMITIVE_TYPES =
    new HashMap<Class<?>, Object>();

  /**
   * Initialize primitive type values.
   */
  static {
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
        new Object[] {new PoolConfig(), },
        new Object[] {new ProviderConfig(), },
        new Object[] {new SaslConfig(), },
        new Object[] {new SslConfig(), },
      };
  }


  /**
   * @param  config  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"config"},
    dataProvider = "configs"
  )
  public void testImmutable(
    // CheckStyle:IllegalType OFF
    final AbstractConfig config)
    // CheckStyle:IllegalType ON
    throws Exception
  {
    config.makeImmutable();
    for (Method method : config.getClass().getMethods()) {
      if (method.getName().startsWith("set") &&
          method.getParameterTypes().length == 1) {
        try {
          final Class<?> type = method.getParameterTypes()[0];
          if (type.isPrimitive()) {
            method.invoke(config, PRIMITIVE_TYPES.get(type));
          } else {
            method.invoke(config, new Object[] {null});
          }
          Assert.fail("Should have thrown IllegalStateException for " + method);
        } catch (Exception e) {
          AssertJUnit.assertEquals(
            IllegalStateException.class,
            e.getCause().getClass());
        }
      }
    }
  }
}
