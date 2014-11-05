/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchResult;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Writes a {@link SearchResult} as DSML version 1 to a {@link Writer}.
 *
 * @author  Middleware Services
 */
public class Dsmlv1Writer implements SearchResultWriter
{

  /** Document builder factory. */
  private static final DocumentBuilderFactory DOC_BUILDER_FACTORY =
    DocumentBuilderFactory.newInstance();

  /** Transformer factory. */
  private static final TransformerFactory TRANSFORMER_FACTORY =
    TransformerFactory.newInstance();


  /**
   * Initialize the document builder factory.
   */
  static {
    DOC_BUILDER_FACTORY.setNamespaceAware(true);
  }

  /** Writer to write to. */
  private final Writer dsmlWriter;

  /**
   * Transformer output properties. See {@link
   * Transformer#setOutputProperty(String, String)}.
   */
  private Map<String, String> outputProperties = new HashMap<>();


  /**
   * Creates a new dsml writer. The following transformer output properties are
   * set by default:
   *
   * <ul>
   *   <li>"doctype-public", "yes"</li>
   *   <li>"indent", "yes"</li>
   *   <li>"{http://xml.apache.org/xslt}indent-amount", "2"</li>
   * </ul>
   *
   * @param  writer  to write DSML to
   */
  public Dsmlv1Writer(final Writer writer)
  {
    dsmlWriter = writer;
    outputProperties.put(OutputKeys.DOCTYPE_PUBLIC, "yes");
    outputProperties.put(OutputKeys.INDENT, "yes");
    outputProperties.put("{http://xml.apache.org/xslt}indent-amount", "2");
  }


  /**
   * Returns the transformer output properties used by this writer.
   *
   * @return  transformer output properties
   */
  public Map<String, String> getOutputProperties()
  {
    return outputProperties;
  }


  /**
   * Sets the transformer output properties used by this writer.
   *
   * @param  properties  transformer output properties
   */
  public void setOutputProperties(final Map<String, String> properties)
  {
    outputProperties = properties;
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
    try {
      final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
      for (Map.Entry<String, String> prop : outputProperties.entrySet()) {
        transformer.setOutputProperty(prop.getKey(), prop.getValue());
      }

      final StreamResult sr = new StreamResult(dsmlWriter);
      final DOMSource source = new DOMSource(createDsml(result));
      transformer.transform(source, sr);
      dsmlWriter.flush();
    } catch (ParserConfigurationException | TransformerException e) {
      throw new IOException(e);
    }
  }


  /**
   * Creates DSML that corresponds to the supplied search result.
   *
   * @param  result  search result to parse
   *
   * @return  DSML
   *
   * @throws  ParserConfigurationException  if a document builder cannot be
   * created
   */
  protected Document createDsml(final SearchResult result)
    throws ParserConfigurationException
  {
    final DocumentBuilder db = DOC_BUILDER_FACTORY.newDocumentBuilder();
    final DOMImplementation domImpl = db.getDOMImplementation();
    final Document doc = domImpl.createDocument(
      "http://www.dsml.org/DSML",
      "dsml:dsml",
      null);
    doc.setXmlStandalone(true);

    final Element entriesElement = doc.createElement("dsml:directory-entries");
    doc.getDocumentElement().appendChild(entriesElement);

    // build document object from result
    if (result != null) {
      for (LdapEntry le : result.getEntries()) {
        final Element entryElement = doc.createElement("dsml:entry");
        if (le.getDn() != null) {
          entryElement.setAttribute("dn", le.getDn());
        }
        for (Element e : createDsmlAttributes(doc, le.getAttributes())) {
          entryElement.appendChild(e);
        }
        entriesElement.appendChild(entryElement);
      }
    }

    return doc;
  }


  /**
   * Returns a list of <dsml:attr/> elements for the supplied attributes.
   *
   * @param  doc  to source elements from
   * @param  attrs  to iterate over
   *
   * @return  list of elements contains attributes
   */
  protected List<Element> createDsmlAttributes(
    final Document doc,
    final Collection<LdapAttribute> attrs)
  {
    final List<Element> attrElements = new ArrayList<>();
    for (LdapAttribute attr : attrs) {
      final String attrName = attr.getName();
      Element attrElement;
      if ("objectclass".equalsIgnoreCase(attrName)) {
        attrElement = createObjectclassElement(doc, attr);
        if (attrElement.hasChildNodes()) {
          attrElements.add(0, attrElement);
        }
      } else {
        attrElement = createAttrElement(doc, attr);
        if (attrElement.hasChildNodes()) {
          attrElements.add(attrElement);
        }
      }
    }
    return attrElements;
  }


  /**
   * Returns a <dsml:attr/> element for the supplied ldap attribute.
   *
   * @param  doc  to source elements from
   * @param  attr  ldap attribute to add
   *
   * @return  element containing the attribute
   */
  protected Element createAttrElement(
    final Document doc,
    final LdapAttribute attr)
  {
    final Element attrElement = doc.createElement("dsml:attr");
    attrElement.setAttribute("name", attr.getName());
    for (String s : attr.getStringValues()) {
      final Element valueElement = doc.createElement("dsml:value");
      attrElement.appendChild(valueElement);
      setAttrValue(doc, valueElement, s, attr.isBinary());
    }
    return attrElement;
  }


  /**
   * Returns a <dsml:objectclass/> element for the supplied ldap attribute.
   *
   * @param  doc  to source elements from
   * @param  attr  ldap attribute to add
   *
   * @return  element containing the attribute values
   */
  protected Element createObjectclassElement(
    final Document doc,
    final LdapAttribute attr)
  {
    final Element ocElement = doc.createElement("dsml:objectclass");
    for (String s : attr.getStringValues()) {
      final Element ocValueElement = doc.createElement("dsml:oc-value");
      ocElement.appendChild(ocValueElement);
      setAttrValue(doc, ocValueElement, s, attr.isBinary());
    }
    return ocElement;
  }


  /**
   * Adds the supplied string to the value element.
   *
   * @param  doc  to create nodes with
   * @param  valueElement  to append value to
   * @param  value  to create node for
   * @param  isBase64  whether the value is base64 encoded
   */
  protected void setAttrValue(
    final Document doc,
    final Element valueElement,
    final String value,
    final boolean isBase64)
  {
    if (value != null) {
      valueElement.appendChild(doc.createTextNode(value));
      if (isBase64) {
        valueElement.setAttribute("encoding", "base64");
      }
    }
  }
}
