// Usage: jshell --class-path ... ldapsearch.jsh
import org.ldaptive.*
import org.ldaptive.io.*
import org.ldaptive.props.*

var props = new Properties();
var args = System.getProperty("args");
if (args != null && args.length() > 0) {
  // TODO: delimiting on space character is problematic for properties containing spaces
  props.load(new StringReader(args.replaceAll(" ", "\n")));
} else {
  props = null;
}

if (props == null || props.isEmpty() || props.containsKey("-h") || props.containsKey("-help") || props.containsKey("--help")) {
  props = null;
} else {
  // add ldaptive package to properties
  for (String key : props.stringPropertyNames()) {
    if (!key.contains(".")) {
      props.setProperty(PropertySource.PropertyDomain.LDAP.value().concat(key), (String) props.remove(key));
    } else if (key.startsWith("system.") && key.length() > "system.".length()) {
      System.setProperty(key.substring("system.".length()), (String) props.remove(key));
    }
  }
}

if (props != null) {
  var config = new ConnectionConfig();
  new ConnectionConfigPropertySource(config, props).initialize();
  var request = new SearchRequest();
  new SearchRequestPropertySource(request, props).initialize();

  var factory = new DefaultConnectionFactory(config);
  var response = new SearchOperation(factory, request).execute();
  new LdifWriter(new PrintWriter(System.out)).write(response);
} else {
  System.out.println("USAGE: jshell --class-path ... -R-Dargs=\"...\" ldapsearch.jsh");
  System.out.println("  where args includes");
  System.out.println("    baseDn=dc=ldaptive,dc=org");
  System.out.println("    bindDn=uid=dfisher,ou=people,dc=ldaptive,dc=org");
  System.out.println("    bindCredential=password");
  System.out.println("    bindSaslConfig={mechanism=EXTERNAL}");
  System.out.println("    connectTimeout=PT5S");
  System.out.println("    credentialConfig={trustCertificates=file:/path/to/ca.pem}");
  System.out.println("    filter=(mail=dfisher@ldaptive.org)");
  System.out.println("    ldapUrl=ldap://directory.ldaptive.org");
  System.out.println("    responseTimeout=PT5S");
  System.out.println("    returnAttributes=uid,mail");
  System.out.println("    searchScope=SUBTREE");
  System.out.println("    sizeLimit=1");
  System.out.println("    timeLimit=PT0S");
  System.out.println("    useStartTLS=true");
  System.out.println("    system.javax.net.debug=all");
}

/exit
