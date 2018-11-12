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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import doorking.EntryCode.EntryCodeType;
import doorking.EntryCodeAdapter.EntryCodes;
import doorking.Proto.Config;
import doorking.Proto.SecurityLevelMapping;

/** Adapts a custom spreadsheet to a list of DoorKing entries. */
public class EntryAdapter {
  private final Config config;
  private final List<List<Object>> rows;
  private final EntryCodes entryCodes;

  public EntryAdapter(Config config, List<List<Object>> rows,
      EntryCodes entryCodes) {
    this.config = config;
    this.rows = rows;
    this.entryCodes = entryCodes;
  }

  public List<Entry> adapt() {
    Map<EntryCodeType, Integer> securityLevelMap = readSecurityLevelMapping(config);

    List<Entry> result = new ArrayList<>();
    rows.forEach(row -> result.addAll(getResidentEntry(row, securityLevelMap)));

    Preconditions.checkState(entryCodes.residentCodes.isEmpty(),
        "Unencoded resident codes: " + entryCodes.residentCodes);

    entryCodes.vendorCodes.forEach((vendorName, entryCode) -> result
        .add(getVendorEntry(vendorName, entryCode, securityLevelMap)));

    entryCodes.legacyResidentCodes.forEach((legacyResidentName, entryCode) -> result
        .add(getLegacyResidentEntry(legacyResidentName, entryCode, securityLevelMap)));
    return result;
  }

  private static final int COLUMN_STREET = 0;
  private static final int COLUMN_HOUSE_NUMBER = 1;
  private static final int COLUMN_DIRECTORY_NUMBER = 3;
  private static final int COLUMN_DISPLAY_NAME = 4;
  private static final int COLUMN_PHONE_NUMBER = 5;
  private static final int COLUMN_DEVICE_NUMBER = 6;
  private static final int COLUMN_DEVICE_NUMBER2 = 7;
  private static final int COLUMN_DEVICE_NUMBER3 = 8;
  private static final int COLUMN_DEVICE_NUMBER4 = 9;

  private List<Entry> getResidentEntry(List<Object> row,
      Map<EntryCodeType, Integer> securityLevelMap) {
    int key = Objects.hash(row.get(COLUMN_STREET), row.get(COLUMN_HOUSE_NUMBER));

    Entry.Builder builder = Entry.newBuilder();
    String notes = String.format("%s %s", row.get(COLUMN_HOUSE_NUMBER),
        row.get(COLUMN_STREET));
    builder.setNotes(notes);
    String directoryName = (String) row.get(COLUMN_DISPLAY_NAME);
    builder.setDirectoryDisplayName(directoryName);

    int directoryNumber = getDirectoryNumber(row.get(COLUMN_DIRECTORY_NUMBER));
    builder.setDirectoryNumber(directoryNumber);

    addDeviceNumberFromColumn(COLUMN_DEVICE_NUMBER, row, builder);
    addDeviceNumberFromColumn(COLUMN_DEVICE_NUMBER2, row, builder);
    addDeviceNumberFromColumn(COLUMN_DEVICE_NUMBER3, row, builder);
    addDeviceNumberFromColumn(COLUMN_DEVICE_NUMBER4, row, builder);
    
    List<String> phoneNumberComponents = splitPhoneNumber(
        row.get(COLUMN_PHONE_NUMBER));
    if (!phoneNumberComponents.get(0).equals(config.getLocalPhonePrefix())) {
      builder.setAreaCode(phoneNumberComponents.get(0));
    }
    builder.setPhoneNumber(phoneNumberComponents.get(1));

    EntryCode permanentEntryCode = entryCodes.lookupAndRemoveResidentCode(key,
        EntryCodeType.PERMANENT);
    if (permanentEntryCode != null) {
      builder.setEntryCode(permanentEntryCode.code);
      builder.setSecurityLevel(securityLevelMap.get(permanentEntryCode.type));
    }
    // TODO if there is no entry code, is a security level needed?

    List<Entry> result = new ArrayList<>();
    result.add(builder.build());

    // Add hidden entries for additional permanent entry codes.

    int suffix = 2;

    while ((permanentEntryCode = entryCodes.lookupAndRemoveResidentCode(key,
        EntryCodeType.PERMANENT)) != null) {
      builder.clearDeviceNumber();
      builder.clearDirectoryNumber();
      builder.clearAreaCode();
      builder.clearPhoneNumber();
      builder.setDirectoryDisplayName(directoryName + " " + suffix++);
      builder.setEntryCode(permanentEntryCode.code);
      builder.setSecurityLevel(securityLevelMap.get(permanentEntryCode.type));
      builder.setNotes(notes + " permanent");
      builder.markHidden();
      result.add(builder.build());    
    }

    // Add hidden entries for each limited entry codes.

    EntryCode limitedEntryCode = null;
    while ((limitedEntryCode = entryCodes.lookupAndRemoveResidentCode(key,
        EntryCodeType.LIMITED)) != null) {
      builder.clearDeviceNumber();
      builder.clearDirectoryNumber();
      builder.clearAreaCode();
      builder.clearPhoneNumber();
      builder.setDirectoryDisplayName(directoryName + " " + suffix++);
      builder.setEntryCode(limitedEntryCode.code);
      builder.setSecurityLevel(securityLevelMap.get(limitedEntryCode.type));
      builder.setNotes(notes + " limited");
      builder.markHidden();
      result.add(builder.build());
    }
    return result;
  }

  private void addDeviceNumberFromColumn(int column, List<Object> row, Entry.Builder builder) {
    if (row.size() > column) {
      String deviceNumber = (String) row.get(column);
      if (deviceNumber != null && !deviceNumber.isEmpty()) {
        builder.addDeviceNumber(deviceNumber);
      }
    }
  }
  
  private Entry getVendorEntry(String name, EntryCode entryCode,
      Map<EntryCodeType, Integer> securityLevelMap) {
    // TODO Police
    // Preconditions.checkState(entryCode.type == EntryCodeType.LIMITED
    // || entryCode.type == EntryCodeType.DELIVERY,
    // "Permanent entry code for vendor " + name);
    Entry.Builder builder = Entry.newBuilder();
    builder.setDirectoryDisplayName(name);
    builder.setEntryCode(entryCode.code);
    builder.setSecurityLevel(securityLevelMap.get(entryCode.type));
    builder.markVendor();
    return builder.build();
  }

  private Entry getLegacyResidentEntry(String name, EntryCode entryCode,
      Map<EntryCodeType, Integer> securityLevelMap) {
    Preconditions.checkState(entryCode.type == EntryCodeType.PERMANENT);
    Entry.Builder builder = Entry.newBuilder();
    builder.setDirectoryDisplayName(name);
    builder.setNotes("Legacy entry");
    builder.setEntryCode(entryCode.code);
    builder.setSecurityLevel(securityLevelMap.get(entryCode.type));
    builder.markHidden();
    return builder.build();
  }

  private int getDirectoryNumber(Object obj) {
    String text = (String) obj;
    Preconditions.checkState(text.startsWith("#"));
    return Integer.parseInt(text.substring(1));
  }

  private List<String> splitPhoneNumber(Object obj) {
    String text = (String) obj;
    Preconditions.checkState(text.charAt(3) == '-');
    String prefix = text.substring(0, 3);
    String number = text.substring(4);
    return ImmutableList.of(prefix, number);
  }

  private Map<EntryCodeType, Integer> readSecurityLevelMapping(Config config) {
    Map<EntryCodeType, Integer> result = new HashMap<>();
    for (SecurityLevelMapping mapping : config.getSecurityLevelMappingList()) {
      result.put(EntryCodeType.valueOf(mapping.getEntryCodeType()),
          mapping.getSecurityLevel());
    }
    return result;
  }
}