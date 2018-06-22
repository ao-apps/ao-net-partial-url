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

import com.aoindustries.net.Path;
import com.aoindustries.validation.ValidationException;
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
		assertSame(PartialURL.DEFAULT, PartialURL.of((Path)null));
		assertSame(PartialURL.DEFAULT, PartialURL.of((String)null, null, null, null, null));
	}

	@Test
	public void testOfLowerScheme() {
		assertEquals(
			"https",
			PartialURL.of("HTTPS", null, null, null, null).getScheme()
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

	@Test(expected = IllegalArgumentException.class)
	public void testOfPathNoTrailingSlash() throws ValidationException {
		PartialURL.of(Path.valueOf("/path"));
	}

	@Test
	public void testOfContextPathAsRoot() throws ValidationException {
		assertEquals(
			Path.ROOT,
			PartialURL.of(null, null, null, Path.ROOT, null).getContextPath()
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOfContextPathWithTrailingSlash() throws ValidationException {
		PartialURL.of(null, null, null, Path.valueOf("/context/"), null);
	}
	// </editor-fold>
}
