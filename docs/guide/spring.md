---
layout: default
title: Ldaptive - spring
redirect_from: "/docs/guide/spring/"
---

# Spring Integration

Ldaptive objects are candidates for configuration via Spring XML context files. The following Spring context XML provides a basic example of wiring up a ConnectionFactory object for use in a Spring application.

## Connection Factory Configuration

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

## Pooling Configuration

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

