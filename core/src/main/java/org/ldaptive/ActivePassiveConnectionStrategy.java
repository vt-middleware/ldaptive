/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Iterator;
import java.util.List;

/**
 * Connection strategy that attempts hosts ordered exactly the way they are configured. This means that the first host
 * will always be attempted first, followed by each host in the list.
 *
 * @author  Middleware Services
 */
public class ActivePassiveConnectionStrategy extends AbstractConnectionStrategy
{


  @Override
  public Iterator<LdapURL> iterator()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    return new Iterator<>() {
      private final List<LdapURL> urls = ldapURLSet.getUrls();
      private int i;


      @Override
      public boolean hasNext()
      {
        return i < urls.size();
      }


      @Override
      public LdapURL next()
      {
        return urls.get(i++);
      }
    };
  }
}
