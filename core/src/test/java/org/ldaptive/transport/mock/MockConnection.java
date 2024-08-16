/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.mock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
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
import org.ldaptive.Request;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchOperationHandle;
import org.ldaptive.SearchRequest;
import org.ldaptive.UnbindRequest;
import org.ldaptive.control.RequestControl;
import org.ldaptive.extended.ExtendedOperationHandle;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.sasl.DefaultSaslClientRequest;
import org.ldaptive.sasl.SaslClientRequest;
import org.ldaptive.transport.DefaultOperationHandle;
import org.ldaptive.transport.TransportConnection;

/**
 * Mock connection for testing.
 *
 * @param  <Q>  type of request
 * @param  <S>  type of response
 *
 * @author  Middleware Services
 */
public final class MockConnection<Q extends Request, S extends Result> extends TransportConnection
{

  /** Message ID. */
  private AtomicInteger messageID = new AtomicInteger(1);

  /** Predicate to control the results of {@link #open(LdapURL)}. */
  private Predicate<LdapURL> openPredicate;

  /** Predicate to control the results of {@link #test(LdapURL)}. */
  private Predicate<LdapURL> testPredicate;

  /** Flag indicating the connection is open. */
  private boolean open;

  /** LDAP URL. */
  private LdapURL ldapURL;

  /** Consumer for abandon operations. */
  private Consumer<AbandonRequest> abandonConsumer;

  /** Consumer for write operations. */
  private Consumer<DefaultOperationHandle<Q, S>> writeConsumer = handle -> {
    handle.messageID(messageID.getAndIncrement());
    handle.sent();
  };

  /** Handle to return for add operations. */
  private Function<AddRequest, OperationHandle<AddRequest, AddResponse>> addOperationFunction;

  /** Handle to return for bind operations. */
  private Function<BindRequest, OperationHandle<BindRequest, BindResponse>> bindOperationFunction;

  /** Handle to return for compare operations. */
  private Function<CompareRequest, CompareOperationHandle> compareOperationFunction;

  /** Handle to return for delete operations. */
  private Function<DeleteRequest, OperationHandle<DeleteRequest, DeleteResponse>> deleteOperationFunction;

  /** Handle to return for extended operations. */
  private Function<ExtendedRequest, ExtendedOperationHandle> extendedOperationFunction;

  /** Handle to return for modify dn operations. */
  private Function<ModifyDnRequest, OperationHandle<ModifyDnRequest, ModifyDnResponse>> modifyDnOperationFunction;

  /** Handle to return for modify operations. */
  private Function<ModifyRequest, OperationHandle<ModifyRequest, ModifyResponse>> modifyOperationFunction;

  /** Handle to return for search operations. */
  private Function<SearchRequest, SearchOperationHandle> searchOperationFunction;


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


  public void setAbandonConsumer(final Consumer<AbandonRequest> consumer)
  {
    abandonConsumer = consumer;
  }


  public void setWriteConsumer(final Consumer<DefaultOperationHandle<Q, S>> consumer)
  {
    writeConsumer = consumer;
  }


  public void setAddOperationFunction(final Function<AddRequest, OperationHandle<AddRequest, AddResponse>> func)
  {
    addOperationFunction = func;
  }


  public void setBindOperationFunction(final Function<BindRequest, OperationHandle<BindRequest, BindResponse>> func)
  {
    bindOperationFunction = func;
  }


  public void setCompareOperationFunction(final Function<CompareRequest, CompareOperationHandle> func)
  {
    compareOperationFunction = func;
  }


  public void setDeleteOperationFunction(
    final Function<DeleteRequest, OperationHandle<DeleteRequest, DeleteResponse>> func)
  {
    deleteOperationFunction = func;
  }


  public void setExtendedOperationFunction(final Function<ExtendedRequest, ExtendedOperationHandle> func)
  {
    extendedOperationFunction = func;
  }


  public void setModifyDnOperationFunction(
    final Function<ModifyDnRequest, OperationHandle<ModifyDnRequest, ModifyDnResponse>> func)
  {
    modifyDnOperationFunction = func;
  }


  public void setModifyOperationFunction(
    final Function<ModifyRequest, OperationHandle<ModifyRequest, ModifyResponse>> func)
  {
    modifyOperationFunction = func;
  }


  public void setSearchOperationFunction(final Function<SearchRequest, SearchOperationHandle> func)
  {
    searchOperationFunction = func;
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
      throw new ConnectException(ResultCode.CONNECT_ERROR, "Cannot connect to " + url.getHostnameWithSchemeAndPort());
    }
    ldapURL = url;
    open = true;
  }


  @Override
  public LdapURL getLdapURL()
  {
    return ldapURL;
  }


  @Override
  protected void operation(final UnbindRequest request)
  {
    throw new UnsupportedOperationException();
  }


  @Override
  @SuppressWarnings("unchecked")
  protected void write(final DefaultOperationHandle handle)
  {
    if (writeConsumer != null) {
      writeConsumer.accept(handle);
    } else {
      throw new UnsupportedOperationException();
    }
  }


  @Override
  protected void complete(final DefaultOperationHandle handle) {}


  @Override
  public void operation(final AbandonRequest request)
  {
    if (abandonConsumer != null) {
      abandonConsumer.accept(request);
    } else {
      throw new UnsupportedOperationException();
    }
  }


  @Override
  public OperationHandle<AddRequest, AddResponse> operation(final AddRequest request)
  {
    if (addOperationFunction != null) {
      return addOperationFunction.apply(request);
    }
    throw new UnsupportedOperationException();
  }


  @Override
  public OperationHandle<BindRequest, BindResponse> operation(final BindRequest request)
  {
    if (bindOperationFunction != null) {
      return bindOperationFunction.apply(request);
    }
    throw new UnsupportedOperationException();
  }


  @Override
  public CompareOperationHandle operation(final CompareRequest request)
  {
    if (compareOperationFunction != null) {
      return compareOperationFunction.apply(request);
    }
    throw new UnsupportedOperationException();
  }


  @Override
  public OperationHandle<DeleteRequest, DeleteResponse> operation(final DeleteRequest request)
  {
    if (deleteOperationFunction != null) {
      return deleteOperationFunction.apply(request);
    }
    throw new UnsupportedOperationException();
  }


  @Override
  public ExtendedOperationHandle operation(final ExtendedRequest request)
  {
    if (extendedOperationFunction != null) {
      return extendedOperationFunction.apply(request);
    }
    throw new UnsupportedOperationException();
  }


  @Override
  public OperationHandle<ModifyDnRequest, ModifyDnResponse> operation(final ModifyDnRequest request)
  {
    if (modifyDnOperationFunction != null) {
      return modifyDnOperationFunction.apply(request);
    }
    throw new UnsupportedOperationException();
  }


  @Override
  public OperationHandle<ModifyRequest, ModifyResponse> operation(final ModifyRequest request)
  {
    if (modifyOperationFunction != null) {
      return modifyOperationFunction.apply(request);
    }
    throw new UnsupportedOperationException();
  }


  @Override
  public SearchOperationHandle operation(final SearchRequest request)
  {
    if (searchOperationFunction != null) {
      return searchOperationFunction.apply(request);
    }
    throw new UnsupportedOperationException();
  }


  @Override
  public BindResponse operation(final SaslClientRequest request)
    throws LdapException
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public BindResponse operation(final DefaultSaslClientRequest request)
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


  public static MockConnection.Builder builder(final ConnectionConfig cc)
  {
    return new Builder(cc);
  }


  // CheckStyle:OFF
  public static class Builder
  {

    private final MockConnection object;


    protected Builder(final ConnectionConfig cc)
    {
      object = new MockConnection<>(cc);
    }


    @SuppressWarnings("unchecked")
    public Builder openPredicate(final Predicate<LdapURL> p)
    {
      object.setOpenPredicate(p);
      return this;
    }


    @SuppressWarnings("unchecked")
    public Builder abandonConsumer(final Consumer<AbandonRequest> c)
    {
      object.setAbandonConsumer(c);
      return this;
    }


    @SuppressWarnings("unchecked")
    public Builder writeConsumer(final Consumer<DefaultOperationHandle> c)
    {
      object.setWriteConsumer(c);
      return this;
    }


    @SuppressWarnings("unchecked")
    public Builder addOperationFunction(final Function<AddRequest, OperationHandle<AddRequest, AddResponse>> func)
    {
      object.setAddOperationFunction(func);
      return this;
    }


    @SuppressWarnings("unchecked")
    public Builder bindOperationFunction(final Function<BindRequest, OperationHandle<BindRequest, BindResponse>> func)
    {
      object.setBindOperationFunction(func);
      return this;
    }


    @SuppressWarnings("unchecked")
    public Builder compareOperationFunction(final Function<CompareRequest, CompareOperationHandle> func)
    {
      object.setCompareOperationFunction(func);
      return this;
    }


    @SuppressWarnings("unchecked")
    public Builder deleteOperationFunction(
      final Function<DeleteRequest, OperationHandle<DeleteRequest, DeleteResponse>> func)
    {
      object.setDeleteOperationFunction(func);
      return this;
    }


    @SuppressWarnings("unchecked")
    public Builder extendedOperationFunction(final Function<ExtendedRequest, ExtendedOperationHandle> func)
    {
      object.setExtendedOperationFunction(func);
      return this;
    }


    @SuppressWarnings("unchecked")
    public Builder modifyDnOperationFunction(
      final Function<ModifyDnRequest, OperationHandle<ModifyDnRequest, ModifyDnResponse>> func)
    {
      object.setModifyDnOperationFunction(func);
      return this;
    }


    @SuppressWarnings("unchecked")
    public Builder modifyOperationFunction(
      final Function<ModifyRequest, OperationHandle<ModifyRequest, ModifyResponse>> func)
    {
      object.setModifyOperationFunction(func);
      return this;
    }


    @SuppressWarnings("unchecked")
    public Builder searchOperationFunction(final Function<SearchRequest, SearchOperationHandle> func)
    {
      object.setSearchOperationFunction(func);
      return this;
    }


    public MockConnection build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
