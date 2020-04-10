/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.function.Consumer;
import org.ldaptive.handler.ResultPredicate;

/**
 * Base class for profiling connection factories.
 *
 * @author  Middleware Services
 */
public abstract class AbstractSearchOperationProfile extends AbstractProfile
{

  /** Connection factory. */
  protected ConnectionFactory connectionFactory;

  /** Base DN. */
  protected String baseDn;

  /** Bind DN. */
  protected String bindDn;

  /** Bind credential. */
  protected String bindCredential;


  @Override
  protected void shutdown()
  {
    connectionFactory.close();
  }


  @Override
  protected void setBaseDn(final String dn)
  {
    baseDn = dn;
  }


  @Override
  protected void setBindDn(final String dn)
  {
    bindDn = dn;
  }


  @Override
  protected void setBindCredential(final String pass)
  {
    bindCredential = pass;
  }


  @Override
  protected void doOperation(final Consumer<Object> consumer, final int uid)
  {
    doOperation(connectionFactory, consumer, uid);
  }


  /**
   * Perform a search operation with the supplied connection factory.
   *
   * @param  cf  connection factory
   * @param  consumer  to give result entry to
   * @param  uid  to search for
   */
  protected void doOperation(final ConnectionFactory cf, final Consumer<Object> consumer, final int uid)
  {
    final SearchOperation search = new SearchOperation(cf);
    search.setThrowCondition(ResultPredicate.NOT_SUCCESS);
    search.setEntryHandlers(e -> {
      consumer.accept(e);
      return e;
    });
    search.setExceptionHandler(e -> {
      consumer.accept(e);
    });
    try {
      search.send(SearchRequest.builder()
        .dn(baseDn)
        .filter("(uid=" + uid + ")")
        .returnAttributes(ReturnAttributes.ALL_USER.value())
        .build());
    } catch (LdapException e) {
      consumer.accept(e);
    }
  }


  @Override
  protected void createEntries(final int count)
  {
    createEntries(connectionFactory, UID_START, count);
  }


  @Override
  public String toString()
  {
    return connectionFactory != null ? connectionFactory.toString() : "[null connection factory]";
  }
}
