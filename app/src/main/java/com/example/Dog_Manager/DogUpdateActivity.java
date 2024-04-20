package com.example.Dog_Manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class DogUpdateActivity extends AppCompatActivity {
    private String uid, userName;

    private TextView dogInfoTextView, vetInfoTextView, vaccinesTextView;
    private ArrayList<Map<String, String>> vaccinesData;
    private ArrayList<String> chipNumbers;
    private ImageView imageView;
    private Button goBackBtn, updateDogInfoBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_update);

        Intent newDogIntent = getIntent();
        uid = newDogIntent.getStringExtra("uid");
        userName = newDogIntent.getStringExtra("userName");
        if(uid != null)
            Log.d("uid", uid);
        else
            Log.e("uid", "UID is null");

        // Retrieve data
        String dogName = getIntent().getStringExtra("dogName");
        String dogBreed = getIntent().getStringExtra("dogBreed");
        String dogChipNo = getIntent().getStringExtra("dogChipNo");
        String dogDOB = getIntent().getStringExtra("dogDOB");
        String dogFavFood = getIntent().getStringExtra("dogFavFood");
        String dogImage = getIntent().getStringExtra("dogImage");

        String vetName = getIntent().getStringExtra("vetName");
        String vetLocation = getIntent().getStringExtra("vetLocation");
        String vetLastVisit = getIntent().getStringExtra("vetLastVisit");
        String vetNextVisit = getIntent().getStringExtra("vetNextVisit");

        String vaccinesJson = getIntent().getStringExtra("vaccinesData");
        Type type = new TypeToken<ArrayList<Map<String, String>>>(){}.getType();
        vaccinesData = new Gson().fromJson(vaccinesJson, type);

        chipNumbers = getIntent().getStringArrayListExtra("chipNumbers");

        findViews();

        // Set image
        if (dogImage != null && !dogImage.isEmpty()) {
            Glide.with(this)
                    .load(dogImage)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.dog_placeholder);
        }

        // Set dog info
        dogInfoTextView.setText("Name: " + dogName + "\n\nCheep NO: " + dogChipNo + "\n\ndogBreed: " + dogBreed + "\n\nDOB: " + dogDOB + "\n\nFavorite food: " + dogFavFood);

        // Set vet info
        vetInfoTextView.setText("Vet: " + vetName + "\n\nLocation: " + vetLocation + "\n\nLast Visit: " + vetLastVisit + "\n\nNext Visit: " + vetNextVisit);

        // Set vaccines info
        StringBuilder vaccinesStr = new StringBuilder("Vaccines:\n\n");
        for (Map<String, String> vaccine : vaccinesData) {
            vaccinesStr.append("- ").append(vaccine.get("kind")).append(": ").append(vaccine.get("dateTook")).append("\n\n");
        }
        vaccinesTextView.setText(vaccinesStr.toString());

        // Listeners:
        goBackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DogUpdateActivity.this, HomePageActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("userName", userName);

            startActivity(intent);
            finish();
        });

        updateDogInfoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DogUpdateActivity.this, UpdateDogInfoActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("userName", userName);

            intent.putExtra("dogChipNo", dogChipNo);

            startActivity(intent);
        });

    }

    private void findViews() {
        imageView = findViewById(R.id.detailedDogImageView);
        dogInfoTextView = findViewById(R.id.detailedDogData);
        vetInfoTextView = findViewById(R.id.detailedVetData);
        vaccinesTextView = findViewById(R.id.detailedVaccineData);
        goBackBtn = findViewById(R.id.buttonGoBackToHomePage);
        updateDogInfoBtn = findViewById(R.id.buttonUpdateDogData);
    }
}

