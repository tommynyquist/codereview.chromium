package com.chrome.codereview.model;

import android.content.Context;
import android.text.TextPaint;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by sergeyv on 29/4/14.
 */
public class Diff {

    private static final String INDEX = "Index: ";
    private Map<String, List<String>> fileDiffs = new HashMap<String, List<String>>();
    private final int patchSetId;

    public Diff(int patchSetId, String diffString) {
        parse(diffString);
        this.patchSetId = patchSetId;
    }

    public void parse(String s) {
        Scanner scanner = new Scanner(s);
        List<String> diffs = new ArrayList<String>();
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (line.startsWith(INDEX)) {
                String fileName = line.substring(INDEX.length());
                diffs = new ArrayList<String>();
                fileDiffs.put(fileName, diffs);
                scanner.nextLine(); //diff --git
                scanner.nextLine(); //index
                scanner.nextLine(); //--- a/
                scanner.nextLine(); //+++ b/
                continue;
            }
            diffs.add(line.replace(" ", "\u00A0"));
        }

    }

    public List<String> diffForFile(String fileName) {
        if (!fileDiffs.containsKey(fileName)) {
            return Collections.emptyList();
        }
        return fileDiffs.get(fileName);
    }

    public int patchSetId() {
        return patchSetId;
    }

}
