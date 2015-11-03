DefaultLdapEntryManager<MyObject> manager = new DefaultLdapEntryManager<MyObject>(
  new DefaultLdapEntryMapper(), new DefaultConnectionFactory("ldap://directory.ldaptive.org"));

// add a new entry to the LDAP
MyObject addObject = new MyObject("uid=dfisher,ou=people,dc=ldaptive,dc=org");
addObject.setName("Daniel Fisher");
addObject.setEmail("dfisher@ldaptive.org");
manager.add(addObject);

// modify the entry in the LDAP
MyObject mergeObject = manager.find(new MyObject("uid=dfisher,ou=people,dc=ldaptive,dc=org"));
mergeObject.setPhoneNumber("555-555-1234");
manager.merge(mergeObject);

// simple object containing LDAP attribute data
@Entry(
  dn = "dn",
  attributes = {
    @Attribute(name = "name"),
    @Attribute(name = "email"),
    @Attribute(name = "phoneNumber")})
public class MyObject {
  private String dn;
  private String name;
  private String email;
  private String phoneNumber;

  public MyObject() {}
  public MyObject(String s) { setDn(s); }
  public String getDn() { return dn; }
  public void setDn(String d) { dn = s; }
  public String getName() { return name; }
  public void setName(String s) { name = s; }
  public String getEmail() { return email; }
  public void setEmail(String s) { email = s; }
  public String getPhoneNumber() { return phoneNumber; }
  public void setPhoneNumber(String s) { phoneNumber = s; }
}
