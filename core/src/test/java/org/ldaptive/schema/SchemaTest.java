/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.util.List;
import java.util.Set;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Schema}.
 *
 * @author  Middleware Services
 */
public class SchemaTest
{


  /**
   * Schema test data.
   *
   * @return  custom objects
   *
   * @throws  Exception  if a schema cannot be read
   */
  @DataProvider(name = "schemas")
  public Object[][] createSchemas()
    throws Exception
  {
    return
      new Object[][] {
        new Object[] {
          new Schema(
            Set.of(
              AttributeType.parse("( 2.5.4.42 NAME ( 'givenName' 'gn' ) DESC 'RFC2256: first name' SUP name )"),
              AttributeType.parse("( 2.5.4.41 NAME 'name' DESC 'RFC4519: common supertype of name attributes' " +
                "EQUALITY caseIgnoreMatch SUBSTR caseIgnoreSubstringsMatch " +
                "SYNTAX 1.3.6.1.4.1.1466.115.121.1.15{32768} )"),
              AttributeType.parse("( 2.5.4.4 NAME ( 'sn' 'surname' ) SYNTAX 1.3.6.1.4.1.1466.115.121.1.15{64} " +
                "X-NDS_NAME 'Surname' X-NDS_LOWER_BOUND '1' X-NDS_UPPER_BOUND '64' X-NDS_PUBLIC_READ '1' " +
                "X-NDS_NONREMOVABLE '1' )")),
            Set.of(
              DITContentRule.parse("( 2.16.840.1.113730.3.2.2 NAME 'inetOrgPerson-content-rule' " +
                "AUX strongAuthenticationUser MUST uid MAY c NOT ( telexNumber $ telexTerminalIdentifier ) )")),
            Set.of(
              DITStructureRule.parse("( 1 NAME 'uddiBusinessEntityStructureRule' FORM uddiBusinessEntityNameForm )"),
              DITStructureRule.parse("( 2 NAME 'uddiContactStructureRule' FORM uddiContactNameForm SUP ( 1 ) )"),
              DITStructureRule.parse("( 3 NAME 'uddiNameStructureRule' FORM uddiNameForm SUP ( 2 ) )")),
            Set.of(
              Syntax.parse("( 1.3.6.1.4.1.1466.115.121.1.4 DESC 'Audio' X-NOT-HUMAN-READABLE 'TRUE' )"),
              Syntax.parse("( 1.3.6.1.4.1.1466.115.121.1.5 DESC 'Binary' X-NOT-HUMAN-READABLE 'TRUE' )"),
              Syntax.parse("( 1.3.6.1.4.1.1466.115.121.1.6 DESC 'Bit String' )"),
              Syntax.parse("( 1.3.6.1.4.1.1466.115.121.1.7 DESC 'Boolean' )")),
            Set.of(
              MatchingRule.parse("( 1.3.6.1.1.16.2 NAME 'UUIDMatch' SYNTAX 1.3.6.1.1.16.1 )"),
              MatchingRule.parse("( 2.5.13.4 NAME 'caseIgnoreSubstringsMatch' SYNTAX 1.3.6.1.4.1.1466.115.121.1.58 )"),
              MatchingRule.parse("( 2.5.13.2 NAME 'caseIgnoreMatch' SYNTAX 1.3.6.1.4.1.1466.115.121.1.15 )")),
            Set.of(
              MatchingRuleUse.parse(
                "( 2.5.13.28 NAME 'generalizedTimeOrderingMatch' APPLIES ( createTimestamp $ modifyTimestamp ) )"),
              MatchingRuleUse.parse(
                "( 2.5.13.27 NAME 'generalizedTimeMatch' APPLIES ( createTimestamp $ modifyTimestamp ) )"),
              MatchingRuleUse.parse(
                "( 2.5.13.17 NAME 'octetStringMatch' APPLIES ( userPassword $ olcDbCryptKey $ networkPassword ) )")),
            Set.of(
              NameForm.parse("( 1.3.6.1.1.10.15.1 NAME 'uddiBusinessEntityNameForm' OC uddiBusinessEntity " +
                "MUST ( uddiBusinessKey ) )")),
            Set.of(
              ObjectClass.parse("( 2.5.6.7 NAME 'organizationalPerson' DESC 'RFC2256: an organizational person' " +
                "SUP person STRUCTURAL MAY ( title $ x121Address $ registeredAddress $ destinationIndicator $ " +
                "preferredDeliveryMethod $ telexNumber $ teletexTerminalIdentifier $ telephoneNumber $ " +
                "internationaliSDNNumber $ facsimileTelephoneNumber $ street $ postOfficeBox $ postalCode $ " +
                "postalAddress $ physicalDeliveryOfficeName $ ou $ st $ l ) )"),
              ObjectClass.parse("( 1.3.6.1.4.1.4203.1.4.5 NAME 'OpenLDAPperson' DESC 'OpenLDAP Person' " +
                  "SUP ( pilotPerson $ inetOrgPerson ) STRUCTURAL MUST ( uid $ cn ) " +
                  "MAY (givenName $ labeledURI $ o ) )"),
              ObjectClass.parse("( 2.5.6.6 NAME 'person' DESC 'RFC2256: a person' SUP top STRUCTURAL " +
                  "MUST ( sn $ cn ) MAY ( userPassword $ telephoneNumber $ seeAlso $ description ) )"),
              ObjectClass.parse("( 2.5.6.0 NAME 'top' DESC 'top of the superclass chain' ABSTRACT MUST objectClass )"),
              ObjectClass.parse("( 0.9.2342.19200300.100.4.4 NAME ( 'pilotPerson' 'newPilotPerson' ) SUP person " +
                "STRUCTURAL MAY ( userid $ textEncodedORAddress $ rfc822Mailbox $ favouriteDrink $ roomNumber $ " +
                "userClass $ homeTelephoneNumber $ homePostalAddress $ secretary $ personalTitle $ " +
                "preferredDeliveryMethod $ businessCategory $ janetMailbox $ otherMailbox $ mobileTelephoneNumber $ " +
                "pagerTelephoneNumber $ organizationalStatus $ mailPreferenceOption $ personalSignature ) )"),
              ObjectClass.parse("( 2.16.840.1.113730.3.2.2 NAME 'inetOrgPerson' " +
                "DESC 'RFC2798: Internet Organizational Person' SUP organizationalPerson " +
                "STRUCTURAL MAY ( audio $ businessCategory $ carLicense $ departmentNumber $ displayName $ " +
                "employeeNumber $ employeeType $ givenName $ homePhone $ homePostalAddress $ initials $ jpegPhoto $ " +
                "labeledURI $ mail $ manager $ mobile $ o $ pager $ photo $ roomNumber $ secretary $ uid $ " +
                "userCertificate $ x500uniqueIdentifier $ preferredLanguage $ userSMIMECertificate $ userPKCS12 ) )"))
          ),
        },
      };
  }


  /**
   * @param  schema  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "schemas")
  public void notEmpty(final Schema schema)
    throws Exception
  {
    assertThat(schema).isNotNull();
    assertThat(schema.getAttributeTypes()).isNotEmpty();
    assertThat(schema.getDITContentRules()).isNotEmpty();
    assertThat(schema.getDITStructureRules()).isNotEmpty();
    assertThat(schema.getSyntaxes()).isNotEmpty();
    assertThat(schema.getMatchingRules()).isNotEmpty();
    assertThat(schema.getMatchingRuleUses()).isNotEmpty();
    assertThat(schema.getNameForms()).isNotEmpty();
    assertThat(schema.getObjectClasses()).isNotEmpty();
  }


  /**
   * @param  schema  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "schemas")
  public void attributeType(final Schema schema)
    throws Exception
  {
    assertThat(schema).isNotNull();
    assertThat(schema.getAttributeTypes()).hasSize(3);
    assertThat(schema.getAttributeType("2.5.4.42").getName()).isEqualTo("givenName");
    assertThat(schema.getAttributeType("2.5.4.42").getNames()).isEqualTo(new String[] {"givenName", "gn"});
    assertThat(schema.getAttributeType("givenName").getOID()).isEqualTo("2.5.4.42");
    assertThat(schema.getAttributeType("gn").getOID()).isEqualTo("2.5.4.42");
    assertThat(schema.getAttributeType("givenname")).isEqualTo(schema.getAttributeType("gn"));
    assertThat(schema.getAttributeType("2.5.4.4").getName()).isEqualTo("sn");
    assertThat(schema.getAttributeType("2.5.4.4").getNames()).isEqualTo(new String[] {"sn", "surname"});
    assertThat(schema.getAttributeType("surName").getOID()).isEqualTo("2.5.4.4");
    assertThat(schema.getAttributeType("sn").getOID()).isEqualTo("2.5.4.4");
    assertThat(schema.getAttributeType("surname")).isEqualTo(schema.getAttributeType("sn"));
    assertThat(schema.getAttributeType("2.5.4.43")).isNull();
  }


  /**
   * @param  schema  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "schemas")
  public void ditContentRule(final Schema schema)
    throws Exception
  {
    assertThat(schema).isNotNull();
    assertThat(schema.getDITContentRules()).hasSize(1);
    assertThat(schema.getDITContentRule("2.16.840.1.113730.3.2.2").getName()).isEqualTo("inetOrgPerson-content-rule");
    assertThat(schema.getDITContentRule("2.16.840.1.113730.3.2.2").getNames())
      .isEqualTo(new String[] {"inetOrgPerson-content-rule"});
    assertThat(schema.getDITContentRule("2.16.840.1.113730.3.2.2"))
      .isEqualTo(schema.getDITContentRule("inetOrgPerson-content-rule"));
    assertThat(schema.getDITContentRule("2.16.840.1.113730.3.2.2").getAuxiliaryClasses())
      .isEqualTo(new String[] {"strongAuthenticationUser"});
    assertThat(schema.getDITContentRule("2.16.840.1.113730.3.2.2").getDescription()).isNull();
    assertThat(schema.getDITContentRule("inetorgperson-content-rule").getRequiredAttributes())
      .isEqualTo(new String[] {"uid"});
    assertThat(schema.getDITContentRule("inetorgperson-content-rule").getRestrictedAttributes())
      .isEqualTo(new String[] {"telexNumber", "telexTerminalIdentifier"});
    assertThat(schema.getDITContentRule("2.16.840.1.113730.3.2.222")).isNull();
  }


  /**
   * @param  schema  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "schemas")
  public void ditStructureRule(final Schema schema)
    throws Exception
  {
    assertThat(schema).isNotNull();
    assertThat(schema.getDITStructureRules()).hasSize(3);
    assertThat(schema.getDITStructureRule(1).getName()).isEqualTo("uddiBusinessEntityStructureRule");
    assertThat(schema.getDITStructureRule("uddiContactStructureRule").getID()).isEqualTo(2);
    assertThat(schema.getDITStructureRule(1)).isEqualTo(schema.getDITStructureRule("uddiBusinessEntityStructureRule"));
    assertThat(schema.getDITStructureRule("uddicontactstructurerule").getNameForm()).isEqualTo("uddiContactNameForm");
    assertThat(schema.getDITStructureRule("uddicontactstructurerule").getSuperiorRules()).isEqualTo(new int[] {1});
    assertThat(schema.getDITStructureRule(10)).isNull();
  }


  /**
   * @param  schema  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "schemas")
  public void syntax(final Schema schema)
    throws Exception
  {
    assertThat(schema).isNotNull();
    assertThat(schema.getSyntaxes()).hasSize(4);
    assertThat(schema.getSyntax("1.3.6.1.4.1.1466.115.121.1.5").getDescription()).isEqualTo("Binary");
    assertThat(schema.getSyntax("binary")).isNull();
    assertThat(schema.getSyntax("1.3.6.1.4.1.1466.115.121.1.10")).isNull();
  }


  /**
   * @param  schema  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "schemas")
  public void matchingRule(final Schema schema)
    throws Exception
  {
    assertThat(schema).isNotNull();
    assertThat(schema.getMatchingRules()).hasSize(3);
    assertThat(schema.getMatchingRule("2.5.13.4").getName()).isEqualTo("caseIgnoreSubstringsMatch");
    assertThat(schema.getMatchingRule("2.5.13.4").getNames()).isEqualTo(new String[] {"caseIgnoreSubstringsMatch"});
    assertThat(schema.getMatchingRule("caseIgnoreSubstringsMatch").getOID()).isEqualTo("2.5.13.4");
    assertThat(schema.getMatchingRule("caseignoresubstringsmatch")).isEqualTo(schema.getMatchingRule("2.5.13.4"));
    assertThat(schema.getMatchingRule("2.5.13.4").getSyntaxOID()).isEqualTo("1.3.6.1.4.1.1466.115.121.1.58");
    assertThat(schema.getMatchingRule("2.5.13.13")).isNull();
  }


  /**
   * @param  schema  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "schemas")
  public void matchingRuleUse(final Schema schema)
    throws Exception
  {
    assertThat(schema).isNotNull();
    assertThat(schema.getMatchingRuleUses()).hasSize(3);
    assertThat(schema.getMatchingRuleUse("2.5.13.27").getName()).isEqualTo("generalizedTimeMatch");
    assertThat(schema.getMatchingRuleUse("2.5.13.27").getNames()).isEqualTo(new String[] {"generalizedTimeMatch"});
    assertThat(schema.getMatchingRuleUse("generalizedTimeMatch").getOID()).isEqualTo("2.5.13.27");
    assertThat(schema.getMatchingRuleUse("generalizedtimematch")).isEqualTo(schema.getMatchingRuleUse("2.5.13.27"));
    assertThat(schema.getMatchingRuleUse("2.5.13.27").getAppliesAttributeTypes())
      .isEqualTo(new String[] {"createTimestamp", "modifyTimestamp"});
    assertThat(schema.getMatchingRuleUse("2.5.13.30")).isNull();
  }


  /**
   * @param  schema  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "schemas")
  public void nameForm(final Schema schema)
    throws Exception
  {
    assertThat(schema).isNotNull();
    assertThat(schema.getNameForms()).hasSize(1);
    assertThat(schema.getNameForm("1.3.6.1.1.10.15.1").getName()).isEqualTo("uddiBusinessEntityNameForm");
    assertThat(schema.getNameForm("1.3.6.1.1.10.15.1").getNames())
      .isEqualTo(new String[] {"uddiBusinessEntityNameForm"});
    assertThat(schema.getNameForm("uddiBusinessEntityNameForm").getOID()).isEqualTo("1.3.6.1.1.10.15.1");
    assertThat(schema.getNameForm("uddiBusinessentitynameform")).isEqualTo(schema.getNameForm("1.3.6.1.1.10.15.1"));
    assertThat(schema.getNameForm("1.3.6.1.1.10.15.1").getStructuralClass()).isEqualTo("uddiBusinessEntity");
    assertThat(schema.getNameForm("1.3.6.1.1.10.15.2")).isNull();
  }


  /**
   * @param  schema  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema", dataProvider = "schemas")
  public void objectClass(final Schema schema)
    throws Exception
  {
    assertThat(schema).isNotNull();
    assertThat(schema.getObjectClasses()).hasSize(6);
    assertThat(schema.getObjectClass("1.3.6.1.4.1.4203.1.4.5").getName()).isEqualTo("OpenLDAPperson");
    assertThat(schema.getObjectClass("1.3.6.1.4.1.4203.1.4.5").getNames()).isEqualTo(new String[] {"OpenLDAPperson"});
    assertThat(schema.getObjectClass("OpenLDAPperson").getOID()).isEqualTo("1.3.6.1.4.1.4203.1.4.5");
    assertThat(schema.getObjectClass("openldapperson")).isEqualTo(schema.getObjectClass("1.3.6.1.4.1.4203.1.4.5"));
    assertThat(schema.getObjectClass("1.3.6.1.4.1.4203.1.4.5").getObjectClassType())
      .isEqualTo(ObjectClassType.STRUCTURAL);
    assertThat(schema.getObjectClass("1.3.6.1.4.1.4203.1.4.10")).isNull();
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "schema")
  public void circularObjectClasses()
    throws Exception
  {
    final ObjectClass oc1 = new ObjectClass("1.1.1");
    oc1.setNames(new String[] {"one"});
    oc1.setSuperiorClasses(new String[] {"two"});
    final ObjectClass oc2 = new ObjectClass("2.2.2");
    oc2.setNames(new String[] {"two"});
    oc2.setSuperiorClasses(new String[] {"one"});
    final Schema schema = new Schema();
    schema.setObjectClasses(List.of(oc1, oc2));
    assertThat(schema).isNotNull();
    assertThat(schema.getObjectClasses()).hasSize(2);
  }
}
