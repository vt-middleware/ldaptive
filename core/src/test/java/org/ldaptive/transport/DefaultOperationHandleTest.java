/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.ldaptive.AddRequest;
import org.ldaptive.AddResponse;
import org.ldaptive.AnonymousBindRequest;
import org.ldaptive.BindRequest;
import org.ldaptive.BindResponse;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DeleteResponse;
import org.ldaptive.LdapException;
import org.ldaptive.Request;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.SimpleBindRequest;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.extended.IntermediateResponse;
import org.ldaptive.transport.mock.MockConnection;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DefaultOperationHandle}.
 *
 * @author  Middleware Services
 */
public class DefaultOperationHandleTest
{

  /** To schedule test results. */
  private ScheduledExecutorService executorService;


  /**
   * Returns test data.
   *
   * @return  operation handles
   */
  @DataProvider(name = "default-handles")
  public Object[][] createHandles()
  {
    return
      new Object[][] {
        new Object[] {
          new DefaultOperationHandle<AddRequest, AddResponse>(
            AddRequest.builder().build(),
            MockConnection.builder(
              ConnectionConfig.builder().url("ldap://ds1.ldaptive.org").build()).abandonConsumer(req -> {}).build(),
            Duration.ZERO),
          AddResponse.builder().messageID(1).build(),
          Duration.ofSeconds(2),
          false,
        },
        new Object[] {
          new DefaultOperationHandle<BindRequest, BindResponse>(
            AnonymousBindRequest.builder().build(),
            MockConnection.builder(
              ConnectionConfig.builder().url("ldap://ds1.ldaptive.org").build()).abandonConsumer(req -> {}).build(),
            Duration.ofSeconds(1)),
          BindResponse.builder().messageID(1).build(),
          Duration.ofSeconds(2),
          true,
        },
        new Object[] {
          new DefaultOperationHandle<DeleteRequest, DeleteResponse>(
            DeleteRequest.builder().build(),
            MockConnection.builder(
              ConnectionConfig.builder().url("ldap://ds1.ldaptive.org").build()).abandonConsumer(req -> {}).build(),
            Duration.ofSeconds(3)),
          DeleteResponse.builder().messageID(1).build(),
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
  @Test(groups = "transport", dataProvider = "default-handles")
  public void awaitResult(
    final DefaultOperationHandle<Request, Result> handle,
    final Result response,
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
  @Test(groups = "transport", dataProvider = "default-handles")
  public void awaitResultWithIntermediate(
    final DefaultOperationHandle<Request, Result> handle,
    final Result response,
    final Duration responseTime,
    final boolean throwsTimeout)
    throws Exception
  {
    // timeout will occur since messages do not restart the wait time
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
  @Test(groups = "transport", dataProvider = "default-handles")
  public void awaitResultWithIntermediateThrows(
    final DefaultOperationHandle<Request, Result> handle,
    final Result response,
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
  @Test(groups = "transport", dataProvider = "default-handles")
  public void awaitException(
    final DefaultOperationHandle<Request, Result> handle,
    final Result response,
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
  @Test(groups = "transport", dataProvider = "default-handles")
  public void awaitInterrupted(
    final DefaultOperationHandle<Request, Result> handle,
    final Result response,
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
    final DefaultOperationHandle<BindRequest, BindResponse> handle = new DefaultOperationHandle<>(
      SimpleBindRequest.builder().build(),
      MockConnection.builder(
        ConnectionConfig.builder().url("ldap://ds1.ldaptive.org").build()).abandonConsumer(req -> {}).build(),
      Duration.ofSeconds(1));
    handle.messageID(1);

    final AtomicBoolean handlerExecuted = new AtomicBoolean();
    handle.onResult(result -> {
      final BindResponse response = (BindResponse) result;
      try {
        response.setMessageID(0);
        Assert.fail("Should have thrown exception");
      } catch (Exception e) {
        Assert.assertEquals(e.getClass(), IllegalStateException.class);
      }
      try {
        response.addControls((ResponseControl) null);
        Assert.fail("Should have thrown exception");
      } catch (Exception e) {
        Assert.assertEquals(e.getClass(), IllegalStateException.class);
      }
      try {
        response.setResultCode(ResultCode.LOCAL_ERROR);
        Assert.fail("Should have thrown exception");
      } catch (Exception e) {
        Assert.assertEquals(e.getClass(), IllegalStateException.class);
      }
      try {
        response.setMatchedDN(null);
        Assert.fail("Should have thrown exception");
      } catch (Exception e) {
        Assert.assertEquals(e.getClass(), IllegalStateException.class);
      }
      try {
        response.setDiagnosticMessage(null);
        Assert.fail("Should have thrown exception");
      } catch (Exception e) {
        Assert.assertEquals(e.getClass(), IllegalStateException.class);
      }
      try {
        response.addReferralURLs("");
        Assert.fail("Should have thrown exception");
      } catch (Exception e) {
        Assert.assertEquals(e.getClass(), IllegalStateException.class);
      }
      try {
        response.setServerSaslCreds(null);
        Assert.fail("Should have thrown exception");
      } catch (Exception e) {
        Assert.assertEquals(e.getClass(), IllegalStateException.class);
      }
      Assert.assertTrue(handlerExecuted.compareAndSet(false, true));
    });
    handle.result(BindResponse.builder().messageID(1).build());
    Assert.assertTrue(handlerExecuted.get());
  }
}
