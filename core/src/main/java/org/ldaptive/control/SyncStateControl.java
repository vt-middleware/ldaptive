/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.util.UUID;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.UuidType;

/**
 * Response control for ldap content synchronization. See RFC 4533. Control is defined as:
 *
 * <pre>
     syncStateValue ::= SEQUENCE {
         state ENUMERATED {
             present (0),
             add (1),
             modify (2),
             delete (3)
         },
         entryUUID syncUUID,
         cookie    syncCookie OPTIONAL
     }
 * </pre>
 *
 * @author  Middleware Services
 */
public class SyncStateControl extends AbstractControl implements ResponseControl
{

  /** OID of this control. */
  public static final String OID = "1.3.6.1.4.1.4203.1.9.1.2";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 751;

  /** Types of states. */
  public enum State {

    /** present. */
    PRESENT(0),

    /** add. */
    ADD(1),

    /** modify. */
    MODIFY(2),

    /** delete. */
    DELETE(3);

    /** underlying value. */
    private final int value;


    /**
     * Creates a new mode.
     *
     * @param  i  value
     */
    State(final int i)
    {
      value = i;
    }


    /**
     * Returns the value.
     *
     * @return  enum value
     */
    public int value()
    {
      return value;
    }


    /**
     * Returns the state for the supplied integer constant.
     *
     * @param  i  to find state for
     *
     * @return  state
     */
    public static State valueOf(final int i)
    {
      for (State s : State.values()) {
        if (s.value() == i) {
          return s;
        }
      }
      return null;
    }
  }

  /** sync state. */
  private State syncState;

  /** sync UUID. */
  private UUID entryUuid;

  /** server generated cookie. */
  private byte[] cookie;


  /** Default constructor. */
  public SyncStateControl()
  {
    super(OID);
  }


  /**
   * Creates a new sync state control.
   *
   * @param  critical  whether this control is critical
   */
  public SyncStateControl(final boolean critical)
  {
    super(OID, critical);
  }


  /**
   * Creates a new sync state control.
   *
   * @param  state  sync state
   */
  public SyncStateControl(final State state)
  {
    super(OID);
    setSyncState(state);
  }


  /**
   * Creates a new sync state control.
   *
   * @param  state  sync state
   * @param  critical  whether this control is critical
   */
  public SyncStateControl(final State state, final boolean critical)
  {
    super(OID, critical);
    setSyncState(state);
  }


  /**
   * Creates a new sync state control.
   *
   * @param  state  sync state
   * @param  uuid  sync entry uuid
   * @param  critical  whether this control is critical
   */
  public SyncStateControl(final State state, final UUID uuid, final boolean critical)
  {
    super(OID, critical);
    setSyncState(state);
    setEntryUuid(uuid);
  }


  /**
   * Creates a new sync state control.
   *
   * @param  state  sync state
   * @param  uuid  sync entry uuid
   * @param  value  sync state cookie
   * @param  critical  whether this control is critical
   */
  public SyncStateControl(final State state, final UUID uuid, final byte[] value, final boolean critical)
  {
    super(OID, critical);
    setSyncState(state);
    setEntryUuid(uuid);
    setCookie(value);
  }


  /**
   * Returns the sync state.
   *
   * @return  sync state
   */
  public State getSyncState()
  {
    return syncState;
  }


  /**
   * Sets the sync state.
   *
   * @param  state  sync state
   */
  public void setSyncState(final State state)
  {
    syncState = state;
  }


  /**
   * Returns the entry uuid.
   *
   * @return  entry uuid
   */
  public UUID getEntryUuid()
  {
    return entryUuid;
  }


  /**
   * Sets the entry uuid.
   *
   * @param  uuid  entry uuid
   */
  public void setEntryUuid(final UUID uuid)
  {
    entryUuid = uuid;
  }


  /**
   * Returns the sync state cookie.
   *
   * @return  sync state cookie
   */
  public byte[] getCookie()
  {
    return cookie;
  }


  /**
   * Sets the sync state cookie.
   *
   * @param  value  sync state cookie
   */
  public void setCookie(final byte[] value)
  {
    cookie = value;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SyncStateControl && super.equals(o)) {
      final SyncStateControl v = (SyncStateControl) o;
      return LdapUtils.areEqual(syncState, v.syncState) &&
             LdapUtils.areEqual(entryUuid, v.entryUuid) &&
             LdapUtils.areEqual(cookie, v.cookie);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality(), syncState, entryUuid, cookie);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "criticality=" + getCriticality() + ", " +
      "syncState=" + syncState + ", " +
      "entryUuid=" + entryUuid + ", " +
      "cookie=" + LdapUtils.base64Encode(cookie) + "]";
  }


  @Override
  public void decode(final DERBuffer encoded)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(StateHandler.PATH, new StateHandler(this));
    parser.registerHandler(EntryUuidHandler.PATH, new EntryUuidHandler(this));
    parser.registerHandler(CookieHandler.PATH, new CookieHandler(this));
    parser.parse(encoded);
  }


  /** Parse handler implementation for the sync state. */
  private static class StateHandler extends AbstractParseHandler<SyncStateControl>
  {

    /** DER path to the state. */
    public static final DERPath PATH = new DERPath("/SEQ/ENUM[0]");


    /**
     * Creates a new state handler.
     *
     * @param  control  to configure
     */
    StateHandler(final SyncStateControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final int stateValue = IntegerType.decode(encoded).intValue();
      final State s = State.valueOf(stateValue);
      if (s == null) {
        throw new IllegalArgumentException("Unknown state value " + stateValue);
      }
      getMessage().setSyncState(s);
    }
  }


  /** Parse handler implementation for the entry uuid. */
  private static class EntryUuidHandler extends AbstractParseHandler<SyncStateControl>
  {

    /** DER path to the uuid. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR[1]");


    /**
     * Creates a new entry uuid handler.
     *
     * @param  control  to configure
     */
    EntryUuidHandler(final SyncStateControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      if (encoded.hasRemaining()) {
        getMessage().setEntryUuid(UuidType.decode(encoded));
      }
    }
  }


  /** Parse handler implementation for the cookie. */
  private static class CookieHandler extends AbstractParseHandler<SyncStateControl>
  {

    /** DER path to cookie value. */
    public static final DERPath PATH = new DERPath("/SEQ/OCTSTR[2]");


    /**
     * Creates a new cookie handler.
     *
     * @param  control  to configure
     */
    CookieHandler(final SyncStateControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final byte[] cookie = encoded.getRemainingBytes();
      if (cookie != null && cookie.length > 0) {
        getMessage().setCookie(cookie);
      }
    }
  }
}
