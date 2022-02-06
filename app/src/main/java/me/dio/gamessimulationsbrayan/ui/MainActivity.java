package me.dio.gamessimulationsbrayan.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.icu.text.UnicodeSetIterator;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Random;

import me.dio.gamessimulationsbrayan.data.MatchesApi;
import me.dio.gamessimulationsbrayan.databinding.ActivityMainBinding;
import me.dio.gamessimulationsbrayan.domain.Match;
import me.dio.gamessimulationsbrayan.ui.adapter.MatchesAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final UnicodeSetIterator R = ;
    private ActivityMainBinding binding;
    private MatchesApi matchesApi;
    private MatchesAdapter mathesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupHttpClient();
        setupMatchesList();
        setupMatchesRefresh();
        setupFloatingActionButton();
    }

    private void setupHttpClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://brayancrc.github.io/matches-simulator-api/matches.json")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

      matchesApi = retrofit.create(MatchesApi.class);
    }


    private void setupMatchesList() {
        binding.rvMatches.setHasFixedSize(true);
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        findMatchesFromApi();
    }


    private void setupMatchesRefresh() {
        binding.srlMatches.setOnRefreshListener(this::findMatchesFromApi);
    }

    private void setupFloatingActionButton() {
        binding.fabSimulate.setOnClickListener(view -> {
            view.animate().rotationBy(360).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    Random random = new Random();
                    for (int i = 0; i < mathesAdapter.getItemCount(); i++) {
                        Match match = mathesAdapter.getMatches().get(i);
                        match.getHomeTeam().setScore(random.nextInt(match.getHomeTeam().getStars() + 1));
                        match.getAwayTeam().setScore(random.nextInt(match.getAwayTeam().getStars() + 1));
                        mathesAdapter.notifyItemChanged(i);

                    }
                }
            });
        });
    }

    private void findMatchesFromApi() {
        binding.srlMatches.setRefreshing(true);
        matchesApi.getMatches().enqueue(new Callback<List<MatchesApi>>() {
            @Override
            public void onResponse(Call<List<MatchesApi>> call, Response<List<MatchesApi>> response) {
                if (response.isSuccessful()) {
                    List<Match> matches = response.body();
                    mathesAdapter = new MatchesAdapter(matches);
                    binding.rvMatches.setAdapter(mathesAdapter);
                } else {
                    showErrorMessage();
                }
                binding.srlMatches.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<MatchesApi>> call, Throwable t) {
                showErrorMessage();
                binding.srlMatches.setRefreshing(false);

            }
        });
    }

    private void showErrorMessage() {
        Snackbar.make(binding.fabSimulate, R.string.error_api, Snackbar.LENGTH_LONG).show();
    }
}
