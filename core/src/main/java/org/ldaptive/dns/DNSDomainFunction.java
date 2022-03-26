/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dns;

import java.util.function.Function;
import org.ldaptive.dn.Dn;
import org.ldaptive.dn.RDn;

/**
 * Maps a DN to a domain name using the process described in
 * <a href="https://datatracker.ietf.org/doc/html/draft-ietf-ldapext-locate-08">draft-ietf-ldapext-locate</a>
 *
 * @author  Middleware Services
 */
public class DNSDomainFunction implements Function<Dn, String>
{


  @Override
  public String apply(final Dn dn)
  {
    final StringBuilder domain = new StringBuilder();
    for (RDn rdn : dn.getRDns()) {
      if (rdn.size() == 1 && rdn.getNameValue().hasName("DC")) {
        final String attrValue = rdn.getNameValue().getStringValue();
        // ignore empty DC components or any component containing a single dot
        if (attrValue != null && !attrValue.isEmpty() && !".".equals(attrValue)) {
          if (domain.length() > 0) {
            domain.append('.');
          }
          domain.append(attrValue);
        }
      } else if (domain.length() > 0) {
        // clear the domain if anything other than a single value, DC component is encountered
        // this enforces that the DC components used must be at the end of the RDN sequence
        domain.setLength(0);
      }
    }
    return domain.toString();
  }
}
