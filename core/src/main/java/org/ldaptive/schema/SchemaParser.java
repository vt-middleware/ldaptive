/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.lang.reflect.Constructor;
import org.ldaptive.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a {@link SchemaFunction} and exposes a convenience static method for parsing schema definitions. The
 * schema function used by this class can be set using the system property {@link #SCHEMA_FUNCTION_PROPERTY}.
 *
 * @author  Middleware Services
 */
public final class SchemaParser
{

  /** Schema function system property. */
  private static final String SCHEMA_FUNCTION_PROPERTY = "org.ldaptive.schema.function";

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaParser.class);

  /** Default schema function. */
  private static final SchemaFunction SCHEMA_FUNCTION = getSchemaFunction();

  /** Custom schema parser constructor. */
  private static final Constructor<?> SCHEMA_FUNCTION_CONSTRUCTOR;

  static {
    // Initialize a custom attribute type function if a system property is found
    SCHEMA_FUNCTION_CONSTRUCTOR = LdapUtils.createConstructorFromProperty(SCHEMA_FUNCTION_PROPERTY);
    if (SCHEMA_FUNCTION_CONSTRUCTOR != null) {
      LOGGER.info("Setting schema definition function to {}", SCHEMA_FUNCTION_CONSTRUCTOR);
    }
  }


  /** Default constructor. */
  private SchemaParser() {}


  /**
   * The {@link #SCHEMA_FUNCTION_PROPERTY} property is checked and that class is loaded if provided. Otherwise, the
   * {@link DefaultSchemaFunction} is returned.
   *
   * @return  default filter function
   */
  public static SchemaFunction getSchemaFunction()
  {
    if (SCHEMA_FUNCTION_CONSTRUCTOR != null) {
      try {
        return (SchemaFunction) SCHEMA_FUNCTION_CONSTRUCTOR.newInstance();
      } catch (Exception e) {
        LOGGER.error("Error creating new schema function instance with {}", SCHEMA_FUNCTION_CONSTRUCTOR, e);
        throw new IllegalStateException(e);
      }
    }
    return new DefaultSchemaFunction();
  }


  /**
   * Parses the supplied string representation of a schema element.
   *
   * @param  <T>  type of schema element
   * @param  type  of schema element
   * @param  definition  to parse
   *
   * @return  parsed schema element
   *
   * @throws  SchemaParseException  if definition is invalid
   */
  public static <T extends SchemaElement> T parse(final Class<T> type, final String definition)
    throws SchemaParseException
  {
    return SCHEMA_FUNCTION.parse(type, definition);
  }


  /** CharBuffer based implementation for schema functions. */
  public static class DefaultSchemaFunction implements SchemaFunction
  {

    /** Default syntax function. */
    private static final Syntax.DefaultDefinitionFunction SYNTAX_FUNCTION = new Syntax.DefaultDefinitionFunction();

    /** Default attribute type function. */
    private static final AttributeType.DefaultDefinitionFunction ATTRIBUTE_TYPE_FUNCTION =
      new AttributeType.DefaultDefinitionFunction();

    /** Default DIT structure rule function. */
    private static final DITStructureRule.DefaultDefinitionFunction DIT_STRUCTURE_RULE_FUNCTION =
      new DITStructureRule.DefaultDefinitionFunction();

    /** Default matching rule use function. */
    private static final MatchingRuleUse.DefaultDefinitionFunction MATCHING_RULE_USE_FUNCTION =
      new MatchingRuleUse.DefaultDefinitionFunction();

    /** Default object class function. */
    private static final ObjectClass.DefaultDefinitionFunction OBJECT_CLASS_FUNCTION =
      new ObjectClass.DefaultDefinitionFunction();

    /** Default name form function. */
    private static final NameForm.DefaultDefinitionFunction NAME_FORM_FUNCTION =
      new NameForm.DefaultDefinitionFunction();

    /** Default DIT content rule function. */
    private static final DITContentRule.DefaultDefinitionFunction DIT_CONTENT_RULE_FUNCTION =
      new DITContentRule.DefaultDefinitionFunction();

    /** Default DIT matching rule function. */
    private static final MatchingRule.DefaultDefinitionFunction MATCHING_RULE_FUNCTION =
      new MatchingRule.DefaultDefinitionFunction();


    @Override
    @SuppressWarnings("unchecked")
    public <T extends SchemaElement> T parse(final Class<? extends T> type, final String definition)
      throws SchemaParseException
    {
      final T element;
      if (Syntax.class == type) {
        element = (T) SYNTAX_FUNCTION.parse(definition);
      } else if (AttributeType.class == type) {
        element = (T) ATTRIBUTE_TYPE_FUNCTION.parse(definition);
      } else if (DITStructureRule.class == type) {
        element = (T) DIT_STRUCTURE_RULE_FUNCTION.parse(definition);
      } else if (MatchingRuleUse.class == type) {
        element = (T) MATCHING_RULE_USE_FUNCTION.parse(definition);
      } else if (ObjectClass.class == type) {
        element = (T) OBJECT_CLASS_FUNCTION.parse(definition);
      } else if (NameForm.class == type) {
        element = (T) NAME_FORM_FUNCTION.parse(definition);
      } else if (DITContentRule.class == type) {
        element = (T) DIT_CONTENT_RULE_FUNCTION.parse(definition);
      } else if (MatchingRule.class == type) {
        element = (T) MATCHING_RULE_FUNCTION.parse(definition);
      } else {
        throw new IllegalStateException("Unknown schema element " + type);
      }
      return element;
    }
  }


  /** Regular expression based implementation for schema functions. */
  public static class RegexSchemaFunction implements SchemaFunction
  {

    /** Regex syntax function. */
    private static final Syntax.RegexDefinitionFunction SYNTAX_FUNCTION = new Syntax.RegexDefinitionFunction();

    /** Regex attribute type function. */
    private static final AttributeType.RegexDefinitionFunction ATTRIBUTE_TYPE_FUNCTION =
      new AttributeType.RegexDefinitionFunction();

    /** Regex DIT structure rule function. */
    private static final DITStructureRule.RegexDefinitionFunction DIT_STRUCTURE_RULE_FUNCTION =
      new DITStructureRule.RegexDefinitionFunction();

    /** Regex matching rule use function. */
    private static final MatchingRuleUse.RegexDefinitionFunction MATCHING_RULE_USE_FUNCTION =
      new MatchingRuleUse.RegexDefinitionFunction();

    /** Regex object class function. */
    private static final ObjectClass.RegexDefinitionFunction OBJECT_CLASS_FUNCTION =
      new ObjectClass.RegexDefinitionFunction();

    /** Regex name form function. */
    private static final NameForm.RegexDefinitionFunction NAME_FORM_FUNCTION = new NameForm.RegexDefinitionFunction();

    /** Regex DIT content rule function. */
    private static final DITContentRule.RegexDefinitionFunction DIT_CONTENT_RULE_FUNCTION =
      new DITContentRule.RegexDefinitionFunction();

    /** Regex DIT matching rule function. */
    private static final MatchingRule.RegexDefinitionFunction MATCHING_RULE_FUNCTION =
      new MatchingRule.RegexDefinitionFunction();


    @Override
    @SuppressWarnings("unchecked")
    public <T extends SchemaElement> T parse(final Class<? extends T> type, final String definition)
      throws SchemaParseException
    {
      final T element;
      if (Syntax.class == type) {
        element = (T) SYNTAX_FUNCTION.parse(definition);
      } else if (AttributeType.class == type) {
        element = (T) ATTRIBUTE_TYPE_FUNCTION.parse(definition);
      } else if (DITStructureRule.class == type) {
        element = (T) DIT_STRUCTURE_RULE_FUNCTION.parse(definition);
      } else if (MatchingRuleUse.class == type) {
        element = (T) MATCHING_RULE_USE_FUNCTION.parse(definition);
      } else if (ObjectClass.class == type) {
        element = (T) OBJECT_CLASS_FUNCTION.parse(definition);
      } else if (NameForm.class == type) {
        element = (T) NAME_FORM_FUNCTION.parse(definition);
      } else if (DITContentRule.class == type) {
        element = (T) DIT_CONTENT_RULE_FUNCTION.parse(definition);
      } else if (MatchingRule.class == type) {
        element = (T) MATCHING_RULE_FUNCTION.parse(definition);
      } else {
        throw new IllegalStateException("Unknown schema element " + type);
      }
      return element;
    }
  }
}
