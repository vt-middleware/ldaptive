/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Bean for a DIT content rule schema element.
 *
 * <pre>
   DITStructureRuleDescription = LPAREN WSP
     ruleid                     ; rule identifier
     [ SP "NAME" SP qdescrs ]   ; short names (descriptors)
     [ SP "DESC" SP qdstring ]  ; description
     [ SP "OBSOLETE" ]          ; not active
     SP "FORM" SP oid           ; NameForm
     [ SP "SUP" ruleids ]       ; superior rules
     extensions WSP RPAREN      ; extensions
 * </pre>
 *
 * @author  Middleware Services
 */
public final class DITStructureRule extends AbstractNamedSchemaElement
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1153;

  /** ID. */
  private final int id;

  /** Name form. */
  private String nameForm;

  /** Superior rules. */
  private int[] superiorRules;


  /**
   * Creates a new DIT structure rule.
   *
   * @param  i  id
   */
  public DITStructureRule(final int i)
  {
    id = i;
  }


  /**
   * Creates a new DIT structure rule.
   *
   * @param  id  id
   * @param  names  names
   * @param  description  description
   * @param  obsolete  obsolete
   * @param  nameForm  name form
   * @param  superiorRules  superior rules
   * @param  extensions  extensions
   */
  // CheckStyle:ParameterNumber|HiddenField OFF
  public DITStructureRule(
    final int id,
    final String[] names,
    final String description,
    final boolean obsolete,
    final String nameForm,
    final int[] superiorRules,
    final Extensions extensions)
  {
    this(id);
    setNames(names);
    setDescription(description);
    setObsolete(obsolete);
    setNameForm(nameForm);
    setSuperiorRules(superiorRules);
    setExtensions(extensions);
  }
  // CheckStyle:ParameterNumber|HiddenField ON


  /**
   * Returns the id.
   *
   * @return  id
   */
  public int getID()
  {
    return id;
  }


  /**
   * Returns the name form.
   *
   * @return  name form
   */
  public String getNameForm()
  {
    return nameForm;
  }


  /**
   * Sets the name form.
   *
   * @param  s  name form
   */
  public void setNameForm(final String s)
  {
    assertMutable();
    nameForm = s;
  }


  /**
   * Returns the superior rules.
   *
   * @return  superior rules
   */
  public int[] getSuperiorRules()
  {
    return superiorRules;
  }


  /**
   * Sets the superior rules.
   *
   * @param  i  superior rules
   */
  public void setSuperiorRules(final int[] i)
  {
    assertMutable();
    superiorRules = i;
  }


  /**
   * Parses the supplied definition string and creates an initialized DIT structure rule.
   *
   * @param  definition  to parse
   *
   * @return  DIT structure rule
   *
   * @throws  SchemaParseException  if the supplied definition is invalid
   */
  public static DITStructureRule parse(final String definition)
    throws SchemaParseException
  {
    return SchemaParser.parse(DITStructureRule.class, definition);
  }


  @Override
  public String format()
  {
    final StringBuilder sb = new StringBuilder("( ");
    sb.append(id).append(" ");
    if (getNames() != null && getNames().length > 0) {
      sb.append("NAME ").append(SchemaUtils.formatDescriptors(getNames()));
    }
    if (getDescription() != null) {
      sb.append("DESC ").append(SchemaUtils.formatDescriptors(getDescription()));
    }
    if (isObsolete()) {
      sb.append("OBSOLETE ");
    }
    if (nameForm != null) {
      sb.append("FORM ").append(nameForm).append(" ");
    }
    if (superiorRules != null && superiorRules.length > 0) {
      sb.append("SUP ").append(SchemaUtils.formatNumbers(superiorRules));
    }
    if (getExtensions() != null) {
      sb.append(getExtensions().format());
    }
    sb.append(")");
    return sb.toString();
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof DITStructureRule) {
      final DITStructureRule v = (DITStructureRule) o;
      return LdapUtils.areEqual(id, v.id) &&
             LdapUtils.areEqual(getNames(), v.getNames()) &&
             LdapUtils.areEqual(getDescription(), v.getDescription()) &&
             LdapUtils.areEqual(isObsolete(), v.isObsolete()) &&
             LdapUtils.areEqual(nameForm, v.nameForm) &&
             LdapUtils.areEqual(superiorRules, v.superiorRules) &&
             LdapUtils.areEqual(getExtensions(), v.getExtensions());
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        id,
        getNames(),
        getDescription(),
        isObsolete(),
        nameForm,
        superiorRules,
        getExtensions());
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "id=" + id + ", " +
      "names=" + Arrays.toString(getNames()) + ", " +
      "description=" + getDescription() + ", " +
      "obsolete=" + isObsolete() + ", " +
      "nameForm=" + nameForm + ", " +
      "superiorRules=" + Arrays.toString(superiorRules) + ", " +
      "extensions=" + getExtensions() + "]";
  }


  /** Parses a DIT structure rule definition using a char buffer. */
  public static class DefaultDefinitionFunction extends AbstractDefaultDefinitionFunction<DITStructureRule>
  {


    @Override
    public DITStructureRule parse(final String definition)
      throws SchemaParseException
    {
      final CharBuffer buffer = validate(definition);
      skipSpaces(buffer);
      final DITStructureRule dsr = new DITStructureRule(readRuleID(buffer));
      final Extensions exts = new Extensions();
      while (buffer.hasRemaining()) {
        skipSpaces(buffer);
        final String token = readUntilSpace(buffer);
        skipSpaces(buffer);
        switch (token) {
        case "NAME":
          dsr.setNames(readQDStrings(buffer));
          break;
        case "DESC":
          dsr.setDescription(readQDString(buffer));
          break;
        case "OBSOLETE":
          dsr.setObsolete(true);
          break;
        case "FORM":
          dsr.setNameForm(readUntilSpace(buffer));
          break;
        case "SUP":
          dsr.setSuperiorRules(readRuleIDs(buffer));
          break;
        case "":
          break;
        default:
          if (!token.startsWith("X-")) {
            throw new SchemaParseException(
              "Definition '" + definition + "' contains invalid extension '" + token + "'");
          }
          skipSpaces(buffer);
          exts.addExtension(token, List.of(readQDStrings(buffer)));
          break;
        }
      }
      if (!exts.isEmpty()) {
        dsr.setExtensions(exts);
      }
      return dsr;
    }
  }


  /** Parses a DIT structure rule definition using a regular expression. */
  public static class RegexDefinitionFunction extends AbstractRegexDefinitionFunction<DITStructureRule>
  {

    /** Pattern to match definitions. */
    private static final Pattern DEFINITION_PATTERN = Pattern.compile(
      WSP_REGEX + "\\(" +
        WSP_REGEX + "(\\p{Digit}+)" +
        WSP_REGEX + "(?:NAME" + ONE_WSP_REGEX + "(?:'([^']+)'|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(?:DESC" + ONE_WSP_REGEX + "'([^']*)')?" +
        WSP_REGEX + "(OBSOLETE)?" +
        WSP_REGEX + "(?:FORM" + ONE_WSP_REGEX + "(" + NO_WSP_REGEX + "))?" +
        WSP_REGEX + "(?:SUP" + ONE_WSP_REGEX + "(?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(?:(X-[^ ]+.*))?" +
        WSP_REGEX + "\\)" + WSP_REGEX);


    @Override
    public DITStructureRule parse(final String definition)
      throws SchemaParseException
    {
      final Matcher m = DEFINITION_PATTERN.matcher(definition);
      if (!m.matches()) {
        throw new SchemaParseException("Invalid DIT structure rule definition: " + definition);
      }

      final DITStructureRule dsrd = new DITStructureRule(Integer.parseInt(m.group(1).trim()));

      // CheckStyle:MagicNumber OFF
      // parse names
      if (m.group(2) != null) {
        dsrd.setNames(SchemaUtils.parseDescriptors(m.group(2).trim()));
      } else if (m.group(3) != null) {
        dsrd.setNames(SchemaUtils.parseDescriptors(m.group(3).trim()));
      }

      dsrd.setDescription(m.group(4) != null ? m.group(4).trim() : null);
      dsrd.setObsolete(m.group(5) != null);
      dsrd.setNameForm(m.group(6) != null ? m.group(6).trim() : null);

      // parse superior rules
      if (m.group(7) != null) {
        dsrd.setSuperiorRules(SchemaUtils.parseNumbers(m.group(7).trim()));
      } else if (m.group(8) != null) {
        dsrd.setSuperiorRules(SchemaUtils.parseNumbers(m.group(8).trim()));
      }

      // parse extensions
      if (m.group(9) != null) {
        dsrd.setExtensions(parseExtensions(m.group(9).trim()));
      }
      return dsrd;
      // CheckStyle:MagicNumber ON
    }
  }
}
