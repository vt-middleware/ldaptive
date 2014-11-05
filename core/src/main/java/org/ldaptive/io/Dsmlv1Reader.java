/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResult;
import org.ldaptive.SortBehavior;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Reads DSML version 1 from a {@link Reader} and returns a {@link
 * SearchResult}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class Dsmlv1Reader implements SearchResultReader
{

  /** Document builder factory. */
  private static final DocumentBuilderFactory DOC_BUILDER_FACTORY =
    DocumentBuilderFactory.newInstance();


  /**
   * Initialize the document builder factory.
   */
  static {
    DOC_BUILDER_FACTORY.setNamespaceAware(true);
  }

  /** Reader to read from. */
  private final Reader dsmlReader;

  /** Sort behavior. */
  private final SortBehavior sortBehavior;


  /**
   * Creates a new dsml reader.
   *
   * @param  reader  to read DSML from
   */
  public Dsmlv1Reader(final Reader reader)
  {
    this(reader, SortBehavior.getDefaultSortBehavior());
  }


  /**
   * Creates a new dsml reader.
   *
   * @param  reader  to read DSML from
   * @param  sb  sort behavior of the ldap result
   */
  public Dsmlv1Reader(final Reader reader, final SortBehavior sb)
  {
    dsmlReader = reader;
    if (sb == null) {
      throw new IllegalArgumentException("Sort behavior cannot be null");
    }
    sortBehavior = sb;
  }


  /**
   * Reads DSML data from the reader and returns a search result.
   *
   * @return  search result derived from the DSML
   *
   * @throws  IOException  if an error occurs using the reader
   */
  @Override
  public SearchResult read()
    throws IOException
  {
    try {
      final DocumentBuilder db = DOC_BUILDER_FACTORY.newDocumentBuilder();
      final Document dsml = db.parse(new InputSource(dsmlReader));
      return createSearchResult(dsml);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IOException(e);
    }
  }


  /**
   * Creates a search result that corresponds to the supplied DSML document.
   *
   * @param  doc  DSML to parse
   *
   * @return  search result
   */
  protected SearchResult createSearchResult(final Document doc)
  {
    final SearchResult result = new SearchResult(sortBehavior);

    if (doc != null && doc.hasChildNodes()) {
      final NodeList nodes = doc.getElementsByTagName("dsml:entry");
      for (int i = 0; i < nodes.getLength(); i++) {
        final LdapEntry le = createLdapEntry((Element) nodes.item(i));
        result.addEntry(le);
      }
    }

    return result;
  }


  /**
   * Converts the supplied DSML entry element into an ldap entry object.
   *
   * @param  entryElement  to parse
   *
   * @return  ldap entry
   */
  protected LdapEntry createLdapEntry(final Element entryElement)
  {
    final LdapEntry ldapEntry = new LdapEntry(sortBehavior);
    ldapEntry.setDn("");

    if (entryElement != null) {

      final String name = entryElement.getAttribute("dn");
      if (name != null) {
        ldapEntry.setDn(name);
      }

      if (entryElement.hasChildNodes()) {

        final NodeList ocNodes = entryElement.getElementsByTagName(
          "dsml:objectclass");
        if (ocNodes.getLength() > 0) {
          final Element ocElement = (Element) ocNodes.item(0);
          if (ocElement != null && ocElement.hasChildNodes()) {
            final LdapAttribute ldapAttribute = createLdapAttribute(
              "objectClass",
              ocElement.getElementsByTagName("dsml:oc-value"));
            ldapEntry.addAttribute(ldapAttribute);
          }
        }

        final NodeList attrNodes = entryElement.getElementsByTagName(
          "dsml:attr");
        for (int i = 0; i < attrNodes.getLength(); i++) {
          final Element attrElement = (Element) attrNodes.item(i);
          final String attrName = attrElement.hasAttribute("name")
            ? attrElement.getAttribute("name") : null;
          if (attrName != null && attrElement.hasChildNodes()) {
            final LdapAttribute ldapAttribute = createLdapAttribute(
              attrName,
              attrElement.getElementsByTagName("dsml:value"));
            ldapEntry.addAttribute(ldapAttribute);
          }
        }
      }
    }

    return ldapEntry;
  }


  /**
   * Returns an ldap attribute derived from the supplied node list.
   *
   * @param  name  of the ldap attribute
   * @param  nodes  to parse
   *
   * @return  ldap attribute
   */
  protected LdapAttribute createLdapAttribute(
    final String name,
    final NodeList nodes)
  {
    boolean isBase64 = false;
    final Set<Object> values = new HashSet<>();
    for (int i = 0; i < nodes.getLength(); i++) {
      final Element valueElement = (Element) nodes.item(i);
      if (valueElement != null) {
        if (
          valueElement.hasAttribute("encoding") &&
            "base64".equals(valueElement.getAttribute("encoding"))) {
          isBase64 = true;
        }
        values.add(getAttrValue(valueElement, isBase64));
      }
    }
    return LdapAttribute.createLdapAttribute(sortBehavior, name, values);
  }


  /**
   * Returns the value of the supplied element taking into account whether the
   * value needs to be base64 decoded.
   *
   * @param  valueElement  to read value from
   * @param  base64  whether to base64 decode the value
   *
   * @return  String or byte[] depending on the base64 flag
   */
  protected Object getAttrValue(
    final Element valueElement,
    final boolean base64)
  {
    final String value = valueElement.getChildNodes().item(0).getNodeValue();
    if (base64) {
      return LdapUtils.base64Decode(value);
    }
    return value;
  }
}
