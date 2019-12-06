## JMeter Throughput Testing

Throughput testing will occur during the `verify` phase of the project build lifecycle.
See `Alternative Standalone Execution` for running tests manually.

Testing will include basic `search` and `auth` operations executing against the test ldap server that is started as 
part of integration testing.

#### JMeter Requirements

  - All `.jmx` test plan files must be in `src/test/jmeter` or one of its subdirectories
  - Tests are executed in the Maven `verify` phase
  - Java classes that are used in the test plan must be packaged and copied to `/target/jmeter/lib/ext` (previous 
    requirement supports this)

#### Default Test Parameters

  - authFilter : (uid={user})
  - authRequestId : 1
  - baseDn : ou=test,dc=vt,dc=edu
  - bindDn : uid=1,ou=test,dc=vt,dc=edu
  - ldapUrl : ldap://ldap-test
  - searchFilter : (uid={1})
  - numUsers : 10
    - This is the global (can be overridden for individual thread groups) number of threads (simulated users) that 
      JMeter will create for each test in each test plan.
      A single .jmx file should be 1 test plan.
    - numSearchSingleUsers : Number of threads for Search Operations using a SingleConnectionFactory. Defaults to 
      `${numUsers}`
    - numSearchPooledUsers : Number of threads for Search Operations using a PooledConnectionFactory. Defaults to 
      `${numUsers}`
    - numSearchDefaultUsers : Number of threads for Search Operations using a DefaultConnectionFactory. Defaults to 
      `${numUsers}`
    - numAuthDefaultUsers : Number of threads for Search Operations using a DefaultConnectionFactory. Defaults to 
      `${numUsers}`
  - userRampUp : 10 (should be <= `runDuration`)
    - This is the number of seconds it will take JMeter to start ALL threads for a test. For example, if a test uses
      20 threads and the ramp-up time is 20 seconds, JMeter will start 1 thread every 1 second. A test that uses 20
      threads with a ramp-up of 10 seconds will start a thread every half second. A good starting point is to have
      `userRampUp == numUsers` and adjust up/down as needed.
  - runDuration : 15 (seconds)
    - How long the entire test will execute. This should be at least a few seconds longer than the `userRampUp` so the
      last thread has time to contribute to the load testing.
  - useStartTls : true
    - `org.ldaptive.ConnectionConfig#useStartTLS`
  - autoReconnect : false
    - `org.ldaptive.ConnectionConfig#autoReconnect`
  - minPoolSize : 10
    - The initial number of connections in a PooledConnectionFactory
  - maxPoolSize : 10
    - The maximum number of connections in a PooledConnectionFactory
    
#### Overriding Default Test Parameters

All of the parameters above can be overridden in the `jmeter-maven-plugin` via the `<propertiesUser>` section under
`<configuration>`.
Example:
  ```xml
  <configuration>
    <propertiesUser>
      <ldapUrl>ldap://some-other-url</ldapUrl>
      <numUsers>250</numUsers>
    </propertiesUser>
  </configuration>
  ```
 
#### View Results

Once the integration tests finish the results can be viewed in the JMeter Dashboard by opening 
`integration/target/jmeter/results/index.html`.
Additional graphs can be viewed by starting the JMeter GUI, opening the current TestPlan and importing the results 
(csv) file in `integration/target/jmeter/results/*.csv`

#### Alternative Standalone Execution

Test plans can be executed outside of ldaptive integration testing by following the steps below.
  - Download and unpack JMeter locally
  - Start a local LDAP server or have the necessary server info of a remote LDAP instance
  - Execute the tests via:
    - GUI: Good for a visual of the tests and their execution, but shouldn't be used if max load testing is desired.
      - Start the JMeter GUI by executing the `jmeter` script in the `bin` folder of the JMeter installation and 
        provide the desired test plan via the `-t` flag. 
        Ex: `jmeter-install/bin/jmeter -t /dir/to/plan/LdaptiveLoadTestingPlan.jmx`
      - Provide test parameters via the `User Defined Variables` in the test plan.
      - Hit the play button
    - JMeter CLI: This is recommended way for executing load tests!
      - `jmeter-install/bin/jmeter -n -t /dir/to/plan/LdaptiveLoadTestingPlan.jmx -l /dir/to/logfile/filename.csv 
         -JparamName=someVal`
        - Custom properties are provided as flags prefixed with `-J`. Ex: `-JldapUrl=ldap://ldapHost` 

