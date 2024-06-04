/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;
import org.ldaptive.ad.handler.AbstractBinaryAttributeHandler;
import org.ldaptive.extended.IntermediateResponse;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.MergeResultHandler;
import org.ldaptive.handler.SortResultHandler;
import org.ldaptive.transport.mock.MockConnection;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DefaultSearchOperationHandle}.
 *
 * @author  Middleware Services
 */
public class DefaultSearchOperationHandleTest
{

  /** To schedule test results. */
  private ScheduledExecutorService executorService;


  /**
   * Returns test data.
   *
   * @return  operation handles
   */
  @DataProvider(name = "search-handles")
  public Object[][] createHandles()
  {
    final TestBinaryAttributeHandler entryHandler = new TestBinaryAttributeHandler();
    entryHandler.freeze();
    return
      new Object[][] {
        new Object[] {
          new DefaultSearchOperationHandle(
            SearchRequest.builder().build(),
            MockConnection.builder(
              ConnectionConfig.builder().url("ldap://ds1.ldaptive.org").build()).abandonConsumer(req -> {}).build(),
            Duration.ZERO).onEntry(entryHandler),
          SearchResponse.builder().messageID(1).build(),
          Duration.ofSeconds(2),
          false,
        },
        new Object[] {
          new DefaultSearchOperationHandle(
            SearchRequest.builder().build(),
            MockConnection.builder(
              ConnectionConfig.builder().url("ldap://ds1.ldaptive.org").build()).abandonConsumer(req -> {}).build(),
            Duration.ofSeconds(1)).onEntry(entryHandler),
          SearchResponse.builder().messageID(1).build(),
          Duration.ofSeconds(2),
          true,
        },
        new Object[] {
          new DefaultSearchOperationHandle(
            SearchRequest.builder().build(),
            MockConnection.builder(
              ConnectionConfig.builder().url("ldap://ds1.ldaptive.org").build()).abandonConsumer(req -> {}).build(),
            Duration.ofSeconds(3)).onEntry(entryHandler),
          SearchResponse.builder().messageID(1).build(),
          Duration.ofSeconds(2),
          false,
        },
      };
  }


  @BeforeClass
  public void setup()
  {
    executorService = Executors.newScheduledThreadPool(5);
  }


  @AfterClass
  public void shutdown()
  {
    executorService.shutdown();
  }


  /**
   * @param  handle  to test
   * @param  response  to send to handle
   * @param  responseTime  time to wait until sending the response
   * @param  throwsTimeout  whether a timeout exception is expected
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport", dataProvider = "search-handles")
  public void awaitResult(
    final DefaultSearchOperationHandle handle,
    final SearchResponse response,
    final Duration responseTime,
    final boolean throwsTimeout)
    throws Exception
  {
    executorService.schedule(() -> handle.result(response), responseTime.toMillis(), TimeUnit.MILLISECONDS);
    if (throwsTimeout) {
      try {
        handle.execute();
        Assert.fail("Should have thrown exception");
      } catch (Exception ex) {
        if (ex instanceof LdapException) {
          Assert.assertEquals(ResultCode.LDAP_TIMEOUT, ((LdapException) ex).getResultCode());
        } else {
          throw ex;
        }
      }
    } else {
      Assert.assertEquals(handle.execute(), response);
    }
  }


  /**
   * @param  handle  to test
   * @param  response  to send to handle
   * @param  responseTime  time to wait until sending the response
   * @param  throwsTimeout  whether a timeout exception is expected
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport", dataProvider = "search-handles")
  public void awaitResultWithEntries(
    final DefaultSearchOperationHandle handle,
    final SearchResponse response,
    final Duration responseTime,
    final boolean throwsTimeout)
    throws Exception
  {
    final LdapEntry entry1 = LdapEntry.builder().messageID(1).dn("uid=1,ou=robots,dc=ldaptive,dc=org").build();
    final LdapEntry entry2 = LdapEntry.builder().messageID(1).dn("uid=2,ou=robots,dc=ldaptive,dc=org").build();
    final SearchResultReference ref1 =
      SearchResultReference.builder().messageID(1).uris("ldap://ds2.ldaptive.org/dc=example,dc=com??sub?").build();
    // timeout will not occur since messages restart the wait time
    executorService.schedule(() -> handle.entry(entry1), responseTime.dividedBy(4).toMillis(), TimeUnit.MILLISECONDS);
    executorService.schedule(() -> handle.reference(ref1), responseTime.dividedBy(2).toMillis(), TimeUnit.MILLISECONDS);
    executorService.schedule(
      () -> handle.entry(entry2), responseTime.multipliedBy(3).dividedBy(4).toMillis(), TimeUnit.MILLISECONDS);
    executorService.schedule(() -> handle.result(response), responseTime.toMillis(), TimeUnit.MILLISECONDS);
    Assert.assertEquals(
      handle.execute(), SearchResponse.builder().messageID(1).entry(entry1, entry2).reference(ref1).build());
  }


  /**
   * @param  handle  to test
   * @param  response  to send to handle
   * @param  responseTime  time to wait until sending the response
   * @param  throwsTimeout  whether a timeout exception is expected
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport", dataProvider = "search-handles")
  public void awaitResultWithEntriesThrows(
    final DefaultSearchOperationHandle handle,
    final SearchResponse response,
    final Duration responseTime,
    final boolean throwsTimeout)
    throws Exception
  {
    final LdapEntry entry1 = LdapEntry.builder().messageID(1).dn("uid=1,ou=robots,dc=ldaptive,dc=org").build();
    final LdapEntry entry2 = LdapEntry.builder().messageID(1).dn("uid=2,ou=robots,dc=ldaptive,dc=org").build();
    final SearchResultReference ref1 =
      SearchResultReference.builder().messageID(1).uris("ldap://ds2.ldaptive.org/dc=example,dc=com??sub?").build();
    executorService.schedule(() -> handle.entry(entry1), responseTime.dividedBy(4).toMillis(), TimeUnit.MILLISECONDS);
    executorService.schedule(() -> handle.reference(ref1), responseTime.dividedBy(2).toMillis(), TimeUnit.MILLISECONDS);
    executorService.schedule(
      () -> handle.entry(entry2), responseTime.multipliedBy(3).dividedBy(4).toMillis(), TimeUnit.MILLISECONDS);
    if (throwsTimeout) {
      executorService.schedule(
        () -> handle.result(response), responseTime.multipliedBy(2).toMillis(), TimeUnit.MILLISECONDS);
      try {
        handle.execute();
        Assert.fail("Should have thrown exception");
      } catch (Exception ex) {
        if (ex instanceof LdapException) {
          Assert.assertEquals(ResultCode.LDAP_TIMEOUT, ((LdapException) ex).getResultCode());
        } else {
          throw ex;
        }
      }
    } else {
      executorService.schedule(() -> handle.result(response), responseTime.toMillis(), TimeUnit.MILLISECONDS);
      Assert.assertEquals(
        handle.execute(), SearchResponse.builder().messageID(1).entry(entry1, entry2).reference(ref1).build());
    }
  }


  /**
   * @param  handle  to test
   * @param  response  to send to handle
   * @param  responseTime  time to wait until sending the response
   * @param  throwsTimeout  whether a timeout exception is expected
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport", dataProvider = "search-handles")
  public void awaitResultWithIntermediate(
    final DefaultSearchOperationHandle handle,
    final SearchResponse response,
    final Duration responseTime,
    final boolean throwsTimeout)
    throws Exception
  {
    // timeout will not occur since messages restart the wait time
    executorService.schedule(
      () -> handle.intermediate(
        IntermediateResponse.builder().messageID(1).build()),
      responseTime.dividedBy(4).toMillis(),
      TimeUnit.MILLISECONDS);
    executorService.schedule(
      () -> handle.intermediate(
        IntermediateResponse.builder().messageID(1).build()),
      responseTime.dividedBy(2).toMillis(),
      TimeUnit.MILLISECONDS);
    executorService.schedule(
      () -> handle.intermediate(
        IntermediateResponse.builder().messageID(1).build()),
      responseTime.multipliedBy(3).dividedBy(4).toMillis(),
      TimeUnit.MILLISECONDS);
    executorService.schedule(() -> handle.result(response), responseTime.toMillis(), TimeUnit.MILLISECONDS);
    Assert.assertEquals(handle.execute(), response);
  }


  /**
   * @param  handle  to test
   * @param  response  to send to handle
   * @param  responseTime  time to wait until sending the response
   * @param  throwsTimeout  whether a timeout exception is expected
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport", dataProvider = "search-handles")
  public void awaitResultWithIntermediateThrows(
    final DefaultSearchOperationHandle handle,
    final SearchResponse response,
    final Duration responseTime,
    final boolean throwsTimeout)
    throws Exception
  {
    executorService.schedule(
      () -> handle.intermediate(
        IntermediateResponse.builder().messageID(1).build()),
      responseTime.dividedBy(4).toMillis(),
      TimeUnit.MILLISECONDS);
    executorService.schedule(
      () -> handle.intermediate(
        IntermediateResponse.builder().messageID(1).build()),
      responseTime.dividedBy(2).toMillis(),
      TimeUnit.MILLISECONDS);
    executorService.schedule(
      () -> handle.intermediate(
        IntermediateResponse.builder().messageID(1).build()),
      responseTime.multipliedBy(3).dividedBy(4).toMillis(),
      TimeUnit.MILLISECONDS);
    if (throwsTimeout) {
      executorService.schedule(
        () -> handle.result(response), responseTime.multipliedBy(2).toMillis(), TimeUnit.MILLISECONDS);
      try {
        handle.execute();
        Assert.fail("Should have thrown exception");
      } catch (Exception ex) {
        if (ex instanceof LdapException) {
          Assert.assertEquals(ResultCode.LDAP_TIMEOUT, ((LdapException) ex).getResultCode());
        } else {
          throw ex;
        }
      }
    } else {
      executorService.schedule(() -> handle.result(response), responseTime.toMillis(), TimeUnit.MILLISECONDS);
      Assert.assertEquals(handle.execute(), response);
    }
  }


  /**
   * @param  handle  to test
   * @param  response  to send to handle
   * @param  responseTime  time to wait until sending the response
   * @param  throwsTimeout  whether a timeout exception is expected
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport", dataProvider = "search-handles")
  public void awaitException(
    final DefaultSearchOperationHandle handle,
    final SearchResponse response,
    final Duration responseTime,
    final boolean throwsTimeout)
    throws Exception
  {
    final LdapException e = new LdapException("Test exception");
    executorService.schedule(() -> handle.exception(e), responseTime.toMillis(), TimeUnit.MILLISECONDS);
    try {
      handle.execute();
      Assert.fail("Should have thrown exception");
    } catch (Exception ex) {
      if (throwsTimeout) {
        if (ex instanceof LdapException) {
          Assert.assertEquals(ResultCode.LDAP_TIMEOUT, ((LdapException) ex).getResultCode());
        } else {
          throw ex;
        }
      } else {
        Assert.assertEquals(ex, e);
      }
    }
  }


  /**
   * @param  handle  to test
   * @param  response  to send to handle
   * @param  responseTime  time to wait until sending the response
   * @param  throwsTimeout  whether a timeout exception is expected
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport", dataProvider = "search-handles")
  public void awaitInterrupted(
    final DefaultSearchOperationHandle handle,
    final SearchResponse response,
    final Duration responseTime,
    final boolean throwsTimeout)
    throws Throwable
  {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<LdapException> ldapException = new AtomicReference<>();
    handle.onException(e -> {
      ldapException.set(e);
      latch.countDown();
    });
    final Future<?> future = executorService.submit(() -> {
      try {
        handle.execute();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    try {
      // allow some time for the handle to start blocking
      Thread.sleep(100);
      future.cancel(true);
      future.get();
    } catch (Exception e) {
      Assert.assertEquals(CancellationException.class, e.getClass());
    }
    if (!latch.await(Duration.ofSeconds(2).toMillis(), TimeUnit.MILLISECONDS)) {
      Assert.fail("Exception was not set on the handle");
    }
    Assert.assertEquals(ResultCode.LOCAL_ERROR, ldapException.get().getResultCode());
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport")
  public void immutableResult()
    throws Exception
  {
    final DefaultSearchOperationHandle handle = new DefaultSearchOperationHandle(
      SearchRequest.builder().build(),
      MockConnection.builder(
        ConnectionConfig.builder().url("ldap://ds1.ldaptive.org").build()).abandonConsumer(req -> {}).build(),
      Duration.ofSeconds(1));
    handle.messageID(1);

    final AtomicBoolean handlerExecuted = new AtomicBoolean();
    handle.onSearchResult(result -> {
      result.addReferences(SearchResultReference.builder().messageID(result.getMessageID()).build());
      result.addEntries(LdapEntry.builder().messageID(result.getMessageID()).build());
      Assert.assertTrue(handlerExecuted.compareAndSet(false, true));
      return result;
    });
    handle.entry(LdapEntry.builder().messageID(1).build());
    handle.result(SearchResponse.builder().messageID(1).resultCode(ResultCode.SUCCESS).build());
    final SearchResponse result = handle.await();
    Assert.assertTrue(handlerExecuted.get());
    try {
      result.addReferences(SearchResultReference.builder().messageID(result.getMessageID()).build());
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
    try {
      result.addEntries(LdapEntry.builder().messageID(result.getMessageID()).build());
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport")
  public void sortResultHandler()
    throws Exception
  {
    final DefaultSearchOperationHandle handle = new DefaultSearchOperationHandle(
      SearchRequest.builder().build(),
      MockConnection.builder(
        ConnectionConfig.builder().url("ldap://ds1.ldaptive.org").build()).abandonConsumer(req -> {}).build(),
      Duration.ofSeconds(1));
    handle.messageID(1);

    handle.onSearchResult(new SortResultHandler());
    handle.entry(LdapEntry.builder().messageID(1).dn("uid=xyz,ou=sort,dc=ldaptive,dc=org").build());
    handle.entry(LdapEntry.builder().messageID(1).dn("uid=abc,ou=sort,dc=ldaptive,dc=org").build());
    handle.result(SearchResponse.builder().messageID(1).resultCode(ResultCode.SUCCESS).build());
    final SearchResponse result = handle.await();
    Assert.assertNotNull(result.getEntry());
    Assert.assertEquals(result.getEntry().getDn(), "uid=abc,ou=sort,dc=ldaptive,dc=org");
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport")
  public void mergeResultHandler()
    throws Exception
  {
    final DefaultSearchOperationHandle handle = new DefaultSearchOperationHandle(
      SearchRequest.builder().build(),
      MockConnection.builder(
        ConnectionConfig.builder().url("ldap://ds1.ldaptive.org").build()).abandonConsumer(req -> {}).build(),
      Duration.ofSeconds(1));
    handle.messageID(1);

    handle.onSearchResult(new MergeResultHandler());
    handle.entry(LdapEntry.builder()
      .messageID(1)
      .dn("uid=alice,ou=merge,dc=ldaptive,dc=org")
      .attributes(
        LdapAttribute.builder().name("givenName").values("alice").build())
      .build());
    handle.entry(LdapEntry.builder()
      .messageID(1)
      .dn("uid=bob,ou=merge,dc=ldaptive,dc=org")
      .attributes(
        LdapAttribute.builder().name("givenName").values("bob", "robert").build())
      .build());
    handle.result(SearchResponse.builder().messageID(1).resultCode(ResultCode.SUCCESS).build());
    final SearchResponse result = handle.await();
    Assert.assertEquals(result.entrySize(), 1);
    Assert.assertNotNull(result.getEntry());
    Assert.assertEquals("uid=alice,ou=merge,dc=ldaptive,dc=org", result.getEntry().getDn());
    Assert.assertEquals(
      List.of("alice", "bob", "robert"), result.getEntry().getAttribute("givenName").getStringValues());
  }


  /** Test class. */
  protected static class TestBinaryAttributeHandler extends AbstractBinaryAttributeHandler<LdapEntry>
    implements LdapEntryHandler
  {


    @Override
    protected String convertValue(final byte[] value)
    {
      return new String(value, StandardCharsets.UTF_8);
    }


    @Override
    public LdapEntry apply(final LdapEntry ldapEntry)
    {
      return ldapEntry;
    }


    @Override
    public TestBinaryAttributeHandler newInstance()
    {
      return new TestBinaryAttributeHandler();
    }
  }
}
