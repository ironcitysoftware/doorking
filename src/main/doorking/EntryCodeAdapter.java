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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import doorking.EntryCode.EntryCodeType;

/** Adapts a custom spreadsheet to a set of entry codes and types. */
public class EntryCodeAdapter {
  private final List<List<Object>> rows;
  private final Set<Integer> deletedCodes;

  public EntryCodeAdapter(List<List<Object>> rows, Set<Integer> deletedCodes) {
    this.rows = rows;
    this.deletedCodes = deletedCodes;
  }

  private static final int COLUMN_STREET = 0;
  private static final int COLUMN_HOUSE_NUMBER = 1;
  private static final int COLUMN_NAME = 2;
  private static final int COLUMN_ENTRY_CODE = 3;
  private static final int COLUMN_ENTRY_CODE_TYPE = 4;
  private static final int COLUMN_NOTES = 5;

  public class EntryCodes {
    public EntryCodes(
        SetMultimap<Integer, EntryCode> residentCodes,
        Map<String, EntryCode> vendorCodes,
        Map<String, EntryCode> legacyResidentCodes) {
      this.residentCodes = residentCodes;
      this.vendorCodes = vendorCodes;
      this.legacyResidentCodes = legacyResidentCodes;
    }

    public EntryCode lookupAndRemoveResidentCode(int key, EntryCodeType type) {
      EntryCode foundCode = null;
      if (residentCodes.containsKey(key)) {
        for (EntryCode code : residentCodes.get(key)) {
          if (code.type == type) {
            foundCode = code;
          }
        }
      }
      if (foundCode != null) {
        Preconditions.checkState(residentCodes.remove(key, foundCode));
      }
      return foundCode;
    }

    final SetMultimap<Integer, EntryCode> residentCodes;
    final Map<String, EntryCode> vendorCodes;
    final Map<String, EntryCode> legacyResidentCodes;

    @Override
    public int hashCode() {
      return Objects.hash(residentCodes, vendorCodes, legacyResidentCodes);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof EntryCodes)) {
        return false;
      }
      EntryCodes that = (EntryCodes) obj;
      return Objects.equals(this.residentCodes, that.residentCodes)
          && Objects.equals(this.vendorCodes, that.vendorCodes)
          && Objects.equals(this.legacyResidentCodes, that.legacyResidentCodes);
    }

    @Override
    public String toString() {
      return "Resident codes: " + residentCodes + "; vendor codes: " + vendorCodes +
          "; legacy resident codes: " + legacyResidentCodes;
    }
  }

  public EntryCodes adapt() {
    SetMultimap<Integer, EntryCode> residentCodes = HashMultimap.create();
    Map<String, EntryCode> vendorCodes = new HashMap<>();
    Map<String, EntryCode> legacyResidentCodes = new HashMap<>();
    for (List<Object> row : rows) {
      if (row.size() < 4) {
        continue;  // no code specified on this row
      }

      int codeDigits = Integer.parseInt((String) row.get(COLUMN_ENTRY_CODE));
      Preconditions.checkState(!deletedCodes.contains(codeDigits),
          String.format("Code %04d is present on the deleted entry codes tab",
              codeDigits));
      String codeType = (String) row.get(COLUMN_ENTRY_CODE_TYPE);
      EntryCode entryCode = new EntryCode(codeDigits,
          EntryCodeType.valueOf(codeType.toUpperCase()));

      String street = (String) row.get(COLUMN_STREET);
      if (street == null || street.isEmpty()) {
        String name = (String) row.get(COLUMN_NAME);
        if (name == null || name.isEmpty()) {
          // legacy resident code
          name = (String) row.get(COLUMN_NOTES);
          Preconditions.checkState(legacyResidentCodes.put(name, entryCode) == null,
              "Unsupported: multiple codes for legacy resident " + name);
          continue;
        } else {
          // vendor code
          Preconditions.checkState(vendorCodes.put(name, entryCode) == null,
              "Unsupported: multiple codes for vendor " + name);
          continue;
        }
      } else {
        int key = Objects.hash(street, row.get(COLUMN_HOUSE_NUMBER));
        residentCodes.put(key, entryCode);
      }
    }
    return new EntryCodes(residentCodes, vendorCodes, legacyResidentCodes);
  }
}