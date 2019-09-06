/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads an LDIF from a {@link Reader} and returns a {@link SearchResponse}. This implementation only supports entry
 * records. It does not support change records or include statements.
 *
 * @author  Middleware Services
 */
public class LdifReader implements SearchResultReader
{

  /** Mark read back buffer size. */
  private static final int READ_AHEAD_LIMIT = 1024;

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Reader to read from. */
  private final Reader ldifReader;


  /**
   * Creates a new ldif reader.
   *
   * @param reader to read LDIF from
   */
  public LdifReader(final Reader reader)
  {
    ldifReader = reader;
  }


  /**
   * Reads LDIF data from the reader and returns a search result.
   *
   * @return search result derived from the LDIF
   *
   * @throws  IOException  if an error occurs using the reader
   */
  @Override
  public SearchResponse read()
    throws IOException
  {
    final SearchResponse result = new SearchResponse();
    final BufferedReader br = new BufferedReader(ldifReader);
    String line;
    br.mark(READ_AHEAD_LIMIT);
    while ((line = br.readLine()) != null) {
      if (!"".equals(line)) {
        br.reset();
        final List<String> section = readSection(br);
        if (!section.isEmpty()) {
          if (section.get(0).startsWith("version")) {
            section.remove(0);
          }
          if (!section.isEmpty()) {
            if (section.get(0).startsWith("dn")) {
              result.addEntries(parseEntry(section));
            } else if (section.get(0).startsWith("ref")) {
              result.addReferences(parseReference(section));
            } else {
              logger.debug("Unknown LDIF section {}", section.get(0));
            }
          }
        }
      }
      br.mark(READ_AHEAD_LIMIT);
    }
    return result;
  }


  /**
   * Reads the supplied reader line-by-line until the reader is empty or a empty line is encountered. Lines containing
   * comments are ignored.
   *
   * @param  reader  to read
   *
   * @return  list of a lines in the section
   *
   * @throws  IOException  if an error occurs reading
   */
  private List<String> readSection(final BufferedReader reader)
    throws IOException
  {
    final List<String> section = new ArrayList<>();
    boolean readingComment = false;
    String line;
    while ((line = reader.readLine()) != null) {
      if ("".equals(line)) {
        // end of section
        break;
      } else if (line.startsWith("#")) {
        readingComment = true;
      } else if (line.startsWith(" ")) {
        if (!readingComment) {
          section.add(section.remove(section.size() - 1) + line.substring(1));
        }
      } else {
        readingComment = false;
        section.add(line);
      }
    }
    return section;
  }


  /**
   * Parses the supplied array of LDIF lines and returns an LDAP entry.
   *
   * @param  section  of LDIF lines
   *
   * @return  ldap entry
   *
   * @throws  IOException  if an errors occurs reading a URI in the LDIF
   */
  private LdapEntry parseEntry(final List<String> section)
    throws IOException
  {
    final LdapEntry entry = new LdapEntry();
    for (String line : section) {
      if (!line.contains(":")) {
        throw new IllegalArgumentException("Invalid LDAP entry data: " + line);
      }
      final LdapAttribute newAttr = parseAttribute(line);
      if ("dn".equals(newAttr.getName())) {
        entry.setDn(newAttr.getStringValue());
      } else {
        final LdapAttribute existingAttr = entry.getAttribute(newAttr.getName());
        if (existingAttr == null) {
          entry.addAttributes(newAttr);
        } else {
          if (existingAttr.isBinary()) {
            existingAttr.addBinaryValues(newAttr.getBinaryValue());
          } else {
            existingAttr.addStringValues(newAttr.getStringValue());
          }
        }
      }
    }
    return entry;
  }


  /**
   * Parses the supplied line and returns an attribute with a single value found in the line.
   *
   * @param  line  to parse
   *
   * @return  ldap attribute
   *
   * @throws  IOException  if an errors occurs reading a URI in the LDIF
   */
  private LdapAttribute parseAttribute(final String line)
    throws IOException
  {
    boolean isBase64 = false;
    boolean isUrl = false;
    final String[] parts = line.split(":", 2);
    final String attrName = parts[0];
    String attrValue = parts[1];
    if (attrValue.startsWith(":")) {
      isBase64 = true;
      attrValue = attrValue.substring(1);
    } else if (attrValue.startsWith("<")) {
      isUrl = true;
      attrValue = attrValue.substring(1);
    }
    // remove leading whitespace
    while (attrValue.startsWith(" ")) {
      attrValue = attrValue.substring(1);
    }
    final LdapAttribute attr = new LdapAttribute(attrName);
    if (isBase64) {
      attr.addBinaryValues(LdapUtils.base64Decode(attrValue));
    } else if (isUrl) {
      final byte[] b;
      if (LdapUtils.isResource(attrValue)) {
        b = LdapUtils.readInputStream(LdapUtils.getResource(attrValue));
      } else {
        b = LdapUtils.readURL(new URL(attrValue));
      }
      attr.addBinaryValues(b);
    } else {
      attr.addStringValues(attrValue);
    }
    return attr;
  }


  /**
   * Parses the supplied array of LDIF lines and returns a search reference.
   *
   * @param  section  of LDIF lines
   *
   * @return  search reference
   */
  private SearchResultReference parseReference(final List<String> section)
  {
    final SearchResultReference ref = new SearchResultReference();
    for (String line : section) {
      if (!line.contains(":")) {
        throw new IllegalArgumentException("Invalid LDAP reference data: " + line);
      }
      final String[] parts = line.split(":", 2);
      if (!"ref".equals(parts[0])) {
        throw new IllegalArgumentException("Invalid LDAP reference data: " + line);
      }
      if (parts[1].startsWith(" ")) {
        ref.addUris(parts[1].substring(1));
      } else if (parts[1].length() > 0) {
        throw new IllegalArgumentException("Invalid LDAP reference data: " + line);
      }
    }
    return ref;
  }
}
