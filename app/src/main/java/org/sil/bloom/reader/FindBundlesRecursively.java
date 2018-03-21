package org.sil.bloom.reader;


import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class FindBundlesRecursively {
    private ArrayList<File> files;
    public final String FILTER = ".bloombundle";
    private boolean _locked;
    private static FindBundlesRecursively instance;
    private String root;

    public static FindBundlesRecursively getInstance() {
        if(instance == null) {
            instance = new FindBundlesRecursively();
        }
        return instance;
    }

    private FindBundlesRecursively() {
        files = new ArrayList<File>();
        _locked = false;
    }

    private void scan(File root) {
        if(root != null) {
            File[] list = root.listFiles();

            for (File f : list) {
                Log.d("BloomReader", f.getName() + " is dir: "
                        + Boolean.toString(f.isDirectory()) + " is file: " + Boolean.toString(f.isFile()));

                if (f.isFile() && f.getName().contains(FILTER))
                    files.add(f);
                else if (f.isDirectory())
                    scan(f);
            }
        }
    }

    private void scan(String root) {
        if(root !=null) {
            File f = new File(root);
            this.scan(f);
        }
    }

    public void startScan(String root) {
        if(root != null) {
            startScan(new File(root));
        }
    }


    public void startScan(File root) {
        if(!_locked) {
            if(files.size() > 0) {
                files.clear(); // Because it is a singleton then the last search results will need to be cleared.
            }

            _locked = true;
            scan(root);
            _locked = false;
        }
    }

    public boolean isScanLocked() {
        return _locked;
    }

    public ArrayList<File> getFiles() {
        return files;
    }
}
