/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dns;

import java.util.HashSet;
import java.util.Set;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all DNS resolvers.
 *
 * @param <T> Type of record to resolve.
 *
 * @author  Middleware Services
 */
public abstract class AbstractDNSResolver<T> implements DNSResolver<T>
{

  /** Class logger. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Factory to create DNS connections. */
  private final DNSContextFactory contextFactory;


  /**
   * Creates a new abstract DNS resolver.
   *
   * @param  factory  DNS context factory
   */
  public AbstractDNSResolver(final DNSContextFactory factory)
  {
    contextFactory = factory;
  }


  @Override
  public Set<T> resolve(final String name)
  {
    DirContext ctx = null;
    try {
      ctx = contextFactory.create();
      final Set<String> records = new HashSet<>();
      for (String key : getAttributes()) {
        resolveOne(ctx, name, key, records);
      }
      final Set<T> results = processRecords(records);
      logger.debug("Resolved {} for domain {} using {}", results, name, ctx);
      return results;
    } catch (NamingException e) {
      throw new RuntimeException("DNS lookup failed for " + name, e);
    } finally {
      if (ctx != null) {
        try {
          ctx.close();
        } catch (NamingException e) {
          logger.warn("Error closing DirContext", e);
        }
      }
    }
  }


  /**
   * Get the types of records to query for, e.g. <code>{"A", "AAAA"}</code>.
   *
   * @return  Array of JNDI attribute names.
   */
  protected abstract String[] getAttributes();


  /**
   * Process a set of DNS records.
   *
   * @param records Set of raw DNS records returned from a name query.
   *
   * @return Set of converted/processed records.
   */
  protected abstract Set<T> processRecords(Set<String> records);


  /**
   * Query for a single kind of DNS record.
   *
   * @param ctx Directory context.
   * @param name Name to query for.
   * @param attrId DNS record type, e.g. <code>A</code>.
   * @param records Set of records to append results to.
   *
   * @throws NamingException on DNS lookup failure.
   */
  private void resolveOne(final DirContext ctx, final String name, final String attrId, final Set<String> records)
    throws NamingException
  {
    NamingEnumeration<?> en = null;
    try {
      final Attributes attrs = ctx.getAttributes(name, new String[] {attrId});
      if (attrs != null) {
        final Attribute attr = attrs.get(attrId);
        if (attr != null) {
          en = attr.getAll();
          while (en.hasMore()) {
            records.add((String) en.next());
          }
        }
      }
    } catch (final NameNotFoundException e) {
      logger.debug("No DNS records of type {} found for {}.", attrId, name);
    } finally {
      if (en != null) {
        en.close();
      }
    }
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" + "contextFactory=" + contextFactory;
  }
}
