---
layout: default_v1
title: Ldaptive - bean mapping
redirect_from: "/v1/docs/guide/beans/mapping/"
---

{% include relative %}

# Bean Mapping

Creating custom domain objects from LDAP entries and vice versa is a common pattern. Ldaptive provides an API for mapping an `LdapEntry` to any `Object` provided that object is properly annotated. This support is provided in a separate library that is available in the _jars_ directory of the [latest download]({{ relative }}download.html).

Or included as a maven dependency:

{% highlight xml %}
 <dependencies>
  <dependency>
    <groupId>org.ldaptive</groupId>
    <artifactId>ldaptive-beans</artifactId>
    <version>{{ site.version_v1 }}</version>
  </dependency>
</dependencies>
{% endhighlight %}

The interface for ldap entry mapper looks like:

{% highlight java %}
public interface LdapEntryMapper<T>
{
  String mapDn(T object);

  void map(T source, LdapEntry dest);

  void map(LdapEntry source, T dest);
}
{% endhighlight %}

There are two annotations that describe the relationship between your object and an LDAP entry, @Entry and @Attribute. The @Entry annotation is a class level annotation that contains a DN and all the attributes for the entry. The following sample object demonstrates how to use these annotations.

{% highlight java %}


@Entry(
  dn = "distinguishedName", // this can be a property in your object or a hard coded string
  attributes = {
    @Attribute(name = "displayName", property = "name"),  // name of the attribute and the property name on this object
    @Attribute(name = "mail", property = "email"),
    @Attribute(name = "telephoneNumber", property = "phoneNumber"),
    @Attribute(name = "jpegPhoto", property = "photo", binary = true),
    @Attribute(name = "objectClass", values = {"person", "inetOrgPerson"})  // hard coded attribute with values, does not exist on this object
  })
public class MyObject
{
  private String distinguishedName;
  private String name;
  private String email;
  private String phoneNumber;
  private byte[] photo;

  public MyObject() {}

  public MyObject(String dn) { distinguishedName = dn; }


  public String getDistinguishedName() { return distinguishedName; }

  public void setDistinguishedName(String s) { distinguishedName = s; }


  public String getName() { return name; }

  private void setName(String s) { name = s; }

  public String getEmail() { return email; }

  public void setEmail(String s) { email = s; }


  public String getPhoneNumber() { return phoneNumber; }

  public void setPhoneNumber(String s) { phoneNumber = s; }


  public byte[] getPhoto() { return photo; }

  public void setPhoto(byte[] b) { photo = b; }
}
{% endhighlight %}

## DefaultLdapEntryMapper

Ldaptive provides a default implementation of the the LdapEntryMapper that supports the following types:

- Boolean
- Double
- Float
- Integer
- Long
- Short
- String
- byte[]
- char[]
- Certificate
- Calendar (ZonedDateTime in v1.2)
- UUID

Collections and arrays are supported, but property nesting is not. Only simple Java beans should be used with this ldap entry mapper.

{% highlight java %}
{% include source_v1/beans/mapping/1.java %}
{% endhighlight %}

## SpringLdapEntryMapper

Ldaptive provides another implementation of `LdapEntryMapper` that can leverage [SPEL](http://docs.spring.io/spring/docs/4.0.x/spring-framework-reference/html/expressions.html) for bean property resolution. This allows the conversion of LDAP data for more complex object graphs. The syntax for the @Attribute property is the SPEL expression. Note that the ldaptive-beans pom lists spring as an optional dependency, so if you're using maven then you'll have the declare the spring dependency.

