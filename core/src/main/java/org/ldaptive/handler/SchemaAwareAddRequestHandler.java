/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.BiConsumer;
import org.ldaptive.AddRequest;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapUtils;
import org.ldaptive.schema.Schema;

/**
 * Add request handler that provides the ability to inspect and/or mutate LDAP attributes based on a schema.
 *
 * @author  Middleware Services
 */
public class SchemaAwareAddRequestHandler implements RequestHandler<AddRequest>
{

  /** Schema with attribute information. */
  private final Schema schema;

  /** Ldap attribute consumer. */
  private final BiConsumer<Schema, LdapAttribute> attributeConsumer;


  /**
   * Creates a new schema aware add request handler.
   *
   * @param  s  schema
   * @param  consumer  to process attributes supplied in an add request
   */
  public SchemaAwareAddRequestHandler(final Schema s, final BiConsumer<Schema, LdapAttribute> consumer)
  {
    schema = LdapUtils.assertNotNullArg(s, "Schema cannot be null");
    attributeConsumer = LdapUtils.assertNotNullArg(consumer, "Ldap attribute consumer cannot be null");
  }


  @Override
  public void accept(final AddRequest request)
  {
    for (LdapAttribute attr : request.getAttributes()) {
      attributeConsumer.accept(schema, attr);
    }
  }
}
