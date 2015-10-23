Map<String, Object> props = new HashMap<String, Object>();
props.put("custom.property.name", "custom.property.value");
DefaultConnectionFactory connFactory = new DefaultConnectionFactory("ldap://directory.ldaptive.org");
connFactory.getProvider().getProviderConfig().setProperties(props);
Connection conn = connFactory.getConnection();
