/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link DERPath} class.
 *
 * @author  Middleware Services
 */
public class DERPathTest
{

  /** @throws  Exception  On test failure. */
  @Test(groups = "asn1")
  public void testPushPop()
    throws Exception
  {
    final DERPath path = new DERPath("/SEQ");
    path.pushNode("OCTSTR");
    assertThat(path.toString()).isEqualTo("/SEQ/OCTSTR");
    assertThat(path.popNode()).isEqualTo("OCTSTR");
    path.pushNode("INT", 1);
    assertThat(path.toString()).isEqualTo("/SEQ/INT[1]");
    assertThat(path.popNode()).isEqualTo("INT[1]");
    assertThat(path.popNode()).isEqualTo("SEQ");
    assertThat(path.popNode()).isNull();
    assertThat(path.popNode()).isNull();
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "asn1")
  public void testToNode()
    throws Exception
  {
    assertThat(DERPath.toNode("SEQ").getName()).isEqualTo("SEQ");
    assertThat(DERPath.toNode("SEQ").getChildIndex()).isEqualTo(-1);
    assertThat(DERPath.toNode("APP(4)").getName()).isEqualTo("APP(4)");
    assertThat(DERPath.toNode("APP(4)").getChildIndex()).isEqualTo(-1);
    assertThat(DERPath.toNode("ENUM[0]").getName()).isEqualTo("ENUM");
    assertThat(DERPath.toNode("ENUM[0]").getChildIndex()).isEqualTo(0);
    assertThat(DERPath.toNode("OCTSTR[1]").getName()).isEqualTo("OCTSTR");
    assertThat(DERPath.toNode("OCTSTR[1]").getChildIndex()).isEqualTo(1);
    assertThat(DERPath.toNode("INT[3]").getName()).isEqualTo("INT");
    assertThat(DERPath.toNode("INT[3]").getChildIndex()).isEqualTo(3);
    assertThat(DERPath.toNode("CTX(0)").getName()).isEqualTo("CTX(0)");
    assertThat(DERPath.toNode("CTX(0)").getChildIndex()).isEqualTo(-1);
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "asn1")
  public void testHashCode()
    throws Exception
  {
    assertThat(new DERPath("SEQ").hashCode()).isEqualTo(new DERPath("SEQ").hashCode());
    assertThat(new DERPath("ENUM").hashCode()).isNotEqualTo(new DERPath("INT").hashCode());
    assertThat(new DERPath("APP(0)").pushNode("CTX(0)").hashCode())
      .isEqualTo(new DERPath("APP(0)").pushNode("CTX(0)").hashCode());
    assertThat(new DERPath("APP(0)").pushNode("CTX(0)").hashCode())
      .isNotEqualTo(new DERPath("CTX(0)").pushNode("APP(0)").hashCode());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "asn1")
  public void testEquals()
    throws Exception
  {
    assertThat(new DERPath("/SEQ[0]/OCTSTR[1]")).isEqualTo(new DERPath("/SEQ[0]/OCTSTR[1]"));
    assertThat(new DERPath("/SEQ[0]/OCTSTR[1]")).isNotEqualTo(new DERPath("/SEQ[0]/OCTSTR[2]"));
    assertThat(new DERPath("/SEQ[0]/OCTSTR[1]")).isNotEqualTo(new DERPath("/SEQ/OCTSTR[2]"));
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "asn1")
  public void testHashcode()
    throws Exception
  {
    assertThat(new DERPath("/SEQ[0]/OCTSTR[1]").hashCode()).isEqualTo(new DERPath("/SEQ[0]/OCTSTR[1]").hashCode());
    assertThat(new DERPath("/SEQ[0]/OCTSTR[1]").hashCode() == new DERPath("/SEQ[0]/OCTSTR[2]").hashCode()).isFalse();
    assertThat(new DERPath("/SEQ[0]/OCTSTR[1]").hashCode() == new DERPath("/SEQ/OCTSTR[2]").hashCode()).isFalse();
  }


  /**
   * DER path test data.
   *
   * @return  der paths
   */
  @DataProvider(name = "paths")
  public Object[][] createTestParams()
  {
    return
      new Object[][] {
        new Object[] {
          "/SET/SEQ/INT[1]",
          "/SET/SEQ/INT[1]",
        },
        new Object[] {
          "/SET/CTX(0)/INT",
          "/SET/CTX(0)/INT",
        },
        new Object[] {
          "/SET/APP(0)[1]/CTX(1)",
          "/SET/APP(0)[1]/CTX(1)",
        },
      };
  }


  /**
   * @param  testPath  to test
   * @param  expected  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "asn1", dataProvider = "paths")
  public void testToString(final String testPath, final String expected)
    throws Exception
  {
    assertThat(new DERPath(testPath).toString()).isEqualTo(expected);
  }


  /**
   * @param  testPath  to test
   * @param  expected  to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "asn1", dataProvider = "paths")
  public void testEquals(final String testPath, final String expected)
    throws Exception
  {
    assertThat(new DERPath(testPath)).isEqualTo(new DERPath(expected));
  }


  @Test
  public void testEqualsContract()
  {
    EqualsVerifier.forClass(DERPath.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .verify();
  }
}
