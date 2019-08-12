ConnectionConfig.builder()
  .url("ldap://directory.ldaptive.org")
  .useStartTLS(true)
  .connectTimeout(Duration.ofSeconds(5))
  .responseTimeout(Duration.ofSeconds(5))
  .autoReconnect(true)
  .autoReconnectCondition(attempt -> {
    if (attempt <= 5) {
      try {
        final Duration sleepTime = Duration.ofSeconds(1).multipliedBy(attempt);
        Thread.sleep(sleepTime.toMillis());
      } catch (InterruptedException ie) {}
        return true;
      }
    return false;})
  .build()