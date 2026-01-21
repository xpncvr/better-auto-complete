package github.xpncvr.autocomplete;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandPredictor {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final int MAX_SIZE = 500;
  private static final String FILENAME = "better_command_history.txt";

  private final Path path;

  private static class CommandEntry {

    int weight;
    long lastUsed;
    boolean isPattern;

    CommandEntry(int weight, long lastUsed, boolean isPattern) {
      this.weight = weight;
      this.lastUsed = lastUsed;
      this.isPattern = isPattern;
    }
  }

  private final LinkedHashMap<String, CommandEntry> weightedHistory = new LinkedHashMap<>();

  public CommandPredictor(Path directoryPath) {
    this.path = directoryPath.resolve(FILENAME);
    load();
  }

  private void load() {
    if (!Files.exists(path)) return;

    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      reader
        .lines()
        .forEach(line -> {
          if (line.isEmpty()) return;
          boolean isPattern = line.startsWith("!");
          try {
            if (isPattern) {
              String[] parts = line.split(",", 2);
              if (parts.length != 2) return;

              String weightPart = parts[0];
              int weight = Integer.parseInt(weightPart.substring(1));
              String command = parts[1];
              weightedHistory.put(command, new CommandEntry(weight, Integer.MAX_VALUE, true));
            } else {
              String[] parts = line.split(",", 3);
              if (parts.length != 3) return;

              String weightPart = parts[0];
              int weight = Integer.parseInt(weightPart);
              long lastUsed = Long.parseLong(parts[1]);
              String command = parts[2];
              weightedHistory.put(command, new CommandEntry(weight, lastUsed, false));
            }
          } catch (NumberFormatException e) {
            LOGGER.warn("Invalid line in {}: {}", FILENAME, line);
          }
        });
    } catch (IOException e) {
      LOGGER.error("Failed to read {}", FILENAME, e);
    }
  }

  private void write() {
    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      for (Map.Entry<String, CommandEntry> entry : weightedHistory.entrySet()) {
        CommandEntry ce = entry.getValue();
        if (ce.isPattern) {
          writer.write("!" + ce.weight + "," + entry.getKey());
        } else {
          writer.write(ce.weight + "," + ce.lastUsed + "," + entry.getKey());
        }
        writer.newLine();
      }
    } catch (IOException e) {
      LOGGER.error("Failed to write {}", FILENAME, e);
    }
  }

  public void decayWeights() {
    long now = System.currentTimeMillis();

    weightedHistory
      .entrySet()
      .removeIf(entry -> {
        CommandEntry ce = entry.getValue();

        if (ce.isPattern) {
          return false;
        }

        long elapsedDays = (now - ce.lastUsed) / (24 * 60 * 60 * 1000L); // 24h
        if (elapsedDays > 0) {
          ce.weight -= elapsedDays;
          return ce.weight <= 0;
        }

        return false;
      });
  }

  public void add(String command) {
    long now = System.currentTimeMillis();

    CommandEntry existing = weightedHistory.get(command);

    if (existing != null && !existing.isPattern) {
      existing.weight++;
      existing.lastUsed = now;
    } else if (existing == null) {
      weightedHistory.put(command, new CommandEntry(60, now, false));
    }

    write();
  }

  public Optional<String> predictCommand(String input) {
    Optional<String> result = weightedHistory
      .entrySet()
      .stream()
      .filter(e -> {
        if (e.getValue().isPattern) {
          return matchesWildcard(e.getKey(), input);
        } else {
          return e.getKey().startsWith(input);
        }
      })
      .max(Comparator.comparingInt(e -> e.getValue().weight))
      .map(e -> {
        CommandEntry ce = e.getValue();
        String cmd = e.getKey();

        if (ce.isPattern) {
          String[] parts = cmd.split("\\*\\*\\*", 2);
          String before = parts[0];
          String after = parts.length > 1 ? parts[1] : "";

          if (!input.startsWith(before)) {
            return null;
          }

          String wildcardPart = input.substring(before.length());

          int spaceIndex = wildcardPart.indexOf(' ');
          if (spaceIndex != -1) {
            String s = wildcardPart.substring(spaceIndex);

            int startIndex = Math.min(s.length(), after.length());
            return input + after.substring(startIndex);
          } else {
            return input;
          }
        } else {
          return cmd;
        }
      });

    return result;
  }

  public boolean matchesWildcard(String wildcard, String input) {
    int idx = wildcard.indexOf("***");
    if (idx == -1) {
      return input.equals(wildcard);
    }

    String prefix = wildcard.substring(0, idx);
    String suffix = wildcard.substring(idx + 3);

    if (!input.startsWith(prefix)) {
      return false;
    }

    String rest = input.substring(prefix.length());

    int spaceIndex = rest.indexOf(' ');
    if (spaceIndex == -1) {
      return false;
    }

    String afterWord = rest.substring(spaceIndex);

    return suffix.startsWith(afterWord);
  }
}
