/*
 * ao-net-partial-url - Matches and resolves partial URLs.
 * Copyright (C) 2018, 2019, 2021  AO Industries, Inc.
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
package com.aoapps.net.partialurl;

import com.aoapps.lang.validation.ValidationException;
import com.aoapps.net.HostAddress;
import com.aoapps.net.Path;
import com.aoapps.net.Port;
import com.aoapps.net.Protocol;
import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * @see PartialURLMap
 *
 * @author  AO Industries, Inc.
 */
public class PartialURLMapTest {

	private static final SinglePartialURL httpsOnly;
	private static final SinglePartialURL httpOnly;
	private static final SinglePartialURL aorepoOnly;
	private static final SinglePartialURL wwwAorepoOnly;
	private static final SinglePartialURL port443Only;
	private static final SinglePartialURL port80Only;
	private static final SinglePartialURL contextOnly;
	private static final SinglePartialURL contextSubOnly;
	private static final SinglePartialURL prefixOnly;
	private static final SinglePartialURL prefixSubOnly;
	private static final PartialURL schemesOnly;
	private static final PartialURL hostsOnly;
	private static final PartialURL portsOnly;
	private static final PartialURL contextsOnly;
	private static final PartialURL prefixesOnly;
	static {
		try {
			httpsOnly = PartialURL.valueOf("https", null, null, null, null);
			httpOnly = PartialURL.valueOf("http", null, null, null, null);
			aorepoOnly = PartialURL.valueOf(null, HostAddress.valueOf("aorepo.org"), null, null, null);
			wwwAorepoOnly = PartialURL.valueOf(null, HostAddress.valueOf("www.aorepo.org"), null, null, null);
			port443Only = PartialURL.valueOf(null, null, Port.valueOf(443, Protocol.TCP), null, null);
			port80Only = PartialURL.valueOf(null, null, Port.valueOf(80, Protocol.TCP), null, null);
			contextOnly = PartialURL.valueOf(null, null, null, Path.valueOf("/context"), null);
			contextSubOnly = PartialURL.valueOf(null, null, null, Path.valueOf("/context/sub"), null);
			prefixOnly = PartialURL.valueOf(null, null, null, null, Path.valueOf("/prefix/"));
			prefixSubOnly = PartialURL.valueOf(null, null, null, null, Path.valueOf("/prefix/sub/"));
			schemesOnly = PartialURL.valueOf(new String[] {"https", "http"}, null, null, null);
			hostsOnly = PartialURL.valueOf(null, new HostAddress[] {HostAddress.valueOf("aorepo.org"), HostAddress.valueOf("www.aorepo.org")}, null, null);
			portsOnly = PartialURL.valueOf(null, null, new Port[] {Port.valueOf(443, Protocol.TCP), Port.valueOf(80, Protocol.TCP)}, null);
			contextsOnly = PartialURL.valueOf(null, null, null, new Path[] {Path.valueOf("/context"), Path.valueOf("/context/sub")});
			prefixesOnly = PartialURL.valueOf(null, null, null, null, new Path[] {Path.valueOf("/prefix/"), Path.valueOf("/prefix/sub/")});
		} catch(ValidationException e) {
			throw new AssertionError(e);
		}
	}

	// <editor-fold defaultstate="collapsed" desc="Test single fields with single values">
	private static PartialURLMap<Integer> getTestSingleFieldSingleMap() {
		PartialURLMap<Integer> testMap = new PartialURLMap<>();
		testMap.put(httpsOnly, 1);
		testMap.put(aorepoOnly, 2);
		testMap.put(port443Only, 3);
		testMap.put(contextOnly, 4);
		testMap.put(prefixOnly, 5);
		return testMap;
	}

	@Test
	public void testGetBySchemeMatches() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				httpsOnly,
				httpsOnly,
				new URL("https://aoindustries.com:80"),
				1
			),
			getTestSingleFieldSingleMap().get(new URLFieldSource(new URL("HTTPS://aoindustries.com:80/")))
		);
	}

	@Test
	public void testGetBySchemeNotMatches() throws MalformedURLException {
		assertNull(
			getTestSingleFieldSingleMap().get(new URLFieldSource(new URL("HTTP://aoindustries.com:80/")))
		);
	}

	@Test
	public void testGetByHostMatches() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				aorepoOnly,
				aorepoOnly,
				new URL("http://aorepo.org"),
				2
			),
			getTestSingleFieldSingleMap().get(new URLFieldSource(new URL("http://AOREPO.ORG:80/")))
		);
	}

	@Test
	public void testGetByHostNotMatches() throws MalformedURLException {
		assertNull(
			getTestSingleFieldSingleMap().get(new URLFieldSource(new URL("http://WWW.AOREPO.ORG:80/")))
		);
	}

	@Test
	public void testGetByPortMatches() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				port443Only,
				port443Only,
				new URL("http://aoindustries.com:443"),
				3
			),
			getTestSingleFieldSingleMap().get(new URLFieldSource(new URL("http://aoindustries.com:443/")))
		);
	}

	@Test
	public void testGetByPortNotMatches() throws MalformedURLException {
		assertNull(
			getTestSingleFieldSingleMap().get(new URLFieldSource(new URL("http://aoindustries.com:80/")))
		);
	}

	@Test
	public void testGetByContextMatches() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				contextOnly,
				contextOnly,
				new URL("http://aoindustries.com:80/context"),
				4
			),
			getTestSingleFieldSingleMap().get(
				new URLFieldSource(new URL("http://aoindustries.com:80/")) {
					@Override
					public Path getContextPath() {
						try {
							return Path.valueOf("/context");
						} catch(ValidationException e) {
							throw new AssertionError(e);
						}
					}
				}
			)
		);
	}

	@Test
	public void testGetByContextNotMatchesPrefix() throws MalformedURLException {
		assertNull(
			getTestSingleFieldSingleMap().get(
				new URLFieldSource(new URL("http://aoindustries.com:80/")) {
					@Override
					public Path getContextPath() {
						return Path.ROOT;
					}
				}
			)
		);
	}

	@Test
	public void testGetByContextNotMatchesSuffix() throws MalformedURLException {
		assertNull(
			getTestSingleFieldSingleMap().get(
				new URLFieldSource(new URL("http://aoindustries.com:80/")) {
					@Override
					public Path getContextPath() {
						try {
							return Path.valueOf("/context/other");
						} catch(ValidationException e) {
							throw new AssertionError(e);
						}
					}
				}
			)
		);
	}

	@Test
	public void testGetByPrefixMatchesExact() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				prefixOnly,
				prefixOnly,
				new URL("http://aoindustries.com:80/prefix/"),
				5
			),
			getTestSingleFieldSingleMap().get(new URLFieldSource(new URL("http://aoindustries.com:80/prefix/")))
		);
	}

	@Test
	public void testGetByPrefixMatchesSuffix() throws MalformedURLException {
		// TODO: Return and test path in result: /suffix in this case
		assertEquals(
			new PartialURLMatch<>(
				prefixOnly,
				prefixOnly,
				new URL("http://aoindustries.com:80/prefix/"),
				5
			),
			getTestSingleFieldSingleMap().get(new URLFieldSource(new URL("http://aoindustries.com:80/prefix/suffix")))
		);
	}

	@Test
	public void testGetByPrefixNotMatchesNoSlash() throws MalformedURLException {
		assertNull(
			getTestSingleFieldSingleMap().get(new URLFieldSource(new URL("http://aoindustries.com:80/prefix")))
		);
	}

	@Test
	public void testGetByPrefixNotMatchesRoot() throws MalformedURLException {
		assertNull(
			getTestSingleFieldSingleMap().get(new URLFieldSource(new URL("http://aoindustries.com:80/")))
		);
	}

	@Test
	public void testGetByPrefixNotMatchesBlank() throws MalformedURLException {
		assertNull(
			getTestSingleFieldSingleMap().get(new URLFieldSource(new URL("http://aoindustries.com:80")))
		);
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Test single fields with multiple values">
	private static PartialURLMap<Integer> getTestSingleFieldMultiMap() {
		PartialURLMap<Integer> testMap = new PartialURLMap<>();
		testMap.put(schemesOnly, 1);
		testMap.put(hostsOnly, 2);
		testMap.put(portsOnly, 3);
		testMap.put(contextsOnly, 4);
		testMap.put(prefixesOnly, 5);
		return testMap;
	}

	@Test
	public void testGetBySchemesMatches1() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				schemesOnly,
				httpsOnly,
				new URL("https://aoindustries.com:81"),
				1
			),
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("HTTPS://aoindustries.com:81/")))
		);
	}

	@Test
	public void testGetBySchemesMatches2() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				schemesOnly,
				httpOnly,
				new URL("http://aoindustries.com:81"),
				1
			),
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("HTTP://aoindustries.com:81/")))
		);
	}

	@Test
	public void testGetBySchemesNotMatches() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("FTP://aoindustries.com:81/")))
		);
	}

	@Test
	public void testGetByHostsMatches1() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				hostsOnly,
				aorepoOnly,
				new URL("ftp://aorepo.org:81"),
				2
			),
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://AOREPO.ORG:81/")))
		);
	}

	@Test
	public void testGetByHostsMatches2() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				hostsOnly,
				wwwAorepoOnly,
				new URL("ftp://www.aorepo.org:81"),
				2
			),
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://WWW.AOREPO.ORG:81/")))
		);
	}

	@Test
	public void testGetByHostsNotMatches() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://WWW2.AOREPO.ORG:81/")))
		);
	}

	@Test
	public void testGetByPortMatches1() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				portsOnly,
				port443Only,
				new URL("ftp://aoindustries.com:443"),
				3
			),
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://aoindustries.com:443/")))
		);
	}

	@Test
	public void testGetByPortMatches2() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				portsOnly,
				port80Only,
				new URL("ftp://aoindustries.com:80"),
				3
			),
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://aoindustries.com:80/")))
		);
	}

	@Test
	public void testGetByPortsNotMatches() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://aoindustries.com:81/")))
		);
	}

	@Test
	public void testGetByContextsMatches1() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				contextsOnly,
				contextOnly,
				new URL("ftp://aoindustries.com:81/context"),
				4
			),
			getTestSingleFieldMultiMap().get(
				new URLFieldSource(new URL("ftp://aoindustries.com:81/")) {
					@Override
					public Path getContextPath() {
						try {
							return Path.valueOf("/context");
						} catch(ValidationException e) {
							throw new AssertionError(e);
						}
					}
				}
			)
		);
	}

	@Test
	public void testGetByContextsMatches2() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				contextsOnly,
				contextSubOnly,
				new URL("ftp://aoindustries.com:81/context/sub"),
				4
			),
			getTestSingleFieldMultiMap().get(
				new URLFieldSource(new URL("ftp://aoindustries.com:81/")) {
					@Override
					public Path getContextPath() {
						try {
							return Path.valueOf("/context/sub");
						} catch(ValidationException e) {
							throw new AssertionError(e);
						}
					}
				}
			)
		);
	}

	@Test
	public void testGetByContextsNotMatchesPrefix() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMultiMap().get(
				new URLFieldSource(new URL("ftp://aoindustries.com:81/")) {
					@Override
					public Path getContextPath() {
						return Path.ROOT;
					}
				}
			)
		);
	}

	@Test
	public void testGetByContextsNotMatchesSuffix() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMultiMap().get(
				new URLFieldSource(new URL("ftp://aoindustries.com:81/")) {
					@Override
					public Path getContextPath() {
						try {
							return Path.valueOf("/context/other");
						} catch(ValidationException e) {
							throw new AssertionError(e);
						}
					}
				}
			)
		);
	}

	@Test
	public void testGetByPrefixesMatchesExact1() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				prefixesOnly,
				prefixOnly,
				new URL("ftp://aoindustries.com:81/prefix/"),
				5
			),
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://aoindustries.com:81/prefix/")))
		);
	}

	@Test
	public void testGetByPrefixesMatchesExact2() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				prefixesOnly,
				prefixSubOnly,
				new URL("ftp://aoindustries.com:81/prefix/sub/"),
				5
			),
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://aoindustries.com:81/prefix/sub/")))
		);
	}

	@Test
	public void testGetByPrefixesMatchesSuffix1() throws MalformedURLException {
		// TODO: Return and test path in result: /suffix in this case
		assertEquals(
			new PartialURLMatch<>(
				prefixesOnly,
				prefixOnly,
				new URL("ftp://aoindustries.com:81/prefix/"),
				5
			),
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://aoindustries.com:81/prefix/suffix")))
		);
	}

	@Test
	public void testGetByPrefixesMatchesSuffix2() throws MalformedURLException {
		// TODO: Return and test path in result: /suffix in this case
		assertEquals(
			new PartialURLMatch<>(
				prefixesOnly,
				prefixSubOnly,
				new URL("ftp://aoindustries.com:81/prefix/sub/"),
				5
			),
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://aoindustries.com:81/prefix/sub/suffix")))
		);
	}

	@Test
	public void testGetByPrefixesNotMatchesNoSlash1() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://aoindustries.com:81/prefix")))
		);
	}

	@Test
	public void testGetByPrefixesNotMatchesNoSlash2() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<>(
				prefixesOnly,
				prefixOnly,
				new URL("ftp://aoindustries.com:81/prefix/"),
				5
			),
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://aoindustries.com:81/prefix/sub")))
		);
	}

	@Test
	public void testGetByPrefixesNotMatchesRoot() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://aoindustries.com:81/")))
		);
	}

	@Test
	public void testGetByPrefixesNotMatchesBlank() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMultiMap().get(new URLFieldSource(new URL("ftp://aoindustries.com:81")))
		);
	}
	// </editor-fold>

	// TODO: Test multiple fields with multiple values, while testing ordering when multiple fields match

}
