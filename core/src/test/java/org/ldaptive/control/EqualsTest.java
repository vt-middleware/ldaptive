/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
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
          AuthorizationIdentityRequestControl.class,
        },
        new Object[] {
          AuthorizationIdentityResponseControl.class,
        },
        new Object[] {
          EntryChangeNotificationControl.class,
        },
        new Object[] {
          GenericControl.class,
        },
        new Object[] {
          ManageDsaITControl.class,
        },
        new Object[] {
          MatchedValuesRequestControl.class,
        },
        new Object[] {
          PagedResultsControl.class,
        },
        new Object[] {
          PasswordExpiredControl.class,
        },
        new Object[] {
          PasswordExpiringControl.class,
        },
        new Object[] {
          PasswordPolicyControl.class,
        },
        new Object[] {
          PersistentSearchRequestControl.class,
        },
        new Object[] {
          ProxyAuthorizationControl.class,
        },
        new Object[] {
          RelaxControl.class,
        },
        new Object[] {
          SessionTrackingControl.class,
        },
        new Object[] {
          SortRequestControl.class,
        },
        new Object[] {
          SortResponseControl.class,
        },
        new Object[] {
          SyncDoneControl.class,
        },
        new Object[] {
          SyncRequestControl.class,
        },
        new Object[] {
          SyncStateControl.class,
        },
        new Object[] {
          TreeDeleteControl.class,
        },
        new Object[] {
          VirtualListViewRequestControl.class,
        },
        new Object[] {
          VirtualListViewResponseControl.class,
        },
      };
  }


  @Test
  public void sortKey()
  {
    EqualsVerifier.forClass(SortKey.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .verify();
  }


  @Test(dataProvider = "control-classes")
  public void controls(final Class<?> clazz)
  {
    EqualsVerifier.forClass(clazz)
      .suppress(Warning.STRICT_INHERITANCE)
      .suppress(Warning.NONFINAL_FIELDS)
      .withIgnoredFields("logger")
      .verify();
  }
}
