/*
 * ao-net-partial-url - Matches and resolves partial URLs.
 * Copyright (C) 2018  AO Industries, Inc.
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
 * along with ao-net-partial-url.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.net.partialurl;

import com.aoindustries.net.HostAddress;
import com.aoindustries.net.Path;
import com.aoindustries.net.Port;
import com.aoindustries.net.Protocol;
import com.aoindustries.validation.ValidationException;
import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @see SinglePartialURL
 *
 * @author  AO Industries, Inc.
 */
public class SinglePartialURLTest {

	// <editor-fold defaultstate="collapsed" desc="Test valueOf">
	@Test
	public void testValueOfDefault() {
		assertSame(SinglePartialURL.DEFAULT, SinglePartialURL.valueOf(null));
		assertSame(SinglePartialURL.DEFAULT, SinglePartialURL.valueOf(null, null, null, null, null));
	}

	@Test
	public void testValueOfLowerScheme() {
		assertEquals(
			"https",
			SinglePartialURL.valueOf("HTTPS", null, null, null, null).getScheme()
		);
	}

	@Test
	public void testValueOfPrefixOnly() throws ValidationException {
		Path path = Path.valueOf("/path/");
		assertEquals(
			SinglePartialURL.valueOf(path),
			SinglePartialURL.valueOf(null, null, null, null, path)
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOfPathNoTrailingSlash() throws ValidationException {
		SinglePartialURL.valueOf(Path.valueOf("/path"));
	}

	@Test
	public void testValueOfContextPathAsRoot() throws ValidationException {
		assertEquals(
			Path.ROOT,
			SinglePartialURL.valueOf(null, null, null, Path.ROOT, null).getContextPath()
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOfContextPathWithTrailingSlash() throws ValidationException {
		SinglePartialURL.valueOf(null, null, null, Path.valueOf("/context/"), null);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test toString">
	@Test
	public void testToStringDefault() {
		assertEquals(
			"//*:*/*/**",
			SinglePartialURL.DEFAULT.toString()
		);
	}

	@Test
	public void testToStringSchemeOnly() {
		assertEquals(
			"https://*:*/*/**",
			SinglePartialURL.valueOf("hTtPS", null, null, null, null).toString()
		);
	}

	@Test
	public void testToStringHostnameOnly() throws ValidationException {
		assertEquals(
			"//aoindustries.com:*/*/**",
			SinglePartialURL.valueOf(null, HostAddress.valueOf("aoindustries.com"), null, null, null).toString()
		);
	}

	@Test
	public void testToStringIPv4Only() throws ValidationException {
		assertEquals(
			"//192.0.2.45:*/*/**",
			SinglePartialURL.valueOf(null, HostAddress.valueOf("192.0.2.45"), null, null, null).toString()
		);
	}

	@Test
	public void testToStringIPv6Only() throws ValidationException {
		assertEquals(
			"//[2001:db8::d0]:*/*/**",
			SinglePartialURL.valueOf(null, HostAddress.valueOf("2001:DB8::D0"), null, null, null).toString()
		);
	}

	@Test
	public void testToStringIPv6OnlyBracketed() throws ValidationException {
		assertEquals(
			"//[2001:db8::d0]:*/*/**",
			SinglePartialURL.valueOf(null, HostAddress.valueOf("[2001:DB8::D0]"), null, null, null).toString()
		);
	}

	@Test
	public void testToStringPortOnly() throws ValidationException {
		assertEquals(
			"//*:443/*/**",
			SinglePartialURL.valueOf(null, null, Port.valueOf(443, Protocol.TCP), null, null).toString()
		);
	}

	@Test
	public void testToStringContextPathOnly() throws ValidationException {
		assertEquals(
			"//*:*/context/**",
			SinglePartialURL.valueOf(null, null, null, Path.valueOf("/context"), null).toString()
		);
	}

	@Test
	public void testToStringContextPathOnlyRoot() throws ValidationException {
		assertEquals(
			"//*:*/**",
			SinglePartialURL.valueOf(null, null, null, Path.ROOT, null).toString()
		);
	}

	@Test
	public void testToStringPrefixOnly() throws ValidationException {
		assertEquals(
			"//*:*/*/prefix/",
			SinglePartialURL.valueOf(null, null, null, null, Path.valueOf("/prefix/")).toString()
		);
	}

	@Test
	public void testToStringPrefixOnlyRoot() throws ValidationException {
		assertEquals(
			"//*:*/*/",
			SinglePartialURL.valueOf(null, null, null, null, Path.ROOT).toString()
		);
	}

	@Test
	public void testToStringCompleteHttpDefaultPort() throws ValidationException {
		assertEquals(
			"http://aoindustries.com/",
			SinglePartialURL.valueOf(
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
			SinglePartialURL.valueOf(
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
			SinglePartialURL.valueOf(
				"httpS",
				HostAddress.valueOf("aoindustries.com"),
				Port.valueOf(443, Protocol.TCP),
				Path.ROOT,
				Path.ROOT
			).toString()
		);
	}

	@Test
	public void testToStringCompleteHttpPort80() throws ValidationException {
		assertEquals(
			"https://aoindustries.com:80/",
			SinglePartialURL.valueOf(
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
			SinglePartialURL.valueOf(null, HostAddress.valueOf("aoindustries.com"), null, null, null).compareTo(
				SinglePartialURL.DEFAULT
			) < 0
		);
	}

	@Test
	public void testCompareToContextPathOnlyBeforeDefault() throws ValidationException {
		assertTrue(
			SinglePartialURL.valueOf(null, null, null, Path.ROOT, null).compareTo(
				SinglePartialURL.DEFAULT
			) < 0
		);
	}

	@Test
	public void testCompareToPrefixOnlyBeforeDefault() throws ValidationException {
		assertTrue(
			SinglePartialURL.valueOf(null, null, null, null, Path.ROOT).compareTo(
				SinglePartialURL.DEFAULT
			) < 0
		);
	}

	@Test
	public void testCompareToPortOnlyBeforeDefault() throws ValidationException {
		assertTrue(
			SinglePartialURL.valueOf(null, null, Port.valueOf(45, Protocol.TCP), null, null).compareTo(
				SinglePartialURL.DEFAULT
			) < 0
		);
	}

	@Test
	public void testCompareToSchemeOnlyBeforeDefault() throws ValidationException {
		assertTrue(
			SinglePartialURL.valueOf("other", null, null, null, null).compareTo(
				SinglePartialURL.DEFAULT
			) < 0
		);
	}

	@Test
	public void testCompareToHostOrderingByTld() throws ValidationException {
		assertTrue(
			SinglePartialURL.valueOf(null, HostAddress.valueOf("xyz.com"), null, null, null).compareTo(
				SinglePartialURL.valueOf(null, HostAddress.valueOf("abc.org"), null, null, null)
			) < 0
		);
	}

	@Test
	public void testCompareToHostOrderingBySubdomainAfter() throws ValidationException {
		assertTrue(
			SinglePartialURL.valueOf(null, HostAddress.valueOf("aoindustries.com"), null, null, null).compareTo(
				SinglePartialURL.valueOf(null, HostAddress.valueOf("www.aoindustries.com"), null, null, null)
			) < 0
		);
	}

	@Test
	public void testCompareToContextPathOrdering() throws ValidationException {
		assertTrue(
			SinglePartialURL.valueOf(null, null, null, Path.valueOf("/context"), null).compareTo(
				SinglePartialURL.valueOf(null, null, null, Path.valueOf("/context/deeper"), null)
			) < 0
		);
	}

	@Test
	public void testCompareToPrefixOrderingLexical() throws ValidationException {
		assertTrue(
			SinglePartialURL.valueOf(null, null, null, null, Path.valueOf("/abc/deeper/")).compareTo(
				SinglePartialURL.valueOf(null, null, null, null, Path.valueOf("/xyz/deeper/"))
			) < 0
		);
	}

	@Test
	public void testCompareToPrefixOrderingDeeperFirst() throws ValidationException {
		assertTrue(
			SinglePartialURL.valueOf(null, null, null, null, Path.valueOf("/path/deeper/")).compareTo(
				SinglePartialURL.valueOf(null, null, null, null, Path.valueOf("/path/"))
			) < 0
		);
	}

	@Test
	public void testCompareToPortOrdering() throws ValidationException {
		assertTrue(
			SinglePartialURL.valueOf(null, null, Port.valueOf(80, Protocol.TCP), null, null).compareTo(
				SinglePartialURL.valueOf(null, null, Port.valueOf(443, Protocol.TCP), null, null)
			) < 0
		);
	}

	@Test
	public void testCompareToSchemeHttpBeforeHttps() throws ValidationException {
		assertTrue(
			SinglePartialURL.valueOf("http", null, null, null, null).compareTo(
				SinglePartialURL.valueOf("HTTPS", null, null, null, null)
			) < 0
		);
	}

	// TODO: Test relative ordering of fields, like host sorted before contextPath?
	//       aoindustries.com:/xyz before semanticcms.com/abc  (same for other ordering sequence)

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test matches">
	private static final FieldSource testURLSource;
	private static final FieldSource testURLSourceIPv4;
	private static final FieldSource testURLSourceIPv6;
	static {
		try {
			testURLSource = new URLFieldSource(new URL("HTTPS", "aoindustries.com", 443, "/contact")) {
				@Override
				public Path getContextPath() {
					try {
						return Path.valueOf("/context");
					} catch(ValidationException e) {
						throw new AssertionError(e);
					}
				}
			};
			testURLSourceIPv4 = new URLFieldSource(new URL("HTTPS", "192.0.2.38", 443, "/contact/"));
			testURLSourceIPv6 = new URLFieldSource(new URL("HTTPS", "[2001:DB8::D0]", 443, "/contact/other"));
		} catch(MalformedURLException e) {
			throw new AssertionError(e);
		}
	}

	@Test
	public void testDefaultMatchesNullFieldSource() throws MalformedURLException {
		assertEquals(
			SinglePartialURL.DEFAULT,
			SinglePartialURL.DEFAULT.matches(null)
		);
	}

	@Test
	public void testSchemeMatches() throws MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf("https", null, null, null, null);
		assertEquals(
			singleURL,
			singleURL.matches(testURLSource)
		);
	}

	@Test
	public void testSchemeNotMatches() throws MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf("http", null, null, null, null);
		assertNull(
			singleURL.matches(testURLSource)
		);
	}

	@Test
	public void testHostMatchesHostname() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, HostAddress.valueOf("AOIndustries.COM"), null, null, null);
		assertEquals(
			singleURL,
			singleURL.matches(testURLSource)
		);
	}

	@Test
	public void testSchemeNotMatchesHostname() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, HostAddress.valueOf("WWW.AOIndustries.COM"), null, null, null);
		assertNull(
			singleURL.matches(testURLSource)
		);
	}

	@Test
	public void testHostMatchesIPv4() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, HostAddress.valueOf("192.0.2.38"), null, null, null);
		assertEquals(
			singleURL,
			singleURL.matches(testURLSourceIPv4)
		);
	}

	@Test
	public void testSchemeNotMatchesIPv4() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, HostAddress.valueOf("192.0.2.39"), null, null, null);
		assertNull(
			singleURL.matches(testURLSourceIPv4)
		);
	}

	@Test
	public void testHostMatchesIPv6() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, HostAddress.valueOf("2001:db8::d0"), null, null, null);
		assertEquals(
			singleURL,
			singleURL.matches(testURLSourceIPv6)
		);
	}

	@Test
	public void testSchemeNotMatchesIPv6() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, HostAddress.valueOf("2001:db8::d1"), null, null, null);
		assertNull(
			singleURL.matches(testURLSourceIPv6)
		);
	}

	@Test
	public void testHostMatchesIPv6Bracketed() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, HostAddress.valueOf("[2001:db8::d0]"), null, null, null);
		assertEquals(
			singleURL,
			singleURL.matches(testURLSourceIPv6)
		);
	}

	@Test
	public void testSchemeNotMatchesIPv6Bracketed() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, HostAddress.valueOf("[2001:db8::d1]"), null, null, null);
		assertNull(
			singleURL.matches(testURLSourceIPv6)
		);
	}

	@Test
	public void testPortMatches() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, null, Port.valueOf(443, Protocol.TCP), null, null);
		assertEquals(
			singleURL,
			singleURL.matches(testURLSource)
		);
	}

	@Test
	public void testPortNotMatchesPort() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, null, Port.valueOf(80, Protocol.TCP), null, null);
		assertNull(
			singleURL.matches(testURLSource)
		);
	}

	@Test
	public void testPortNotMatchesProtocol() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, null, Port.valueOf(443, Protocol.UDP), null, null);
		assertNull(
			singleURL.matches(testURLSource)
		);
	}

	@Test
	public void testContextPathMatches() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, null, null, Path.valueOf("/context"), null);
		assertEquals(
			singleURL,
			singleURL.matches(testURLSource)
		);
	}

	@Test
	public void testContextPathNotMatchesRoot() throws MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, null, null, Path.ROOT, null);
		assertNull(
			singleURL.matches(testURLSource)
		);
	}

	@Test
	public void testContextPathNotMatchesSubpath() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, null, null, Path.valueOf("/context/sub"), null);
		assertNull(
			singleURL.matches(testURLSource)
		);
	}

	@Test
	public void testPrefixMatchesRoot() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, null, null, null, Path.ROOT);
		assertEquals(
			singleURL,
			singleURL.matches(testURLSource)
		);
		assertEquals(
			singleURL,
			singleURL.matches(testURLSourceIPv4)
		);
		assertEquals(
			singleURL,
			singleURL.matches(testURLSourceIPv6)
		);
	}

	@Test
	public void testPrefixMatchesContact() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, null, null, null, Path.valueOf("/contact/"));
		assertNull(
			singleURL.matches(testURLSource)
		);
		assertEquals(
			singleURL,
			singleURL.matches(testURLSourceIPv4)
		);
		assertEquals(
			singleURL,
			singleURL.matches(testURLSourceIPv6)
		);
	}

	@Test
	public void testPrefixNotMatchesContactOther() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = SinglePartialURL.valueOf(null, null, null, null, Path.valueOf("/contact/other/"));
		assertNull(
			singleURL.matches(testURLSource)
		);
		assertNull(
			singleURL.matches(testURLSourceIPv4)
		);
		assertNull(
			singleURL.matches(testURLSourceIPv6)
		);
	}
	// </editor-fold>

	// TODO: Test isComplete?

	// TODO: Test getPrimary?

	// TODO: Test getCombinations?

	// TODO: Test toURL (partial and complete)
}
