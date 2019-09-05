---
layout: default
title: Ldaptive - templates
redirect_from: "/docs/guide/templates/"
---

{% include relative %}

# Templates

The templates project provides a framework for accepting some number of words (or terms) and producing search results from an LDAP. The most common use case being free form search input, like one would expect for a search engine.

A `SearchTemplatesOperation` is configured with an ordered number of `SearchTemplates`. The first `SearchTemplates` object is expected to format search filters for one term queries. The second `SearchTemplates` object is expected to format search filters for two term queries, and so forth. The appropriate `SearchTemplates` is selected based on the `Query` that is provided to the operation and a search is executed for each of the filters in that templates. A single `SearchResponse` containing all results is returned. In this fashion a set of search terms can be transformed into LDAP search results.

Some named parameters are defined by the templates in order to write search filters:

- {term1} is replaced with the first query term
- {initial1} is replaced with the first letter of the first query term
- {term2} is replaced with the second query term
- {initial2} is replaced with the first letter of the second query term
- ...
- {termN} is replaced with the Nth query term
- {initialN} is replaced with the first letter of the Nth query term

{% highlight java %}
{% include source/templates/1.java %}
{% endhighlight %}

Templates support is provided in a separate library that is available in the _jars_ directory of the [latest download]({{ relative }}download.html).

Or included as a maven dependency:

{% highlight xml %}
 <dependencies>
   <dependency>
     <groupId>org.ldaptive</groupId>
     <artifactId>ldaptive-templates</artifactId>
     <version>{{ site.version }}</version>
   </dependency>
</dependencies>
{% endhighlight %}

## Web Application

A common use case for this type of application is a web search engine. Ldaptive provides a web application which can be configured with custom search templates to expose results as LDIF, DSMLv1, or JSON. The configuration for this webapp leverages Spring IOC to configure the `SearchTemplates`.

[![PeopleSearch]({{ relative }}assets/images/peoplesearch.png)](http://webapps.middleware.vt.edu/peoplesearch)

### Sample Templates Context

Expects the ${ldapUrl} and ${baseDn} properties to be replaced at build time.

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
                           http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.2.xsd">

  <bean id="connectionFactory"
        class="org.ldaptive.PooledConnectionFactory"
        init-method="initialize"
        p:ldapUrl="${ldapUrl}"
        p:blockWaitTime="5000"
        p:minPoolSize="5"
        p:maxPoolSize="10"
        p:validatePeriodically="true">
    <property name="validator">
      <bean class="org.ldaptive.pool.SearchValidator"/>
    </property>
  </bean>

  <bean id="searchOperation"
        class="org.ldaptive.SearchOperation"
        p:baseDn="${baseDn}"
        p:connectionFactory-ref="connectionFactory"/>

  <bean id="searchOperationWorker"
        class="org.ldaptive.concurrent.SearchOperationWorker"
        p:operation-ref="searchOperation"/>

  <!-- SEARCH CONFIG -->

  <!-- searches are defined in the following section
       A bean is defined to handle a specific number of query terms.
       When a query arrives the searches are executed that match the number of
       terms entered.
       The following syntax is used to match query terms:
       {term1} == the first query term entered
       {term2} == the second query term entered, and so forth
       {initial1} == the first letter of the first query term entered
       in this manner you can construct search filters such as:
       (givenName={term1})(middleName={initial2}*)(sn={term3}) -->

  <!-- ONE TERM QUERIES -->
  <bean id="oneTermSearch" class="org.ldaptive.templates.SearchTemplates">
    <constructor-arg>
      <list>
        <!-- phone number search -->
        <!-- note that openldap removes dashes and spaces for all phone number queries -->
        <value>(telephoneNumber={term1})</value>
        <value>(telephoneNumber=*{term1})</value>

        <!-- name search -->
        <value>(|(givenName={term1})(sn={term1}))</value>
        <value>(|(givenName={term1}*)(sn={term1}*))</value>
        <value>(|(givenName=*{term1}*)(sn=*{term1}*))</value>

        <!-- email search -->
        <value>(mail={term1})</value>
        <value>(mail={term1}*)</value>
        <value>(mail=*{term1}*)</value>
      </list>
    </constructor-arg>
  </bean>

  <!-- TWO TERM QUERIES -->
  <bean id="twoTermSearch" class="org.ldaptive.templates.SearchTemplates">
    <constructor-arg>
      <list>
        <!-- name search -->
        <value>(&amp;(givenName={term1})(sn={term2}))</value>
        <value>(cn={term1} {term2})</value>
        <value>(&amp;(givenName={term1}*)(sn={term2}*))</value>
        <value>(cn={term1}* {term2}*)</value>
        <value>(&amp;(givenName=*{term1}*)(sn=*{term2}*))</value>
        <value>(cn=*{term1}* *{term2}*)</value>

        <!-- initial search -->
        <value>(|(&amp;(givenName={initial1}*)(sn={term2}))(&amp;(middleName={initial1}*)(sn={term2})))</value>

        <!-- last name search -->
        <value>(sn={term2})</value>
      </list>
    </constructor-arg>
  </bean>

  <!-- THREE TERM QUERIES -->
  <bean id="threeTermSearch" class="org.ldaptive.templates.SearchTemplates">
    <constructor-arg>
      <list>
        <!-- name search -->
        <value>(|(&amp;(givenName={term1})(sn={term3}))(&amp;(givenName={term2})(sn={term3})))</value>
        <value>(|(cn={term1} {term2} {term3})(cn={term2} {term1} {term3}))</value>
        <value>(|(&amp;(givenName={term1}*)(sn={term3}*))(&amp;(givenName={term2}*)(sn={term3}*)))</value>
        <value>(|(cn={term1}* {term2}* {term3}*)(cn={term2}* {term1}* {term3}*))</value>
        <value>(|(&amp;(givenName=*{term1}*)(sn=*{term3}*))(&amp;(givenName=*{term2}*)(sn=*{term3}*)))</value>
        <value>(|(cn=*{term1}* *{term2}* *{term3}*)(cn=*{term2}* *{term1}* *{term3}*))</value>

        <!-- initial search -->
        <value>(|(&amp;(givenName={term1})(middleName={initial2}*)(sn={term3}))(&amp;(givenName={term2})(middleName={initial1}*)(sn={term3})))</value>
        <value>(|(&amp;p(givenName={initial1}*)(middlename={initial2}*)(sn={term3}))(&amp;(givenName={initial2}*)(middleName={initial1}*)(sn={term3})))</value>

        <!-- last name search -->
        <value>(sn={term3})</value>
      </list>
    </constructor-arg>
  </bean>

</beans>
{% endhighlight %}

### Sample web.xml

Expects to find /templates-context.xml in the classpath and returns search results in JSON format. Ignores query terms of length 1 or 2, which typically aren't indexed in LDAP.

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<web-app id="ldaptive-templates"
         version="2.4"
         xmlns="http://java.sun.com/xml/ns/j2ee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd">

  <display-name>Templates Search</display-name>
  <description>
    Templates Search is a web application which accepts search queries and returns best fit information from a LDAP.
  </description>

  <!-- Search servlet -->
  <servlet>
    <servlet-name>JsonSearch</servlet-name>
    <servlet-class>org.ldaptive.servlets.SearchServlet</servlet-class>
    <init-param>
      <param-name>searchExecutorClass</param-name>
      <param-value>org.ldaptive.servlets.JsonServletSearchTemplatesExecutor</param-value>
    </init-param>
    <!-- Classpath location of the spring context -->
    <init-param>
      <param-name>springContextPath</param-name>
      <param-value>/templates-context.xml</param-value>
    </init-param>
    <!-- Ignore pattern -->
    <init-param>
      <param-name>ignorePattern</param-name>
      <param-value>^\w{1,2}$</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>

  <servlet-mapping>
    <servlet-name>JsonSearch</servlet-name>
    <url-pattern>/Search</url-pattern>
  </servlet-mapping>

</web-app>
{% endhighlight %}

This webapp can be used with the maven war overlay to customize it's configuration:

{% highlight xml %}
 <dependencies>
   <dependency>
     <groupId>org.ldaptive</groupId>
     <artifactId>ldaptive-webapp</artifactId>
     <version>{{ site.version }}</version>
   </dependency>
</dependencies>
{% endhighlight %}

