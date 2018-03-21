package org.sil.bloom.reader;


import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BundleBrowserDialogFragement extends DialogFragment {
    public static final String SEARCH_BUNDLES_DIALOG_FRAGMENT_TAG = "search_bundles_dialog";

    private String currentPath = Environment.getExternalStorageDirectory().getPath(); // Default
    private final String FILTER = ".bloombundle";
    private List<File> files = Collections.synchronizedList(new ArrayList<File>());
    private ListView listFiles;
    private TextView txtTitle;
    private Button btnCancel;
    private Drawable folderIcon;
    private Drawable bloomBundleIcon;
    private int selectedIndex;
    private FindBundlesRecursively bundles = FindBundlesRecursively.getInstance();
    private Thread searchBundlesThread;
    private ArrayAdapter<File> listAdapter;


    private class BuildBundleListTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            stopProgressBar();
            if(files.size() > 0)
                listFiles.setAdapter(new ArrayAdapter<File>(getView().getContext(), android.R.layout.simple_list_item_1, files));
            else
                noBundlesFound();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            bundles.startScan(currentPath);
            synchronized (files) {
                if (files.size() >= 0) {
                    files.clear();
                    selectedIndex = -1;
                }
                files.addAll(bundles.getFiles());
            }
            return null;
        }
    }

    private class BundleAdapter extends ArrayAdapter<File> {
        private List<File> files;
        private Drawable bloomBundleIcon;

        public BundleAdapter(@NonNull Context context, int resource, @NonNull List<File> files) {
            super(context, resource, files);
            this.files = files;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if(view == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.dialog_share_books, null);
            }

            bloomBundleIcon = view.getResources().getDrawable(R.drawable.bookshelf);
            File f = files.get(position);
            TextView title = (TextView) view.findViewById(R.id.listFiles);
            title.setText(f.getName());

            return view;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        new BuildBundleListTask().execute();
        selectedIndex = 0;
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


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getDialog().setCanceledOnTouchOutside(true);

        return view;
    }

    public void stopProgressBar() {
        ProgressBar progressLoadBundles = getView().findViewById(R.id.progressLoadBundles);
        progressLoadBundles.setVisibility(View.INVISIBLE);
    }

    public void noBundlesFound() {
        changeTitle("Sorry no bundles found.");
    }

    private void changeTitle(String text) {
        txtTitle.setText(text);
    }

    private void changeTitle(int id) {
        txtTitle.setText(id);
    }
}
