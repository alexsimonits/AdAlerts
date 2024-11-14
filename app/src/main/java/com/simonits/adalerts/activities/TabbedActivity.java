package com.simonits.adalerts.activities;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayoutMediator;
import com.simonits.adalerts.R;
import com.google.android.material.tabs.TabLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.view.Menu;
import android.view.MenuItem;

import com.simonits.adalerts.activities.ui.main.SectionsStateAdapter;

import java.util.Objects;

public class TabbedActivity extends AppCompatActivity {

    public static TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String name = getIntent().getStringExtra("name");
        int alertID = getIntent().getIntExtra("alertId", -1);

        setTitle(name);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        SectionsStateAdapter sectionsStateAdapter = new SectionsStateAdapter(getSupportFragmentManager(), getLifecycle(), this, alertID);
        ViewPager2 viewPager = findViewById(R.id.details_viewPager);
        viewPager.setOffscreenPageLimit(3); // this needs to be one upped for every new Site I add
        if (sectionsStateAdapter.getItemCount() == 1) {
            viewPager.setUserInputEnabled(false);
        }
        viewPager.setAdapter(sectionsStateAdapter);
        tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager,
                (tab, position) -> tab.setText(sectionsStateAdapter.getTabTitle(position))
        ).attach();
        for (int i = 0; i < sectionsStateAdapter.getItemCount(); i++) {
            if (sectionsStateAdapter.getTabTitle(i).contains("(!)")) {
                viewPager.setCurrentItem(i);
                i = sectionsStateAdapter.getItemCount();
            }
        }

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.scale_in, R.anim.slide_out_left);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.scale_in, R.anim.slide_out_left);
        }

        return super.onOptionsItemSelected(item);
    }

}