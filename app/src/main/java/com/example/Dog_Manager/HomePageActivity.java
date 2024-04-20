package com.example.Dog_Manager;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Dog_Manager.Adapters.DogAdapter;
import com.example.Dog_Manager.Interfaces.DogItemClickListener;
import com.example.Dog_Manager.Objects.Dog;
import com.example.Dog_Manager.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

public class HomePageActivity extends AppCompatActivity implements DogItemClickListener {

    private TextView welcomeTextView;
    private Button addNewDogBtn, signOutBtn;
    private String uid, userName;

    private RecyclerView dogsRecyclerView;
    private DogAdapter dogsAdapter;
    private ArrayList<Dog> dogsList;
    private ArrayList<String> chipNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        this.dogsList = new ArrayList<>();
        this.chipNumbers = new ArrayList<>();

        Intent homeIntent = getIntent();
        uid = homeIntent.getStringExtra("uid");
        if(uid != null)
            Log.d("homePageActivity - uid", uid);
        else
            Log.e("uid", "uid is null");

        findViews();

        getDogsDataFromFirebase();

        //Introduction to user only if came from loginActivity:
        String fromLogin = homeIntent.getStringExtra("cameFromLogin");
        if (fromLogin != null && fromLogin.equals("true")) {
            Toast
                    .makeText(this, "Welcome!\nShort tap on the dog's image to update the data\nLong tap to see how long till the next birthday!", Toast.LENGTH_LONG)
                    .show();
        } else {
            Log.d("HomePageActivity", "Not initiated from login activity");
        }

        // Get the user's name from the login intent
        userName = getIntent().getStringExtra("userName");
        if (userName != null && !userName.isEmpty()) {
            welcomeTextView.setText("Hello, " + userName + "!");
        } else {
            welcomeTextView.setText("Hello, Guest!");
        }

        signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomePageActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        addNewDogBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, AddNewDogActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("userName", userName);
            intent.putStringArrayListExtra("chipNumbers", chipNumbers);
            startActivity(intent);
        });


    }

    protected void onStop() {
        super.onStop();
        // Sign out the user
        FirebaseAuth.getInstance().signOut();
    }

    private void findViews() {
        welcomeTextView = findViewById(R.id.welcomeTextView);
        addNewDogBtn = findViewById(R.id.addNewDogButton);
        signOutBtn = findViewById(R.id.signOutButton);
    }

    private void getDogsDataFromFirebase() {
        // Initialize the RecyclerView and Adapter
        RecyclerView dogsRecyclerView = findViewById(R.id.dogsRecyclerView);
        DogAdapter dogAdapter = new DogAdapter(new ArrayList<>(), this);
        dogsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dogsRecyclerView.setAdapter(dogAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userDogsRef = database.getReference("users").child(uid).child("dogs");

        userDogsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Dog> tempDogsList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, String> dogData = (Map<String, String>) snapshot.child("dogData").getValue();
                    Map<String, String> vetData = (Map<String, String>) snapshot.child("vetData").getValue();
                    ArrayList<Map<String, String>> vaccinesData = new ArrayList<>();
                    for (DataSnapshot vaccineSnapshot : snapshot.child("vaccines").getChildren()) {
                        vaccinesData.add((Map<String, String>) vaccineSnapshot.getValue());
                    }
                    String imageUrl = null;
                    if (snapshot.hasChild("imageUrl")) {
                        imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    }
                    tempDogsList.add(new Dog(dogData, vetData, vaccinesData, imageUrl));
                }
                tempDogsList.forEach(dog -> {
                    Log.d("HomePageDogs", dog.toString());
                    chipNumbers.add(dog.getDogData().get("chipNumber"));
                    });

                dogAdapter.updateDogsList(tempDogsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseDB", "Failed to load dogs data", databaseError.toException());
            }
        });
    }

    @Override
    public void onDogClick(Dog dog) {
        Intent intent = new Intent(HomePageActivity.this, DogUpdateActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("userName", userName);

        // Dog data
        intent.putExtra("dogName", dog.getDogData().get("name"));
        intent.putExtra("dogChipNo", dog.getDogData().get("chipNumber"));
        intent.putExtra("dogBreed", dog.getDogData().get("breed"));
        intent.putExtra("dogDOB", dog.getDogData().get("dateOfBirth"));
        intent.putExtra("dogFavFood", dog.getDogData().get("favoriteFood"));
        intent.putExtra("dogImage", dog.getImageUrl());

        // Vet data with null checks
        if (dog.getVetData() != null) {
            intent.putExtra("vetName", dog.getVetData().get("name"));
            intent.putExtra("vetLocation", dog.getVetData().get("location"));
            intent.putExtra("vetLastVisit", dog.getVetData().get("lastVisitDate"));
            intent.putExtra("vetNextVisit", dog.getVetData().get("nextVisitDate"));
        } else {
            intent.putExtra("vetName", "Not available");
            intent.putExtra("vetLocation", "Not available");
            intent.putExtra("vetLastVisit", "Not available");
            intent.putExtra("vetNextVisit", "Not available");
        }

        // Vaccine data with Gson to pass it through intent
        intent.putExtra("vaccinesData", new Gson().toJson(dog.getVaccinesData()));

        intent.putStringArrayListExtra("chipNumbers", chipNumbers);

        startActivity(intent);
    }


}
