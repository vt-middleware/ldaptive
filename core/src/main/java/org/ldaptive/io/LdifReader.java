/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResult;
import org.ldaptive.SortBehavior;

/**
 * Reads an LDIF from a {@link Reader} and returns a {@link SearchResult}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2893 $ $Date: 2014-03-07 10:47:46 -0500 (Fri, 07 Mar 2014) $
 */
public class LdifReader implements SearchResultReader
{

  /** Reader to read from. */
  private final Reader ldifReader;

  /** Sort behavior. */
  private final SortBehavior sortBehavior;


  /**
   * Creates a new ldif reader.
   *
   * @param  reader  to read LDIF from
   */
  public LdifReader(final Reader reader)
  {
    this(reader, SortBehavior.getDefaultSortBehavior());
  }


  /**
   * Creates a new ldif reader.
   *
   * @param  reader  to read LDIF from
   * @param  sb  sort behavior of the ldap result
   */
  public LdifReader(final Reader reader, final SortBehavior sb)
  {
    ldifReader = reader;
    if (sb == null) {
      throw new IllegalArgumentException("Sort behavior cannot be null");
    }
    sortBehavior = sb;
  }


  /**
   * Reads LDIF data from the reader and returns a search result.
   *
   * @return  search result derived from the LDIF
   *
   * @throws  IOException  if an error occurs using the reader
   */
  @Override
  public SearchResult read()
    throws IOException
  {
    final SearchResult result = new SearchResult(sortBehavior);
    final BufferedReader br = new BufferedReader(ldifReader);
    String line;
    int lineCount = 0;
    LdapEntry ldapEntry = null;
    StringBuffer lineValue = new StringBuffer();

    while ((line = br.readLine()) != null) {
      lineCount++;
      if (line.startsWith("dn:")) {
        lineValue.append(line);
        ldapEntry = new LdapEntry(sortBehavior);
        break;
      }
    }

    boolean read = true;
    while (read) {
      line = br.readLine();
      if (line == null) {
        read = false;
        line = "";
      }
      if (!line.startsWith("#")) {
        if (line.startsWith("dn:")) {
          result.addEntry(ldapEntry);
          ldapEntry = new LdapEntry(sortBehavior);
        }
        if (line.startsWith(" ")) {
          lineValue.append(line.substring(1));
        } else {
          final String s = lineValue.toString();
          if (s.contains(":")) {
            boolean isBinary = false;
            boolean isUrl = false;
            final String[] parts = s.split(":", 2);
            final String attrName = parts[0];
            String attrValue = parts[1];
            if (attrValue.startsWith(":")) {
              isBinary = true;
              attrValue = attrValue.substring(1);
            } else if (attrValue.startsWith("<")) {
              isUrl = true;
              attrValue = attrValue.substring(1);
            }
            if (attrValue.startsWith(" ")) {
              attrValue = attrValue.substring(1);
            }
            if ("dn".equals(attrName)) {
              ldapEntry.setDn(attrValue);
            } else {
              LdapAttribute ldapAttr = ldapEntry.getAttribute(attrName);
              if (ldapAttr == null) {
                ldapAttr = new LdapAttribute(sortBehavior, isBinary || isUrl);
                ldapAttr.setName(attrName);
                ldapEntry.addAttribute(ldapAttr);
              }
              if (isBinary) {
                ldapAttr.addValue(
                  new ByteArrayValueTranscoder(),
                  LdapUtils.base64Decode(attrValue));
              } else if (isUrl) {
                ldapAttr.addValue(
                  new ByteArrayValueTranscoder(),
                  LdapUtils.readURL(new URL(attrValue)));
              } else {
                ldapAttr.addValue(new StringValueTranscoder(), attrValue);
              }
            }
          }
          lineValue = new StringBuffer(line);
        }
      }
    }
    if (ldapEntry != null) {
      result.addEntry(ldapEntry);
    }
    return result;
  }
}
