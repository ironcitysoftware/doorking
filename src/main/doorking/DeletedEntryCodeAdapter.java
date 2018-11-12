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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

/** Adapts a custom spreadsheet to a set of entry codes. */
public class DeletedEntryCodeAdapter {
  private final List<List<Object>> rows;

  public DeletedEntryCodeAdapter(List<List<Object>> rows) {
    this.rows = rows;
  }

  private static final int COLUMN_ENTRY_CODE = 0;

  public Set<Integer> adapt() {
    Set<Integer> codes = new HashSet<>();
    for (List<Object> row : rows) {
      int codeDigits = Integer.parseInt((String) row.get(COLUMN_ENTRY_CODE));
      Preconditions.checkState(codes.add(codeDigits), 
          String.format("Duplicate deleted entry code %04d", codeDigits));
    }
    return codes;
  }
}