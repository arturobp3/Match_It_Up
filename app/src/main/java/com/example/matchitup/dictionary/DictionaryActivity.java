package com.example.matchitup.dictionary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.matchitup.R;
import com.example.matchitup.Word;
import com.example.matchitup.WordLoader;

import java.io.IOException;
import java.util.List;

public class DictionaryActivity extends AppCompatActivity {
    private DictionaryLoaderCallbacks bookLoaderCallbacks = new DictionaryLoaderCallbacks();
    private TextView definitionTitle, examplesTitle, description, examples, noResults, searchText;
    private ImageButton audio;
    private String audioUrl;
    private SearchView searchView;


    private boolean internetConnectionAvailable(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (connMgr != null) {
            activeNetwork = connMgr.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        searchText = findViewById(R.id.searchText);

        searchView = findViewById(R.id.search_view);
        EditText searchEditText =  searchView.findViewById(R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.colorAccent));
        searchEditText.setHintTextColor(getResources().getColor(R.color.normal_2));
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    configSearch(0f, 1f, R.color.white);
                } else {
                    configSearch(1f, 0.5f, R.drawable.gradient_menu);
                    searchView.setIconified(true);
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText.setVisibility(View.VISIBLE);
                searchView.clearFocus();
                searchText.setText(query);
                searchWord(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        this.definitionTitle = findViewById(R.id.definitionTitle);
        this.examplesTitle = findViewById(R.id.examplesTitle);
        this.description = findViewById(R.id.descriptionView);
        this.examples = findViewById(R.id.examplesView);
        this.noResults = findViewById(R.id.noResults);
        this.audio = findViewById(R.id.audioButton);
    }

    public void searchWord(String query) {

        if(internetConnectionAvailable() && (query.length() != 0)) {
            Bundle queryBundle = new Bundle();
            queryBundle.putString(DictionaryLoaderCallbacks.PARAM_QUERY, query);
            LoaderManager.getInstance(this).restartLoader(WordLoader.WORD_LOADER_ID, queryBundle, bookLoaderCallbacks);
        } else {
            noResultsToUser(getString(R.string.ups));
        }
    }

    public void playAudio(View view) {
        MediaPlayer mp = new MediaPlayer();

        try {
            mp.setDataSource(audioUrl);
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            Log.e("DictActivity, audio", "prepare() failed");
        }
    }

    private void configSearch(float weightSearchView, float weightTitle, int background){
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                weightSearchView
        );
        searchView.setLayoutParams(param);
        searchView.setBackgroundResource(background);
        param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                weightTitle
        );
        searchText.setLayoutParams(param);

    }

    public void noResultsToUser(String info){
        definitionTitle.setVisibility(View.GONE);
        examplesTitle.setVisibility(View.GONE);
        description.setVisibility(View.GONE);
        examples.setVisibility(View.GONE);
        noResults.setVisibility(View.VISIBLE);
        noResults.setText(info);
    }

    public void prepareResultsToUser(){
        definitionTitle.setVisibility(View.VISIBLE);
        examplesTitle.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
        examples.setVisibility(View.VISIBLE);
        noResults.setVisibility(View.GONE);
    }

    public class DictionaryLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Word>>  {
        public static final String PARAM_QUERY = "queryParam";

        /**
         * Si al llamar a initLoader() en onCreate() de MainActivity el ID no existe, se ejecuta esta
         * función. Si existía, borra el loader que había y vuelve aquí.
         */
        @Override
        public Loader<List<Word>> onCreateLoader(int id, @Nullable Bundle args) {
            return new WordLoader(DictionaryActivity.this, args.getString(PARAM_QUERY));
        }

        /**
         * Si en initLoader() del onCreate de MainActivity existiera el ID y ya ha generado datos, se
         * llama a esta función ya que significa que habría otro loader creado con el mismo ID.
         *
         * Esta función se ejecuta también cuando un Loader previo se ha terminado. Este suele ser el
         * punto en el que mueve los datos a las View mediante el adapter. La ejecución del
         * loadInBackground() pasa a esta función.
         */
        @Override
        public void onLoadFinished(@NonNull Loader<List<Word>> loader, List<Word> data) {
            if (data != null && data.size() > 0) {
                Word w = data.get(0);

                if (w.getDef() != null){
                    prepareResultsToUser();
                    description.setText(w.getDef());
                    if (w.getExamples() != null)
                        examples.setText(w.getExamples().toString());
                    if (w.getAudio() != null) {
                        audioUrl = w.getAudio();
                        audio.setVisibility(View.VISIBLE);
                    } else {
                        audio.setVisibility(View.INVISIBLE);
                    }
                }  else {
                    noResultsToUser(getString(R.string.no_results));
                }
            }

            destroyLoader(loader.getId());
        }

        /**
         * Cuando un Loader previamente creado se está resetando. En este punto la app debería eliminar
         * cualquier referencia que tenga de los datos del Loader.
         *
         * Esta función se llama solamente cuando el Loader actual se está destruyendo, por lo que se puede
         * dejar en blanco la mayoría del tiempo. No intentaremos acceder a datos después de hayan sido
         * borrados
         */
        @Override
        public void onLoaderReset(@NonNull Loader<List<Word>> loader) {
            loader.reset();
            description.setText("");
            examples.setText("");
            audioUrl = "";
        }
    }

    /**
     * Used by the LoaderCallback to destroy the current Loader.
     * DO NOT USE IT IN ANY OTHER CASE.
     * @param id the id of the Loader.
     */
    protected void destroyLoader(int id) {
        LoaderManager.getInstance(this).destroyLoader(id);
    }
}