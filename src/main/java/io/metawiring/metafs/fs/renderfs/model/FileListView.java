package io.metawiring.metafs.fs.renderfs.model;

import java.util.List;

public class FileListView {

    private List<String> files;

    public FileListView(List<String> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String file : files) {
            sb.append("- ").append(file).append("\n");
        }
        return sb.toString();
    }
}
