/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.mock;

import java.util.function.Predicate;
import org.ldaptive.AbandonRequest;
import org.ldaptive.AddRequest;
import org.ldaptive.AddResponse;
import org.ldaptive.BindRequest;
import org.ldaptive.BindResponse;
import org.ldaptive.CompareOperationHandle;
import org.ldaptive.CompareRequest;
import org.ldaptive.ConnectException;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DeleteResponse;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyDnResponse;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ModifyResponse;
import org.ldaptive.OperationHandle;
import org.ldaptive.Result;
import org.ldaptive.SearchOperationHandle;
import org.ldaptive.SearchRequest;
import org.ldaptive.UnbindRequest;
import org.ldaptive.control.RequestControl;
import org.ldaptive.extended.ExtendedOperationHandle;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.provider.DefaultOperationHandle;
import org.ldaptive.provider.ProviderConnection;
import org.ldaptive.sasl.SaslClientRequest;

/**
 * Mock connection for testing.
 *
 * @author  Middleware Services
 */
public final class MockConnection extends ProviderConnection
{

  /** Predicate to control the results of {@link #open(LdapURL)}. */
  private Predicate<LdapURL> openPredicate;

  /** Predicate to control the results of {@link #test(LdapURL)}. */
  private Predicate<LdapURL> testPredicate;

  /** Flag indicating the connection is open. */
  private boolean open;

  /**
   * Creates a new mock connection.
   *
   * @param  config  connection config
   */
  public MockConnection(final ConnectionConfig config)
  {
    super(config);
  }


  public void setOpenPredicate(final Predicate<LdapURL> p)
  {
    openPredicate = p;
  }


  public void setTestPredicate(final Predicate<LdapURL> p)
  {
    testPredicate = p;
  }


  @Override
  protected boolean test(final LdapURL url)
  {
    return testPredicate.test(url);
  }


  @Override
  protected void open(final LdapURL url)
    throws LdapException
  {
    if (!openPredicate.test(url)) {
      throw new ConnectException("Cannot connect to " + url.getHostnameWithSchemeAndPort());
    }
    open = true;
  }


  @Override
  protected void operation(final UnbindRequest request)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  protected void write(final DefaultOperationHandle handle)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public void operation(final AbandonRequest request)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public OperationHandle<AddRequest, AddResponse> operation(final AddRequest request)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public OperationHandle<BindRequest, BindResponse> operation(final BindRequest request)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public CompareOperationHandle operation(final CompareRequest request)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public OperationHandle<DeleteRequest, DeleteResponse> operation(final DeleteRequest request)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public ExtendedOperationHandle operation(final ExtendedRequest request)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public OperationHandle<ModifyRequest, ModifyResponse> operation(final ModifyRequest request)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public OperationHandle<ModifyDnRequest, ModifyDnResponse> operation(final ModifyDnRequest request)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public SearchOperationHandle operation(final SearchRequest request)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public Result operation(final SaslClientRequest request)
    throws LdapException
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public boolean isOpen()
  {
    return open;
  }


  @Override
  public void close(final RequestControl... controls)
  {
    open = false;
  }
}
