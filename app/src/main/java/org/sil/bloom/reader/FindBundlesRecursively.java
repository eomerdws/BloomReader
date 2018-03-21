package org.sil.bloom.reader;


import java.io.File;
import java.util.ArrayList;

public class FindBundlesRecursively {
    private ArrayList<File> files;
    public final String FILTER = ".bloombundle";

    public FindBundlesRecursively() {
        files = new ArrayList<File>();
    }

    public void scan(File root) {
        if(root != null) {
            File[] list = root.listFiles();

            for (File f : list) {
                if (f.isDirectory()) {
                    scan(f);
                } else if (f.isFile() && f.getName().matches(FILTER)) {
                    files.add(f);
                }
            }
        }
    }

    public void scan(String root) {
        if(root !=null) {
            File f = new File(root);
            this.scan(f);
        }
    }

    public ArrayList<File> getFiles() {
        return files;
    }
}
