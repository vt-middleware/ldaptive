---
layout: default
title: Ldaptive - spring
redirect_from: "/docs/guide/spring/"
---

# Spring Integration

## Spring Beans

Ldaptive objects are candidates for configuration via Spring XML context files. The following Spring context XML provides a basic example of wiring up a ConnectionFactory object for use in a Spring application.

### Connection Factory Configuration

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans-3.1.xsd">

  <bean id="connectionFactory"
    class="org.ldaptive.DefaultConnectionFactory"
    p:connectionConfig-ref="connectionConfig"
  />

  <bean id="connectionConfig"
    class="org.ldaptive.ConnectionConfig"
    p:ldapUrl="ldap://directory.ldaptive.org"
    p:bindSaslConfig-ref="saslConfig"
    p:useStartTLS="true"
    p:sslConfig-ref="sslConfig"
  />

  <bean id="saslConfig"
    class="org.ldaptive.sasl.ExternalConfig"
  />

  <bean id="sslConfig"
    class="org.ldaptive.ssl.SslConfig"
    p:credentialConfig-ref="credentialConfig"
  />

  <bean id="credentialConfig"
    class="org.ldaptive.ssl.KeyStoreCredentialConfig"
    p:keyStore="classpath:/ldaptive.keystore"
    p:keyStoreType="BKS"
    p:keyStorePassword="changeit"
    p:trustStore="classpath:/ldaptive.truststore"
    p:trustStoreType="BKS"
    p:trustStorePassword="changeit"
  />
</beans>
{% endhighlight %}

To access your connection factory: 

{% highlight java %}
ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"/path_to_my/spring-context.xml", });
ConnectionFactory cf = context.getBean("connectionFactory", ConnectionFactory.class);
Connection conn = cf.getConnection();
{% endhighlight %}

### Pooling Configuration

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
                           http://www.springframework.org/schema/util
                           http://www.springframework.org/schema/util/spring-util-3.1.xsd">

  <bean id="pool"
    class="org.ldaptive.pool.BlockingConnectionPool"
    init-method="initialize"
    p:blockWaitTime="5000">
    <constructor-arg index="0">
      <bean class="org.ldaptive.pool.PoolConfig"
        p:minPoolSize="5"
        p:maxPoolSize="20"
        p:validatePeriodically="true"
        p:validatePeriod="30"
      />
    </constructor-arg>
    <constructor-arg index="1" ref="connectionFactory"/>
  </bean>

  <bean id="connectionFactory"
    class="org.ldaptive.DefaultConnectionFactory"
    p:connectionConfig-ref="connectionConfig"
  />

  <bean id="connectionConfig"
    class="org.ldaptive.ConnectionConfig"
    p:ldapUrl="ldap://directory.ldaptive.org"
    p:bindSaslConfig-ref="saslConfig"
    p:useStartTLS="true"
    p:sslConfig-ref="sslConfig"
  />

  <bean id="saslConfig"
    class="org.ldaptive.sasl.ExternalConfig"
  />

  <bean id="sslConfig"
    class="org.ldaptive.ssl.SslConfig"
    p:credentialConfig-ref="credentialConfig"
  />

  <bean id="credentialConfig"
    class="org.ldaptive.ssl.KeyStoreCredentialConfig"
    p:keyStore="classpath:/ldaptive.keystore"
    p:keyStoreType="BKS"
    p:keyStorePassword="changeit"
    p:trustStore="classpath:/ldaptive.truststore"
    p:trustStoreType="BKS"
    p:trustStorePassword="changeit"
  />
</beans>
{% endhighlight %}

To access your pool:

{% highlight java %}
ClassPathXmlApplicationContext poolContext = new ClassPathXmlApplicationContext(new String[] {"/path_to_my/spring-pool-context.xml", });
BlockingConnectionPool pool = poolContext.getBean("pool", BlockingConnectionFactory.class);
Connection conn = pool.getConnection();
{% endhighlight %}

## Spring Extensible XML

Ldaptive provides a [schema extension](http://www.ldaptive.org/schema/spring-ext.xsd) that can simplify configuration.

### Connection Factory
{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:ldaptive="http://www.ldaptive.org/schema/spring-ext"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
            http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
            http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.ldaptive.org/schema/spring-ext http://www.ldaptive.org/schema/spring-ext-{{ site.version }}.xsd">

  <context:property-placeholder location="classpath:/spring-ext.properties"/>

  <ldaptive:connection-factory
    ldapUrl="ldap://directory.ldaptive.org"
    bindDn="cn=manager,ou=people,dc=ldaptive,dc=org"
    bindCredential="not-a-real-password"
    useStartTLS="true"
    trustCertificates="file:/path/to/ldaptive.trust.crt"
  />

</beans>
{% endhighlight %}

{% highlight java %}
ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"/path_to_my/spring-ext-context.xml", });
DefaultConnectionFactory connectionFactory = context.getBean("connection-factory", DefaultConnectionFactory.class);
{% endhighlight %}

### Pooled Connection Factory

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:ldaptive="http://www.ldaptive.org/schema/spring-ext"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
            http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
            http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.ldaptive.org/schema/spring-ext http://www.ldaptive.org/schema/spring-ext-{{ site.version }}.xsd">

  <context:property-placeholder location="classpath:/spring-ext.properties"/>

  <ldaptive:pooled-connection-factory
    ldapUrl="ldap://directory.ldaptive.org"
    bindDn="cn=manager,ou=people,dc=ldaptive,dc=org"
    bindCredential="not-a-real-password"
    useStartTLS="true"
    trustCertificates="file:/path/to/ldaptive.trust.crt"
    minPoolSize="5"
    maxPoolSize="10"
  />

</beans>
{% endhighlight %}

{% highlight java %}
ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"/path_to_my/spring-ext-context.xml", });
PooledConnectionFactory pooledConnectionFactory = context.getBean("pooled-connection-factory", PooledConnectionFactory.class);
{% endhighlight %}

### Search Executor

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:ldaptive="http://www.ldaptive.org/schema/spring-ext"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
            http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
            http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.ldaptive.org/schema/spring-ext http://www.ldaptive.org/schema/spring-ext-{{ site.version }}.xsd">

  <context:property-placeholder location="classpath:/spring-ext.properties"/>

  <ldaptive:search-executor
    baseDn="ou=people,dc=ldaptive,dc=org"
    searchFilter="(mail=*)"
    returnAttributes="cn,givenName,sn"
    timeLimit="PT5S"
    sizeLimit="100"
    binaryAttributes="jpegPhoto,userCertificate"
    sortBehavior="ORDERED"
  />

</beans>
{% endhighlight %}

{% highlight java %}
ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"/path_to_my/spring-ext-context.xml", });
SearchExecutor executor = context.getBean("search-executor", SearchExecutor.class);
{% endhighlight %}

### Authenticator

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:ldaptive="http://www.ldaptive.org/schema/spring-ext"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
            http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
            http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.ldaptive.org/schema/spring-ext http://www.ldaptive.org/schema/spring-ext-{{ site.version }}.xsd">

  <context:property-placeholder location="classpath:/spring-ext.properties"/>

  <ldaptive:bind-search-authenticator
    id="bind-search-disable-pool"
    ldapUrl="ldap://directory.ldaptive.org"
    trustStore="classpath:/ldaptive.truststore"
    trustStorePassword="changeit"
    baseDn="ou=people,dc=ldaptive,dc=org"
    userFilter="(mail={user})"
    bindDn="cn=manager,ou=people,dc=ldaptive,dc=org"
    bindCredential="file:/path/to/credential"
    connectTimeout="PT2S"
    useStartTLS="true">
    <ldaptive:authentication-response-handler>
      <ldaptive:password-policy-handler/>
    </ldaptive:authentication-response-handler>
  </ldaptive:bind-search-authenticator>

</beans>
{% endhighlight %}

{% highlight java %}
ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"/path_to_my/spring-ext-context.xml", });
Authenticator bindSearchAuthenticator = context.getBean("bind-search-authenticator", Authenticator.class);
{% endhighlight %}

