package org.sil.bloom.reader;


import android.app.DialogFragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileBrowserDialogFragment extends DialogFragment {
    public static final String SEARCH_BUNDLES_DIALOG_FRAGMENT_TAG = "search_bundles_dialog";

    private String currentPath = Environment.getExternalStorageDirectory().getPath(); // Default
    private List<File> files = new ArrayList<File>();
    private ListView listFiles;
    private TextView txtTitle;
    private Button btnCancel;
    private Drawable folderIcon;
    private Drawable bloomBundleIcon;
    private FilenameFilter filenameFilter;
    private int selectedIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // folderIcon = getContext().getResources().getDrawable(R.drawable.ic_folder);
        // bloomBundleIcon = getContext().getResources().getDrawable(R.drawable.bookshelf);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_search_bundles, container, false);
        listFiles = view.findViewById(R.id.listFiles);
        txtTitle = view.findViewById(R.id.txtTitle);
        btnCancel = view.findViewById(R.id.btnCancel);

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

    private List<File> getFiles(String directoryPath) {
        File directory = new File(directoryPath);
        File[] list = directory.listFiles(filenameFilter);
        if (list == null) {
            list = new File[]{};
        }

        List<File> fileList = Arrays.asList(list);

        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                if(file1.isDirectory() && file2.isFile())
                    return -1;
                else if (file1.isFile() && file2.isDirectory())
                    return 1;
                else
                    return file1.getPath().compareTo(file2.getPath());
            }
        });
        return fileList;
    }

    private void buildFileList(ArrayAdapter<File> adapter) {
        try {
            List<File> fileList = getFiles(currentPath);
            files.clear();
            selectedIndex = -1;
            files.addAll(fileList);
            adapter.notifyDataSetChanged();
        } catch(NullPointerException e) {
            String message = getResources().getString(android.R.string.unknownName);
            Toast.makeText(this.getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
