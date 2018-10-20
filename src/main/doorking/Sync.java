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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.TextFormat;

import doorking.EntryCodeAdapter.EntryCodes;
import doorking.GoogleRetriever.Result;
import doorking.Proto.Config;

/** Generates a CSV file for import into DoorKing Account Manager. */
public class Sync {
  public static void main(String args[]) throws Exception {
    new Sync().run();
  }

  private static final String OUTPUT_FILE = "/tmp/doorking.csv";

  public void run() throws Exception {
    Config config = readConfig();
    Result result = new GoogleRetriever(config).retrieve();

    EntryCodeAdapter entryCodeAdapter = new EntryCodeAdapter(result.codes);
    EntryCodes entryCodes = entryCodeAdapter.adapt();

    EntryAdapter adapter = new EntryAdapter(config, result.entries, entryCodes);
    List<Entry> entries = adapter.adapt();

    List<String> lines = new ArrayList<>();
    lines.add("ACCOUNT," + Entry.getHeaders());
    entries.forEach(entry -> lines.add(config.getAccountName() + "," + entry));
    Files.write(Paths.get(OUTPUT_FILE), lines, StandardCharsets.UTF_8);

    System.err.println("Wrote " + OUTPUT_FILE);
  }

  public static Config readConfig() throws Exception {
    Path proto = Paths.get(System.getProperty("user.home"), ".doorking");
    Config.Builder config = Config.newBuilder();
    TextFormat.getParser().merge(
        String.join(System.lineSeparator(), Files.readAllLines(proto)),
        config);
    return config.build();
  }
}
