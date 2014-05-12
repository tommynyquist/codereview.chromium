package com.chrome.codereview.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by sergeyv on 12/5/14.
 */
public class FileDiff {

    private final List<String> diffLines;

    public FileDiff(List<String> diffLines) {
        this.diffLines = diffLines;
    }

    public List<String> content() {
        return diffLines;
    }

    public static FileDiff from(String fileDiff) {
        Scanner scanner = new Scanner(fileDiff);
        if (!scanner.hasNext()) {
            return null;
        }
        String line = scanner.nextLine();
        while (!line.startsWith("@@")) {
            if (!scanner.hasNext()) {
                return null;
            }
            line = scanner.nextLine();
        }

        List<String> diffs = new ArrayList<String>();
        diffs.add(line);
        while (scanner.hasNext()) {
            diffs.add(scanner.nextLine());
        }
        return new FileDiff(diffs);
    }

}
