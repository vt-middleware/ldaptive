/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import org.ldaptive.control.SortRequestControl;
import org.ldaptive.extended.ExtendedOperation;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.handler.RequestHandler;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for objects that provide a static copy method.
 *
 * @author  Middleware Services
 */
public class CopyTest
{


  /**
   * Copy classes.
   *
   * @return  test data
   */
  @DataProvider(name = "copy-classes")
  public Object[][] copyClasses()
  {
    return
      new Object[][] {
        new Object[] {
          SearchRequest.builder().build(),
        },
        new Object[] {
          SearchRequest.builder()
            .dn("dc=ldaptive,dc=org")
            .scope(SearchScope.OBJECT)
            .aliases(DerefAliases.ALWAYS)
            .sizeLimit(5)
            .timeLimit(Duration.ofMinutes(1))
            .typesOnly(false)
            .filter("(uid=1234)")
            .returnAttributes("cn", "sn", "jpegPhoto")
            .binaryAttributes("jpegPhoto")
            .controls(new SortRequestControl())
            .responseTimeout(Duration.ofSeconds(3))
            .build(),
          },
        new Object[]{
          ConnectionConfig.builder().build(),
        },
        new Object[]{
          ConnectionConfig.builder()
            .url("ldap://directory.ldaptive.org")
            .connectTimeout(Duration.ofSeconds(2))
            .startTLSTimeout(Duration.ofSeconds(3))
            .responseTimeout(Duration.ofMinutes(1))
            .reconnectTimeout(Duration.ofSeconds(5))
            .autoReconnect(false)
            .autoReconnectCondition(retryMetadata -> false)
            .autoReplay(true)
            .sslConfig(SslConfig.builder()
              .credentialConfig(new X509CredentialConfig())
              .build())
            .useStartTLS(true)
            .connectionInitializers(BindConnectionInitializer.builder()
              .dn("cn=manager")
              .credential("password")
              .build())
            .connectionStrategy(new RoundRobinConnectionStrategy())
            .connectionValidator(SearchConnectionValidator.builder().build())
            .transportOption("foo", "bar")
            .build(),
        },
        new Object[] {
          AddOperation.builder().build(),
        },
        new Object[] {
          AddOperation.builder()
            .onRequest((RequestHandler<AddRequest>) System.out::println)
            .onResult(r -> System.out.println(r.getResultCode()))
            .onControl(c -> System.out.println(c.getOID()))
            .onReferral(uris -> System.out.println(Arrays.toString(uris)))
            .onIntermediate(r -> System.out.println(r.getResponseName()))
            .onException(e -> System.out.println(e.getMessage()))
            .throwIf(result -> false)
            .onUnsolicitedNotification(not -> System.out.println(not.getResponseName()))
            .factory(DefaultConnectionFactory.builder()
              .config(ConnectionConfig.builder()
                .url("ldap://directory.ldaptive.org")
                .build())
              .build())
            .build(),
        },
        new Object[] {
          BindOperation.builder().build(),
        },
        new Object[] {
          BindOperation.builder()
            .onRequest((RequestHandler<BindRequest>) System.out::println)
            .onResult(r -> System.out.println(r.getResultCode()))
            .onControl(c -> System.out.println(c.getOID()))
            .onReferral(uris -> System.out.println(Arrays.toString(uris)))
            .onIntermediate(r -> System.out.println(r.getResponseName()))
            .onException(e -> System.out.println(e.getMessage()))
            .throwIf(result -> false)
            .onUnsolicitedNotification(not -> System.out.println(not.getResponseName()))
            .factory(DefaultConnectionFactory.builder()
              .config(ConnectionConfig.builder()
                .url("ldap://directory.ldaptive.org")
                .build())
              .build())
            .build(),
        },
        new Object[] {
          CompareOperation.builder().build(),
        },
        new Object[] {
          CompareOperation.builder()
            .onRequest((RequestHandler<CompareRequest>) System.out::println)
            .onResult(r -> System.out.println(r.getResultCode()))
            .onControl(c -> System.out.println(c.getOID()))
            .onReferral(uris -> System.out.println(Arrays.toString(uris)))
            .onIntermediate(r -> System.out.println(r.getResponseName()))
            .onException(e -> System.out.println(e.getMessage()))
            .throwIf(result -> false)
            .onUnsolicitedNotification(not -> System.out.println(not.getResponseName()))
            .factory(DefaultConnectionFactory.builder()
              .config(ConnectionConfig.builder()
                .url("ldap://directory.ldaptive.org")
                .build())
              .build())
            .build(),
        },
        new Object[] {
          DeleteOperation.builder().build(),
        },
        new Object[] {
          DeleteOperation.builder()
            .onRequest((RequestHandler<DeleteRequest>) System.out::println)
            .onResult(r -> System.out.println(r.getResultCode()))
            .onControl(c -> System.out.println(c.getOID()))
            .onReferral(uris -> System.out.println(Arrays.toString(uris)))
            .onIntermediate(r -> System.out.println(r.getResponseName()))
            .onException(e -> System.out.println(e.getMessage()))
            .throwIf(result -> false)
            .onUnsolicitedNotification(not -> System.out.println(not.getResponseName()))
            .factory(DefaultConnectionFactory.builder()
              .config(ConnectionConfig.builder()
                .url("ldap://directory.ldaptive.org")
                .build())
              .build())
            .build(),
        },
        new Object[] {
          ExtendedOperation.builder().build(),
        },
        new Object[] {
          ExtendedOperation.builder()
            .onRequest((RequestHandler<ExtendedRequest>) System.out::println)
            .onResult(r -> System.out.println(r.getResultCode()))
            .onControl(c -> System.out.println(c.getOID()))
            .onReferral(uris -> System.out.println(Arrays.toString(uris)))
            .onIntermediate(r -> System.out.println(r.getResponseName()))
            .onException(e -> System.out.println(e.getMessage()))
            .throwIf(result -> false)
            .onUnsolicitedNotification(not -> System.out.println(not.getResponseName()))
            .factory(DefaultConnectionFactory.builder()
              .config(ConnectionConfig.builder()
                .url("ldap://directory.ldaptive.org")
                .build())
              .build())
            .build(),
        },
        new Object[] {
          ModifyDnOperation.builder().build(),
        },
        new Object[] {
          ModifyDnOperation.builder()
            .onRequest((RequestHandler<ModifyDnRequest>) System.out::println)
            .onResult(r -> System.out.println(r.getResultCode()))
            .onControl(c -> System.out.println(c.getOID()))
            .onReferral(uris -> System.out.println(Arrays.toString(uris)))
            .onIntermediate(r -> System.out.println(r.getResponseName()))
            .onException(e -> System.out.println(e.getMessage()))
            .throwIf(result -> false)
            .onUnsolicitedNotification(not -> System.out.println(not.getResponseName()))
            .factory(DefaultConnectionFactory.builder()
              .config(ConnectionConfig.builder()
                .url("ldap://directory.ldaptive.org")
                .build())
              .build())
            .build(),
        },
        new Object[] {
          ModifyOperation.builder().build(),
        },
        new Object[] {
          ModifyOperation.builder()
            .onRequest((RequestHandler<ModifyRequest>) System.out::println)
            .onResult(r -> System.out.println(r.getResultCode()))
            .onControl(c -> System.out.println(c.getOID()))
            .onReferral(uris -> System.out.println(Arrays.toString(uris)))
            .onIntermediate(r -> System.out.println(r.getResponseName()))
            .onException(e -> System.out.println(e.getMessage()))
            .throwIf(result -> false)
            .onUnsolicitedNotification(not -> System.out.println(not.getResponseName()))
            .factory(DefaultConnectionFactory.builder()
              .config(ConnectionConfig.builder()
                .url("ldap://directory.ldaptive.org")
                .build())
              .build())
            .build(),
        },
        new Object[] {
          SearchOperation.builder().build(),
        },
        new Object[] {
          SearchOperation.builder()
            .onRequest((RequestHandler<SearchRequest>) System.out::println)
            .onResult(r -> System.out.println(r.getResultCode()))
            .onControl(c -> System.out.println(c.getOID()))
            .onReferral(uris -> System.out.println(Arrays.toString(uris)))
            .onIntermediate(r -> System.out.println(r.getResponseName()))
            .onException(e -> System.out.println(e.getMessage()))
            .throwIf(result -> false)
            .onUnsolicitedNotification(not -> System.out.println(not.getResponseName()))
            .factory(DefaultConnectionFactory.builder()
              .config(ConnectionConfig.builder()
                .url("ldap://directory.ldaptive.org")
                .build())
              .build())
            .onEntry(e -> e)
            .onReference(ref -> System.out.println(Arrays.toString(ref.getUris())))
            .onSearchResult(r -> r)
            .request(SearchRequest.builder().build())
            .template(FilterTemplate.builder().build())
            .build(),
        },
      };
  }


  @Test(dataProvider = "copy-classes")
  public void copy(final Object o)
    throws Exception
  {
    final Class<?> clazz = o.getClass();
    final Method method = clazz.getMethod("copy", clazz);
    final Object copy = method.invoke(null, o);
    assertThat(o)
      .usingRecursiveComparison()
      // connection strategy cannot be initialized as part of this test
      .ignoringFields("connectionStrategy")
      .isEqualTo(copy);
  }
}
