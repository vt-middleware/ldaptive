SoftLimitConnectionPool pool = new SoftLimitConnectionPool(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
pool.initialize();
PooledConnectionFactory connFactory = new PooledConnectionFactory(pool);

Connection conn = connFactory.getConnection();
try {
  // connection is already open, perform an operation

} finally {
  // closing a connection returns it to the pool
  conn.close();
}

// close the pool
pool.close();
