/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Enum to define ldap result codes.
 *
 * @author  Middleware Services
 */
public enum ResultCode {

  /** success. */
  SUCCESS(0),

  /** operations error. */
  OPERATIONS_ERROR(1),

  /** protocol error. */
  PROTOCOL_ERROR(2),

  /** time limit exceeded. */
  TIME_LIMIT_EXCEEDED(3),

  /** size limit exceeded. */
  SIZE_LIMIT_EXCEEDED(4),

  /** compare false. */
  COMPARE_FALSE(5),

  /** compare true. */
  COMPARE_TRUE(6),

  /** authentication method not supported. */
  AUTH_METHOD_NOT_SUPPORTED(7),

  /** strong authentication required. */
  STRONG_AUTH_REQUIRED(8),

  /** partial results. */
  PARTIAL_RESULTS(9),

  /** referral. */
  REFERRAL(10),

  /** admin limit exceeded. */
  ADMIN_LIMIT_EXCEEDED(11),

  /** unavailable critical extension. */
  UNAVAILABLE_CRITICAL_EXTENSION(12),

  /** confidentiality required. */
  CONFIDENTIALITY_REQUIRED(13),

  /** sasl bind in progress. */
  SASL_BIND_IN_PROGRESS(14),

  /** no such attribute. */
  NO_SUCH_ATTRIBUTE(16),

  /** undefined attribute type. */
  UNDEFINED_ATTRIBUTE_TYPE(17),

  /** inappropriate matching. */
  INAPPROPRIATE_MATCHING(18),

  /** constraint violation. */
  CONSTRAINT_VIOLATION(19),

  /** attribute or value exists. */
  ATTRIBUTE_OR_VALUE_EXISTS(20),

  /** invalid attribute syntax. */
  INVALID_ATTRIBUTE_SYNTAX(21),

  /** no such object. */
  NO_SUCH_OBJECT(32),

  /** alias problem. */
  ALIAS_PROBLEM(33),

  /** invalid dn syntax. */
  INVALID_DN_SYNTAX(34),

  /** is leaf. */
  IS_LEAF(35),

  /** alias dereferencing problem. */
  ALIAS_DEREFERENCING_PROBLEM(36),

  /** inappropriate authentication. */
  INAPPROPRIATE_AUTHENTICATION(48),

  /** invalid credentials. */
  INVALID_CREDENTIALS(49),

  /** insufficient access rights. */
  INSUFFICIENT_ACCESS_RIGHTS(50),

  /** busy. */
  BUSY(51),

  /** unavailable. */
  UNAVAILABLE(52),

  /** unwilling to perform. */
  UNWILLING_TO_PERFORM(53),

  /** loop detect. */
  LOOP_DETECT(54),

  /** sort control missing, See draft-ietf-ldapext-ldapv3-vlv. */
  SORT_CONTROL_MISSING(60),

  /** offset range error, See draft-ietf-ldapext-ldapv3-vlv. */
  OFFSET_RANGE_ERROR(61),

  /** naming violation. */
  NAMING_VIOLATION(64),

  /** object class violation. */
  OBJECT_CLASS_VIOLATION(65),

  /** not allowed on nonleaf. */
  NOT_ALLOWED_ON_NONLEAF(66),

  /** not allowed on rdn. */
  NOT_ALLOWED_ON_RDN(67),

  /** entry already exists. */
  ENTRY_ALREADY_EXISTS(68),

  /** object class mods prohibited. */
  OBJECT_CLASS_MODS_PROHIBITED(69),

  /** affected multiple dsas. */
  AFFECTS_MULTIPLE_DSAS(71),

  /** virtual list view error, See draft-ietf-ldapext-ldapv3-vlv. */
  VIRTUAL_LIST_VIEW_ERROR(76),

  /** other. */
  OTHER(80),

  /** server down. */
  SERVER_DOWN(81),

  /** local error. */
  LOCAL_ERROR(82),

  /** encoding error. */
  ENCODING_ERROR(83),

  /** decoding error. */
  DECODING_ERROR(84),

  /** ldap timeout. */
  LDAP_TIMEOUT(85),

  /** auth unknown. */
  AUTH_UNKNOWN(86),

  /** filter error. */
  FILTER_ERROR(87),

  /** user cancelled. */
  USER_CANCELLED(88),

  /** param error. */
  PARAM_ERROR(89),

  /** no memory. */
  NO_MEMORY(90),

  /** connect error. */
  CONNECT_ERROR(91),

  /** ldap not supported. */
  LDAP_NOT_SUPPORTED(92),

  /** control not found. */
  CONTROL_NOT_FOUND(93),

  /** no results returned. */
  NO_RESULTS_RETURNED(94),

  /** more results to return. */
  MORE_RESULTS_TO_RETURN(95),

  /** client loop. */
  CLIENT_LOOP(96),

  /** referral limit exceeded. */
  REFERRAL_LIMIT_EXCEEDED(97),

  /** invalid response. */
  INVALID_RESPONSE(100),

  /** ambiguous response. */
  AMBIGUOUS_RESPONSE(101),

  /** tls not supported. */
  TLS_NOT_SUPPORTED(112),

  /** operation canceled, See RFC 3909. */
  CANCELED(118),

  /** no such operation, See RFC 3909. */
  NO_SUCH_OPERATION(119),

  /** too late, See RFC 3909. */
  TOO_LATE(120),

  /** cannot cancel, See RFC 3909. */
  CANNOT_CANCEL(121),

  /** assertion failed, See RFC 4528. */
  ASSERTION_FAILED(122),

  /** authorization denied, See RFC 4370. */
  AUTHORIZATION_DENIED(123),

  /** e-syncRefreshRequired, See RFC 4533. */
  E_SYNC_REFRESH_REQUIRED(4096);

  /** underlying error code. */
  private final int code;


  /**
   * Creates a new result code.
   *
   * @param  i  error code
   */
  ResultCode(final int i)
  {
    code = i;
  }


  /**
   * Returns the result code value.
   *
   * @return  ldap result code
   */
  public int value()
  {
    return code;
  }


  /**
   * Returns the result code for the supplied integer constant.
   *
   * @param  code  to find result code for
   *
   * @return  result code
   */
  public static ResultCode valueOf(final int code)
  {
    for (ResultCode rc : ResultCode.values()) {
      if (rc.value() == code) {
        return rc;
      }
    }
    return null;
  }
}
