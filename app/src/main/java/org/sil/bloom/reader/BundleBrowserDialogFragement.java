package org.sil.bloom.reader;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.sil.bloom.reader.models.BookCollection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BundleBrowserDialogFragement extends DialogFragment {
    public static final String SEARCH_BUNDLES_DIALOG_FRAGMENT_TAG = "search_bundles_dialog";

    private String mCurrentPath = Environment.getExternalStorageDirectory().getPath(); // Default
    private final String FILTER = ".bloombundle";
    private List<File> mFiles = Collections.synchronizedList(new ArrayList<File>());
    private ListView mListFiles;
    private TextView mTxtTitle;
    private Button mBtnCancel;
    private Drawable mFolderIcon;
    private Drawable mBloomBundleIcon;
    private FindBundlesRecursively mBundles = FindBundlesRecursively.getInstance();
    private ArrayAdapter<File> mListAdapter;


    private class BuildBundleListTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            stopProgressBar();
            if (mFiles.size() > 0)
                mListFiles.setAdapter(new BundleAdapter(getView().getContext(), R.layout.dialog_search_item, mFiles));
            else
                noBundlesFound();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mBundles.startScan(mCurrentPath);
            synchronized (mFiles) {
                if (mFiles.size() >= 0) {
                    mFiles.clear();
                }
                mFiles.addAll(mBundles.getFiles());
            }
            return null;
        }
    }

    private class BundleAdapter extends ArrayAdapter<File> {
        private List<File> files;
        private Drawable bloomBundleIcon;
        private View.OnClickListener listener;

        public BundleAdapter(@NonNull Context context, int resource, @NonNull List<File> files) {
            super(context, resource, files);
            this.files = files;
            listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = (TextView) v;
                    File file = mBundles.getFileByFileName(tv.getText().toString());
                    Log.d("BloomReader", tv.getText().toString());
                    Log.d("BloomReader", file.getAbsolutePath());
                    if(file.getPath().endsWith(IOUtilities.BLOOM_BUNDLE_FILE_EXTENSION))
                        importBloomBundle(Uri.fromFile(file));
                    dismiss();
                }
            };
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.dialog_search_item, null);

            File f = files.get(position);
            TextView textBundleName = (TextView) view.findViewById(R.id.textBundleName);
            ImageView bundleIcon = (ImageView) view.findViewById(R.id.bundleIcon);
            bundleIcon.setImageResource(R.drawable.bookshelf);
            textBundleName.setText(f.getName());
            textBundleName.setOnClickListener(listener);
            bundleIcon.setOnClickListener(listener);

            return view;
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        if (!mBundles.isFilled())
            new BuildBundleListTask().execute();
        this.setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_search_bundles, container, false);
        mListAdapter = new BundleAdapter(view.getContext(), R.layout.dialog_search_item, mFiles);
        mListAdapter.setNotifyOnChange(true);

        mListFiles = view.findViewById(R.id.listFiles);
        mListFiles.setAdapter(mListAdapter);
        mTxtTitle = (TextView) view.findViewById(R.id.txtTitle);
        mBtnCancel = (Button) view.findViewById(R.id.btnCancel);

        mFolderIcon = view.getResources().getDrawable(R.drawable.ic_folder);


        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getDialog().setCanceledOnTouchOutside(true);

        return view;
    }

    @Override
    public void onDestroyView() {
        //https://stackoverflow.com/a/15444485/485386
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public void stopProgressBar() {
        ProgressBar progressLoadBundles = getView().findViewById(R.id.progressLoadBundles);
        progressLoadBundles.setVisibility(View.INVISIBLE);
    }

    public void noBundlesFound() {
        changeTitle("Sorry no mBundles found.");
        mTxtTitle.setTextSize(32);
    }


    private void changeTitle(String text) {
        mTxtTitle.setText(text);
    }

    private void changeTitle(int id) {
        mTxtTitle.setText(id);
    }

    private void importBloomBundle(Uri bloomBundleUri) {
        // Toast.makeText(getView(), "Got Bloom bundle: " + bloomBundleUri.getPath(), Toast.LENGTH_LONG).show();
        List<String> newBooks = null;
        try {
            newBooks = IOUtilities.extractBloomBundle(getView().getContext().getApplicationContext(), bloomBundleUri);
            // We assume that they will be happy with us removing from where ever the bundle was,
            // so long as it is on the same device (e.g. not coming from an sd card they plan to pass
            // around the room).
            if(!IOUtilities.seemToBeDifferentVolumes(bloomBundleUri.getPath(), BookCollection.getLocalBooksDirectory().getPath())) {
                (new File(bloomBundleUri.getPath())).delete();
            }
        }
        catch (IOException e) {
            Log.e("BundleIO", "IO exception reading bloom bundle: " + e.getMessage());
            e.printStackTrace();
            // Toast.makeText(this, "Had a problem reading the bundle", Toast.LENGTH_LONG).show();
        }
        // try {
        //     // Reinitialize completely to get the new state of things.
        //     _bookCollection.init(this.getApplicationContext());
        // } catch (ExtStorageUnavailableException e) {
        //     Log.wtf("BloomReader", "Could not use external storage when reloading project!", e); // should NEVER happen
        // }
        // updateDisplay();
        // highlightItems(newBooks);
    }
}
