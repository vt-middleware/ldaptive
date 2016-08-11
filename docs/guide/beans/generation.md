---
layout: default
title: Ldaptive - generation
redirect_from: "/docs/guide/beans/generation/"
---

{% include relative %}

# Bean Generation

Ldaptive supports generating Java beans from an LDAP schema. The [Sun codemodel](https://codemodel.java.net/) library is used for java source generation. This support is provided in a separate library that is available in the _jars_ directory of the [latest download]({{ relative }}download.html).

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

## Using Java to generate beans

{% highlight java %}
{% include source/beans/generation/1.java %}
{% endhighlight %}

This would generate a bean that looks like:

{% highlight java %}

/**
 * Ldaptive generated bean for objectClass 'inetOrgPerson'
 *
 * Note: Many properties have been removed for brevity
 *
 */
@Entry(dn = "dn", attributes = {
    @Attribute(name = "cn"),
    @Attribute(name = "createTimestamp"),
    @Attribute(name = "creatorsName"),
    @Attribute(name = "description"),
    @Attribute(name = "entryDN"),
    @Attribute(name = "entryUUID"),
    @Attribute(name = "jpegPhoto", binary = true),
    @Attribute(name = "uid"),
    @Attribute(name = "userCertificate"),
})
public class InetOrgPerson {

    private String dn;
    private Collection<String> cn;
    private ZonedDateTime createTimestamp;
    private String creatorsName;
    private Collection<String> description;
    private String entryDN;
    private UUID entryUUID;
    private Collection<byte[]> jpegPhoto;
    private Collection<String> uid;
    private Collection<Certificate> userCertificate;

    public String getDn() {
        return dn;
    }

    public void setDn(String s) {
        this.dn = s;
    }

    public Collection<String> getCn() {
        return cn;
    }

    public void setCn(Collection<String> c) {
        this.cn = c;
    }

    public ZonedDateTime getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(ZonedDateTime t) {
        this.createTimestamp = t;
    }

    public String getCreatorsName() {
        return creatorsName;
    }

    public void setCreatorsName(String s) {
        this.creatorsName = s;
    }

    public Collection<String> getDescription() {
        return description;
    }

    public void setDescription(Collection<String> c) {
        this.description = c;
    }

    public String getEntryDN() {
        return entryDN;
    }

    public void setEntryDN(String s) {
        this.entryDN = s;
    }

    public UUID getEntryUUID() {
        return entryUUID;
    }

    public void setEntryUUID(UUID s) {
        this.entryUUID = s;
    }

    public Collection<byte[]> getJpegPhoto() {
        return jpegPhoto;
    }

    public void setJpegPhoto(Collection<byte[]> c) {
        this.jpegPhoto = c;
    }

    public Collection<String> getUid() {
        return uid;
    }

    public void setUid(Collection<String> c) {
        this.uid = c;
    }

    public Collection<Certificate> getUserCertificate() {
        return userCertificate;
    }

    public void setUserCertificate(Collection<Certificate> c) {
        this.userCertificate = c;
    }
}
{% endhighlight %}

## Using Maven to generate beans

Add the dependencies to your pom.xml:

{% highlight xml %}
 <dependencies>
  <dependency>
    <groupId>org.ldaptive</groupId>
    <artifactId>ldaptive-beans</artifactId>
    <version>{{ site.version}}</version>
  </dependency>
  <dependency>
    <groupId>com.sun.codemodel</groupId>
    <artifactId>codemodel</artifactId>
    <version>2.6</version>
    <optional>true</optional>
  </dependency>
</dependencies>
{% endhighlight %}

Create a properties file for your configuration: src/main/resources/ldaptive-gen.properties

{% highlight text %}

# ldaptive bean generation properties

# package to create the beans in
org.ldaptive.packageName=org.ldaptive.schema.beans

# whether to generate beans that include optional attributes
org.ldaptive.useOptionalAttributes=true

# whether to generate beans that include operational attributes
org.ldaptive.useOperationalAttributes=true

# object classes to generate
org.ldaptive.objectClasses=inetOrgPerson

# provide friendly names some attributes
org.ldaptive.nameMappings=c=countryName,l=localityName

# don't generate properties for these attributes
org.ldaptive.excludedNames=userPassword

# directory to read the schema from
org.ldaptive.ldapUrl=ldap://directory.vt.edu
{% endhighlight %}

Declare a maven exec plugin to generate the beans:

{% highlight xml %}
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>exec-maven-plugin</artifactId>
  <version>1.2.1</version>
  <configuration>
    <mainClass>org.ldaptive.beans.generate.BeanGenerator</mainClass>
    <arguments>
      <argument>file:src/main/resources/ldaptive-gen.properties</argument>
      <argument>target/generated-sources/ldaptive</argument>
    </arguments>
  </configuration>
  <executions>
    <execution>
      <id>exec</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>java</goal>
      </goals>
    </execution>
  </executions>
</plugin>
{% endhighlight %}

Declare a maven build helper to compile the generated source:

{% highlight xml %}
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>build-helper-maven-plugin</artifactId>
  <version>1.8</version>
  <executions>
    <execution>
      <id>add-source</id>
      <goals>
        <goal>add-source</goal>
      </goals>
      <phase>process-sources</phase>
      <configuration>
        <sources>
          <source>${project.build.directory}/generated-sources/ldaptive</source>
        </sources>
      </configuration>
    </execution>
  </executions>
</plugin>
{% endhighlight %}

Run the mvn compile command to generate and compile your beans.

