/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import org.ldaptive.ad.handler.ObjectGuidHandler;
import org.ldaptive.ad.handler.ObjectSidHandler;
import org.ldaptive.ad.handler.PrimaryGroupIdHandler;
import org.ldaptive.ad.handler.RangeEntryHandler;
import org.ldaptive.auth.AggregateAuthenticationHandler;
import org.ldaptive.auth.AggregateAuthenticationResponseHandler;
import org.ldaptive.auth.AggregateDnResolver;
import org.ldaptive.auth.AggregateEntryResolver;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.AuthorizationIdentityEntryResolver;
import org.ldaptive.auth.CompareAuthenticationHandler;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.SearchDnResolver;
import org.ldaptive.auth.SearchEntryResolver;
import org.ldaptive.auth.SimpleBindAuthenticationHandler;
import org.ldaptive.auth.WhoAmIEntryResolver;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.EDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.FreeIPAAuthenticationResponseHandler;
import org.ldaptive.control.SortKey;
import org.ldaptive.control.util.PagedResultsClient;
import org.ldaptive.control.util.VirtualListViewClient;
import org.ldaptive.handler.CaseChangeEntryHandler;
import org.ldaptive.handler.DnAttributeEntryHandler;
import org.ldaptive.handler.MergeAttributeEntryHandler;
import org.ldaptive.handler.RecursiveResultHandler;
import org.ldaptive.jaas.SearchRoleResolver;
import org.ldaptive.pool.BindConnectionPassivator;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.referral.FollowSearchReferralHandler;
import org.ldaptive.referral.FollowSearchResultReferenceHandler;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.ssl.SslConfig;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for objects that implement {@link Freezable}.
 *
 * @author  Middleware Services
 */
public class FreezableTest
{


  /**
   * Immutable classes.
   *
   * @return  test data
   */
  @DataProvider(name = "immutable-classes")
  public Object[][] immutableClasses()
  {
    return
      new Object[][] {
        new Object[] {
          CompareAuthenticationHandler.class,
        },
        new Object[] {
          SimpleBindAuthenticationHandler.class,
        },
        new Object[] {
          ConnectionConfig.class,
        },
        new Object[] {
          SaslConfig.class,
        },
        new Object[] {
          SslConfig.class,
        },
        new Object[] {
          PooledConnectionFactory.class,
        },
        new Object[] {
          ActivePassiveConnectionStrategy.class,
        },
        new Object[] {
          DnsResolverConnectionStrategy.class,
        },
        new Object[] {
          DnsSrvConnectionStrategy.class,
        },
        new Object[] {
          RandomConnectionStrategy.class,
        },
        new Object[] {
          RoundRobinConnectionStrategy.class,
        },
        new Object[] {
          CompareConnectionValidator.class,
        },
        new Object[] {
          SearchConnectionValidator.class,
        },
        new Object[] {
          IdlePruneStrategy.class,
        },
        new Object[] {
          AuthorizationIdentityEntryResolver.class,
        },
        new Object[] {
          SearchEntryResolver.class,
        },
        new Object[] {
          WhoAmIEntryResolver.class,
        },
        new Object[] {
          SearchDnResolver.class,
        },
        new Object[] {
          SearchRoleResolver.class,
        },
        new Object[] {
          ActiveDirectoryAuthenticationResponseHandler.class,
        },
        new Object[] {
          AggregateAuthenticationHandler.class,
        },
        new Object[] {
          AggregateAuthenticationResponseHandler.class,
        },
        new Object[] {
          AggregateDnResolver.class,
        },
        new Object[] {
          AggregateEntryResolver.class,
        },
        new Object[] {
          Authenticator.class,
        },
        new Object[] {
          BindConnectionInitializer.class,
        },
        new Object[] {
          BindConnectionPassivator.class,
        },
        new Object[] {
          DefaultConnectionFactory.class,
        },
        new Object[] {
          SingleConnectionFactory.class,
        },
        new Object[] {
          EDirectoryAuthenticationResponseHandler.class,
        },
        new Object[] {
          FormatDnResolver.class,
        },
        new Object[] {
          FreeIPAAuthenticationResponseHandler.class,
        },
        new Object[] {
          ObjectGuidHandler.class,
        },
        new Object[] {
          ObjectSidHandler.class,
        },
        new Object[] {
          CaseChangeEntryHandler.class,
        },
        new Object[] {
          DnAttributeEntryHandler.class,
        },
        new Object[] {
          MergeAttributeEntryHandler.class,
        },
        new Object[] {
          PrimaryGroupIdHandler.class,
        },
        new Object[] {
          RangeEntryHandler.class,
        },
        new Object[] {
          RecursiveResultHandler.class,
        },
        new Object[] {
          FollowSearchReferralHandler.class,
        },
        new Object[] {
          FollowSearchResultReferenceHandler.class,
        },
      };
  }


  @Test(dataProvider = "immutable-classes")
  public void immutables(final Class<? extends Freezable> clazz) throws Exception
  {
    final Constructor<? extends Freezable> constructor = clazz.getDeclaredConstructor();
    constructor.setAccessible(true);
    final Freezable i = constructor.newInstance();
    i.freeze();
    invokeMethods(clazz, i);
  }


  @Test
  public void noDefaultConstructor()
  {
    final PagedResultsClient pagedResultsClient = new PagedResultsClient(null, 0);
    pagedResultsClient.freeze();
    invokeMethods(PagedResultsClient.class, pagedResultsClient);

    final VirtualListViewClient virtualListViewClient = new VirtualListViewClient(null, (SortKey) null);
    virtualListViewClient.freeze();
    invokeMethods(VirtualListViewClient.class, virtualListViewClient);
  }


  /**
   * Invokes all single arguments setters on the supplied immutable and confirms that an IllegalStateException is
   * thrown.
   *
   * @param  clazz  to discover setter methods
   * @param i  to invoke methods on
   */
  private void invokeMethods(final Class<? extends Freezable> clazz, final Freezable i)
  {
    for (Method method : clazz.getMethods()) {
      if (!method.isBridge()) {
        final boolean invokeMethod =
          (method.getName().startsWith("set") || method.getName().startsWith("add")) &&
            method.getParameterTypes().length == 1;
        if (invokeMethod) {
          try {
            method.invoke(i, createParamType(method.getParameterTypes()[0]));
            Assert.fail("Method " + method + " should have thrown exception for " + clazz);
          } catch (Exception e) {
            Assert.assertEquals(e.getClass(), InvocationTargetException.class);
            Assert.assertEquals(
              ((InvocationTargetException) e).getTargetException().getClass(),
              IllegalStateException.class,
              "Method " + method + " should have thrown illegal state exception for " + clazz);
          }
        }
      }
    }
  }


  /**
   * Creates a default parameter for the supplied type.
   *
   * @param  type  to create parameter for
   *
   * @return  new parameter
   */
  protected Object createParamType(final Class<?> type)
  {
    Object newValue = null;
    if (float.class == type || Float.class == type) {
      newValue = Float.valueOf("0");
    } else if (int.class == type || Integer.class == type) {
      newValue = Integer.valueOf("0");
    } else if (long.class == type || Long.class == type) {
      newValue = Long.valueOf("0");
    } else if (short.class == type || Short.class == type) {
      newValue = Short.valueOf("0");
    } else if (double.class == type || Double.class == type) {
      newValue = Double.valueOf("0");
    } else if (boolean.class == type || Boolean.class == type) {
      newValue = Boolean.valueOf("false");
    } else if (Duration.class == type) {
      newValue = Duration.ofSeconds(1);
    }
    return newValue;
  }
}
