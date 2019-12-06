/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.control.AbstractControl;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.ResponseControl;

/**
 * Request/response control for active directory servers to return statistics along with search results. This
 * implementation supports the format for Windows Server 2008, Windows Server 2008 R2, and Windows Server 2012 DCs. The
 * response control is defined as:
 *
 * <pre>
   SEQUENCE {
     threadCountTag        INTEGER
     threadCount           INTEGER
     callTimeTag           INTEGER
     callTime              INTEGER
     entriesReturnedTag    INTEGER
     entriesReturned       INTEGER
     entriesVisitedTag     INTEGER
     entriesVisited        INTEGER
     filterTag             INTEGER
     filter                OCTET STRING
     indexTag              INTEGER
     index                 OCTET STRING
     pagesReferencedTag    INTEGER
     pagesReferenced       INTEGER
     pagesReadTag          INTEGER
     pagesRead             INTEGER
     pagesPrereadTag       INTEGER
     pagesPreread          INTEGER
     pagesDirtiedTag       INTEGER
     pagesDirtied          INTEGER
     pagesRedirtiedTag     INTEGER
     pagesRedirtied        INTEGER
     logRecordCountTag     INTEGER
     logRecordCount        INTEGER
     logRecordBytesTag     INTEGER
     logRecordBytes        INTEGER
   }
 * </pre>
 *
 * <p>See http://msdn.microsoft.com/en-us/library/cc223350.aspx</p>
 *
 * @author  Middleware Services
 */
public class GetStatsControl extends AbstractControl implements RequestControl, ResponseControl
{

  /** OID of this control. */
  public static final String OID = "1.2.840.113556.1.4.970";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 929;

  /** statistics. */
  private final Map<String, Object> statistics = new HashMap<>();


  /** Default constructor. */
  public GetStatsControl()
  {
    super(OID);
  }


  /**
   * Creates a new get stats control.
   *
   * @param  critical  whether this control is critical
   */
  public GetStatsControl(final boolean critical)
  {
    super(OID, critical);
  }


  @Override
  public boolean hasValue()
  {
    return true;
  }


  /**
   * Returns the statistics.
   *
   * @return  statistics
   */
  public Map<String, Object> getStatistics()
  {
    return statistics;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof GetStatsControl && super.equals(o)) {
      final GetStatsControl v = (GetStatsControl) o;
      return LdapUtils.areEqual(statistics, v.statistics);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), statistics);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("criticality=").append(getCriticality()).append(", ")
      .append("statistics=").append(statistics).append("]").toString();
  }


  @Override
  public byte[] encode()
  {
    return null;
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler("/SEQ/INT[1]", new IntegerHandler(this, "threadCount"));
    parser.registerHandler("/SEQ/INT[3]", new IntegerHandler(this, "callTime"));
    parser.registerHandler("/SEQ/INT[5]", new IntegerHandler(this, "entriesReturned"));
    parser.registerHandler("/SEQ/INT[7]", new IntegerHandler(this, "entriesVisited"));
    parser.registerHandler("/SEQ/OCTSTR[9]", new StringHandler(this, "filter"));
    parser.registerHandler("/SEQ/OCTSTR[11]", new StringHandler(this, "index"));
    parser.registerHandler("/SEQ/INT[13]", new IntegerHandler(this, "pagesReferenced"));
    parser.registerHandler("/SEQ/INT[15]", new IntegerHandler(this, "pagesRead"));
    parser.registerHandler("/SEQ/INT[17]", new IntegerHandler(this, "pagesPreread"));
    parser.registerHandler("/SEQ/INT[19]", new IntegerHandler(this, "pagesDirtied"));
    parser.registerHandler("/SEQ/INT[21]", new IntegerHandler(this, "pagesRedirtied"));
    parser.registerHandler("/SEQ/INT[23]", new IntegerHandler(this, "logRecordCount"));
    parser.registerHandler("/SEQ/INT[25]", new IntegerHandler(this, "logRecordBytes"));
    parser.parse(encoded);
  }


  /** Parse handler implementation for integer stats. */
  private static class IntegerHandler extends AbstractParseHandler<GetStatsControl>
  {

    /** name of this statistic. */
    private final String statName;


    /**
     * Creates a new integer handler.
     *
     * @param  control  to configure
     * @param  name  of the statistic
     */
    IntegerHandler(final GetStatsControl control, final String name)
    {
      super(control);
      statName = name;
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().getStatistics().put(statName, IntegerType.decode(encoded).intValue());
    }
  }


  /** Parse handler implementation for string stats. */
  private static class StringHandler extends AbstractParseHandler<GetStatsControl>
  {

    /** name of this statistic. */
    private final String statName;


    /**
     * Creates a new string handler.
     *
     * @param  control  to configure
     * @param  name  of the statistic
     */
    StringHandler(final GetStatsControl control, final String name)
    {
      super(control);
      statName = name;
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      // strings are terminated with 0x00(null), use trim to remove
      getObject().getStatistics().put(statName, OctetStringType.decode(encoded).trim());
    }
  }
}
