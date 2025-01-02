/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.ldaptive.control.ResponseControl;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for objects that override equals.
 *
 * @author  Middleware Services
 */
public class EqualsTest
{


  /**
   * Control classes.
   *
   * @return  test data
   */
  @DataProvider(name = "control-classes")
  public Object[][] controlClasses()
  {
    return
      new Object[][] {
        new Object[] {
          DirSyncControl.class,
        },
        new Object[] {
          ExtendedDnControl.class,
        },
        new Object[] {
          ForceUpdateControl.class,
        },
        new Object[] {
          GetStatsControl.class,
        },
        new Object[] {
          LazyCommitControl.class,
        },
        new Object[] {
          NotificationControl.class,
        },
        new Object[] {
          PermissiveModifyControl.class,
        },
        new Object[] {
          RangeRetrievalNoerrControl.class,
        },
        new Object[] {
          SearchOptionsControl.class,
        },
        new Object[] {
          ShowDeactivatedLinkControl.class,
        },
        new Object[] {
          ShowDeletedControl.class,
        },
        new Object[] {
          ShowRecycledControl.class,
        },
        new Object[] {
          VerifyNameControl.class,
        },
      };
  }


  @Test(dataProvider = "control-classes")
  public void controls(final Class<?> clazz)
  {
    if (ResponseControl.class.isAssignableFrom(clazz)) {
      EqualsVerifier.forClass(clazz)
        .suppress(Warning.STRICT_INHERITANCE)
        .suppress(Warning.NONFINAL_FIELDS)
        .withIgnoredFields("logger", "immutable")
        .verify();
    } else {
      EqualsVerifier.forClass(clazz)
        .suppress(Warning.STRICT_INHERITANCE)
        .suppress(Warning.NONFINAL_FIELDS)
        .withIgnoredFields("logger")
        .verify();
    }
  }
}
