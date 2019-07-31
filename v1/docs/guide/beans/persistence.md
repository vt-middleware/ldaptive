---
layout: default
title: Ldaptive - persistence
redirect_from: "/docs/guide/beans/persistence/"
---

{% include relative %}

# Bean Persistence

Ldaptive provides an API similar to the J2EE entity manager to facilitate reading and writing LDAP data with Java beans. This support is provided in a separate library that is available in the _jars_ directory of the [latest download]({{ relative }}download.html).

Or included as a maven dependency:

{% highlight xml %}
 <dependencies>
  <dependency>
    <groupId>org.ldaptive</groupId>
    <artifactId>ldaptive-beans</artifactId>
    <version>{{ site.version }}</version>
  </dependency>
</dependencies>
{% endhighlight %}

The interface for ldap entry manager looks like:

{% highlight java %}
public interface LdapEntryManager<T>
{
  T find(T object) throws LdapException;

  Response<Void> add(T object) throws LdapException;

  Response<Void> merge(T object) throws LdapException;

  Response<Void> delete(T object) throws LdapException;
}
{% endhighlight %}

## DefaultLdapEntryManager

Ldaptive provides a default implementation of the `LdapEntryManager` which delegates to the appropriate add, merge, or delete operation.

Simple Java bean to illustrate usage:

{% highlight java %}
@Entry(
  dn = "distinguishedName"
  attributes = {
    @Attribute(name = "displayName", property = "name"),
    @Attribute(name = "mail", property = "email"),
    @Attribute(name = "telephoneNumber", property = "phoneNumber"),
  })
public class MyObject
{
  private String distinguishedName;
  private String name;
  private String email;
  private String phoneNumber;

  public MyObject() {}

  public MyObject(String dn) { distinguisedName = dn; }

  public String getDistinguishedName() { return distinguishedName; }

  public String getName() { return name; }

  public void setName(String s) { name = s; }

  public String getEmail() { return email; }

  public void setEmail(String s) { email = s; }

  public String getPhoneNumber() { return phoneNumber; }

  public void setPhoneNumber(String s) { phoneNumber = s; }
}
{% endhighlight %}

Perform some common operations against the LDAP:

{% highlight java %}
{% include source/beans/persistence/1.java %}
{% endhighlight %}

