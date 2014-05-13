package com.chrome.codereview.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sergeyv on 12/5/14.
 */
public class FileDiff {

    public enum LineType {
        MARKER,
        BOTH_SIDE,
        LEFT,
        RIGHT
    }

    public static class DiffLine {

        private final int leftLineNumber;
        private final int rightLineNumber;
        private final LineType type;
        private final String text;

        private DiffLine(LineType type, int leftLineNumber, int rightLineNumber, String text) {
            this.leftLineNumber = leftLineNumber;
            this.rightLineNumber = rightLineNumber;
            this.type = type;
            this.text = text.replace(" ", "\u00A0");
        }

        public int leftLineNumber() {
            return leftLineNumber;
        }

        public int rightLineNumber() {
            return rightLineNumber;
        }

        public LineType type() {
            return type;
        }

        public String text() {
            return text;
        }

        private static DiffLine markerLine(int leftLineNumber, int rightLineNumber ,String text) {
            return new DiffLine(LineType.MARKER, leftLineNumber, rightLineNumber, text);
        }

        private static DiffLine leftSideLine(int lineNumber, String text) {
            return new DiffLine(LineType.LEFT, lineNumber, 0, text);
        }

        private static DiffLine rightSideLine(int lineNumber, String text) {
            return new DiffLine(LineType.RIGHT, 0, lineNumber, text);
        }

        private static DiffLine bothSideLine(int leftLineNumber, int rightLineNumber, String text) {
            return new DiffLine(LineType.BOTH_SIDE, leftLineNumber, rightLineNumber, text);
        }
    }

    private static final Pattern MARKER_PATTERN = Pattern.compile("^@@\\s+\\-(?:(\\d+)(?:,(\\d+))?)\\s+\\+(?:(\\d+)(?:,(\\d+))?)\\s+@@.*");

    private final List<DiffLine> diffLines;


    public FileDiff(List<DiffLine> diffLines) {
        this.diffLines = diffLines;
    }

    public List<DiffLine> content() {
        return diffLines;
    }

    public static FileDiff from(String fileDiff) {
        Scanner scanner = new Scanner(fileDiff);
        int leftLines = -1;
        int rightLines = -1;
        boolean chunkInitialised = false;
        List<DiffLine> diffs = new ArrayList<DiffLine>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher matcher = MARKER_PATTERN.matcher(line);

            if (matcher.matches()) {
                chunkInitialised = true;
                leftLines = Integer.parseInt(matcher.group(1));
                rightLines = Integer.parseInt(matcher.group(3));
                diffs.add(DiffLine.markerLine(leftLines, rightLines, line));
                continue;
            }
            if (!chunkInitialised) {
                continue;
            }
            if (line.startsWith("-")) {
                diffs.add(DiffLine.leftSideLine(leftLines, line));
                leftLines++;
                continue;
            }
            if (line.startsWith("+")) {
                diffs.add(DiffLine.rightSideLine(rightLines, line));
                rightLines++;
                continue;
            }
            diffs.add(DiffLine.bothSideLine(leftLines, rightLines, line));
            leftLines++;
            rightLines++;
        }

        return new FileDiff(diffs);
    }

}
