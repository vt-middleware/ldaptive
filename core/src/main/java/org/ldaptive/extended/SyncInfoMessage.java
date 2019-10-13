/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.ParseHandler;
import org.ldaptive.asn1.UuidType;

/**
 * Intermediate response message for LDAP content synchronization. See RFC 4533. Message is defined as:
 *
 * <pre>
   IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
     responseName     [0] LDAPOID OPTIONAL,
     responseValue    [1] OCTET STRING OPTIONAL }


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
public class SyncInfoMessage extends IntermediateResponse
{

  /** OID of this response. */
  public static final String OID = "1.3.6.1.4.1.4203.1.9.1.4";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10321;

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


  /**
   * Default constructor.
   */
  protected SyncInfoMessage() {}


  /**
   * Creates a new sync info message.
   *
   * @param  buffer  to decode
   */
  public SyncInfoMessage(final DERBuffer buffer)
  {
    super(buffer);
  }


  /**
   * Returns the parse handler for the response value.
   *
   * @return  parse handler
   */
  @Override
  protected ParseHandler getResponseValueParseHandler()
  {
    return (parser, encoded) -> {
      final DERParser p = new DERParser();
      p.registerHandler("/CTX(0)", new NewCookieHandler(SyncInfoMessage.this));
      p.registerHandler("/CTX(1)", new RefreshDeleteHandler(SyncInfoMessage.this));
      p.registerHandler("/CTX(1)/OCTSTR[0]", new RefreshDeleteCookieHandler(SyncInfoMessage.this));
      p.registerHandler("/CTX(1)/BOOL[1]", new RefreshDeleteDoneHandler(SyncInfoMessage.this));
      p.registerHandler("/CTX(2)", new RefreshPresentHandler(SyncInfoMessage.this));
      p.registerHandler("/CTX(2)/OCTSTR[0]", new RefreshPresentCookieHandler(SyncInfoMessage.this));
      p.registerHandler("/CTX(2)/BOOL[1]", new RefreshPresentDoneHandler(SyncInfoMessage.this));
      p.registerHandler("/CTX(3)", new SyncIdSetHandler(SyncInfoMessage.this));
      p.registerHandler("/CTX(3)/OCTSTR[0]", new SyncIdSetCookieHandler(SyncInfoMessage.this));
      p.registerHandler("/CTX(3)/BOOL[1]", new SyncIdSetDeletesHandler(SyncInfoMessage.this));
      p.registerHandler("/CTX(3)/SET/OCTSTR", new SyncIdSetUuidsHandler(SyncInfoMessage.this));
      p.parse(encoded);
    };
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
   * Adds the supplied UUIDs to this message.
   *
   * @param  uuids  to add
   */
  public void addEntryUuids(final UUID... uuids)
  {
    for (UUID id : uuids) {
      entryUuids.add(id);
    }
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
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SyncInfoMessage && super.equals(o)) {
      final SyncInfoMessage v = (SyncInfoMessage) o;
      return LdapUtils.areEqual(messageType, v.messageType) &&
        LdapUtils.areEqual(cookie, v.cookie) &&
        LdapUtils.areEqual(refreshDone, v.refreshDone) &&
        LdapUtils.areEqual(refreshDeletes, v.refreshDeletes) &&
        LdapUtils.areEqual(entryUuids, v.entryUuids);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getMessageID(),
        getControls(),
        getResponseName(),
        messageType,
        cookie,
        refreshDone,
        refreshDeletes,
        entryUuids);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      super.toString()).append(", ")
      .append("messageType=").append(messageType).append(", ")
      .append("cookie=").append(LdapUtils.base64Encode(cookie)).append(", ")
      .append("refreshDone=").append(refreshDone).append(", ")
      .append("refreshDeletes=").append(refreshDeletes).append(", ")
      .append("entryUuids=").append(entryUuids).toString();
  }


  /** Parse handler implementation for new cookie. */
  private static class NewCookieHandler extends AbstractParseHandler<SyncInfoMessage>
  {


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
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setMessageType(Type.NEW_COOKIE);

      final byte[] cookie = encoded.getRemainingBytes();
      if (cookie != null && cookie.length > 0) {
        getObject().setCookie(cookie);
      }
    }
  }


  /** Parse handler implementation for refresh delete. */
  private static class RefreshDeleteHandler extends AbstractParseHandler<SyncInfoMessage>
  {


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
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setMessageType(Type.REFRESH_DELETE);
    }
  }


  /** Parse handler implementation for refresh delete cookie. */
  private static class RefreshDeleteCookieHandler extends AbstractParseHandler<SyncInfoMessage>
  {


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
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final byte[] cookie = encoded.getRemainingBytes();
      if (cookie != null && cookie.length > 0) {
        getObject().setCookie(cookie);
      }
    }
  }


  /** Parse handler implementation for refresh delete done. */
  private static class RefreshDeleteDoneHandler extends AbstractParseHandler<SyncInfoMessage>
  {


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
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setRefreshDone(BooleanType.decode(encoded));
    }
  }


  /** Parse handler implementation for refresh present. */
  private static class RefreshPresentHandler extends AbstractParseHandler<SyncInfoMessage>
  {


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
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setMessageType(Type.REFRESH_PRESENT);
    }
  }


  /** Parse handler implementation for refresh present cookie. */
  private static class RefreshPresentCookieHandler extends AbstractParseHandler<SyncInfoMessage>
  {


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
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final byte[] cookie = encoded.getRemainingBytes();
      if (cookie != null && cookie.length > 0) {
        getObject().setCookie(cookie);
      }
    }
  }


  /** Parse handler implementation for refresh present done. */
  private static class RefreshPresentDoneHandler extends AbstractParseHandler<SyncInfoMessage>
  {


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
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setRefreshDone(BooleanType.decode(encoded));
    }
  }


  /** Parse handler implementation for sync id set. */
  private static class SyncIdSetHandler extends AbstractParseHandler<SyncInfoMessage>
  {


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
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setMessageType(Type.SYNC_ID_SET);
    }
  }


  /** Parse handler implementation for sync id set cookie. */
  private static class SyncIdSetCookieHandler extends AbstractParseHandler<SyncInfoMessage>
  {


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
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final byte[] cookie = encoded.getRemainingBytes();
      if (cookie != null && cookie.length > 0) {
        getObject().setCookie(cookie);
      }
    }
  }


  /** Parse handler implementation for sync id set deletes. */
  private static class SyncIdSetDeletesHandler extends AbstractParseHandler<SyncInfoMessage>
  {


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
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setRefreshDeletes(BooleanType.decode(encoded));
    }
  }


  /** Parse handler implementation for sync id set uuids. */
  private static class SyncIdSetUuidsHandler extends AbstractParseHandler<SyncInfoMessage>
  {


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
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().getEntryUuids().add(UuidType.decode(encoded));
    }
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  public static class Builder extends IntermediateResponse.Builder
  {


    protected Builder()
    {
      super(new SyncInfoMessage());
    }


    protected Builder(final SyncInfoMessage m)
    {
      super(m);
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder type(final Type t)
    {
      ((SyncInfoMessage) object).messageType = t;
      return this;
    }


    public Builder cookie(final byte[] b)
    {
      ((SyncInfoMessage) object).cookie = b;
      return this;
    }


    public Builder refreshDone(final boolean b)
    {
      ((SyncInfoMessage) object).refreshDone = b;
      return this;
    }


    public Builder refreshDeletes(final boolean b)
    {
      ((SyncInfoMessage) object).refreshDeletes = b;
      return this;
    }


    public Builder uuids(final UUID... uuids)
    {
      ((SyncInfoMessage) object).addEntryUuids(uuids);
      return this;
    }
  }
  // CheckStyle:ON
}
