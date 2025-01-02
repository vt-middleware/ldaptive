/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.ResultCode;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;

/**
 * Response control for server side sorting. See RFC 2891. Control is defined as:
 *
 * <pre>
       SortResult ::= SEQUENCE {
          sortResult  ENUMERATED {
              success                   (0), -- results are sorted
              operationsError           (1), -- server internal failure
              timeLimitExceeded         (3), -- timelimit reached before
                                             -- sorting was completed
              strongAuthRequired        (8), -- refused to return sorted
                                             -- results via insecure
                                             -- protocol
              adminLimitExceeded       (11), -- too many matching entries
                                             -- for the server to sort
              noSuchAttribute          (16), -- unrecognized attribute
                                             -- type in sort key
              inappropriateMatching    (18), -- unrecognized or
                                             -- inappropriate matching
                                             -- rule in sort key
              insufficientAccessRights (50), -- refused to return sorted
                                             -- results to this client
              busy                     (51), -- too busy to process
              unwillingToPerform       (53), -- unable to sort
              other                    (80)
              },
        attributeType [0] AttributeDescription OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public class SortResponseControl extends AbstractResponseControl
{

  /** OID of this control. */
  public static final String OID = "1.2.840.113556.1.4.474";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 733;

  /** Result of the server side sorting. */
  private ResultCode sortResult;

  /** Failed attribute name. */
  private String attributeName;


  /** Default constructor. */
  public SortResponseControl()
  {
    super(OID);
  }


  /**
   * Creates a new sort response control.
   *
   * @param  critical  whether this control is critical
   */
  public SortResponseControl(final boolean critical)
  {
    super(OID, critical);
  }


  /**
   * Creates a new sort response control.
   *
   * @param  code  result of the sort
   * @param  critical  whether this control is critical
   */
  public SortResponseControl(final ResultCode code, final boolean critical)
  {
    super(OID, critical);
    sortResult = code;
    freeze();
  }


  /**
   * Creates a new sort response control.
   *
   * @param  code  result of the sort
   * @param  attrName  name of the failed attribute
   * @param  critical  whether this control is critical
   */
  public SortResponseControl(final ResultCode code, final String attrName, final boolean critical)
  {
    super(OID, critical);
    sortResult = code;
    attributeName = attrName;
    freeze();
  }


  /**
   * Returns the result code of the server side sort.
   *
   * @return  result code
   */
  public ResultCode getSortResult()
  {
    return sortResult;
  }


  /**
   * Returns the attribute name that caused the sort to fail.
   *
   * @return  attribute name
   */
  public String getAttributeName()
  {
    return attributeName;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SortResponseControl && super.equals(o)) {
      final SortResponseControl v = (SortResponseControl) o;
      return LdapUtils.areEqual(sortResult, v.sortResult) &&
             LdapUtils.areEqual(attributeName, v.attributeName);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), sortResult, attributeName);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "criticality=" + getCriticality() + ", " +
      "sortResult=" + sortResult + ", " +
      "attributeName=" + attributeName + "]";
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    freezeAndAssertMutable();
    final DERParser parser = new DERParser();
    parser.registerHandler(SortResultHandler.PATH, new SortResultHandler(this));
    parser.registerHandler(AttributeTypeHandler.PATH, new AttributeTypeHandler(this));
    try {
      parser.parse(encoded);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error parsing response", e);
    }
  }


  /** Parse handler implementation for the sort result. */
  private static class SortResultHandler extends AbstractParseHandler<SortResponseControl>
  {

    /** DER path to result code. */
    public static final DERPath PATH = new DERPath("/SEQ/ENUM[0]");


    /**
     * Creates a new sort result handler.
     *
     * @param  control  to configure
     */
    SortResultHandler(final SortResponseControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final int resultValue = IntegerType.decode(encoded).intValue();
      final ResultCode rc = ResultCode.valueOf(resultValue);
      if (rc == null) {
        throw new IllegalArgumentException("Unknown result code " + resultValue);
      }
      getObject().sortResult = rc;
    }
  }


  /** Parse handler implementation for the attribute type. */
  private static class AttributeTypeHandler extends AbstractParseHandler<SortResponseControl>
  {

    /** DER path to attr value. */
    public static final DERPath PATH = new DERPath("/SEQ/CTX(1)");


    /**
     * Creates a new attribute type handler.
     *
     * @param  control  to configure
     */
    AttributeTypeHandler(final SortResponseControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().attributeName = OctetStringType.decode(encoded);
    }
  }
}
