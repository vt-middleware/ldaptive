DefaultConnectionFactory factory = new DefaultConnectionFactory("ldap://directory.ldaptive.org");
Schema schema = SchemaFactory.createSchema(factory);
BeanGenerator generator = BeanGenerator.builder()
  .schema(schema)
  .excludedNames("objectClass")
  .objectClasses("inetOrgPerson")
  .packageName("org.ldaptive.beans.schema")
  .useOperationalAttributes(true)
  .useOptionalAttributes(true)
  .includeSuperiorClasses(true)
  .build();
generator.generate();
generator.write("target/generated-test-sources/ldaptive");
