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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

/**
 * @see PartialURL
 *
 * @author  AO Industries, Inc.
 */
public class PartialURLTest {

	// <editor-fold defaultstate="collapsed" desc="Test of">
	@Test
	public void testOfDefault() {
		List<String> emptySchemes = Collections.emptyList();
		List<String> nullSchemes = Arrays.asList(null, null, null, null);
		List<Path> emptyPrefixes = Collections.emptyList();
		List<Path> nullPrefixes = Arrays.asList(null, null, null, null);
		assertSame(PartialURL.DEFAULT, PartialURL.of((String)null, null, null, null, null));
		assertSame(PartialURL.DEFAULT, PartialURL.of((Path)null));
		assertSame(PartialURL.DEFAULT, PartialURL.of((Iterable<? extends String>)null, null, null, null, null));
		assertSame(PartialURL.DEFAULT, PartialURL.of(emptySchemes, null, null, null, null));
		assertSame(PartialURL.DEFAULT, PartialURL.of(nullSchemes, null, null, null, null));
		assertSame(PartialURL.DEFAULT, PartialURL.of((String[])null, null, null, null));
		assertSame(PartialURL.DEFAULT, PartialURL.of(new String[0], null, null, null));
		assertSame(PartialURL.DEFAULT, PartialURL.of(new String[] {null, null, null}, null, null, null));
		assertSame(PartialURL.DEFAULT, PartialURL.of((Iterable<? extends Path>)null));
		assertSame(PartialURL.DEFAULT, PartialURL.of(emptyPrefixes));
		assertSame(PartialURL.DEFAULT, PartialURL.of(nullPrefixes));
		assertSame(PartialURL.DEFAULT, PartialURL.of());
		assertSame(PartialURL.DEFAULT, PartialURL.of(new Path[0]));
		assertSame(PartialURL.DEFAULT, PartialURL.of(new Path[] {null, null, null}));
	}

	@Test
	public void testOfLowerScheme() {
		assertEquals(
			"https",
			PartialURL.of("HTTPS", null, null, null, null).getScheme()
		);
	}

	@Test
	public void testOfLowerSchemes() {
		assertEquals(
			new LinkedHashSet<String>(Arrays.asList("https", "http")),
			((MultiPartialURL)PartialURL.of(new String[] {"HTTPS", "HTTP"}, null, null, null)).getSchemes()
		);
	}

	@Test
	public void testOfPrefixOnly() throws ValidationException {
		Path path = Path.valueOf("/path/");
		assertEquals(
			PartialURL.of(path),
			PartialURL.of(null, null, null, null, path)
		);
	}

	@Test
	public void testOfPrefixesOnlyIterable() throws ValidationException {
		Path path = Path.valueOf("/path/");
		Path path2 = Path.valueOf("/path2/");
		assertEquals(
			PartialURL.of(path, path2),
			PartialURL.of(null, null, null, null, Arrays.asList(path, path2))
		);
	}

	@Test
	public void testOfPrefixesOnlyArray() throws ValidationException {
		Path path = Path.valueOf("/path/");
		Path path2 = Path.valueOf("/path2/");
		assertEquals(
			PartialURL.of(path, path2),
			PartialURL.of((String[])null, null, null, null, path, path2)
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOfPrefixNoTrailingSlash() throws ValidationException {
		PartialURL.of(Path.valueOf("/path"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOfPrefixesNoTrailingSlashIterable() throws ValidationException {
		PartialURL.of(Arrays.asList(Path.valueOf("/path"), Path.valueOf("/path2")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOfPrefixesNoTrailingSlashArray() throws ValidationException {
		PartialURL.of(Path.valueOf("/path"), Path.valueOf("/path2"));
	}

	@Test
	public void testOfContextPathAsRoot() {
		assertEquals(
			Path.ROOT,
			PartialURL.of(null, null, null, Path.ROOT, null).getContextPath()
		);
	}

	@Test
	public void testOfContextPathAsRootIterable() {
		assertEquals(
			Path.ROOT,
			((SinglePartialURL)PartialURL.of(null, null, null, Arrays.asList(Path.ROOT), null)).getContextPath()
		);
	}

	@Test
	public void testOfContextPathAsRootArray() {
		assertEquals(
			Path.ROOT,
			((SinglePartialURL)PartialURL.of(null, null, null, new Path[] {Path.ROOT})).getContextPath()
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOfContextPathWithTrailingSlash() throws ValidationException {
		PartialURL.of(null, null, null, Path.valueOf("/context/"), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOfContextPathWithTrailingSlashIterable() throws ValidationException {
		PartialURL.of(null, null, null, Arrays.asList(Path.valueOf("/context/"), Path.valueOf("/context2/")), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOfContextPathWithTrailingSlashArray() throws ValidationException {
		PartialURL.of(null, null, null, new Path[] {Path.valueOf("/context/"), Path.valueOf("/context2/")});
	}

	@Test
	public void testOfOneSchemeIsSingle() {
		SinglePartialURL singleURL = PartialURL.of("https", null, null, null, null);
		assertEquals(
			singleURL,
			PartialURL.of(new String[] {"https"}, null, null, null)
		);
		assertEquals(
			singleURL,
			PartialURL.of(new String[] {"hTTps", null}, null, null, null)
		);
		assertEquals(
			singleURL,
			PartialURL.of(new String[] {"https", "HTTPS"}, null, null, null)
		);
	}

	@Test
	public void testOfOneHostIsSingle() throws ValidationException {
		SinglePartialURL singleURL = PartialURL.of(null, HostAddress.valueOf("aoindustries.com"), null, null, null);
		assertEquals(
			singleURL,
			PartialURL.of(null, new HostAddress[] {HostAddress.valueOf("AOIndustries.COM")}, null, null)
		);
		assertEquals(
			singleURL,
			PartialURL.of(null, new HostAddress[] {HostAddress.valueOf("AOIndustries.COM"), null}, null, null)
		);
		assertEquals(
			singleURL,
			PartialURL.of(null, new HostAddress[] {HostAddress.valueOf("aoindustries.com"), HostAddress.valueOf("AOIndustries.COM")}, null, null)
		);
	}

	@Test
	public void testOfOnePortIsSingle() throws ValidationException {
		Port port80 = Port.valueOf(80, Protocol.TCP);
		SinglePartialURL singleURL = PartialURL.of(null, null, port80, null, null);
		assertEquals(
			singleURL,
			PartialURL.of(null, null, new Port[] {port80}, null)
		);
		assertEquals(
			singleURL,
			PartialURL.of(null, null, new Port[] {port80, null}, null)
		);
		assertEquals(
			singleURL,
			PartialURL.of(null, null, new Port[] {port80, port80}, null)
		);
	}

	@Test
	public void testOfOneContextPathIsSingle() throws ValidationException {
		Path contextPath = Path.valueOf("/context");
		SinglePartialURL singleURL = PartialURL.of(null, null, null, contextPath, null);
		assertEquals(
			singleURL,
			PartialURL.of(null, null, null, new Path[] {contextPath})
		);
		assertEquals(
			singleURL,
			PartialURL.of(null, null, null, new Path[] {contextPath, null})
		);
		assertEquals(
			singleURL,
			PartialURL.of(null, null, null, new Path[] {contextPath, contextPath})
		);
	}

	@Test
	public void testOfOnePrefixIsSingle() throws ValidationException {
		Path prefix = Path.valueOf("/prefix/");
		SinglePartialURL singleURL = PartialURL.of(null, null, null, null, prefix);
		assertEquals(
			singleURL,
			PartialURL.of(null, null, null, null, new Path[] {prefix})
		);
		assertEquals(
			singleURL,
			PartialURL.of(null, null, null, null, new Path[] {prefix, null})
		);
		assertEquals(
			singleURL,
			PartialURL.of(null, null, null, null, new Path[] {prefix, prefix})
		);
	}
	// </editor-fold>
}
