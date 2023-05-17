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
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd">

  <bean id="connectionFactory"
        class="org.ldaptive.DefaultConnectionFactory">
    <property name="connectionConfig">
      <bean class="org.ldaptive.ConnectionConfig" p:ldapUrl="ldap://directory.ldaptive.org" p:useStartTLS="true">
        <property name="sslConfig">
          <bean class="org.ldaptive.ssl.SslConfig">
            <property name="credentialConfig">
              <bean class="org.ldaptive.ssl.KeyStoreCredentialConfig"
                    p:keyStore="classpath:/ldaptive.keystore"
                    p:keyStoreType="BKS"
                    p:keyStorePassword="changeit"
                    p:trustStore="classpath:/ldaptive.truststore"
                    p:trustStoreType="BKS"
                    p:trustStorePassword="changeit" />
            </property>
          </bean>
        </property>
        <property name="connectionInitializers">
          <bean class="org.ldaptive.BindConnectionInitializer">
            <property name="bindSaslConfig">
              <bean class="org.ldaptive.sasl.SaslConfig" p:mechanism="EXTERNAL" />
            </property>
          </bean>
        </property>
      </bean>
    </property>
  </bean>

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
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd">

  <bean id="conversionService"
        class="org.springframework.context.support.ConversionServiceFactoryBean">
    <property name="converters">
      <set>
        <bean class="org.ldaptive.beans.spring.convert.StringToDurationConverter"/>
        <bean class="org.ldaptive.beans.spring.convert.DurationToStringConverter"/>
      </set>
    </property>
  </bean>

  <bean id="pool" class="org.ldaptive.PooledConnectionFactory" init-method="initialize"
        p:blockWaitTime="PT5S"
        p:minPoolSize="5"
        p:maxPoolSize="20"
        p:validatePeriodically="true">
    <property name="validator">
      <bean class="org.ldaptive.SearchConnectionValidator" p:validatePeriod="PT30S" />
    </property>
    <property name="connectionConfig">
      <bean class="org.ldaptive.ConnectionConfig" p:ldapUrl="ldap://directory.ldaptive.org" p:useStartTLS="true">
        <property name="sslConfig">
          <bean class="org.ldaptive.ssl.SslConfig">
            <property name="credentialConfig">
              <bean class="org.ldaptive.ssl.KeyStoreCredentialConfig"
                    p:keyStore="classpath:/ldaptive.keystore"
                    p:keyStoreType="BKS"
                    p:keyStorePassword="changeit"
                    p:trustStore="classpath:/ldaptive.truststore"
                    p:trustStoreType="BKS"
                    p:trustStorePassword="changeit" />
            </property>
          </bean>
        </property>
        <property name="connectionInitializers">
          <bean class="org.ldaptive.BindConnectionInitializer">
            <property name="bindSaslConfig">
              <bean class="org.ldaptive.sasl.SaslConfig" p:mechanism="EXTERNAL" />
            </property>
          </bean>
        </property>
      </bean>
    </property>
  </bean>

</beans>
{% endhighlight %}

To access your pool:

{% highlight java %}
ClassPathXmlApplicationContext poolContext = new ClassPathXmlApplicationContext(new String[] {"/path_to_my/spring-pool-context.xml", });
PooledConnectionFactory pool = poolContext.getBean("pool", PooledConnectionFactory.class);
{% endhighlight %}

