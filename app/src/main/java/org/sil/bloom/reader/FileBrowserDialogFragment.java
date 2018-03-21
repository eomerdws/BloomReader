package org.sil.bloom.reader;


import android.app.DialogFragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileBrowserDialogFragment extends DialogFragment {
    public static final String SEARCH_BUNDLES_DIALOG_FRAGMENT_TAG = "search_bundles_dialog";

    private String currentPath = Environment.getExternalStorageDirectory().getPath(); // Default
    private final String FILTER = ".bloombundle";
    private List<File> files = Collections.synchronizedList(new ArrayList<File>());
    private ListView listFiles;
    private TextView txtTitle;
    private Button btnCancel;
    private Drawable folderIcon;
    private Drawable bloomBundleIcon;
    private FilenameFilter filenameFilter;
    private int selectedIndex;
    private FindBundlesRecursively bundles = new FindBundlesRecursively();
    private Thread searchBundlesThread;
    private ArrayAdapter<File> listAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);

        searchBundlesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                bundles.scan(currentPath);
                synchronized (files) {
                    files.addAll(bundles.getFiles());
                    files.notify();
                    for(File f : files) {
                        Log.d("BloomReader", f.getName());
                    }
                }
            }
        });
        searchBundlesThread.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_search_bundles, container, false);
        listAdapter = new ArrayAdapter<File>(view.getContext(), android.R.layout.simple_list_item_1, files);

        listFiles = (ListView) view.findViewById(R.id.listFiles);
        listFiles.setAdapter(listAdapter);
        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        btnCancel = (Button) view.findViewById(R.id.btnCancel);

        folderIcon = view.getResources().getDrawable(R.drawable.ic_folder);
        bloomBundleIcon = view.getResources().getDrawable(R.drawable.bookshelf);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getDialog().setCanceledOnTouchOutside(true);

        return view;
    }

    private void changeTitle(String text) {
        txtTitle.setText(text);
    }

    private void changeTitle(int id) {
        txtTitle.setText(id);
    }
}
