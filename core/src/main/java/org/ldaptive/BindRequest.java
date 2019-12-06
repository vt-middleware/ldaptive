/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * LDAP bind request defined as:
 *
 * <pre>
   BindRequest ::= [APPLICATION 0] SEQUENCE {
     version                 INTEGER (1 ..  127),
     name                    LDAPDN,
     authentication          AuthenticationChoice }

   AuthenticationChoice ::= CHOICE {
     simple                  [0] OCTET STRING,
                             -- 1 and 2 reserved
     sasl                    [3] SaslCredentials,
     ...  }

   SaslCredentials ::= SEQUENCE {
     mechanism               LDAPString,
     credentials             OCTET STRING OPTIONAL }
 * </pre>
 *
 * @author  Middleware Services
 */
// CheckStyle:InterfaceIsType OFF
public interface BindRequest extends Request
{

  /** BER protocol number. */
  int PROTOCOL_OP = 0;

  /** bind protocol version. */
  int VERSION = 3;
}
// CheckStyle:InterfaceIsType ON
