/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.intermediate;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UuidType;
import org.ldaptive.control.ResponseControl;

/**
 * Intermediate response message for ldap content synchronization. See RFC 4533. Message is defined as:
 *
 * <pre>
     syncInfoValue ::= CHOICE {
         newcookie      [0] syncCookie,
         refreshDelete  [1] SEQUENCE {
             cookie         syncCookie OPTIONAL,
             refreshDone    BOOLEAN DEFAULT TRUE
         },
         refreshPresent [2] SEQUENCE {
             cookie         syncCookie OPTIONAL,
             refreshDone    BOOLEAN DEFAULT TRUE
         },
         syncIdSet      [3] SEQUENCE {
             cookie         syncCookie OPTIONAL,
             refreshDeletes BOOLEAN DEFAULT FALSE,
             syncUUIDs      SET OF syncUUID
         }
     }
 * </pre>
 *
 * @author  Middleware Services
 */
public class SyncInfoMessage extends AbstractIntermediateResponse
{


  /** OID of this response. */
  public static final String OID = "1.3.6.1.4.1.4203.1.9.1.4";

  /** hash value seed. */
  private static final int HASH_CODE_SEED = 761;

  /** Types of request modes. */
  public enum Type {

    /** new cookie. */
    NEW_COOKIE,

    /** refresh delete. */
    REFRESH_DELETE,

    /** refresh present. */
    REFRESH_PRESENT,

    /** sync id set. */
    SYNC_ID_SET
  }

  /** message type. */
  private Type messageType;

  /** server generated cookie. */
  private byte[] cookie;

  /** refresh done. */
  private boolean refreshDone = true;

  /** refresh deletes. */
  private boolean refreshDeletes;

  /** entry uuids. */
  private Set<UUID> entryUuids = new HashSet<>();


  /** Default constructor. */
  public SyncInfoMessage()
  {
    super(OID, null, -1);
  }


  /**
   * Creates a new sync info message.
   *
   * @param  responseControls  for this message
   * @param  id  message id
   */
  public SyncInfoMessage(final ResponseControl[] responseControls, final int id)
  {
    super(OID, responseControls, id);
  }


  /**
   * Returns the message type.
   *
   * @return  message type
   */
  public Type getMessageType()
  {
    return messageType;
  }


  /**
   * Sets the message type.
   *
   * @param  type  message type
   */
  public void setMessageType(final Type type)
  {
    messageType = type;
  }


  /**
   * Returns the sync request cookie.
   *
   * @return  sync request cookie
   */
  public byte[] getCookie()
  {
    return cookie;
  }


  /**
   * Sets the sync request cookie.
   *
   * @param  value  sync request cookie
   */
  public void setCookie(final byte[] value)
  {
    cookie = value;
  }


  /**
   * Returns whether refreshes are done.
   *
   * @return  refresh done
   */
  public boolean getRefreshDone()
  {
    return refreshDone;
  }


  /**
   * Sets whether refreshes are done.
   *
   * @param  b  refresh done
   */
  public void setRefreshDone(final boolean b)
  {
    refreshDone = b;
  }


  /**
   * Returns whether to refresh deletes.
   *
   * @return  whether to refresh deletes
   */
  public boolean getRefreshDeletes()
  {
    return refreshDeletes;
  }


  /**
   * Sets whether to refresh deletes.
   *
   * @param  b  whether to refresh deletes
   */
  public void setRefreshDeletes(final boolean b)
  {
    refreshDeletes = b;
  }


  /**
   * Returns the entry uuids.
   *
   * @return  entry uuids
   */
  public Set<UUID> getEntryUuids()
  {
    return entryUuids;
  }


  /**
   * Sets the entry uuids.
   *
   * @param  uuids  entry uuids
   */
  public void setEntryUuids(final Set<UUID> uuids)
  {
    entryUuids = uuids;
  }


  @Override
  public void decode(final byte[] berValue)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(NewCookieHandler.PATH, new NewCookieHandler(this));
    parser.registerHandler(RefreshDeleteHandler.PATH, new RefreshDeleteHandler(this));
    parser.registerHandler(RefreshDeleteCookieHandler.PATH, new RefreshDeleteCookieHandler(this));
    parser.registerHandler(RefreshDeleteDoneHandler.PATH, new RefreshDeleteDoneHandler(this));
    parser.registerHandler(RefreshPresentHandler.PATH, new RefreshPresentHandler(this));
    parser.registerHandler(RefreshPresentCookieHandler.PATH, new RefreshPresentCookieHandler(this));
    parser.registerHandler(RefreshPresentDoneHandler.PATH, new RefreshPresentDoneHandler(this));
    parser.registerHandler(SyncIdSetHandler.PATH, new SyncIdSetHandler(this));
    parser.registerHandler(SyncIdSetCookieHandler.PATH, new SyncIdSetCookieHandler(this));
    parser.registerHandler(SyncIdSetDeletesHandler.PATH, new SyncIdSetDeletesHandler(this));
    parser.registerHandler(SyncIdSetUuidsHandler.PATH, new SyncIdSetUuidsHandler(this));
    parser.parse(ByteBuffer.wrap(berValue));
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SyncInfoMessage) {
      final SyncInfoMessage v = (SyncInfoMessage) o;
      return LdapUtils.areEqual(getOID(), v.getOID()) &&
             LdapUtils.areEqual(messageType, v.messageType) &&
             LdapUtils.areEqual(cookie, v.cookie) &&
             LdapUtils.areEqual(refreshDone, v.refreshDone) &&
             LdapUtils.areEqual(refreshDeletes, v.refreshDeletes) &&
             LdapUtils.areEqual(entryUuids, v.entryUuids) &&
             LdapUtils.areEqual(getControls(), v.getControls()) &&
             LdapUtils.areEqual(getMessageId(), v.getMessageId());
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getOID(),
        messageType,
        cookie,
        refreshDone,
        refreshDeletes,
        entryUuids,
        getControls(),
        getMessageId());
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::messageType=%s, cookie=%s, refreshDone=%s, " +
        "refreshDeletes=%s, entryUuids=%s, responseControls=%s, messageId=%s]",
        getClass().getName(),
        hashCode(),
        messageType,
        LdapUtils.base64Encode(cookie),
        refreshDone,
        refreshDeletes,
        entryUuids,
        Arrays.toString(getControls()),
        getMessageId());
  }


  /** Parse handler implementation for new cookie. */
  private static class NewCookieHandler extends AbstractParseHandler<SyncInfoMessage>
  {

    /** DER path to value. */
    public static final DERPath PATH = new DERPath("/CTX(0)");


    /**
     * Creates a new cookie handler.
     *
     * @param  message  to configure
     */
    NewCookieHandler(final SyncInfoMessage message)
    {
      super(message);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setMessageType(Type.NEW_COOKIE);

      final byte[] cookie = OctetStringType.readBuffer(encoded);
      if (cookie != null && cookie.length > 0) {
        getObject().setCookie(cookie);
      }
    }
  }


  /** Parse handler implementation for refresh delete. */
  private static class RefreshDeleteHandler extends AbstractParseHandler<SyncInfoMessage>
  {

    /** DER path to value. */
    public static final DERPath PATH = new DERPath("/CTX(1)");


    /**
     * Creates a refresh delete handler.
     *
     * @param  message  to configure
     */
    RefreshDeleteHandler(final SyncInfoMessage message)
    {
      super(message);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setMessageType(Type.REFRESH_DELETE);
    }
  }


  /** Parse handler implementation for refresh delete cookie. */
  private static class RefreshDeleteCookieHandler extends AbstractParseHandler<SyncInfoMessage>
  {

    /** DER path to value. */
    public static final DERPath PATH = new DERPath("/CTX(1)/OCTSTR");


    /**
     * Creates a refresh delete cookie handler.
     *
     * @param  message  to configure
     */
    RefreshDeleteCookieHandler(final SyncInfoMessage message)
    {
      super(message);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      final byte[] cookie = OctetStringType.readBuffer(encoded);
      if (cookie != null && cookie.length > 0) {
        getObject().setCookie(cookie);
      }
    }
  }


  /** Parse handler implementation for refresh delete done. */
  private static class RefreshDeleteDoneHandler extends AbstractParseHandler<SyncInfoMessage>
  {

    /** DER path to value. */
    public static final DERPath PATH = new DERPath("/CTX(1)/BOOL");


    /**
     * Creates a refresh delete done handler.
     *
     * @param  message  to configure
     */
    RefreshDeleteDoneHandler(final SyncInfoMessage message)
    {
      super(message);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setRefreshDone(BooleanType.decode(encoded));
    }
  }


  /** Parse handler implementation for refresh present. */
  private static class RefreshPresentHandler extends AbstractParseHandler<SyncInfoMessage>
  {

    /** DER path to value. */
    public static final DERPath PATH = new DERPath("/CTX(2)");


    /**
     * Creates a refresh present handler.
     *
     * @param  message  to configure
     */
    RefreshPresentHandler(final SyncInfoMessage message)
    {
      super(message);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setMessageType(Type.REFRESH_PRESENT);
    }
  }


  /** Parse handler implementation for refresh present cookie. */
  private static class RefreshPresentCookieHandler extends AbstractParseHandler<SyncInfoMessage>
  {

    /** DER path to value. */
    public static final DERPath PATH = new DERPath("/CTX(2)/OCTSTR");


    /**
     * Creates a refresh present cookie handler.
     *
     * @param  message  to configure
     */
    RefreshPresentCookieHandler(final SyncInfoMessage message)
    {
      super(message);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      final byte[] cookie = OctetStringType.readBuffer(encoded);
      if (cookie != null && cookie.length > 0) {
        getObject().setCookie(cookie);
      }
    }
  }


  /** Parse handler implementation for refresh present done. */
  private static class RefreshPresentDoneHandler extends AbstractParseHandler<SyncInfoMessage>
  {

    /** DER path to value. */
    public static final DERPath PATH = new DERPath("/CTX(2)/BOOL");


    /**
     * Creates a refresh present done handler.
     *
     * @param  message  to configure
     */
    RefreshPresentDoneHandler(final SyncInfoMessage message)
    {
      super(message);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setRefreshDone(BooleanType.decode(encoded));
    }
  }


  /** Parse handler implementation for sync id set. */
  private static class SyncIdSetHandler extends AbstractParseHandler<SyncInfoMessage>
  {

    /** DER path to value. */
    public static final DERPath PATH = new DERPath("/CTX(3)");


    /**
     * Creates a sync id set handler.
     *
     * @param  message  to configure
     */
    SyncIdSetHandler(final SyncInfoMessage message)
    {
      super(message);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setMessageType(Type.SYNC_ID_SET);
    }
  }


  /** Parse handler implementation for sync id set cookie. */
  private static class SyncIdSetCookieHandler extends AbstractParseHandler<SyncInfoMessage>
  {

    /** DER path to value. */
    public static final DERPath PATH = new DERPath("/CTX(3)/OCTSTR");


    /**
     * Creates a sync id set cookie handler.
     *
     * @param  message  to configure
     */
    SyncIdSetCookieHandler(final SyncInfoMessage message)
    {
      super(message);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      final byte[] cookie = OctetStringType.readBuffer(encoded);
      if (cookie != null && cookie.length > 0) {
        getObject().setCookie(cookie);
      }
    }
  }


  /** Parse handler implementation for sync id set deletes. */
  private static class SyncIdSetDeletesHandler extends AbstractParseHandler<SyncInfoMessage>
  {

    /** DER path to value. */
    public static final DERPath PATH = new DERPath("/CTX(3)/BOOL");


    /**
     * Creates a sync id set deletes handler.
     *
     * @param  message  to configure
     */
    SyncIdSetDeletesHandler(final SyncInfoMessage message)
    {
      super(message);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().setRefreshDeletes(BooleanType.decode(encoded));
    }
  }


  /** Parse handler implementation for sync id set uuids. */
  private static class SyncIdSetUuidsHandler extends AbstractParseHandler<SyncInfoMessage>
  {

    /** DER path to value. */
    public static final DERPath PATH = new DERPath("/CTX(3)/SET/OCTSTR");


    /**
     * Creates a sync id set uuids handler.
     *
     * @param  message  to configure
     */
    SyncIdSetUuidsHandler(final SyncInfoMessage message)
    {
      super(message);
    }


    @Override
    public void handle(final DERParser parser, final ByteBuffer encoded)
    {
      getObject().getEntryUuids().add(UuidType.decode(encoded));
    }
  }
}
