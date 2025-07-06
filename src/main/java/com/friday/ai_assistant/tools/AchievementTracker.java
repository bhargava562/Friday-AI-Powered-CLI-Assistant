package com.friday.ai_assistant.tools;

import com.friday.ai_assistant.model.InfinityStone;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AchievementTracker {

    private final Set<InfinityStone> collected = EnumSet.noneOf(InfinityStone.class);
    private final String SAVE_FILE = "infinity_stones_progress.txt";
    private final String GAUNTLET_FILE = "src/main/resources/thanos.txt";

    // ANSI colors for stones
    private final Map<InfinityStone, String> stoneColors = Map.of(
            InfinityStone.POWER, "\u001B[35m",     // Purple
            InfinityStone.REALITY, "\u001B[91m",   // Bright Red
            InfinityStone.SPACE, "\u001B[96m",     // Light Blue
            InfinityStone.MIND, "\u001B[93m",      // Bright Yellow
            InfinityStone.SOUL, "\u001B[38;5;208m",// Orange
            InfinityStone.TIME, "\u001B[32m"       // Green
    );

    // Sandal/Brown color for the gauntlet (using previous Mind stone yellow)
    private final String GAUNTLET_COLOR = "\u001B[33m"; // Yellow
    private final String RESET_COLOR = "\u001B[0m";

    // Stone coordinate ranges - all coordinates where stones should be colored
    private final Map<InfinityStone, List<int[]>> stoneCoordinateRanges = Map.of(
            InfinityStone.SOUL, Arrays.asList(
                    new int[]{25, 13, 17}, // Y:25, X:13-17
                    new int[]{25, 14, 16}, // Y:25, X:14-16 (existing)
                    new int[]{26, 12, 18}, // Y:26, X:12-18 (existing)
                    new int[]{27, 12, 18}, // Y:27, X:12-18 (existing)
                    new int[]{28, 12, 18}, // Y:28, X:12-18 (updated)
                    new int[]{28, 13, 17}, // Y:28, X:13-17 (existing)
                    new int[]{29, 14, 16}  // Y:29, X:14-16 (new)
            ),
            InfinityStone.REALITY, Arrays.asList(
                    new int[]{26, 22, 28}, // Range: y:26, x:22 to 28
                    new int[]{27, 22, 28}, // Range: y:27, x:22 to 28
                    new int[]{25, 23, 27}, // Range: y:25, x:23 to 27 (decreased by 1)
                    new int[]{28, 22, 28}, // Range: y:28, x:22 to 28 (decreased by 1)
                    new int[]{29, 23, 27}  // Range: y:29, x:23 to 27 (decreased by 1)
            ),
            InfinityStone.SPACE, Arrays.asList(
                    new int[]{25, 35, 39}, // Y:25, X:35-39 (increased start by 1)
                    new int[]{26, 34, 40}, // Y:26, X:34-40 (increased end by 1)
                    new int[]{27, 33, 40}, // Y:27, X:33-40 (increased end by 1)
                    new int[]{28, 34, 40}, // Y:28, X:34-40 (increased end by 2)
                    new int[]{29, 35, 38}  // Y:29, X:35-38 (new)
            ),
            InfinityStone.POWER, Arrays.asList(
                    new int[]{25, 46, 49}, // Y:25, X:46-49 (increased start and end by 1)
                    new int[]{26, 45, 50}, // Y:26, X:45-50 (decreased start by 1, increased end by 1)
                    new int[]{27, 44, 51}, // Y:27, X:44-51 (decreased start by 2, increased end by 2)
                    new int[]{28, 44, 50}, // Y:28, X:44-50 (increased start and end by 1)
                    new int[]{29, 45, 49}  // Y:29, X:45-49 (increased end by 1)
            ),
            InfinityStone.TIME, Arrays.asList(
                    new int[]{35, 60, 65}, // Range: y:35, x:60 to 65
                    new int[]{36, 59, 65}, // Range: y:36, x:59 to 65 (increased end by 2)
                    new int[]{37, 59, 64}, // Range: y:37, x:59 to 64 (increased end by 3)
                    new int[]{34, 60, 65}, // Range: y:34, x:60 to 65 (decreased by 1)
                    new int[]{38, 58, 62}  // Range: y:38, x:58 to 62 (decreased by 1)
            ),
            InfinityStone.MIND, Arrays.asList(
                    new int[]{32, 27, 35}, // Range: y:32, x:27 to 35
                    new int[]{33, 27, 35}, // Range: y:33, x:27 to 35
                    new int[]{34, 26, 36}, // Range: y:34, x:26 to 36
                    new int[]{35, 26, 36}, // Range: y:35, x:26 to 36
                    new int[]{36, 26, 36}, // Range: y:36, x:26 to 36
                    new int[]{37, 27, 35}, // Range: y:37, x:27 to 35
                    new int[]{38, 28, 34}, // Range: y:38, x:28 to 34
                    new int[]{31, 28, 33}, // Range: y:31, x:28 to 33 (decreased by 1)
                    new int[]{39, 29, 33}  // Range: y:39, x:29 to 33 (decreased by 1)
            )
    );

    // Constructor - Load saved progress
    public AchievementTracker() {
        loadProgress();
    }

    /**
     * Load previously collected stones from file
     */
    private void loadProgress() {
        try {
            Path savePath = Paths.get(SAVE_FILE);
            if (Files.exists(savePath)) {
                List<String> lines = Files.readAllLines(savePath);
                for (String line : lines) {
                    try {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split(":");
                        if (parts.length >= 2 && "STONE".equals(parts[0])) {
                            InfinityStone stone = InfinityStone.valueOf(parts[1].trim().toUpperCase());
                            collected.add(stone);
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("Unknown stone in save file: " + line);
                    }
                }
                System.out.println("✅ Loaded " + collected.size() + " previously collected stones");
            }
        } catch (IOException e) {
            System.err.println("⚠️ Could not load previous progress: " + e.getMessage());
        }
    }

    /**
     * Save current progress to file with coordinates
     */
    private void saveProgress() {
        try {
            List<String> saveData = new ArrayList<>();

            // Save collected stones with their coordinates
            for (InfinityStone stone : collected) {
                saveData.add("STONE:" + stone.name());
                saveData.add("COLOR:" + stoneColors.get(stone));
                saveData.add("COORDINATES_START");

                List<int[]> ranges = stoneCoordinateRanges.get(stone);
                for (int[] range : ranges) {
                    saveData.add("RANGE:" + range[0] + "," + range[1] + "," + range[2]);
                }
                saveData.add("COORDINATES_END");
                saveData.add("---");
            }

            Files.write(Paths.get(SAVE_FILE), saveData);
            System.out.println("💾 Progress saved: " + collected.size() + " stones with coordinates");
        } catch (IOException e) {
            System.err.println("⚠️ Could not save progress: " + e.getMessage());
        }
    }

    public String collectStone(String query) {
        InfinityStone stone = switch (query.toLowerCase()) {
            case "completed learning java", "power stone" -> InfinityStone.POWER;
            case "completed a real time project", "reality stone" -> InfinityStone.REALITY;
            case "leetcode", "space stone" -> InfinityStone.SPACE;
            case "aptitude", "mind stone" -> InfinityStone.MIND;
            case "soft skill", "soul stone" -> InfinityStone.SOUL;
            case "hackathon", "time stone" -> InfinityStone.TIME;
            default -> null;
        };

        if (stone == null) {
            return "❓ Unknown achievement or stone. Available achievements:\n" + getAvailableAchievements();
        }

        if (collected.contains(stone)) {
            return "⚠️ " + stone.name() + " STONE already collected!";
        }

        collected.add(stone);
        saveProgress(); // Save immediately after collecting

        String result = "✅ " + stoneColors.get(stone) + stone.name() + " STONE" + RESET_COLOR + " collected! (" + collected.size() + "/6)\n";
        result += "🎉 Achievement unlocked: " + getAchievementDescription(stone) + "\n";
        result += "📍 Stone coordinates saved permanently\n";

        if (collected.size() == 6) {
            result += "\n🔥 ALL STONES COLLECTED! You can now SNAP to balance the universe! 🔥";
        }

        return result;
    }

    private String getAchievementDescription(InfinityStone stone) {
        return switch (stone) {
            case POWER -> "Mastered Java programming fundamentals";
            case REALITY -> "Completed a real-world project implementation";
            case SPACE -> "Conquered LeetCode algorithmic challenges";
            case MIND -> "Sharpened logical and analytical thinking";
            case SOUL -> "Developed essential soft skills";
            case TIME -> "Participated in time-pressured hackathon";
        };
    }

    private String getAvailableAchievements() {
        return """
                📚 Available Achievements:
                • 'completed learning java' → Power Stone (Purple)
                • 'completed a real time project' → Reality Stone (Red)
                • 'leetcode' → Space Stone (Light Blue)
                • 'aptitude' → Mind Stone (Yellow)
                • 'soft skill' → Soul Stone (Orange)
                • 'hackathon' → Time Stone (Green)
                """;
    }

    /**
     * Check if a coordinate position should be colored as a stone
     */
    private InfinityStone getStoneAtPosition(int y, int x) {
        for (InfinityStone stone : collected) {
            List<int[]> ranges = stoneCoordinateRanges.get(stone);
            for (int[] range : ranges) {
                int rangeY = range[0];
                int startX = range[1];
                int endX = range[2];

                if (y == rangeY && x >= startX && x <= endX) {
                    return stone;
                }
            }
        }
        return null;
    }

    public String showGauntlet() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(GAUNTLET_FILE));
            StringBuilder result = new StringBuilder();

            // Process each line
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                StringBuilder coloredLine = new StringBuilder();

                // Process each character in the line
                for (int charIndex = 0; charIndex < line.length(); charIndex++) {
                    char currentChar = line.charAt(charIndex);

                    // Check if this position should be colored as a stone
                    InfinityStone stoneAtPosition = getStoneAtPosition(lineIndex, charIndex);

                    if (stoneAtPosition != null) {
                        // Color this position as a stone with block character
                        String stoneColor = stoneColors.get(stoneAtPosition);
                        coloredLine.append(stoneColor).append("█").append(RESET_COLOR);
                    } else if (currentChar != ' ') {
                        // Color as gauntlet (non-space characters)
                        coloredLine.append(GAUNTLET_COLOR).append(currentChar).append(RESET_COLOR);
                    } else {
                        // Keep spaces as they are
                        coloredLine.append(currentChar);
                    }
                }

                result.append(coloredLine).append("\n");
            }

            // Add status information
            result.append("\n").append(GAUNTLET_COLOR).append("🥊 INFINITY GAUNTLET").append(RESET_COLOR).append("\n");
            result.append("💾 Stones Collected: ").append(collected.size()).append("/6 (Saved Permanently)\n");

            if (!collected.isEmpty()) {
                result.append("Collected: ");
                for (InfinityStone stone : collected) {
                    result.append(stoneColors.get(stone)).append("█").append(stone.name()).append(RESET_COLOR).append(" ");
                }
                result.append("\n");
            }

            if (collected.size() == 6) {
                result.append("\n🔥 ALL STONES COLLECTED! You can now SNAP! 🔥\n");
            }

            return result.toString();
        } catch (IOException e) {
            return "⚠️ Failed to load gauntlet: " + e.getMessage() +
                    "\nMake sure " + GAUNTLET_FILE + " exists!";
        }
    }

    public String snap() {
        if (collected.size() == 6) {
            // Create a dramatic snap animation
            StringBuilder snapAnimation = new StringBuilder();
            snapAnimation.append("💥 SNAP! All 6 stones collected. The universe is 50% balanced.\n\n");

            // Show each stone's power
            for (InfinityStone stone : collected) {
                snapAnimation.append(stoneColors.get(stone)).append("█ ").append(stone.name())
                        .append(" STONE").append(RESET_COLOR).append(" - ")
                        .append(getStoneSnapEffect(stone)).append("\n");
            }

            snapAnimation.append("\n🌌 Reality bends to your will...\n");
            snapAnimation.append("🔁 Stones have been reset. The gauntlet awaits new achievements!\n");
            snapAnimation.append("💾 Progress saved automatically.\n");

            // Reset stones
            collected.clear();
            saveProgress();

            return snapAnimation.toString();
        }
        return "❌ Not enough stones to SNAP. You need to collect all 6 Infinity Stones.\n" +
                "Currently collected: " + collected.size() + "/6\n" +
                "Missing: " + getMissingStones();
    }

    private String getStoneSnapEffect(InfinityStone stone) {
        return switch (stone) {
            case POWER -> "⚡ Infinite power courses through the universe";
            case REALITY -> "🌀 Reality warps and reshapes itself";
            case SPACE -> "🌌 Space folds and expands infinitely";
            case MIND -> "🧠 Universal consciousness awakens";
            case SOUL -> "💀 The price of balance is paid";
            case TIME -> "🕐 Time itself stops and restarts";
        };
    }

    private String getMissingStones() {
        Set<InfinityStone> allStones = EnumSet.allOf(InfinityStone.class);
        allStones.removeAll(collected);
        return allStones.stream()
                .map(stone -> stoneColors.get(stone) + "○" + stone.name() + RESET_COLOR)
                .collect(Collectors.joining(", "));
    }

    public String getStatus() {
        StringBuilder status = new StringBuilder();
        status.append("🎯 Achievement Progress: ").append(collected.size()).append("/6\n");
        status.append("💾 Progress is saved permanently with coordinates!\n\n");

        status.append(getAvailableAchievements());

        if (!collected.isEmpty()) {
            status.append("\n✅ Collected Stones:\n");
            for (InfinityStone stone : collected) {
                status.append("  ").append(stoneColors.get(stone)).append("█").append(stone.name())
                        .append(RESET_COLOR).append(" - ").append(getAchievementDescription(stone)).append("\n");
            }
        }

        if (collected.size() < 6) {
            status.append("\n🔍 Missing Stones:\n");
            Set<InfinityStone> missing = EnumSet.allOf(InfinityStone.class);
            missing.removeAll(collected);
            for (InfinityStone stone : missing) {
                status.append("  ").append(stoneColors.get(stone)).append("○").append(stone.name())
                        .append(RESET_COLOR).append(" - ").append(getAchievementDescription(stone)).append("\n");
            }
        }

        return status.toString();
    }

    /**
     * Show coordinate ranges for debugging
     */
    public String showCoordinates() {
        StringBuilder coords = new StringBuilder();
        coords.append("📍 STONE COORDINATE RANGES:\n\n");

        for (InfinityStone stone : InfinityStone.values()) {
            coords.append(stoneColors.get(stone)).append("█ ").append(stone.name())
                    .append(" STONE").append(RESET_COLOR).append(":\n");

            List<int[]> ranges = stoneCoordinateRanges.get(stone);
            for (int[] range : ranges) {
                coords.append("  Line ").append(range[0]).append(": columns ")
                        .append(range[1]).append(" to ").append(range[2]).append("\n");
            }
            coords.append("\n");
        }

        return coords.toString();
    }

    /**
     * Manual reset for testing purposes
     */
    public String resetStones() {
        collected.clear();
        saveProgress();
        return "🔄 All stones have been manually reset and saved!\n💾 Coordinate data preserved for future collections.";
    }
}