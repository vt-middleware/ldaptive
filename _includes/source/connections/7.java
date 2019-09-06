ConnectionConfig.builder()
  .url("ldap://directory.ldaptive.org")
  .useStartTLS(true)
  .connectTimeout(Duration.ofSeconds(5))
  .responseTimeout(Duration.ofSeconds(5))
  .autoReconnect(true)
  .autoReconnectCondition(metadata -> {
    if (metadata.getAttempts() <= 5) {
      try {
        final Duration sleepTime = Duration.ofSeconds(1).multipliedBy(metadata.getAttempts());
        Thread.sleep(sleepTime.toMillis());
      } catch (InterruptedException ie) {}
        return true;
      }
    return false;})
  .build();
