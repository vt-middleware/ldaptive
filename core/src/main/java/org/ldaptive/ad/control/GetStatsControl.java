/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
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

  /** DER path to thread count. */
  private static final DERPath THREAD_COUNT_PATH = new DERPath("/SEQ/INT[1]");

  /** DER path to call time. */
  private static final DERPath CALL_TIME_PATH = new DERPath("/SEQ/INT[3]");

  /** DER path to entries returned. */
  private static final DERPath ENTRIES_RETURNED_PATH = new DERPath("/SEQ/INT[5]");

  /** DER path to entries visited. */
  private static final DERPath ENTRIES_VISITED_PATH = new DERPath("/SEQ/INT[7]");

  /** DER path to filter. */
  private static final DERPath FILTER_PATH = new DERPath("/SEQ/OCTSTR[9]");

  /** DER path to index. */
  private static final DERPath INDEX_PATH = new DERPath("/SEQ/OCTSTR[11]");

  /** DER path to pages referenced. */
  private static final DERPath PAGES_REFERENCED_PATH = new DERPath("/SEQ/INT[13]");

  /** DER path to pages read. */
  private static final DERPath PAGES_READ_PATH = new DERPath("/SEQ/INT[15]");

  /** DER path to pages preread. */
  private static final DERPath PAGES_PREREAD_PATH = new DERPath("/SEQ/INT[17]");

  /** DER path to pages dirtied. */
  private static final DERPath PAGES_DIRTIED_PATH = new DERPath("/SEQ/INT[19]");

  /** DER path to pages redirtied. */
  private static final DERPath PAGES_REDIRTIED_PATH = new DERPath("/SEQ/INT[21]");

  /** DER path to log record count. */
  private static final DERPath LOG_RECORD_COUNT_PATH = new DERPath("/SEQ/INT[23]");

  /** DER path to log record bytes. */
  private static final DERPath LOG_RECORD_BYTES_PATH = new DERPath("/SEQ/INT[25]");

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
    parser.registerHandler(THREAD_COUNT_PATH, new IntegerHandler(this, "threadCount"));
    parser.registerHandler(CALL_TIME_PATH, new IntegerHandler(this, "callTime"));
    parser.registerHandler(ENTRIES_RETURNED_PATH, new IntegerHandler(this, "entriesReturned"));
    parser.registerHandler(ENTRIES_VISITED_PATH, new IntegerHandler(this, "entriesVisited"));
    parser.registerHandler(FILTER_PATH, new StringHandler(this, "filter"));
    parser.registerHandler(INDEX_PATH, new StringHandler(this, "index"));
    parser.registerHandler(PAGES_REFERENCED_PATH, new IntegerHandler(this, "pagesReferenced"));
    parser.registerHandler(PAGES_READ_PATH, new IntegerHandler(this, "pagesRead"));
    parser.registerHandler(PAGES_PREREAD_PATH, new IntegerHandler(this, "pagesPreread"));
    parser.registerHandler(PAGES_DIRTIED_PATH, new IntegerHandler(this, "pagesDirtied"));
    parser.registerHandler(PAGES_REDIRTIED_PATH, new IntegerHandler(this, "pagesRedirtied"));
    parser.registerHandler(LOG_RECORD_COUNT_PATH, new IntegerHandler(this, "logRecordCount"));
    parser.registerHandler(LOG_RECORD_BYTES_PATH, new IntegerHandler(this, "logRecordBytes"));
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
