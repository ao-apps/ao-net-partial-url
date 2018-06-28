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
import org.junit.Test;

/**
 * @see PartialURLMap
 *
 * @author  AO Industries, Inc.
 */
public class PartialURLMapTest {

	// <editor-fold defaultstate="collapsed" desc="Test single fields with single values">
	private static final SinglePartialURL httpsOnly;
	private static final SinglePartialURL aorepoOnly;
	private static final SinglePartialURL port443Only;
	private static final SinglePartialURL contextOnly;
	private static final SinglePartialURL prefixOnly;
	static {
		try {
			httpsOnly = PartialURL.valueOf("https", null, null, null, null);
			aorepoOnly = PartialURL.valueOf(null, HostAddress.valueOf("aorepo.org"), null, null, null);
			port443Only = PartialURL.valueOf(null, null, Port.valueOf(443, Protocol.TCP), null, null);
			contextOnly = PartialURL.valueOf(null, null, null, Path.valueOf("/context"), null);
			prefixOnly = PartialURL.valueOf(null, null, null, null, Path.valueOf("/prefix/"));
		} catch(ValidationException e) {
			throw new AssertionError(e);
		}
	}

	private static PartialURLMap<Integer> getTestSingleFieldMap() {
		PartialURLMap<Integer> testMap = new PartialURLMap<Integer>();
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
			new PartialURLMatch<Integer>(
				httpsOnly,
				httpsOnly,
				new URL("https://aoindustries.com:80"),
				1
			),
			getTestSingleFieldMap().get(new URLFieldSource(new URL("HTTPS://aoindustries.com:80/")))
		);
	}

	@Test
	public void testGetBySchemeNotMatches() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMap().get(new URLFieldSource(new URL("HTTP://aoindustries.com:80/")))
		);
	}

	@Test
	public void testGetByHostMatches() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<Integer>(
				aorepoOnly,
				aorepoOnly,
				new URL("http://aorepo.org"),
				2
			),
			getTestSingleFieldMap().get(new URLFieldSource(new URL("http://AOREPO.ORG:80/")))
		);
	}

	@Test
	public void testGetByHostNotMatches() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMap().get(new URLFieldSource(new URL("http://WWW.AOREPO.ORG:80/")))
		);
	}

	@Test
	public void testGetByPortMatches() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<Integer>(
				port443Only,
				port443Only,
				new URL("http://aoindustries.com:443"),
				3
			),
			getTestSingleFieldMap().get(new URLFieldSource(new URL("http://aoindustries.com:443/")))
		);
	}

	@Test
	public void testGetByPortNotMatches() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMap().get(new URLFieldSource(new URL("http://aoindustries.com:80/")))
		);
	}

	@Test
	public void testGetByContextMatches() throws MalformedURLException {
		assertEquals(
			new PartialURLMatch<Integer>(
				contextOnly,
				contextOnly,
				new URL("http://aoindustries.com:80/context"),
				4
			),
			getTestSingleFieldMap().get(
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
			getTestSingleFieldMap().get(
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
			getTestSingleFieldMap().get(
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
			new PartialURLMatch<Integer>(
				prefixOnly,
				prefixOnly,
				new URL("http://aoindustries.com:80/prefix/"),
				5
			),
			getTestSingleFieldMap().get(new URLFieldSource(new URL("http://aoindustries.com:80/prefix/")))
		);
	}

	@Test
	public void testGetByPrefixMatchesSuffix() throws MalformedURLException {
		// TODO: Return and test path in result: /suffix in this case
		assertEquals(
			new PartialURLMatch<Integer>(
				prefixOnly,
				prefixOnly,
				new URL("http://aoindustries.com:80/prefix/"),
				5
			),
			getTestSingleFieldMap().get(new URLFieldSource(new URL("http://aoindustries.com:80/prefix/suffix")))
		);
	}

	@Test
	public void testGetByPrefixNotMatchesRoot() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMap().get(new URLFieldSource(new URL("http://aoindustries.com:80/")))
		);
	}

	@Test
	public void testGetByPrefixNotMatchesBlank() throws MalformedURLException {
		assertNull(
			getTestSingleFieldMap().get(new URLFieldSource(new URL("http://aoindustries.com:80")))
		);
	}
	// </editor-fold>

	// TODO: Test multiple fields

	// TODO: Test ordering when multiple fields match

}
