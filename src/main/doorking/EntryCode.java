/**
 * Copyright 2018 Iron City Software LLC
 *
 * This file is part of DoorKing.
 *
 * DoorKing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DoorKing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DoorKing.  If not, see <http://www.gnu.org/licenses/>.
 */

package doorking;

import java.util.Objects;

/** Container for an entry code and the associated type. */
public class EntryCode {
  enum EntryCodeType {
    PERMANENT,
    LIMITED,
    DELIVERY;
  }

  EntryCode(int code, EntryCodeType type) {
    this.code = code;
    this.type = type;
  }

  final int code;
  final EntryCodeType type;

  @Override
  public int hashCode() {
    return Objects.hash(code, type);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof EntryCode)) {
      return false;
    }
    EntryCode that = (EntryCode) obj;
    return code == that.code && type == that.type;
  }

  @Override
  public String toString() {
    return String.format("%04d %s", code, type.name());
  }
}