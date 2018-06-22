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
import java.util.Iterator;
import java.util.NoSuchElementException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @see MultiPartialURL
 *
 * @author  AO Industries, Inc.
 */
public class MultiPartialURLTest {

	// <editor-fold defaultstate="collapsed" desc="Test toString">
	@Test
	public void testToStringSchemesOnly() {
		assertEquals(
			"{https,http}://*:*/*/**",
			PartialURL.valueOf(new String[] {"hTtPS", "http"}, null, null, null).toString()
		);
		assertEquals(
			"{http,https}://*:*/*/**",
			PartialURL.valueOf(new String[] {"hTtP", "https"}, null, null, null).toString()
		);
	}

	@Test
	public void testToStringHostnamesOnly() throws ValidationException {
		assertEquals(
			"//{aoindustries.com,www.aoindustries.com}:*/*/**",
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("www.aoindustries.com")}, null, null).toString()
		);
		assertEquals(
			"//{www.aoindustries.com,aoindustries.com}:*/*/**",
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("www.aoindustries.com"), HostAddress.valueOf("aoindustries.com")}, null, null).toString()
		);
		assertEquals(
			"//{www.aoindustries.com,aoindustries.com}:*/*/**",
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("www.aoindustries.com"), HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("WWW.AOIndustries.COM")}, null, null).toString()
		);
	}

	@Test
	public void testToStringIPv4AndIPV6() throws ValidationException {
		assertEquals(
			"//{192.0.2.45,[2001:db8::d0]}:*/*/**",
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("192.0.2.45"), HostAddress.valueOf("2001:DB8::D0")}, null, null).toString()
		);
		assertEquals(
			"//{[2001:db8::d0],192.0.2.45}:*/*/**",
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("2001:DB8::D0"), HostAddress.valueOf("192.0.2.45")}, null, null).toString()
		);
		assertEquals(
			"//{[2001:db8::d0],192.0.2.45}:*/*/**",
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("2001:DB8::D0"), HostAddress.valueOf("192.0.2.45"), HostAddress.valueOf("[2001:DB8::D0]")}, null, null).toString()
		);
	}

	@Test
	public void testToStringIPv4AndIPV6Bracketed() throws ValidationException {
		assertEquals(
			"//{192.0.2.45,[2001:db8::d0]}:*/*/**",
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("192.0.2.45"), HostAddress.valueOf("[2001:DB8::D0]")}, null, null).toString()
		);
		assertEquals(
			"//{[2001:db8::d0],192.0.2.45}:*/*/**",
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("[2001:DB8::D0]"),HostAddress.valueOf("192.0.2.45")}, null, null).toString()
		);
		assertEquals(
			"//{[2001:db8::d0],192.0.2.45}:*/*/**",
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("[2001:DB8::D0]"),HostAddress.valueOf("192.0.2.45"),HostAddress.valueOf("192.000.002.045")}, null, null).toString()
		);
	}

	@Test
	public void testToStringPortsOnly() throws ValidationException {
		assertEquals(
			"//*:{8443,443}/*/**",
			PartialURL.valueOf(null, null, new Port[] {Port.valueOf(8443, Protocol.TCP), Port.valueOf(443, Protocol.TCP)}, null).toString()
		);
		assertEquals(
			"//*:{443,8443}/*/**",
			PartialURL.valueOf(null, null, new Port[] {Port.valueOf(443, Protocol.TCP), Port.valueOf(8443, Protocol.TCP)}, null).toString()
		);
		assertEquals(
			"//*:{443,8443}/*/**",
			PartialURL.valueOf(null, null, new Port[] {Port.valueOf(443, Protocol.TCP), Port.valueOf(8443, Protocol.TCP), Port.valueOf(443, Protocol.TCP)}, null).toString()
		);
	}

	@Test
	public void testToStringContextPathsOnly() throws ValidationException {
		assertEquals(
			"//*:*{/xyz,/abc}/**",
			PartialURL.valueOf(null, null, null, new Path[] {Path.valueOf("/xyz"), Path.valueOf("/abc")}).toString()
		);
		assertEquals(
			"//*:*{/abc,/xyz}/**",
			PartialURL.valueOf(null, null, null, new Path[] {Path.valueOf("/abc"), Path.valueOf("/xyz")}).toString()
		);
		assertEquals(
			"//*:*{/abc,/xyz}/**",
			PartialURL.valueOf(null, null, null, new Path[] {Path.valueOf("/abc"), Path.valueOf("/xyz"), Path.valueOf("/abc")}).toString()
		);
	}

	@Test
	public void testToStringContextPathsOnlyWithRoot() throws ValidationException {
		assertEquals(
			"//*:*{,/abc}/**",
			PartialURL.valueOf(null, null, null, new Path[] {Path.ROOT, Path.valueOf("/abc")}).toString()
		);
		assertEquals(
			"//*:*{/abc,}/**",
			PartialURL.valueOf(null, null, null, new Path[] {Path.valueOf("/abc"), Path.ROOT}).toString()
		);
		assertEquals(
			"//*:*{/abc,}/**",
			PartialURL.valueOf(null, null, null, new Path[] {Path.valueOf("/abc"), Path.ROOT, Path.valueOf("/abc")}).toString()
		);
		assertEquals(
			"//*:*{/abc,}/**",
			PartialURL.valueOf(null, null, null, new Path[] {Path.valueOf("/abc"), Path.ROOT, Path.valueOf("/")}).toString()
		);
	}

	@Test
	public void testToStringPrefixesOnly() throws ValidationException {
		assertEquals(
			"//*:*/*{/abc/,/xyz/}",
			PartialURL.valueOf((String[])null, null, null, null, Path.valueOf("/abc/"), Path.valueOf("/xyz/")).toString()
		);
		assertEquals(
			"//*:*/*{/xyz/,/abc/}",
			PartialURL.valueOf((String[])null, null, null, null, Path.valueOf("/xyz/"), Path.valueOf("/abc/")).toString()
		);
		assertEquals(
			"//*:*/*{/xyz/,/abc/}",
			PartialURL.valueOf((String[])null, null, null, null, Path.valueOf("/xyz/"), Path.valueOf("/abc/"), Path.valueOf("/xyz/")).toString()
		);
	}

	@Test
	public void testToStringPrefixesOnlyWithRoot() throws ValidationException {
		assertEquals(
			"//*:*/*{/abc/,/}",
			PartialURL.valueOf((String[])null, null, null, null, Path.valueOf("/abc/"), Path.ROOT).toString()
		);
		assertEquals(
			"//*:*/*{/,/abc/}",
			PartialURL.valueOf((String[])null, null, null, null, Path.ROOT, Path.valueOf("/abc/")).toString()
		);
		assertEquals(
			"//*:*/*{/,/abc/}",
			PartialURL.valueOf((String[])null, null, null, null, Path.ROOT, Path.valueOf("/abc/"), Path.ROOT).toString()
		);
	}

	@Test
	public void testToStringMultiAll() throws ValidationException {
		assertEquals(
			"{https,http}://{aoindustries.com,WWW.AOIndustries.COM}:{80,443}{,/xyz}{/prefix/,/}",
			PartialURL.valueOf(
				new String[] {"hTTps", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("WWW.AOIndustries.COM")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(443, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/xyz")},
				new Path[] {Path.valueOf("/prefix/"), Path.ROOT}
			).toString()
		);
	}

	@Test
	public void testToStringOneScheme() throws ValidationException {
		assertEquals(
			"https://{aoindustries.com,WWW.AOIndustries.COM}:{8443,443}{,/xyz}{/prefix/,/}",
			PartialURL.valueOf(
				new String[] {"hTTps"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("WWW.AOIndustries.COM")},
				new Port[] {Port.valueOf(8443, Protocol.TCP), Port.valueOf(443, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/xyz")},
				new Path[] {Path.valueOf("/prefix/"), Path.ROOT}
			).toString()
		);
	}

	@Test
	public void testToStringOneHost() throws ValidationException {
		assertEquals(
			"{https,http}://aoindustries.com:{80,443}{,/xyz}{/prefix/,/}",
			PartialURL.valueOf(
				new String[] {"hTTps", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(443, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/xyz")},
				new Path[] {Path.valueOf("/prefix/"), Path.ROOT}
			).toString()
		);
	}

	@Test
	public void testToStringOnePort() throws ValidationException {
		assertEquals(
			"{https,http}://{aoindustries.com,WWW.AOIndustries.COM}:443{,/xyz}{/prefix/,/}",
			PartialURL.valueOf(
				new String[] {"hTTps", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("WWW.AOIndustries.COM")},
				new Port[] {Port.valueOf(443, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/xyz")},
				new Path[] {Path.valueOf("/prefix/"), Path.ROOT}
			).toString()
		);
	}

	@Test
	public void testToStringOneContextPath() throws ValidationException {
		assertEquals(
			"{https,http}://{aoindustries.com,WWW.AOIndustries.COM}:{80,443}{/prefix/,/}",
			PartialURL.valueOf(
				new String[] {"hTTps", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("WWW.AOIndustries.COM")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(443, Protocol.TCP)},
				new Path[] {Path.ROOT},
				new Path[] {Path.valueOf("/prefix/"), Path.ROOT}
			).toString()
		);
		assertEquals(
			"{https,http}://{aoindustries.com,WWW.AOIndustries.COM}:{80,443}/xyz{/prefix/,/}",
			PartialURL.valueOf(
				new String[] {"hTTps", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("WWW.AOIndustries.COM")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(443, Protocol.TCP)},
				new Path[] {Path.valueOf("/xyz")},
				new Path[] {Path.valueOf("/prefix/"), Path.ROOT}
			).toString()
		);
	}

	@Test
	public void testToStringOnePrefix() throws ValidationException {
		assertEquals(
			"{https,http}://{aoindustries.com,WWW.AOIndustries.COM}:{80,443}{,/xyz}/prefix/",
			PartialURL.valueOf(
				new String[] {"hTTps", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("WWW.AOIndustries.COM")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(443, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/xyz")},
				Path.valueOf("/prefix/")
			).toString()
		);
		assertEquals(
			"{https,http}://{aoindustries.com,WWW.AOIndustries.COM}:{80,443}{,/xyz}/",
			PartialURL.valueOf(
				new String[] {"hTTps", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("WWW.AOIndustries.COM")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(443, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/xyz")},
				Path.ROOT
			).toString()
		);
	}

	@Test
	public void testToStringCompleteHttpDefaultPort() throws ValidationException {
		assertEquals(
			"http://{aoindustries.com,WWW.AOIndustries.COM}{,/xyz}{/prefix/,/}",
			PartialURL.valueOf(
				new String[] {"http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("WWW.AOIndustries.COM")},
				new Port[] {Port.valueOf(80, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/xyz")},
				new Path[] {Path.valueOf("/prefix/"), Path.ROOT}
			).toString()
		);
	}

	@Test
	public void testToStringCompleteHttpPort443() throws ValidationException {
		assertEquals(
			"http://{aoindustries.com,WWW.AOIndustries.COM}:443{,/xyz}{/prefix/,/}",
			PartialURL.valueOf(
				new String[] {"http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("WWW.AOIndustries.COM")},
				new Port[] {Port.valueOf(443, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/xyz")},
				new Path[] {Path.valueOf("/prefix/"), Path.ROOT}
			).toString()
		);
	}

	@Test
	public void testToStringCompleteHttpsDefaultPort() throws ValidationException {
		assertEquals(
			"https://{aoindustries.com,WWW.AOIndustries.COM}{,/xyz}{/prefix/,/}",
			PartialURL.valueOf(
				new String[] {"hTTps"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("WWW.AOIndustries.COM")},
				new Port[] {Port.valueOf(443, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/xyz")},
				new Path[] {Path.valueOf("/prefix/"), Path.ROOT}
			).toString()
		);
	}

	@Test
	public void testToStringCompleteHttpsPort80() throws ValidationException {
		assertEquals(
			"https://{aoindustries.com,WWW.AOIndustries.COM}:80{,/xyz}{/prefix/,/}",
			PartialURL.valueOf(
				new String[] {"hTTps"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("WWW.AOIndustries.COM")},
				new Port[] {Port.valueOf(80, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/xyz")},
				new Path[] {Path.valueOf("/prefix/"), Path.ROOT}
			).toString()
		);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test matches">
	private static final URL testUrl;
	private static final URL testUrlIPv4;
	private static final URL testUrlIPv6;
	private static final FieldSource testUrlSource;
	private static final FieldSource testUrlSourceIPv4;
	private static final FieldSource testUrlSourceIPv6;
	static {
		try {
			testUrl = new URL("HTTPS", "aoindustries.com", 443, "/contact");
			testUrlIPv4 = new URL("HTTPS", "192.0.2.38", 443, "/contact/");
			testUrlIPv6 = new URL("HTTPS", "[2001:DB8::D0]", 443, "/contact/other");
			testUrlSource = new URLFieldSource(testUrl) {
				@Override
				public Path getContextPath() {
					try {
						return Path.valueOf("/context");
					} catch(ValidationException e) {
						throw new AssertionError(e);
					}
				}
			};
			testUrlSourceIPv4 = new URLFieldSource(testUrlIPv4);
			testUrlSourceIPv6 = new URLFieldSource(testUrlIPv6);
		} catch(MalformedURLException e) {
			throw new AssertionError(e);
		}
	}

	@Test
	public void testSchemeMatches() throws MalformedURLException {
		SinglePartialURL singleURL = PartialURL.valueOf("https", null, null, null, null);
		assertEquals(
			singleURL,
			PartialURL.valueOf(new String[] {"hTTps", "HTtp"}, null, null, null).matches(testUrlSource)
		);
	}

	@Test
	public void testSchemeNotMatches() throws MalformedURLException {
		assertNull(
			PartialURL.valueOf(new String[] {"other", "http"}, null, null, null).matches(testUrlSource)
		);
	}

	@Test
	public void testHostMatchesHostname() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = PartialURL.valueOf(null, HostAddress.valueOf("AOIndustries.COM"), null, null, null);
		assertEquals(
			singleURL,
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("WWW.AOIndustries.COM"), HostAddress.valueOf("AOIndustries.COM")}, null, null).matches(testUrlSource)
		);
	}

	@Test
	public void testSchemeNotMatchesHostname() throws ValidationException, MalformedURLException {
		assertNull(
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("WWW.AOIndustries.COM"), HostAddress.valueOf("www2.AOIndustries.COM")}, null, null).matches(testUrlSource)
		);
	}

	@Test
	public void testHostMatchesIPv4() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = PartialURL.valueOf(null, HostAddress.valueOf("192.0.2.38"), null, null, null);
		assertEquals(
			singleURL,
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("WWW.AOIndustries.COM"), HostAddress.valueOf("192.000.002.038"), HostAddress.valueOf("AOIndustries.COM")}, null, null).matches(testUrlSourceIPv4)
		);
	}

	@Test
	public void testSchemeNotMatchesIPv4() throws ValidationException, MalformedURLException {
		assertNull(
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("WWW.AOIndustries.COM"), HostAddress.valueOf("192.0.2.39"), HostAddress.valueOf("AOIndustries.COM")}, null, null).matches(testUrlSourceIPv4)
		);
	}

	@Test
	public void testHostMatchesIPv6() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = PartialURL.valueOf(null, HostAddress.valueOf("2001:db8::d0"), null, null, null);
		assertEquals(
			singleURL,
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("WWW.AOIndustries.COM"), HostAddress.valueOf("2001:db8::d0"), HostAddress.valueOf("AOIndustries.COM")}, null, null).matches(testUrlSourceIPv6)
		);
	}

	@Test
	public void testSchemeNotMatchesIPv6() throws ValidationException, MalformedURLException {
		assertNull(
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("WWW.AOIndustries.COM"), HostAddress.valueOf("2001:db8::d1"), HostAddress.valueOf("AOIndustries.COM")}, null, null).matches(testUrlSourceIPv6)
		);
	}

	@Test
	public void testHostMatchesIPv6Bracketed() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = PartialURL.valueOf(null, HostAddress.valueOf("[2001:db8::d0]"), null, null, null);
		assertEquals(
			singleURL,
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("WWW.AOIndustries.COM"), HostAddress.valueOf("[2001:db8::d0]"), HostAddress.valueOf("AOIndustries.COM")}, null, null).matches(testUrlSourceIPv6)
		);
	}

	@Test
	public void testSchemeNotMatchesIPv6Bracketed() throws ValidationException, MalformedURLException {
		assertNull(
			PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("WWW.AOIndustries.COM"), HostAddress.valueOf("[2001:db8::d1]"), HostAddress.valueOf("AOIndustries.COM")}, null, null).matches(testUrlSourceIPv6)
		);
	}

	@Test
	public void testPortMatches() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = PartialURL.valueOf(null, null, Port.valueOf(443, Protocol.TCP), null, null);
		assertEquals(
			singleURL,
			PartialURL.valueOf(null, null, new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(443, Protocol.TCP)}, null).matches(testUrlSource)
		);
	}

	@Test
	public void testPortNotMatchesPort() throws ValidationException, MalformedURLException {
		assertNull(
			PartialURL.valueOf(null, null, new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(8443, Protocol.TCP)}, null).matches(testUrlSource)
		);
	}

	@Test
	public void testPortNotMatchesProtocol() throws ValidationException, MalformedURLException {
		assertNull(
			PartialURL.valueOf(null, null, new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(443, Protocol.UDP)}, null).matches(testUrlSource)
		);
	}

	@Test
	public void testContextPathMatches() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = PartialURL.valueOf(null, null, null, Path.valueOf("/context"), null);
		assertEquals(
			singleURL,
			PartialURL.valueOf(null, null, null, new Path[] {Path.valueOf("/context"), Path.ROOT}).matches(testUrlSource)
		);
	}

	@Test
	public void testContextPathNotMatchesSubpath() throws ValidationException, MalformedURLException {
		assertNull(
			PartialURL.valueOf(null, null, null, new Path[] {Path.valueOf("/context/sub"), Path.ROOT}).matches(testUrlSource)
		);
	}

	@Test
	public void testPrefixMatchesRoot() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = PartialURL.valueOf(null, null, null, null, Path.ROOT);
		assertEquals(
			singleURL,
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/xyzxyz/"), Path.ROOT}).matches(testUrlSource)
		);
		assertEquals(
			singleURL,
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/xyzxyz/"), Path.ROOT}).matches(testUrlSourceIPv4)
		);
		assertEquals(
			singleURL,
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/xyzxyz/"), Path.ROOT}).matches(testUrlSourceIPv6)
		);
	}

	@Test
	public void testPrefixMatchesContact() throws ValidationException, MalformedURLException {
		SinglePartialURL singleURL = PartialURL.valueOf(null, null, null, null, Path.valueOf("/contact/"));
		assertNull(
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/xyzxyz/"), Path.valueOf("/contact/")}).matches(testUrlSource)
		);
		assertEquals(
			singleURL,
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/xyzxyz/"), Path.valueOf("/contact/")}).matches(testUrlSourceIPv4)
		);
		assertEquals(
			singleURL,
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/xyzxyz/"), Path.valueOf("/contact/")}).matches(testUrlSourceIPv6)
		);
	}

	@Test
	public void testPrefixNotMatchesContactOther() throws ValidationException, MalformedURLException {
		assertNull(
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/xyzxyz/"), Path.valueOf("/contact/other/")}).matches(testUrlSource)
		);
		assertNull(
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/xyzxyz/"), Path.valueOf("/contact/other/")}).matches(testUrlSourceIPv4)
		);
		assertNull(
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/xyzxyz/"), Path.valueOf("/contact/other/")}).matches(testUrlSourceIPv6)
		);
	}

	@Test
	public void testPrefixMatchesOrdering() throws ValidationException, MalformedURLException {
		assertEquals(
			PartialURL.valueOf(null, null, null, null, Path.valueOf("/contact/")),
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/xyzxyz/"), Path.valueOf("/contact/")}).matches(testUrlSourceIPv6)
		);
		assertEquals(
			PartialURL.valueOf(null, null, null, null, Path.valueOf("/contact/")),
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/"), Path.valueOf("/contact/")}).matches(testUrlSourceIPv6)
		);
		assertEquals(
			PartialURL.valueOf(null, null, null, null, Path.valueOf("/contact/")),
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/contact/"), Path.valueOf("/")}).matches(testUrlSourceIPv6)
		);
		assertEquals(
			PartialURL.valueOf(null, null, null, null, Path.valueOf("/contact/")),
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/contact/"), Path.valueOf("/contact/other/")}).matches(testUrlSourceIPv6)
		);
		assertEquals(
			PartialURL.valueOf(null, null, null, null, Path.ROOT),
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.ROOT, Path.valueOf("/contact/xyz/"), Path.valueOf("/contact/other/")}).matches(testUrlSourceIPv6)
		);
		assertEquals(
			PartialURL.valueOf(null, null, null, null, Path.ROOT),
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/contact/xyz/"), Path.ROOT, Path.valueOf("/contact/other/")}).matches(testUrlSourceIPv6)
		);
		assertEquals(
			PartialURL.valueOf(null, null, null, null, Path.ROOT),
			PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/contact/xyz/"), Path.valueOf("/contact/other/"), Path.ROOT}).matches(testUrlSourceIPv6)
		);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test isComplete">
	@Test
	public void testIsComplete() throws ValidationException {
		assertTrue(
			"Is complete with prefix",
			PartialURL.valueOf(
				new String[] {"https", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("www.aoindustries.com")},
				new Port[] {Port.valueOf(443, Protocol.TCP), Port.valueOf(80, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/old-root")},
				Path.ROOT
			).isComplete()
		);
		assertTrue(
			"Is also complete without prefix",
			PartialURL.valueOf(
				new String[] {"https", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("www.aoindustries.com")},
				new Port[] {Port.valueOf(443, Protocol.TCP), Port.valueOf(80, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/old-root")}
			).isComplete()
		);
	}

	@Test
	public void testIncompleteScheme() throws ValidationException {
		assertFalse(
			PartialURL.valueOf(
				null,
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("www.aoindustries.com")},
				new Port[] {Port.valueOf(443, Protocol.TCP), Port.valueOf(80, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/old-root")},
				Path.ROOT
			).isComplete()
		);
	}

	@Test
	public void testIncompleteHost() throws ValidationException {
		assertFalse(
			PartialURL.valueOf(
				new String[] {"https", "http"},
				new HostAddress[] {null, null},
				new Port[] {Port.valueOf(443, Protocol.TCP), Port.valueOf(80, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/old-root")},
				Path.ROOT
			).isComplete()
		);
	}

	@Test
	public void testIncompletePort() throws ValidationException {
		assertFalse(
			PartialURL.valueOf(
				new String[] {"https", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("www.aoindustries.com")},
				null,
				new Path[] {Path.ROOT, Path.valueOf("/old-root")},
				Path.ROOT
			).isComplete()
		);
	}

	@Test
	public void testIncompleteContextPath() throws ValidationException {
		assertFalse(
			PartialURL.valueOf(
				new String[] {"https", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("www.aoindustries.com")},
				new Port[] {Port.valueOf(443, Protocol.TCP), Port.valueOf(80, Protocol.TCP)},
				null,
				Path.ROOT
			).isComplete()
		);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test getPrimary">
	@Test
	public void testGetPrimaryComplete() throws ValidationException {
		assertEquals(
			PartialURL.valueOf(
				"https",
				HostAddress.valueOf("aoindustries.com"),
				Port.valueOf(443, Protocol.TCP),
				Path.ROOT,
				Path.ROOT
			),
			PartialURL.valueOf(
				new String[] {"https", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("www.aoindustries.com")},
				new Port[] {Port.valueOf(443, Protocol.TCP), Port.valueOf(80, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/old-root")},
				new Path[] {Path.ROOT, Path.valueOf("/old-prefix/")}
			).getPrimary()
		);
		assertEquals(
			PartialURL.valueOf(
				"https",
				HostAddress.valueOf("aoindustries.com"),
				Port.valueOf(443, Protocol.TCP),
				Path.valueOf("/old-root"),
				Path.ROOT
			),
			PartialURL.valueOf(
				new String[] {"https", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("www.aoindustries.com")},
				new Port[] {Port.valueOf(443, Protocol.TCP), Port.valueOf(80, Protocol.TCP)},
				new Path[] {Path.valueOf("/old-root"), Path.ROOT},
				new Path[] {Path.ROOT, Path.valueOf("/old-prefix/")}
			).getPrimary()
		);
		assertEquals(
			PartialURL.valueOf(
				"https",
				HostAddress.valueOf("aoindustries.com"),
				Port.valueOf(443, Protocol.TCP),
				Path.ROOT,
				Path.valueOf("/old-prefix/")
			),
			PartialURL.valueOf(
				new String[] {"https", "http"},
				new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("www.aoindustries.com")},
				new Port[] {Port.valueOf(443, Protocol.TCP), Port.valueOf(80, Protocol.TCP)},
				new Path[] {Path.ROOT, Path.valueOf("/old-root")},
				new Path[] {Path.valueOf("/old-prefix/"), Path.ROOT}
			).getPrimary()
		);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test getCombinations">
	@Test
	public void testGetCombinationsPrimaryFirst() throws ValidationException {
		MultiPartialURL multiURL = (MultiPartialURL)PartialURL.valueOf(
			new String[] {"https"},
			new HostAddress[] {HostAddress.valueOf("aoindustries.com")},
			new Port[] {Port.valueOf(443, Protocol.TCP)},
			new Path[] {Path.ROOT},
			new Path[] {Path.valueOf("/old-prefix/"), Path.ROOT}
		);
		Iterator<SinglePartialURL> iter = multiURL.getCombinations().iterator();
		assertEquals(
			multiURL.getPrimary(),
			iter.next()
		);
	}

	@Test
	public void testGetCombinationsPrimarySecond() throws ValidationException {
		MultiPartialURL multiURL = (MultiPartialURL)PartialURL.valueOf(
			new String[] {"https"},
			new HostAddress[] {HostAddress.valueOf("aoindustries.com")},
			new Port[] {Port.valueOf(443, Protocol.TCP)},
			new Path[] {Path.ROOT},
			new Path[] {Path.ROOT, Path.valueOf("/old-prefix/")}
		);
		Iterator<SinglePartialURL> iter = multiURL.getCombinations().iterator();
		// Skip first
		iter.next();
		assertEquals(
			multiURL.getPrimary(),
			iter.next()
		);
	}

	@Test(expected = NoSuchElementException.class)
	public void testGetCombinationsOnlyTwoCombinations() throws ValidationException {
		MultiPartialURL multiURL = (MultiPartialURL)PartialURL.valueOf(
			new String[] {"https"},
			new HostAddress[] {HostAddress.valueOf("aoindustries.com")},
			new Port[] {Port.valueOf(443, Protocol.TCP)},
			new Path[] {Path.ROOT},
			new Path[] {Path.ROOT, Path.valueOf("/old-prefix/")}
		);
		Iterator<SinglePartialURL> iter = multiURL.getCombinations().iterator();
		iter.next();
		iter.next();
		iter.next(); // Should fail
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test toURL">

	@Test
	public void testToURLNoScheme() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("HTTPS", "aorepo.org", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				null,
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
	}

	@Test
	public void testToURLSelectScheme() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("https", "aorepo.org", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTP", "HTTPS"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
		assertEquals(
			new URL("https", "aorepo.org", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTPS", "HTTP"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
	}

	@Test
	public void testToURLNoSelectScheme() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("http", "aorepo.org", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTP", "FTP"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
		assertEquals(
			new URL("ftp", "aorepo.org", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"FTP", "HTTP"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
	}

	@Test
	public void testToURLNoHost() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("https", "aoindustries.com", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTP", "HTTPS"},
				null,
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
	}

	@Test
	public void testToURLNoSelectHost() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("https", "aorepo.org", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTP", "HTTPS"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
		assertEquals(
			new URL("https", "www.aorepo.org", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTPS", "HTTP"},
				new HostAddress[] {HostAddress.valueOf("www.aorepo.org"), HostAddress.valueOf("aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
	}

	@Test
	public void testToURLSelectHost() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("https", "AOIndustries.COM", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTP", "HTTPS"},
				new HostAddress[] {HostAddress.valueOf("WWW.AOIndustries.COM"), HostAddress.valueOf("AOIndustries.COM")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
		assertEquals(
			new URL("https", "AOIndustries.COM", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTPS", "HTTP"},
				new HostAddress[] {HostAddress.valueOf("AOIndustries.COM"), HostAddress.valueOf("WWW.AOIndustries.COM")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
	}

	@Test
	public void testToURLNoPort() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("https", "aorepo.org", -1, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTP", "HTTPS"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				null,
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
	}

	@Test
	public void testToURLNoSelectPort() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("https", "aorepo.org", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTP", "HTTPS"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
		assertEquals(
			new URL("https", "aorepo.org", 81, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTPS", "HTTP"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(81, Protocol.TCP), Port.valueOf(80, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
	}

	@Test
	public void testToURLSelectPort() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("https", "aorepo.org", -1, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTP", "HTTPS"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(443, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
		assertEquals(
			new URL("https", "aorepo.org", -1, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTPS", "HTTP"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(443, Protocol.TCP), Port.valueOf(80, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
	}

	@Test
	public void testToURLNoContextPath() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("https", "aorepo.org", 80, "/context/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTP", "HTTPS"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				null,
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
	}

	@Test
	public void testToURLNoSelectContextPath() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("https", "aorepo.org", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTP", "HTTPS"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
		assertEquals(
			new URL("https", "aorepo.org", 80, "/otherContext2/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTPS", "HTTP"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext2"), Path.valueOf("/otherContext")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
	}

	@Test
	public void testToURLSelectContextPath() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("https", "aorepo.org", 80, "/context/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTP", "HTTPS"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/context")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
		assertEquals(
			new URL("https", "aorepo.org", 80, "/context/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTPS", "HTTP"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/context"), Path.valueOf("/otherContext")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(testUrlSource)
		);
	}

	@Test
	public void testToURLNoPrefix() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("https", "aorepo.org", 80, "/otherContext"),
			PartialURL.valueOf(
				new String[] {"HTTPS", "HTTP"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")}
			).toURL(null)
		);
	}

	@Test
	public void testToURLComplete() throws ValidationException, MalformedURLException {
		assertEquals(
			new URL("https", "aorepo.org", 80, "/otherContext/otherPath/"),
			PartialURL.valueOf(
				new String[] {"HTTPS", "HTTP"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath/"), Path.valueOf("/otherPath2/")}
			).toURL(null)
		);
		assertEquals(
			new URL("https", "aorepo.org", 80, "/otherContext/otherPath2/"),
			PartialURL.valueOf(
				new String[] {"HTTPS", "HTTP"},
				new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")},
				new Port[] {Port.valueOf(80, Protocol.TCP), Port.valueOf(81, Protocol.TCP)},
				new Path[] {Path.valueOf("/otherContext"), Path.valueOf("/otherContext2")},
				new Path[] {Path.valueOf("/otherPath2/"), Path.valueOf("/otherPath/")}
			).toURL(null)
		);
	}
	// </editor-fold>
}
