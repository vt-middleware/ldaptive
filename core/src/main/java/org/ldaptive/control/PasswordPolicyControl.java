/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.LoginException;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.AbstractParseHandler;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.DERPath;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.auth.AccountState;

/**
 * Request/response control for password policy. See http://tools.ietf.org/html/draft-behera-ldap-password-policy-11.
 * Control is defined as:
 *
 * <pre>
   PasswordPolicyResponseValue ::= SEQUENCE {
      warning [0] CHOICE {
      timeBeforeExpiration [0] INTEGER (0 .. maxInt),
      graceAuthNsRemaining [1] INTEGER (0 .. maxInt) } OPTIONAL,
      error   [1] ENUMERATED {
      passwordExpired             (0),
      accountLocked               (1),
      changeAfterReset            (2),
      passwordModNotAllowed       (3),
      mustSupplyOldPassword       (4),
      insufficientPasswordQuality (5),
      passwordTooShort            (6),
      passwordTooYoung            (7),
      passwordInHistory           (8),
      passwordTooLong             (9) } OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
public class PasswordPolicyControl extends AbstractControl implements RequestControl, ResponseControl
{

  /** OID of this control. */
  public static final String OID = "1.3.6.1.4.1.42.2.27.8.5.1";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 719;

  /** Enum for ppolicy errors. */
  public enum Error implements AccountState.Error {

    /** password expired. */
    PASSWORD_EXPIRED(0),

    /** account locked. */
    ACCOUNT_LOCKED(1),

    /** change after reset. */
    CHANGE_AFTER_RESET(2),

    /** password modification not allowed. */
    PASSWORD_MOD_NOT_ALLOWED(3),

    /** must supply old password. */
    MUST_SUPPLY_OLD_PASSWORD(4),

    /** insufficient password quality. */
    INSUFFICIENT_PASSWORD_QUALITY(5),

    /** password too short. */
    PASSWORD_TOO_SHORT(6),

    /** password too young. */
    PASSWORD_TOO_YOUNG(7),

    /** password in history. */
    PASSWORD_IN_HISTORY(8),

    /** password too long. */
    PASSWORD_TOO_LONG(9);

    /** underlying error code. */
    private final int code;


    /**
     * Creates a new error.
     *
     * @param  i  error code
     */
    Error(final int i)
    {
      code = i;
    }


    @Override
    public int getCode()
    {
      return code;
    }


    @Override
    public String getMessage()
    {
      return name();
    }


    @Override
    public void throwSecurityException()
      throws LoginException
    {
      switch (this) {

      case PASSWORD_EXPIRED:

      case CHANGE_AFTER_RESET:
        throw new CredentialExpiredException(name());

      case ACCOUNT_LOCKED:
        throw new AccountLockedException(name());

      case PASSWORD_MOD_NOT_ALLOWED:

      case MUST_SUPPLY_OLD_PASSWORD:
        throw new AccountException(name());

      case INSUFFICIENT_PASSWORD_QUALITY:

      case PASSWORD_TOO_SHORT:

      case PASSWORD_TOO_YOUNG:

      case PASSWORD_IN_HISTORY:

      case PASSWORD_TOO_LONG:
        throw new CredentialException(name());

      default:
        throw new IllegalStateException("Unknown password policy error: " + this);
      }
    }


    /**
     * Returns the error for the supplied integer constant.
     *
     * @param  code  to find error for
     *
     * @return  error
     */
    public static Error valueOf(final int code)
    {
      for (Error e : Error.values()) {
        if (e.getCode() == code) {
          return e;
        }
      }
      return null;
    }
  }

  /** Ppolicy warning. */
  private int timeBeforeExpiration = -1;

  /** Ppolicy warning. */
  private int graceAuthNsRemaining = -1;

  /** Ppolicy error. */
  private Error error;


  /** Default constructor. */
  public PasswordPolicyControl()
  {
    super(OID);
  }


  /**
   * Creates a new password policy control.
   *
   * @param  critical  whether this control is critical
   */
  public PasswordPolicyControl(final boolean critical)
  {
    super(OID, critical);
  }


  @Override
  public boolean hasValue()
  {
    return false;
  }


  /**
   * Returns the time before expiration in seconds.
   *
   * @return  time before expiration
   */
  public int getTimeBeforeExpiration()
  {
    return timeBeforeExpiration;
  }


  /**
   * Sets the time before expiration in seconds.
   *
   * @param  time  before expiration
   */
  public void setTimeBeforeExpiration(final int time)
  {
    timeBeforeExpiration = time;
  }


  /**
   * Returns the number of grace authentications remaining.
   *
   * @return  number of grace authentications remaining
   */
  public int getGraceAuthNsRemaining()
  {
    return graceAuthNsRemaining;
  }


  /**
   * Sets the number of grace authentications remaining.
   *
   * @param  count  number of grace authentications remaining
   */
  public void setGraceAuthNsRemaining(final int count)
  {
    graceAuthNsRemaining = count;
  }


  /**
   * Returns the password policy error.
   *
   * @return  password policy error
   */
  public Error getError()
  {
    return error;
  }


  /**
   * Sets the password policy error.
   *
   * @param  e  password policy error
   */
  public void setError(final Error e)
  {
    error = e;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof PasswordPolicyControl && super.equals(o)) {
      final PasswordPolicyControl v = (PasswordPolicyControl) o;
      return LdapUtils.areEqual(timeBeforeExpiration, v.timeBeforeExpiration) &&
             LdapUtils.areEqual(graceAuthNsRemaining, v.graceAuthNsRemaining) &&
             LdapUtils.areEqual(error, v.error);
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
        getCriticality(),
        timeBeforeExpiration,
        graceAuthNsRemaining,
        error);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "criticality=" + getCriticality() + ", " +
      "timeBeforeExpiration=" + timeBeforeExpiration + ", " +
      "graceAuthNsRemaining=" + graceAuthNsRemaining + ", " +
      "error=" + error + "]";
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
    parser.registerHandler(TimeBeforeExpirationHandler.PATH, new TimeBeforeExpirationHandler(this));
    parser.registerHandler(GraceAuthnsRemainingHandler.PATH, new GraceAuthnsRemainingHandler(this));
    parser.registerHandler(ErrorHandler.PATH, new ErrorHandler(this));
    parser.parse(encoded);
  }


  /** Parse handler implementation for the time before expiration. */
  private static class TimeBeforeExpirationHandler extends AbstractParseHandler<PasswordPolicyControl>
  {

    /** DER path to warning. */
    public static final DERPath PATH = new DERPath("/SEQ/CTX(0)/CTX(0)");


    /**
     * Creates a new time before expiration handler.
     *
     * @param  control  to configure
     */
    TimeBeforeExpirationHandler(final PasswordPolicyControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setTimeBeforeExpiration(IntegerType.decode(encoded).intValue());
    }
  }


  /** Parse handler implementation for the grace authns remaining. */
  private static class GraceAuthnsRemainingHandler extends AbstractParseHandler<PasswordPolicyControl>
  {

    /** DER path to warning. */
    public static final DERPath PATH = new DERPath("/SEQ/CTX(0)/CTX(1)");


    /**
     * Creates a new grace authns remaining handler.
     *
     * @param  control  to configure
     */
    GraceAuthnsRemainingHandler(final PasswordPolicyControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      getObject().setGraceAuthNsRemaining(IntegerType.decode(encoded).intValue());
    }
  }


  /** Parse handler implementation for the error. */
  private static class ErrorHandler extends AbstractParseHandler<PasswordPolicyControl>
  {

    /** DER path to error. */
    public static final DERPath PATH = new DERPath("/SEQ/CTX(1)");


    /**
     * Creates a new error handler.
     *
     * @param  control  to configure
     */
    ErrorHandler(final PasswordPolicyControl control)
    {
      super(control);
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      final int errValue = IntegerType.decode(encoded).intValue();
      final PasswordPolicyControl.Error e = PasswordPolicyControl.Error.valueOf(errValue);
      if (e == null) {
        throw new IllegalArgumentException("Unknown error code " + errValue);
      }
      getObject().setError(e);
    }
  }
}
