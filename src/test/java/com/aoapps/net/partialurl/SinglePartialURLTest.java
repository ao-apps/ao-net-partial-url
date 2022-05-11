/*
 * ao-net-partial-url - Matches and resolves partial URLs.
 * Copyright (C) 2018, 2020, 2021, 2022  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-net-partial-url.
 *
 * ao-net-partial-url is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-net-partial-url is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-net-partial-url.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoapps.net.partialurl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.aoapps.collections.AoCollections;
import com.aoapps.lang.validation.ValidationException;
import com.aoapps.net.HostAddress;
import com.aoapps.net.Path;
import com.aoapps.net.Port;
import com.aoapps.net.Protocol;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import org.junit.Test;

/**
 * Tests {@link SinglePartialURL}.
 *
 * @author  AO Industries, Inc.
 */
public class SinglePartialURLTest {

  // <editor-fold defaultstate="collapsed" desc="Test toString">
  @Test
  public void testToStringDefault() {
    assertEquals(
        "//*:*/*/**",
        PartialURL.DEFAULT.toString()
    );
  }

  @Test
  public void testToStringSchemeOnly() {
    assertEquals(
        "https://*:*/*/**",
        PartialURL.valueOf("hTtPS", null, null, null, null).toString()
    );
  }

  @Test
  public void testToStringHostnameOnly() throws ValidationException {
    assertEquals(
        "//aoindustries.com:*/*/**",
        PartialURL.valueOf(null, HostAddress.valueOf("aoindustries.com"), null, null, null).toString()
    );
  }

  @Test
  public void testToStringIpv4Only() throws ValidationException {
    assertEquals(
        "//192.0.2.45:*/*/**",
        PartialURL.valueOf(null, HostAddress.valueOf("192.0.2.45"), null, null, null).toString()
    );
  }

  @Test
  public void testToStringIpv6Only() throws ValidationException {
    assertEquals(
        "//[2001:db8::d0]:*/*/**",
        PartialURL.valueOf(null, HostAddress.valueOf("2001:DB8::D0"), null, null, null).toString()
    );
  }

  @Test
  public void testToStringIpv6OnlyBracketed() throws ValidationException {
    assertEquals(
        "//[2001:db8::d0]:*/*/**",
        PartialURL.valueOf(null, HostAddress.valueOf("[2001:DB8::D0]"), null, null, null).toString()
    );
  }

  @Test
  public void testToStringPortOnly() throws ValidationException {
    assertEquals(
        "//*:443/*/**",
        PartialURL.valueOf(null, null, Port.valueOf(443, Protocol.TCP), null, null).toString()
    );
  }

  @Test
  public void testToStringContextPathOnly() throws ValidationException {
    assertEquals(
        "//*:*/context/**",
        PartialURL.valueOf(null, null, null, Path.valueOf("/context"), null).toString()
    );
  }

  @Test
  public void testToStringContextPathOnlyRoot() {
    assertEquals(
        "//*:*/**",
        PartialURL.valueOf(null, null, null, Path.ROOT, null).toString()
    );
  }

  @Test
  public void testToStringPrefixOnly() throws ValidationException {
    assertEquals(
        "//*:*/*/prefix/",
        PartialURL.valueOf(null, null, null, null, Path.valueOf("/prefix/")).toString()
    );
  }

  @Test
  public void testToStringPrefixOnlyRoot() {
    assertEquals(
        "//*:*/*/",
        PartialURL.valueOf(null, null, null, null, Path.ROOT).toString()
    );
  }

  @Test
  public void testToStringCompleteHttpDefaultPort() throws ValidationException {
    assertEquals(
        "http://aoindustries.com/",
        PartialURL.valueOf(
            "http",
            HostAddress.valueOf("aoindustries.com"),
            Port.valueOf(80, Protocol.TCP),
            Path.ROOT,
            Path.ROOT
        ).toString()
    );
  }

  @Test
  public void testToStringCompleteHttpPort443() throws ValidationException {
    assertEquals(
        "http://aoindustries.com:443/",
        PartialURL.valueOf(
            "HTtp",
            HostAddress.valueOf("aoindustries.com"),
            Port.valueOf(443, Protocol.TCP),
            Path.ROOT,
            Path.ROOT
        ).toString()
    );
  }

  @Test
  public void testToStringCompleteHttpsDefaultPort() throws ValidationException {
    assertEquals(
        "https://aoindustries.com/",
        PartialURL.valueOf(
            "httpS",
            HostAddress.valueOf("aoindustries.com"),
            Port.valueOf(443, Protocol.TCP),
            Path.ROOT,
            Path.ROOT
        ).toString()
    );
  }

  @Test
  public void testToStringCompleteHttpsPort80() throws ValidationException {
    assertEquals(
        "https://aoindustries.com:80/",
        PartialURL.valueOf(
            "HTtps",
            HostAddress.valueOf("aoindustries.com"),
            Port.valueOf(80, Protocol.TCP),
            Path.ROOT,
            Path.ROOT
        ).toString()
    );
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Test compareTo">
  @Test
  public void testCompareToHostOnlyBeforeDefault() throws ValidationException {
    assertTrue(
        PartialURL.valueOf(null, HostAddress.valueOf("aoindustries.com"), null, null, null).compareTo(
            PartialURL.DEFAULT
        ) < 0
    );
  }

  @Test
  public void testCompareToContextPathOnlyBeforeDefault() {
    assertTrue(
        PartialURL.valueOf(null, null, null, Path.ROOT, null).compareTo(
            PartialURL.DEFAULT
        ) < 0
    );
  }

  @Test
  public void testCompareToPrefixOnlyBeforeDefault() {
    assertTrue(
        PartialURL.valueOf(null, null, null, null, Path.ROOT).compareTo(
            PartialURL.DEFAULT
        ) < 0
    );
  }

  @Test
  public void testCompareToPortOnlyBeforeDefault() throws ValidationException {
    assertTrue(
        PartialURL.valueOf(null, null, Port.valueOf(45, Protocol.TCP), null, null).compareTo(
            PartialURL.DEFAULT
        ) < 0
    );
  }

  @Test
  public void testCompareToSchemeOnlyBeforeDefault() {
    assertTrue(
        PartialURL.valueOf("other", null, null, null, null).compareTo(
            PartialURL.DEFAULT
        ) < 0
    );
  }

  @Test
  public void testCompareToHostOrderingByTld() throws ValidationException {
    assertTrue(
        PartialURL.valueOf(null, HostAddress.valueOf("xyz.com"), null, null, null).compareTo(
            PartialURL.valueOf(null, HostAddress.valueOf("abc.org"), null, null, null)
        ) < 0
    );
  }

  @Test
  public void testCompareToHostOrderingBySubdomainAfter() throws ValidationException {
    assertTrue(
        PartialURL.valueOf(null, HostAddress.valueOf("aoindustries.com"), null, null, null).compareTo(
            PartialURL.valueOf(null, HostAddress.valueOf("www.aoindustries.com"), null, null, null)
        ) < 0
    );
  }

  @Test
  public void testCompareToContextPathOrdering() throws ValidationException {
    assertTrue(
        PartialURL.valueOf(null, null, null, Path.valueOf("/context"), null).compareTo(
            PartialURL.valueOf(null, null, null, Path.valueOf("/context/deeper"), null)
        ) < 0
    );
  }

  @Test
  public void testCompareToPrefixOrderingLexical() throws ValidationException {
    assertTrue(
        PartialURL.valueOf(null, null, null, null, Path.valueOf("/abc/deeper/")).compareTo(
            PartialURL.valueOf(null, null, null, null, Path.valueOf("/xyz/deeper/"))
        ) < 0
    );
  }

  @Test
  public void testCompareToPrefixOrderingDeeperFirst() throws ValidationException {
    assertTrue(
        PartialURL.valueOf(null, null, null, null, Path.valueOf("/path/deeper/")).compareTo(
            PartialURL.valueOf(null, null, null, null, Path.valueOf("/path/"))
        ) < 0
    );
  }

  @Test
  public void testCompareToPortOrdering() throws ValidationException {
    assertTrue(
        PartialURL.valueOf(null, null, Port.valueOf(80, Protocol.TCP), null, null).compareTo(
            PartialURL.valueOf(null, null, Port.valueOf(443, Protocol.TCP), null, null)
        ) < 0
    );
  }

  @Test
  public void testCompareToSchemeHttpBeforeHttps() {
    assertTrue(
        PartialURL.valueOf("http", null, null, null, null).compareTo(
            PartialURL.valueOf("HTTPS", null, null, null, null)
        ) < 0
    );
  }

  @Test
  public void testCompareToHostBeforeContextPath() throws ValidationException {
    assertTrue(
        PartialURL.valueOf(null, HostAddress.valueOf("aoindustries.com"), null, Path.valueOf("/xyz"), null).compareTo(
            PartialURL.valueOf(null, HostAddress.valueOf("semanticcms.com"), null, Path.valueOf("/abc"), null)
        ) < 0
    );
  }

  @Test
  public void testCompareToContextPathBeforePrefix() throws ValidationException {
    assertTrue(
        PartialURL.valueOf(null, null, null, Path.valueOf("/abc"), Path.valueOf("/xyz/")).compareTo(
            PartialURL.valueOf(null, null, null, Path.valueOf("/xyz"), Path.valueOf("/abc/"))
        ) < 0
    );
  }

  @Test
  public void testCompareToPrefixBeforePort() throws ValidationException {
    assertTrue(
        PartialURL.valueOf(null, null, Port.valueOf(443, Protocol.TCP), null, Path.valueOf("/abc/")).compareTo(
            PartialURL.valueOf(null, null, Port.valueOf(80, Protocol.TCP), null, Path.valueOf("/xyz/"))
        ) < 0
    );
  }

  @Test
  public void testCompareToPortBeforeScheme() throws ValidationException {
    assertTrue(
        PartialURL.valueOf("https", null, Port.valueOf(80, Protocol.TCP), null, null).compareTo(
            PartialURL.valueOf("http", null, Port.valueOf(443, Protocol.TCP), null, null)
        ) < 0
    );
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Test matches">
  private static final URL testUrl;
  private static final URL testUrlToURL;
  private static final URL testUrlIpv4;
  private static final URL testUrlIpv4ToURL;
  private static final URL testUrlIpv6;
  private static final URL testUrlIpv6ToURL;
  private static final FieldSource testUrlSource;
  private static final FieldSource testUrlSourceIpv4;
  private static final FieldSource testUrlSourceIpv6;

  static {
    try {
      testUrl = new URL("HTTPS", "aoindustries.com", 443, "/contact");
      testUrlToURL = new URL("HTTPS", "aoindustries.com", -1, "/context");
      testUrlIpv4 = new URL("HTTPS", "192.0.2.38", 443, "/contact/");
      testUrlIpv4ToURL = new URL("HTTPS", "192.0.2.38", -1, "");
      testUrlIpv6 = new URL("HTTPS", "[2001:DB8::D0]", 443, "/contact/other");
      testUrlIpv6ToURL = new URL("HTTPS", "[2001:DB8::D0]", -1, "");
      testUrlSource = new URLFieldSource(testUrl) {
        @Override
        public Path getContextPath() {
          try {
            return Path.valueOf("/context");
          } catch (ValidationException e) {
            throw new AssertionError(e);
          }
        }
      };
      testUrlSourceIpv4 = new URLFieldSource(testUrlIpv4);
      testUrlSourceIpv6 = new URLFieldSource(testUrlIpv6);
    } catch (MalformedURLException e) {
      throw new AssertionError(e);
    }
  }

  @Test
  public void testDefaultMatchesNullFieldSource() throws MalformedURLException {
    assertEquals(
        PartialURL.DEFAULT,
        PartialURL.DEFAULT.matches(null)
    );
  }

  @Test
  public void testSchemeMatches() throws MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf("https", null, null, null, null);
    assertEquals(
        singleUrl,
        singleUrl.matches(testUrlSource)
    );
  }

  @Test
  public void testSchemeNotMatches() throws MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf("http", null, null, null, null);
    assertNull(
        singleUrl.matches(testUrlSource)
    );
  }

  @Test
  public void testHostMatchesHostname() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, HostAddress.valueOf("AOIndustries.COM"), null, null, null);
    assertEquals(
        singleUrl,
        singleUrl.matches(testUrlSource)
    );
  }

  @Test
  public void testSchemeNotMatchesHostname() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, HostAddress.valueOf("WWW.AOIndustries.COM"), null, null, null);
    assertNull(
        singleUrl.matches(testUrlSource)
    );
  }

  @Test
  public void testHostMatchesIpv4() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, HostAddress.valueOf("192.0.2.38"), null, null, null);
    assertEquals(
        singleUrl,
        singleUrl.matches(testUrlSourceIpv4)
    );
  }

  @Test
  public void testSchemeNotMatchesIpv4() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, HostAddress.valueOf("192.0.2.39"), null, null, null);
    assertNull(
        singleUrl.matches(testUrlSourceIpv4)
    );
  }

  @Test
  public void testHostMatchesIpv6() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, HostAddress.valueOf("2001:db8::d0"), null, null, null);
    assertEquals(
        singleUrl,
        singleUrl.matches(testUrlSourceIpv6)
    );
  }

  @Test
  public void testSchemeNotMatchesIpv6() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, HostAddress.valueOf("2001:db8::d1"), null, null, null);
    assertNull(
        singleUrl.matches(testUrlSourceIpv6)
    );
  }

  @Test
  public void testHostMatchesIpv6Bracketed() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, HostAddress.valueOf("[2001:db8::d0]"), null, null, null);
    assertEquals(
        singleUrl,
        singleUrl.matches(testUrlSourceIpv6)
    );
  }

  @Test
  public void testSchemeNotMatchesIpv6Bracketed() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, HostAddress.valueOf("[2001:db8::d1]"), null, null, null);
    assertNull(
        singleUrl.matches(testUrlSourceIpv6)
    );
  }

  @Test
  public void testPortMatches() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, null, Port.valueOf(443, Protocol.TCP), null, null);
    assertEquals(
        singleUrl,
        singleUrl.matches(testUrlSource)
    );
  }

  @Test
  public void testPortNotMatchesPort() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, null, Port.valueOf(80, Protocol.TCP), null, null);
    assertNull(
        singleUrl.matches(testUrlSource)
    );
  }

  @Test
  public void testPortNotMatchesProtocol() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, null, Port.valueOf(443, Protocol.UDP), null, null);
    assertNull(
        singleUrl.matches(testUrlSource)
    );
  }

  @Test
  public void testContextPathMatches() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, null, null, Path.valueOf("/context"), null);
    assertEquals(
        singleUrl,
        singleUrl.matches(testUrlSource)
    );
  }

  @Test
  public void testContextPathNotMatchesRoot() throws MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, null, null, Path.ROOT, null);
    assertNull(
        singleUrl.matches(testUrlSource)
    );
  }

  @Test
  public void testContextPathNotMatchesSubpath() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, null, null, Path.valueOf("/context/sub"), null);
    assertNull(
        singleUrl.matches(testUrlSource)
    );
  }

  @Test
  public void testPrefixMatchesRoot() throws MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, null, null, null, Path.ROOT);
    assertEquals(
        singleUrl,
        singleUrl.matches(testUrlSource)
    );
    assertEquals(
        singleUrl,
        singleUrl.matches(testUrlSourceIpv4)
    );
    assertEquals(
        singleUrl,
        singleUrl.matches(testUrlSourceIpv6)
    );
  }

  @Test
  public void testPrefixMatchesContact() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, null, null, null, Path.valueOf("/contact/"));
    assertNull(
        singleUrl.matches(testUrlSource)
    );
    assertEquals(
        singleUrl,
        singleUrl.matches(testUrlSourceIpv4)
    );
    assertEquals(
        singleUrl,
        singleUrl.matches(testUrlSourceIpv6)
    );
  }

  @Test
  public void testPrefixNotMatchesContactOther() throws ValidationException, MalformedURLException {
    SinglePartialURL singleUrl = PartialURL.valueOf(null, null, null, null, Path.valueOf("/contact/other/"));
    assertNull(
        singleUrl.matches(testUrlSource)
    );
    assertNull(
        singleUrl.matches(testUrlSourceIpv4)
    );
    assertNull(
        singleUrl.matches(testUrlSourceIpv6)
    );
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Test isComplete">
  @Test
  public void testIsComplete() throws ValidationException {
    assertTrue(
        "Is complete with prefix",
        PartialURL.valueOf(
            "https",
            HostAddress.valueOf("aoindustries.com"),
            Port.valueOf(443, Protocol.TCP),
            Path.ROOT,
            Path.ROOT
        ).isComplete()
    );
    assertTrue(
        "Is also complete without prefix",
        PartialURL.valueOf(
            "https",
            HostAddress.valueOf("aoindustries.com"),
            Port.valueOf(443, Protocol.TCP),
            Path.ROOT,
            null
        ).isComplete()
    );
  }

  @Test
  public void testIncompleteScheme() throws ValidationException {
    assertFalse(
        PartialURL.valueOf(
            null,
            HostAddress.valueOf("aoindustries.com"),
            Port.valueOf(443, Protocol.TCP),
            Path.ROOT,
            Path.ROOT
        ).isComplete()
    );
  }

  @Test
  public void testIncompleteHost() throws ValidationException {
    assertFalse(
        PartialURL.valueOf(
            "https",
            null,
            Port.valueOf(443, Protocol.TCP),
            Path.ROOT,
            Path.ROOT
        ).isComplete()
    );
  }

  @Test
  public void testIncompletePort() throws ValidationException {
    assertFalse(
        PartialURL.valueOf(
            "https",
            HostAddress.valueOf("aoindustries.com"),
            null,
            Path.ROOT,
            Path.ROOT
        ).isComplete()
    );
  }

  @Test
  public void testIncompleteContextPath() throws ValidationException {
    assertFalse(
        PartialURL.valueOf(
            "https",
            HostAddress.valueOf("aoindustries.com"),
            Port.valueOf(443, Protocol.TCP),
            null,
            Path.ROOT
        ).isComplete()
    );
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Test getPrimary">
  @Test
  public void testGetPrimaryDefault() {
    assertEquals(
        PartialURL.DEFAULT,
        PartialURL.DEFAULT.getPrimary()
    );
  }

  @Test
  public void testGetPrimaryComplete() throws ValidationException {
    SinglePartialURL singleUrl = PartialURL.valueOf(
        "https",
        HostAddress.valueOf("aoindustries.com"),
        Port.valueOf(443, Protocol.TCP),
        Path.ROOT,
        Path.ROOT
    );
    assertEquals(
        singleUrl,
        singleUrl.getPrimary()
    );
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Test getCombinations">
  @Test
  public void testGetCombinationsDefault() {
    assertEquals(
        Collections.singleton(PartialURL.DEFAULT),
        AoCollections.unmodifiableCopySet(PartialURL.DEFAULT.getCombinations())
    );
  }

  @Test
  public void testGetCombinationsComplete() throws ValidationException {
    SinglePartialURL singleUrl = PartialURL.valueOf(
        "https",
        HostAddress.valueOf("aoindustries.com"),
        Port.valueOf(443, Protocol.TCP),
        Path.ROOT,
        Path.ROOT
    );
    assertEquals(
        Collections.singleton(singleUrl),
        AoCollections.unmodifiableCopySet(singleUrl.getCombinations())
    );
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Test toURL">
  @Test
  public void testToURLDefault() throws MalformedURLException {
    assertEquals(
        testUrlToURL,
        PartialURL.DEFAULT.toURL(testUrlSource)
    );
  }

  @Test
  public void testToURLDefaultIpv4() throws MalformedURLException {
    assertEquals(
        testUrlIpv4ToURL,
        PartialURL.DEFAULT.toURL(testUrlSourceIpv4)
    );
  }

  @Test
  public void testToURLDefaultIpv6() throws MalformedURLException {
    assertEquals(
        testUrlIpv6ToURL,
        PartialURL.DEFAULT.toURL(testUrlSourceIpv6)
    );
  }

  @Test
  public void testToURLNoScheme() throws ValidationException, MalformedURLException {
    assertEquals(
        new URL("HTTPS", "aorepo.org", 80, "/otherContext/otherPath/"),
        PartialURL.valueOf(
            null,
            HostAddress.valueOf("aorepo.org"),
            Port.valueOf(80, Protocol.TCP),
            Path.valueOf("/otherContext"),
            Path.valueOf("/otherPath/")
        ).toURL(testUrlSource)
    );
  }

  @Test
  public void testToURLNoHost() throws ValidationException, MalformedURLException {
    assertEquals(
        new URL("https", "aoindustries.com", 80, "/otherContext/otherPath/"),
        PartialURL.valueOf(
            "https",
            null,
            Port.valueOf(80, Protocol.TCP),
            Path.valueOf("/otherContext"),
            Path.valueOf("/otherPath/")
        ).toURL(testUrlSource)
    );
  }

  @Test
  public void testToURLNoPort() throws ValidationException, MalformedURLException {
    assertEquals(
        new URL("https", "aorepo.org", -1, "/otherContext/otherPath/"),
        PartialURL.valueOf(
            "https",
            HostAddress.valueOf("aorepo.org"),
            null,
            Path.valueOf("/otherContext"),
            Path.valueOf("/otherPath/")
        ).toURL(testUrlSource)
    );
  }

  @Test
  public void testToURLNoContextPath() throws ValidationException, MalformedURLException {
    assertEquals(
        new URL("https", "aorepo.org", 80, "/context/otherPath/"),
        PartialURL.valueOf(
            "https",
            HostAddress.valueOf("aorepo.org"),
            Port.valueOf(80, Protocol.TCP),
            null,
            Path.valueOf("/otherPath/")
        ).toURL(testUrlSource)
    );
  }

  @Test
  public void testToURLNoPrefix() throws ValidationException, MalformedURLException {
    assertEquals(
        new URL("https", "aorepo.org", 80, "/otherContext"),
        PartialURL.valueOf(
            "https",
            HostAddress.valueOf("aorepo.org"),
            Port.valueOf(80, Protocol.TCP),
            Path.valueOf("/otherContext"),
            null
        ).toURL(null)
    );
  }

  @Test
  public void testToURLComplete() throws ValidationException, MalformedURLException {
    assertEquals(
        new URL("https", "aorepo.org", 80, "/otherContext/otherPath/"),
        PartialURL.valueOf(
            "https",
            HostAddress.valueOf("aorepo.org"),
            Port.valueOf(80, Protocol.TCP),
            Path.valueOf("/otherContext"),
            Path.valueOf("/otherPath/")
        ).toURL(null)
    );
  }
  // </editor-fold>
}
