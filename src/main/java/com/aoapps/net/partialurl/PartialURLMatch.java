/*
 * ao-net-partial-url - Matches and resolves partial URLs.
 * Copyright (C) 2018, 2019, 2020, 2021, 2022, 2023, 2024  AO Industries, Inc.
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

import java.net.URL;
import java.util.Objects;

/**
 * The result of a call to {@link PartialURLMap#get(com.aoapps.net.partialurl.FieldSource)}.
 */
public class PartialURLMatch<V> {

  private final PartialURL partialUrl;
  private final SinglePartialURL singleUrl;
  private final URL url;
  private final V value;

  PartialURLMatch(
      PartialURL partialUrl,
      SinglePartialURL singleUrl,
      URL url,
      V value
  ) {
    this.partialUrl = partialUrl;
    this.singleUrl = singleUrl;
    this.url = url;
    this.value = value;
  }

  @Override
  public String toString() {
    if (partialUrl == singleUrl) {
      return singleUrl + " → " + url;
    } else {
      return partialUrl + " → " + singleUrl + " → " + url;
    }
  }

  /**
   * Two matches are equal when they have the same partialUrl (by .equals),
   * singleUrl (by .equals), url (by .equals), and value (by identity).
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PartialURLMatch<?>)) {
      return false;
    }
    PartialURLMatch<?> other = (PartialURLMatch<?>) o;
    return
        value == other.value
            && partialUrl.equals(other.partialUrl)
            && singleUrl.equals(other.singleUrl)
            && url.equals(other.url);
  }

  @Override
  public int hashCode() {
    int hash = partialUrl.hashCode();
    hash = hash * 31 + singleUrl.hashCode();
    hash = hash * 31 + url.hashCode();
    hash = hash * 31 + Objects.hashCode(value);
    return hash;
  }

  /**
   * Gets the partial URL that matched the lookup.
   * This might be a {@link MultiPartialURL}.
   */
  public PartialURL getPartialURL() {
    return partialUrl;
  }

  /**
   * Gets the single partial URL that matched the lookup.
   * This will be the same object as {@link PartialURLMatch#getPartialURL()} when it is a
   * {@link SinglePartialURL}.  Will be one of the {@link MultiPartialURL#getCombinations()}
   * when is a {@link MultiPartialURL}.
   */
  public SinglePartialURL getSingleURL() {
    return singleUrl;
  }

  /**
   * Gets the completed {@link URL}, with {@code null} fields selected from the {@link FieldSource} used in
   * {@link PartialURLMap#get(com.aoapps.net.partialurl.FieldSource)}.
   *
   * <p><b>Implementation Note:</b><br>
   * this implementation uses {@link SinglePartialURL#toURL(com.aoapps.net.partialurl.FieldSource)} on
   * {@link PartialURLMatch#getSingleURL()}.</p>
   *
   * @see  PartialURLMatch#getSingleURL()
   * @see  SinglePartialURL#toURL(com.aoapps.net.partialurl.FieldSource)
   */
  public URL getUrl() {
    return url;
  }

  /**
   * Gets the value associated with the partial URL.
   */
  public V getValue() {
    return value;
  }
}
