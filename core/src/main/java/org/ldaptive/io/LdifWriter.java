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

  /** ASCII decimal value of nul. */
  private static final int NUL_CHAR = 0;

  /** ASCII decimal value of line feed. */
  private static final int LF_CHAR = 10;

  /** ASCII decimal value of carriage return. */
  private static final int CR_CHAR = 13;

  /** ASCII decimal value of space. */
  private static final int SP_CHAR = 32;

  /** ASCII decimal value of colon. */
  private static final int COLON_CHAR = 58;

  /** ASCII decimal value of left arrow. */
  private static final int LA_CHAR = 60;

  /** ASCII decimal value of highest character. */
  private static final int MAX_ASCII_CHAR = 127;

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
      if (encodeData(dn)) {
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
        } else if (encodeData(attrValue)) {
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
      if (encodeData(url)) {
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


  /**
   * Determines whether the supplied data should be base64 encoded. See http://www.faqs.org/rfcs/rfc2849.html for more
   * details.
   *
   * @param  data  to inspect
   *
   * @return  whether the data should be base64 encoded
   */
  private boolean encodeData(final String data)
  {
    boolean encode = false;
    final char[] dataCharArray = data.toCharArray();
    for (int i = 0; i < dataCharArray.length; i++) {
      final int charInt = (int) dataCharArray[i];
      // check for NUL
      if (charInt == NUL_CHAR) {
        encode = true;
        // check for LF
      } else if (charInt == LF_CHAR) {
        encode = true;
        // check for CR
      } else if (charInt == CR_CHAR) {
        encode = true;
        // check for SP at beginning or end of string
      } else if (charInt == SP_CHAR && (i == 0 || i == dataCharArray.length - 1)) {
        encode = true;
        // check for colon(:) at beginning of string
      } else if (charInt == COLON_CHAR && i == 0) {
        encode = true;
        // check for left arrow(<) at beginning of string
      } else if (charInt == LA_CHAR && i == 0) {
        encode = true;
        // check for any character above 127
      } else if (charInt > MAX_ASCII_CHAR) {
        encode = true;
      }
    }
    return encode;
  }
}
