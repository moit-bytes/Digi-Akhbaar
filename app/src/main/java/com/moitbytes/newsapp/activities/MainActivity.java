package com.moitbytes.newsapp.activities;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.moitbytes.newsapp.R;
import com.moitbytes.newsapp.WebViewActivity;
import com.moitbytes.newsapp.adapters.AdapterListNews;
import com.moitbytes.newsapp.clicklisteners.AdapterItemClickListener;
import com.moitbytes.newsapp.clicklisteners.NewsDialogClickListeners;
import com.moitbytes.newsapp.databinding.NewsDialogBinding;
import com.moitbytes.newsapp.model.News;
import com.moitbytes.newsapp.restapi.CovidService;
import com.moitbytes.newsapp.utils.LocaleHelper;
import com.moitbytes.newsapp.utils.Util;
import com.moitbytes.newsapp.viewmodels.MainViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity implements LifecycleOwner, AdapterItemClickListener {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.ivToolbarCountry)
    ImageView ivToolbarCountry;
    Retrofit retrofit;
    Snackbar snackbar;


    MainActivity context;
    MainViewModel viewModel;
    AdapterListNews adapterListNews;
    List<News> newsList;
    RecyclerView rv;

    TextView tv1, tv2, tv3, supp, news;
    LinearLayout linearLayout;
    CardView cardView;

    private String firstControl = "firstControl";
    private String countryPositionPref = "countryPositionPref";
    SharedPreferences pref;
    private String[] countrys;
    private TypedArray countrysIcons;
    Animation topAnim, bottomAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getApplicationContext().getSharedPreferences(Util.APP_NAME, MODE_PRIVATE);
        languageControl();
        setContentView(R.layout.activity_main);

        tv1 = findViewById(R.id.active);
        tv2 = findViewById(R.id.recov);
        tv3 = findViewById(R.id.death);
        supp = findViewById(R.id.supp);
        news = findViewById(R.id.news);
        rv = findViewById(R.id.recyclerView);
        linearLayout = findViewById(R.id.icons);
        cardView =findViewById(R.id.cardCovidUpdate);
        cardView.setVisibility(View.GONE);
        linearLayout.setVisibility(View.GONE);
        supp.setVisibility(View.GONE);
        rv.setVisibility(View.VISIBLE);
        news.setVisibility(View.VISIBLE);
        getResponse();



        topAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_animation);




        context = this;
        ButterKnife.bind(this);
        countrys = getResources().getStringArray(R.array.countrys);
        countrysIcons = getResources().obtainTypedArray(R.array.countrysIcons);

        initToolbar();

        newsList = new ArrayList<>();
        adapterListNews = new AdapterListNews(newsList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapterListNews);


        if (pref.contains(countryPositionPref))
            ivToolbarCountry.setImageResource(countrysIcons.getResourceId(pref.getInt(countryPositionPref, 0), 0));

        viewModel = ViewModelProviders.of(context).get(MainViewModel.class);
        viewModel.getNewsLiveData().observe(context, newsListUpdateObserver);
        viewModel.setApiKey(getString(R.string.news_api_key));
        viewModel.setCountryCode(pref.getString(Util.COUNTRY_PREF, "in"));



    }

    private void languageControl() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N && !pref.getBoolean(firstControl, false)) {
            Locale primaryLocale = getResources().getConfiguration().getLocales().get(0);
            LocaleHelper.setLocale(MainActivity.this, primaryLocale.getLanguage());
            int position = getLanguagePosition(primaryLocale.getLanguage());
            pref.edit().putInt(countryPositionPref, position).apply();
            pref.edit().putBoolean(firstControl, true).apply();
            recreate();
        }
    }

    private int getLanguagePosition(String displayLanguage) {
        String[] codes = getResources().getStringArray(R.array.countrysCodes);
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(displayLanguage)) return i;
        }
        return 0;
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle(null);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Util.setSystemBarColor(this, android.R.color.white);
        Util.setSystemBarLight(this);
    }

    private void showLanguageDialog() {
        new AlertDialog.Builder(this).setCancelable(false)
                .setTitle("Choose Country")
                .setSingleChoiceItems(countrys, pref.getInt(countryPositionPref, 0), null)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.ok, (dialog, whichButton) -> {
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    pref.edit().putInt(countryPositionPref, selectedPosition).apply();
                    pref.edit().putString(Util.COUNTRY_PREF, getResources().getStringArray(R.array.countrysCodes)[selectedPosition]).apply();
                    LocaleHelper.setLocale(MainActivity.this, getResources().getStringArray(R.array.countrysCodes)[selectedPosition]);
                    recreate();
                    dialog.dismiss();
                })
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        Util.changeMenuIconColor(menu, Color.BLACK);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }
        searchView.setQueryHint(getString(R.string.search_in_everything));
        if (searchView != null)
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (viewModel != null) viewModel.searchNews(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        return true;
    }

    public void categoryClicked(View view) {
        viewModel.newsCategoryClick(String.valueOf(view.getTag()));
        if((view.getTag().toString()).equals("Covid"))
        {
            cardView.setVisibility(View.VISIBLE);
            supp.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);
            news.setVisibility(View.VISIBLE);
            rv.setVisibility(View.VISIBLE);
            getResponse();
        }
        else if((view.getTag().toString()).equals("support"))
        {
            cardView.setVisibility(View.GONE);
            news.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            supp.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            cardView.setVisibility(View.GONE);
            supp.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);
            news.setVisibility(View.VISIBLE);
            rv.setVisibility(View.VISIBLE);
            snackbar = Snackbar.make(view,"Fetching the Top Headlines for you",
                    Snackbar.LENGTH_LONG);
            snackbar.setAction("Close", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            snackbar.setBackgroundTint(getResources()
                    .getColor(R.color.blue_grey_900));
            snackbar.setActionTextColor(getResources()
                    .getColor(R.color.yellow_900));
            snackbar.show();

        }


    }


    public void countryClick(View view) {
        showLanguageDialog();
    }

    Observer<List<News>> newsListUpdateObserver = new Observer<List<News>>() {
        @Override
        public void onChanged(List<News> news) {
            newsList.clear();
            if (news != null) {
                newsList.addAll(news);
            }
            adapterListNews.notifyDataSetChanged();
        }
    };


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    public void onNewsItemClick(News news) {
        showDialogPolygon(news);
    }

    private void showDialogPolygon(News news) {
        final Dialog dialog = new Dialog(this);
        NewsDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getApplicationContext()), R.layout.dialog_header_polygon, null, false);
        binding.setNews(news);
        binding.setListener(new NewsDialogClickListeners() {
            @Override
            public void onGotoWebSiteClick(String url) {
                Intent i = new Intent(MainActivity.this, WebViewActivity.class);
                i.putExtra("URL", url);
                startActivity(i);
            }

            @Override
            public void onDismissClick() {
                dialog.dismiss();
            }
        });

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(binding.getRoot());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(true);

        dialog.show();
    }

    public void getResponse()
    {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://covid-19-data.p.rapidapi.com")
                .addConverterFactory(ScalarsConverterFactory.create()).build();

        CovidService service = retrofit.create(CovidService.class);
        Call<String> response = service.getFeed("json", pref.getString(Util.COUNTRY_PREF, "in"));
        response.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                try
                {
                    JSONArray countryJSONArray = new JSONArray(response.body());
                    JSONObject eachDate = countryJSONArray.getJSONObject(0);
                    String active = eachDate.getString("confirmed");
                    String death = eachDate.getString("deaths");
                    String recovered = eachDate.getString("recovered");



                    tv1.setAnimation(bottomAnim);
                    tv2.setAnimation(bottomAnim);
                    tv3.setAnimation(bottomAnim);

                    tv1.setText(active);
                    tv2.setText(recovered);
                    tv3.setText(death);

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    public void getLink(View view)
    {
        if((view.getTag().toString()).equals("git"))
        {
            Intent implicit = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/moit-bytes"));
            startActivity(implicit);
        }
        else if((view.getTag().toString()).equals("linkedin"))
        {
            Intent implicit = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/moitbytes/"));
            startActivity(implicit);

        }

    }
}
