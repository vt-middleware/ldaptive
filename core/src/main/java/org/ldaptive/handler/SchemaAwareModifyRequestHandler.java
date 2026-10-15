/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.BiConsumer;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapUtils;
import org.ldaptive.ModifyRequest;
import org.ldaptive.schema.Schema;

/**
 * Modify request handler that provides the ability to inspect and/or mutate LDAP attributes based on a schema.
 *
 * @author  Middleware Services
 */
public class SchemaAwareModifyRequestHandler implements RequestHandler<ModifyRequest>
{

  /** Schema with attribute information. */
  private final Schema schema;

  /** Ldap attribute consumer. */
  private final BiConsumer<Schema, LdapAttribute> attributeConsumer;


  /**
   * Creates a new schema aware modify request handler.
   *
   * @param  s  schema
   * @param  consumer  to process attributes supplied in a modify request
   */
  public SchemaAwareModifyRequestHandler(final Schema s, final BiConsumer<Schema, LdapAttribute> consumer)
  {
    schema = LdapUtils.assertNotNullArg(s, "Schema cannot be null");
    attributeConsumer = LdapUtils.assertNotNullArg(consumer, "Ldap attribute consumer cannot be null");
  }


  @Override
  public void accept(final ModifyRequest request)
  {
    for (AttributeModification mod : request.getModifications()) {
      attributeConsumer.accept(schema, mod.getAttribute());
    }
  }
}
