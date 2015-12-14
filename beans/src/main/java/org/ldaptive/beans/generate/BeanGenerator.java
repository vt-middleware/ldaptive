/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.generate;

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import org.ldaptive.beans.generate.props.BeanGeneratorPropertySource;
import org.ldaptive.schema.AttributeType;
import org.ldaptive.schema.AttributeUsage;
import org.ldaptive.schema.ObjectClass;
import org.ldaptive.schema.Schema;
import org.ldaptive.schema.Syntax;

/**
 * Utility class for creating Java POJOs from an LDAP schema. Sample usage:
 *
 * <pre>
     Schema schema = new Schema(new DefaultConnectionFactory(
       "ldap://directory.ldaptive.org"));
     BeanGenerator generator = new BeanGenerator(
       schema,
       "com.my.package",
       new String[] {"inetOrgPerson"});
     generator.generate();
     generator.write();
 * </pre>
 *
 * @author  Middleware Services
 */
public class BeanGenerator
{

  /** Code model for java class creation. */
  private final JCodeModel codeModel = new JCodeModel();

  /** Schema to generate beans from. */
  private Schema schema;

  /** Package to create beans in. */
  private String packageName;

  /** Object classes to build beans for. */
  private String[] objectClasses;

  /** Whether to include optional attributes. */
  private boolean useOptionalAttributes = true;

  /** Whether to include operational attributes. */
  private boolean useOperationalAttributes;

  /** Whether to include superior classes for each object class. */
  private boolean includeSuperiorClasses;

  /** Mapping to determine attribute value type. */
  private Map<String, Class<?>> typeMappings = getDefaultTypeMappings();

  /** Name mappings. */
  private Map<String, String> nameMappings = new HashMap<>();

  /** Excluded names. */
  private String[] excludedNames = new String[0];


  /** Default constructor. */
  public BeanGenerator() {}


  /**
   * Creates a new bean generator. A bean will be generated for each supplied object class.
   *
   * @param  s  schema containing directory data for generation
   * @param  name  package name to place the generated classes in
   * @param  oc  object classes to generate beans for
   */
  public BeanGenerator(final Schema s, final String name, final String[] oc)
  {
    schema = s;
    packageName = name;
    objectClasses = oc;
  }


  /**
   * Returns the schema.
   *
   * @return  schema
   */
  public Schema getSchema()
  {
    return schema;
  }


  /**
   * Sets the schema.
   *
   * @param  s  schema
   */
  public void setSchema(final Schema s)
  {
    schema = s;
  }


  /**
   * Returns the package name where beans will be generated.
   *
   * @return  package name
   */
  public String getPackageName()
  {
    return packageName;
  }


  /**
   * Sets the package name where beans will be generated.
   *
   * @param  name  package name
   */
  public void setPackageName(final String name)
  {
    packageName = name;
  }


  /**
   * Returns the object classes. A class is generated for each object class.
   *
   * @return  object classes
   */
  public String[] getObjectClasses()
  {
    return objectClasses;
  }


  /**
   * Sets the object classes. A class is generated for each object class.
   *
   * @param  oc  object classes
   */
  public void setObjectClasses(final String... oc)
  {
    objectClasses = oc;
  }


  /**
   * Returns whether to include optional attributes in bean generation.
   *
   * @return  whether to include optional attributes
   */
  public boolean isUseOptionalAttributes()
  {
    return useOptionalAttributes;
  }


  /**
   * Sets whether to include optional attributes in bean generation.
   *
   * @param  b  whether to include optional attributes
   */
  public void setUseOptionalAttributes(final boolean b)
  {
    useOptionalAttributes = b;
  }


  /**
   * Returns whether to include operational attributes in bean generation.
   *
   * @return  whether to include operational attributes
   */
  public boolean isUseOperationalAttributes()
  {
    return useOperationalAttributes;
  }


  /**
   * Sets whether to include operational attributes in bean generation.
   *
   * @param  b  whether to include operational attributes
   */
  public void setUseOperationalAttributes(final boolean b)
  {
    useOperationalAttributes = b;
  }


  /**
   * Returns whether to include superior classes in bean generation.
   *
   * @return  whether to include superior classes attributes
   */
  public boolean isIncludeSuperiorClasses()
  {
    return includeSuperiorClasses;
  }


  /**
   * Sets whether to include superior classes in bean generation.
   *
   * @param  b  whether to include superior classes
   */
  public void setIncludeSuperiorClasses(final boolean b)
  {
    includeSuperiorClasses = b;
  }


  /**
   * Returns the type mappings. Type mappings is syntax OID to class type and is used to determine field type in the
   * generated POJOs.
   *
   * @return  type mappings
   */
  public Map<String, Class<?>> getTypeMappings()
  {
    return typeMappings;
  }


  /**
   * Sets the type mappings. Type mappings is syntax OID to class type and is used to determine field type in the
   * generated POJOs.
   *
   * @param  m  type mappings
   */
  public void setTypeMappings(final Map<String, Class<?>> m)
  {
    typeMappings = m;
  }


  /**
   * Returns the mapping of directory attribute name to bean property. This property is used to override the default
   * schema name. For instance, you may prefer using 'countryName' to 'c', which would be set as 'c'=&gt;'countryName'.
   *
   * @return  attribute name to bean property mapping
   */
  public Map<String, String> getNameMappings()
  {
    return nameMappings;
  }


  /**
   * Sets the mapping of directory attribute name to bean property.
   *
   * @param  m  name mappings
   *
   * @throws  NullPointerException  if m is null
   */
  public void setNameMappings(final Map<String, String> m)
  {
    if (m == null) {
      throw new NullPointerException("Name mappings cannot be null");
    }
    nameMappings = m;
  }


  /**
   * Returns the attribute names to exclude from bean generation. Excludes an attribute from the generated POJO. For
   * instance, you may not want 'userPassword' included in your bean.
   *
   * @return  attribute names to exclude
   */
  public String[] getExcludedNames()
  {
    return excludedNames;
  }


  /**
   * Sets the attribute names to exclude from bean generation.
   *
   * @param  names  to exclude
   *
   * @throws  NullPointerException  if names is null
   */
  public void setExcludedNames(final String... names)
  {
    if (names == null) {
      throw new NullPointerException("Excluded names cannot be null");
    }
    excludedNames = names;
  }


  /**
   * Returns the default syntax types used to determine attribute property type.
   *
   * @return  map of syntax OID to class type
   */
  protected static Map<String, Class<?>> getDefaultTypeMappings()
  {
    final Map<String, Class<?>> m = new HashMap<>();
    m.put("1.3.6.1.4.1.1466.115.121.1.7", Boolean.class);
    m.put("1.3.6.1.4.1.1466.115.121.1.5", byte[].class);
    m.put("1.3.6.1.4.1.1466.115.121.1.8", Certificate.class);
    m.put("1.3.6.1.4.1.1466.115.121.1.24", ZonedDateTime.class);
    m.put("1.3.6.1.4.1.1466.115.121.1.36", Integer.class);
    m.put("1.3.6.1.1.16.1", UUID.class);
    return m;
  }


  /**
   * Returns the class for the supplied attribute type and syntax. If the attribute type syntax OID is found in the
   * default type mapping it is used. Otherwise if the syntax is "X-NOT-HUMAN-READABLE", a byte array is used.
   *
   * @param  type  attribute type
   * @param  syntax  associated with the attribute type
   *
   * @return  syntax type
   */
  protected Class<?> getSyntaxType(final AttributeType type, final Syntax syntax)
  {
    Class<?> t = null;
    for (Map.Entry<String, Class<?>> entry : typeMappings.entrySet()) {
      if (entry.getKey().equals(type.getSyntaxOID(false))) {
        t = entry.getValue();
      }
    }
    if (t == null) {
      if (Syntax.containsBooleanExtension(syntax, "X-NOT-HUMAN-READABLE")) {
        t = byte[].class;
      } else {
        t = String.class;
      }
    }
    return t;
  }


  /**
   * Generates a class for each configured object class. See {@link #objectClasses}. {@link #write(String)} must be
   * invoked to write the classes to disk.
   */
  public void generate()
  {
    for (String objectClass : objectClasses) {
      final JDefinedClass definedClass = createClass(packageName, objectClass);
      final JDocComment jDocComment = definedClass.javadoc();
      jDocComment.add(String.format("Ldaptive generated bean for objectClass '%s'", objectClass));

      final ObjectClass oc = schema.getObjectClass(objectClass);
      final Set<String> attributeNames = getAttributeNames(oc);
      if (useOperationalAttributes) {
        for (AttributeType type : schema.getAttributeTypes()) {
          if (AttributeUsage.DIRECTORY_OPERATION.equals(type.getUsage())) {
            attributeNames.add(type.getName());
          }
        }
      }

      final Map<String, AttributeType> mutators = new TreeMap<>();
      for (String name : attributeNames) {
        final AttributeType type = schema.getAttributeType(name);
        if (!isNameExcluded(type)) {
          if (nameMappings.containsKey(type.getName())) {
            mutators.put(nameMappings.get(type.getName()), type);
          } else {
            mutators.put(formatAttributeName(type.getName()), type);
          }
        }
      }

      // add entry annotation
      final JAnnotationUse entryAnnotation = definedClass.annotate(codeModel.ref(org.ldaptive.beans.Entry.class));
      entryAnnotation.param("dn", "dn");

      final JAnnotationArrayMember attrArray = entryAnnotation.paramArray("attributes");

      // add mutator for the DN
      createMutators(definedClass, "dn", String.class, false);

      // add mutators for each attribute
      for (Map.Entry<String, AttributeType> mutator : mutators.entrySet()) {
        final Class<?> syntaxType = getSyntaxType(
          mutator.getValue(),
          schema.getSyntax(mutator.getValue().getSyntaxOID(false)));
        if (mutator.getValue().isSingleValued()) {
          createMutators(definedClass, mutator.getKey(), syntaxType, false);
        } else {
          createMutators(definedClass, mutator.getKey(), syntaxType, true);
        }

        // add attribute annotation
        final JAnnotationUse attrAnnotation = attrArray.annotate(org.ldaptive.beans.Attribute.class);
        attrAnnotation.param("name", mutator.getValue().getName());
        if (!mutator.getKey().equals(mutator.getValue().getName())) {
          attrAnnotation.param("property", mutator.getKey());
        }
        if (byte[].class.equals(syntaxType)) {
          attrAnnotation.param("binary", true);
        }
      }

      // create additional methods
      createHashCode(definedClass);
      createEquals(definedClass);
      createToString(definedClass);
    }
  }


  /**
   * Returns the attribute names to use for the supplied object class. See {@link #getAttributeNames(ObjectClass, Set)}.
   *
   * @param  objectClass  to retrieve names from
   *
   * @return  set of all attribute names used for bean generation
   */
  private Set<String> getAttributeNames(final ObjectClass objectClass)
  {
    return getAttributeNames(objectClass, new HashSet<ObjectClass>());
  }


  /**
   * Returns the attribute names to use for the supplied object class. This method is invoked recursively if superior
   * classes are included.
   *
   * @param  objectClass  to retrieve names from
   * @param  processed  object classes that have already been processed
   *
   * @return  set of all attribute names used for bean generation
   */
  private Set<String> getAttributeNames(final ObjectClass objectClass, final Set<ObjectClass> processed)
  {
    final Set<String> attributeNames = new HashSet<>();
    if (objectClass != null) {
      if (objectClass.getRequiredAttributes() != null) {
        attributeNames.addAll(Arrays.asList(objectClass.getRequiredAttributes()));
      }
      if (useOptionalAttributes && objectClass.getOptionalAttributes() != null) {
        attributeNames.addAll(Arrays.asList(objectClass.getOptionalAttributes()));
      }
      processed.add(objectClass);
      if (includeSuperiorClasses && objectClass.getSuperiorClasses() != null) {
        for (String oc : objectClass.getSuperiorClasses()) {
          final ObjectClass superiorOc = schema.getObjectClass(oc);
          if (!processed.contains(superiorOc)) {
            attributeNames.addAll(getAttributeNames(superiorOc, processed));
          }
        }
      }
    }
    return attributeNames;
  }


  /**
   * Formats the supplied name for use as a Java property.
   *
   * @param  name  to format
   *
   * @return  formatted name
   */
  private String formatAttributeName(final String name)
  {
    String formatted;
    if (name.contains("-")) {
      formatted = name.replace("-", "");
    } else {
      formatted = name;
    }
    return formatted;
  }


  /**
   * Returns whether the supplied attribute type has a matching OID or name in the excluded names list.
   *
   * @param  type  to compare
   *
   * @return  whether attribute type should be excluded from bean generation
   */
  private boolean isNameExcluded(final AttributeType type)
  {
    if (excludedNames != null && excludedNames.length > 0) {
      for (String excluded : excludedNames) {
        if (type.getOID().equals(excluded) || type.hasName(excluded)) {
          return true;
        }
      }
    }
    return false;
  }


  /**
   * Creates a class in the supplied package.
   *
   * @param  classPackage  to place the class in
   * @param  className  to create
   *
   * @return  class
   *
   * @throws  IllegalArgumentException  if the class already exists
   */
  protected JDefinedClass createClass(final String classPackage, final String className)
  {
    String fqClassName;
    if (!Character.isUpperCase(className.charAt(0))) {
      fqClassName = String.format(
        "%s.%s",
        classPackage,
        className.substring(0, 1).toUpperCase() + className.substring(1, className.length()));
    } else {
      fqClassName = String.format("%s.%s", classPackage, className);
    }

    try {
      return codeModel._class(fqClassName);
    } catch (JClassAlreadyExistsException e) {
      throw new IllegalArgumentException("Class already exists: " + fqClassName, e);
    }
  }


  /**
   * Creates the getter and setter methods on the supplied class for the supplied name.
   *
   * @param  clazz  to put getter and setter methods on
   * @param  name  of the property
   * @param  syntaxType  of the property
   * @param  multivalue  whether this property is a collection
   */
  protected void createMutators(
    final JDefinedClass clazz,
    final String name,
    final Class<?> syntaxType,
    final boolean multivalue)
  {
    final String upperName = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    if (multivalue) {
      final JClass detailClass = codeModel.ref(syntaxType);
      final JClass collectionClass = codeModel.ref(Collection.class);
      final JClass genericClass = collectionClass.narrow(detailClass);
      final JFieldVar field = clazz.field(JMod.PRIVATE, genericClass, name);
      final JMethod getterMethod = clazz.method(JMod.PUBLIC, genericClass, "get" + upperName);
      getterMethod.body()._return(field);

      final JMethod setterMethod = clazz.method(JMod.PUBLIC, Void.TYPE, "set" + upperName);
      setterMethod.param(genericClass, "c");
      setterMethod.body().assign(JExpr._this().ref(name), JExpr.ref("c"));

    } else {
      final JFieldVar field = clazz.field(JMod.PRIVATE, syntaxType, name);
      final JMethod getterMethod = clazz.method(JMod.PUBLIC, syntaxType, "get" + upperName);
      getterMethod.body()._return(field);

      final JMethod setterMethod = clazz.method(JMod.PUBLIC, Void.TYPE, "set" + upperName);
      setterMethod.param(syntaxType, "s");
      setterMethod.body().assign(JExpr._this().ref(name), JExpr.ref("s"));
    }
  }


  /**
   * Creates the hashCode method on the supplied class. Leverages {@link org.ldaptive.LdapUtils#computeHashCode(int,
   * Object...)}.
   *
   * @param  clazz  to put hashCode method on
   */
  private void createHashCode(final JDefinedClass clazz)
  {
    final JClass ldapUtilsClass = codeModel.ref(org.ldaptive.LdapUtils.class);
    final JInvocation computeHashCode = ldapUtilsClass.staticInvoke("computeHashCode");
    final JMethod hashCode = clazz.method(JMod.PUBLIC, int.class, "hashCode");
    hashCode.annotate(java.lang.Override.class);
    // CheckStyle:MagicNumber OFF
    computeHashCode.arg(JExpr.lit(7919));
    // CheckStyle:MagicNumber ON
    for (Map.Entry<String, JFieldVar> entry : clazz.fields().entrySet()) {
      computeHashCode.arg(JExpr._this().ref(entry.getValue()));
    }
    hashCode.body()._return(computeHashCode);
  }


  /**
   * Creates the equals method on the supplied class. Leverages {@link org.ldaptive.LdapUtils#areEqual(Object, Object)}.
   *
   * @param  clazz  to put equals method on
   */
  private void createEquals(final JDefinedClass clazz)
  {
    final JClass ldapUtilsClass = codeModel.ref(org.ldaptive.LdapUtils.class);
    final JInvocation areEqual = ldapUtilsClass.staticInvoke("areEqual");
    final JMethod equals = clazz.method(JMod.PUBLIC, boolean.class, "equals");
    equals.annotate(java.lang.Override.class);
    areEqual.arg(JExpr._this());
    areEqual.arg(equals.param(Object.class, "o"));
    equals.body()._return(areEqual);
  }


  /**
   * Creates the toString method on the supplied class. Creates a string that contains every property on the generated
   * bean.
   *
   * @param  clazz  to put toString method on
   */
  private void createToString(final JDefinedClass clazz)
  {
    final JClass stringClass = codeModel.ref(java.lang.String.class);
    final JInvocation format = stringClass.staticInvoke("format");
    final JMethod toString = clazz.method(JMod.PUBLIC, String.class, "toString");
    toString.annotate(java.lang.Override.class);

    final StringBuilder sb = new StringBuilder("[%s@%d::");
    for (Map.Entry<String, JFieldVar> entry : clazz.fields().entrySet()) {
      sb.append(entry.getKey()).append("=%s, ");
    }
    sb.setLength(sb.length() - 2);
    sb.append("]");
    format.arg(sb.toString());
    format.arg(JExpr._this().invoke("getClass").invoke("getName"));
    format.arg(JExpr._this().invoke("hashCode"));
    for (Map.Entry<String, JFieldVar> entry : clazz.fields().entrySet()) {
      format.arg(JExpr._this().ref(entry.getValue()));
    }
    toString.body()._return(format);
  }


  /**
   * Writes the generated classes to disk. Invokes {@link #write(String)} with ".".
   *
   * @throws  IOException  if the write fails
   */
  public void write()
    throws IOException
  {
    write(".");
  }


  /**
   * Writes the generated classes to disk at the supplied path.
   *
   * @param  path  to write the classes to
   *
   * @throws  IOException  if the write fails
   */
  public void write(final String path)
    throws IOException
  {
    final File f = new File(path);
    if (!f.exists()) {
      f.mkdirs();
    }
    codeModel.build(f);
  }


  /**
   * Provides command line access to a {@link BeanGenerator}. Expects two arguments:
   *
   * <ol>
   *   <li>path to a configuration property file</li>
   *   <li>target directory to write files to</li>
   * </ol>
   *
   * <p>A sample configuration property file looks like:</p>
   *
   * <pre>
     org.ldaptive.packageName=my.package.ldap.beans
     org.ldaptive.objectClasses=eduPerson
     org.ldaptive.nameMappings=c=countryName,l=localityName
     org.ldaptive.excludedNames=userPassword
     org.ldaptive.ldapUrl=ldap://directory.ldaptive.org
   * </pre>
   *
   * @param  args  command line arguments
   *
   * @throws  Exception  if any error occurs
   */
  public static void main(final String[] args)
    throws Exception
  {
    final String propsPath = args[0];
    final String targetPath = args[1];

    final BeanGenerator generator = new BeanGenerator();
    final BeanGeneratorPropertySource source = new BeanGeneratorPropertySource(generator, propsPath);
    source.initialize();
    generator.generate();
    generator.write(targetPath);
  }
}
