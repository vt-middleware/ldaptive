/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.IOException;
import java.io.Writer;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchReference;
import org.ldaptive.SearchResult;

/**
 * Writes a {@link SearchResult} as LDIF to a {@link Writer}.
 *
 * @author  Middleware Services
 */
public class LdifWriter implements SearchResultWriter
{

  /** Line separator. */
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  /** Writer to write to. */
  private final Writer ldifWriter;


  /**
   * Creates a new ldif writer.
   *
   * @param  writer  to write LDIF to
   */
  public LdifWriter(final Writer writer)
  {
    ldifWriter = writer;
  }


  /**
   * Writes the supplied search result to the writer.
   *
   * @param  result  search result to write
   *
   * @throws  IOException  if an error occurs using the writer
   */
  @Override
  public void write(final SearchResult result)
    throws IOException
  {
    ldifWriter.write(createLdif(result));
    ldifWriter.flush();
  }


  /**
   * Creates an LDIF using the supplied search result.
   *
   * @param  result  search result
   *
   * @return  LDIF
   */
  protected String createLdif(final SearchResult result)
  {
    // build string from results
    final StringBuilder ldif = new StringBuilder();
    if (result != null) {
      for (LdapEntry le : result.getEntries()) {
        ldif.append(createLdifEntry(le));
      }
      for (SearchReference sr : result.getReferences()) {
        ldif.append(createSearchReference(sr));
      }
    }

    return ldif.toString();
  }


  /**
   * Creates an LDIF using the supplied ldap entry.
   *
   * @param  entry  ldap entry
   *
   * @return  LDIF
   */
  protected String createLdifEntry(final LdapEntry entry)
  {
    if (entry == null) {
      return "";
    }

    final StringBuilder entryLdif = new StringBuilder();
    final String dn = entry.getDn();
    if (dn != null) {
      if (LdapUtils.shouldBase64Encode(dn)) {
        entryLdif.append("dn:: ").append(LdapUtils.base64Encode(dn)).append(LINE_SEPARATOR);
      } else {
        entryLdif.append("dn: ").append(dn).append(LINE_SEPARATOR);
      }
    }

    for (LdapAttribute attr : entry.getAttributes()) {
      final String attrName = attr.getName();
      for (String attrValue : attr.getStringValues()) {
        if (attr.isBinary()) {
          entryLdif.append(attrName).append(":: ").append(attrValue).append(LINE_SEPARATOR);
        } else if (LdapUtils.shouldBase64Encode(attrValue)) {
          entryLdif.append(attrName).append(":: ").append(LdapUtils.base64Encode(attrValue)).append(LINE_SEPARATOR);
        } else {
          entryLdif.append(attrName).append(": ").append(attrValue).append(LINE_SEPARATOR);
        }
      }
    }

    if (entryLdif.length() > 0) {
      entryLdif.append(LINE_SEPARATOR);
    }
    return entryLdif.toString();
  }


  /**
   * Creates an LDIF using the supplied search reference.
   *
   * @param  ref  search reference
   *
   * @return  LDIF
   */
  protected String createSearchReference(final SearchReference ref)
  {
    if (ref == null) {
      return "";
    }

    final StringBuilder refLdif = new StringBuilder();
    for (String url : ref.getReferralUrls()) {
      if (LdapUtils.shouldBase64Encode(url)) {
        refLdif.append("ref:: ").append(LdapUtils.base64Encode(url)).append(LINE_SEPARATOR);
      } else {
        refLdif.append("ref: ").append(url).append(LINE_SEPARATOR);
      }
    }

    if (refLdif.length() > 0) {
      refLdif.append(LINE_SEPARATOR);
    }
    return refLdif.toString();
  }
}
