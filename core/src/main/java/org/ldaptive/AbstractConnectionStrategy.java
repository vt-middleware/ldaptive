/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for connection strategy implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractConnectionStrategy implements ConnectionStrategy
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Set of LDAP URLs to attempt connections to. */
  protected LdapURLSet ldapURLSet;

  /** Whether this strategy has been successfully initialized. */
  private boolean initialized;

  /** Condition used to determine whether to activate a URL. */
  private Predicate<LdapURL> activateCondition;

  /** Condition used to determine whether to test an inactive URL. */
  private Predicate<LdapURL> retryCondition = url -> Instant.now().isAfter(
    url.getRetryMetadata().getFailureTime().plus(LdapURLActivator.PERIOD));


  @Override
  public boolean isInitialized()
  {
    return initialized;
  }


  @Override
  public synchronized void initialize(final String urls, final Predicate<LdapURL> condition)
  {
    if (isInitialized()) {
      throw new IllegalStateException("Strategy has already been initialized");
    }
    ldapURLSet = new LdapURLSet(this, urls);
    activateCondition = condition;
    initialized = true;
  }


  @Override
  public void populate(final String urls, final LdapURLSet urlSet)
  {
    if (urls == null || urls.isEmpty()) {
      throw new IllegalArgumentException("urls cannot be empty or null");
    }
    if (urls.contains(" ")) {
      urlSet.populate(Stream.of(urls.split(" "))
        .map(s -> {
          final LdapURL url = new LdapURL(s);
          url.setRetryMetadata(new LdapURLRetryMetadata(this));
          return url;
        }).collect(Collectors.toList()));
    } else {
      final LdapURL url = new LdapURL(urls);
      url.setRetryMetadata(new LdapURLRetryMetadata(this));
      urlSet.populate(Collections.singletonList(url));
    }
  }


  @Override
  public Predicate<LdapURL> getActivateCondition()
  {
    return activateCondition;
  }


  @Override
  public Predicate<LdapURL> getRetryCondition()
  {
    return retryCondition;
  }


  /**
   * Sets the retry condition which determines whether an attempt should be made to activate a URL.
   *
   * @param  condition  that determines whether to active a URL
   */
  public void setRetryCondition(final Predicate<LdapURL> condition)
  {
    retryCondition = condition;
  }


  @Override
  public void success(final LdapURL url)
  {
    url.activate();
    url.getRetryMetadata().recordSuccess(Instant.now());
  }


  @Override
  public void failure(final LdapURL url)
  {
    url.deactivate();
    url.getRetryMetadata().recordFailure(Instant.now());
    LdapURLActivator.getInstance().registerUrl(url);
  }


  /** Default iterator implementation. */
  protected static class DefaultLdapURLIterator implements Iterator<LdapURL>
  {

    /** URLs to iterate over. */
    private final List<LdapURL> ldapUrls;

    /** Iterator index. */
    private int i;


    /**
     * Creates a new default LDAP URL iterator.
     *
     * @param  urls  to iterate over
     */
    public DefaultLdapURLIterator(final List<LdapURL> urls)
    {
      ldapUrls = urls;
    }


    @Override
    public boolean hasNext()
    {
      return i < ldapUrls.size();
    }


    @Override
    public LdapURL next()
    {
      return ldapUrls.get(i++);
    }
  }
}
