package com.example.Dog_Manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UpdateDogInfoActivity extends AppCompatActivity {
    private EditText editTextDogName, editTextDogDOB, editTextBreed, editTextFavoriteFood, editTextChipNumber;
    private LinearLayout vetInfoContainer, vaccineInfoContainer;
    private Button updateDogInfoBtn, goBackBtn, addVaccineInfoBtn, selectImageButton, addVetInfoBtn, deleteDogBtn;
    private ImageView selectedDogImageView;
    private Uri imageUri;
    private DatabaseReference dogRef;
    private String uid, userName, chipNumber;
    private View vetView;
    private Map<String, String> vetData;
    private ArrayList<Map<String, String>> allVaccinesData;
    private ActivityResultLauncher<Intent> imagePickerActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_specific_dog);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        userName = intent.getStringExtra("userName");
        chipNumber = intent.getStringExtra("dogChipNo");

        if (chipNumber == null) {
            Toast.makeText(this, "No chip number provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dogRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("dogs").child(chipNumber);

        findViews();

        // Disable chip NO (Chip number shouldn't be changed...)
        editTextChipNumber.setEnabled(false);

        // Making it look disabled
        editTextChipNumber.setTextColor(ContextCompat.getColor(this, R.color.disabled_text_color));
        editTextChipNumber.setBackgroundColor(ContextCompat.getColor(this, R.color.disabled_background_color));

        // Initialize the ActivityResultLauncher
        imagePickerActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            imageUri = data.getData();
                            selectedDogImageView.setImageURI(imageUri);
                        }
                    }
                }
        );

        setupListeners();
        loadDataFromFirebase();
    }

    private void findViews() {
        editTextDogName = findViewById(R.id.editTextDogName);
        editTextDogDOB = findViewById(R.id.editTextDogDOB);
        editTextBreed = findViewById(R.id.editTextBreed);
        editTextFavoriteFood = findViewById(R.id.editTextFavoriteFood);
        editTextChipNumber = findViewById(R.id.editTextChipNumber);
        selectedDogImageView = findViewById(R.id.selectedDogImageView);
        updateDogInfoBtn = findViewById(R.id.buttonUpdateDogInfo);
        goBackBtn = findViewById(R.id.buttonReturnHome);
        addVetInfoBtn = findViewById(R.id.buttonAddVetInfo);
        vetInfoContainer = findViewById(R.id.vetInfoContainer);
        vaccineInfoContainer = findViewById(R.id.vaccineInfoContainer);
        addVaccineInfoBtn = findViewById(R.id.buttonAddVaccineInfo);
        selectImageButton = findViewById(R.id.selectImageButton);
        deleteDogBtn = findViewById(R.id.buttonDeleteDog);

        addTextWatchers();
    }

    private void addTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateDogInfoBtn.setEnabled(
                        !editTextDogName.getText().toString().trim().isEmpty() &&
                                isValidDate(editTextDogDOB.getText().toString().trim()) &&
                                !editTextBreed.getText().toString().trim().isEmpty() &&
                                !editTextFavoriteFood.getText().toString().trim().isEmpty() &&
                                !editTextChipNumber.getText().toString().trim().isEmpty()
                );
            }
        };

        editTextDogName.addTextChangedListener(textWatcher);
        editTextDogDOB.addTextChangedListener(textWatcher);
        editTextBreed.addTextChangedListener(textWatcher);
        editTextFavoriteFood.addTextChangedListener(textWatcher);
        editTextChipNumber.addTextChangedListener(textWatcher);
    }

    private boolean isValidDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidPastDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate nowDate = LocalDate.now();
            LocalDate inputDate = LocalDate.parse(date, formatter);
            return !inputDate.isAfter(nowDate);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidFutureDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate nowDate = LocalDate.now();
            LocalDate inputDate = LocalDate.parse(date, formatter);
            return !inputDate.isBefore(nowDate);
        } catch (Exception e) {
            return false;
        }
    }

    private void setupListeners() {
        selectImageButton.setOnClickListener(view -> selectImage());
        addVaccineInfoBtn.setOnClickListener(v -> addVaccineInfoFields());
        updateDogInfoBtn.setOnClickListener(v -> saveUpdatedDogInfo());
        goBackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateDogInfoActivity.this, HomePageActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });
        addVetInfoBtn.setOnClickListener(v -> addVetInfoFields());
        deleteDogBtn.setOnClickListener(v -> deleteDog());
    }

    private void addVetInfoFields() {
        // Check and load existing vet data, or prepare to input new data
        if (vetData != null && !vetData.isEmpty()) {
            Log.d("UpdateDogInfoActivity", "Vet data found: " + vetData.toString());
            loadVetInfo(vetData);
        } else {
            Log.d("UpdateDogInfoActivity", "No vet data found, adding fields for new data entry.");
            LayoutInflater inflater = LayoutInflater.from(this);

            // Ensure the container is visible
            vetInfoContainer.setVisibility(View.VISIBLE);

            // Inflate the new vet info view
            vetView = inflater.inflate(R.layout.vet_info_fields, vetInfoContainer, false);

            // Create layout parameters to ensure the view is added correctly
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            vetView.setLayoutParams(params);

            // Add the view to the container
            vetInfoContainer.addView(vetView);

            // Request layout updates
            vetInfoContainer.requestLayout();
            vetInfoContainer.invalidate();
        }
    }


    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerActivityResultLauncher.launch(intent);
    }


    private void loadDataFromFirebase() {
        dogRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Map<String, String> dogData = (Map<String, String>) snapshot.child("dogData").getValue();
                vetData = (Map<String, String>) snapshot.child("vetData").getValue();
                if (dogData != null) {
                    editTextDogName.setText(dogData.get("name"));
                    editTextDogDOB.setText(dogData.get("dateOfBirth"));
                    editTextBreed.setText(dogData.get("breed"));
                    editTextFavoriteFood.setText(dogData.get("favoriteFood"));
                    editTextChipNumber.setText(chipNumber);
                }

                // Load and store vaccine data, but do not display it (vaccine can't be changed...)
                allVaccinesData = new ArrayList<>();
                for (DataSnapshot vaccineSnapshot : snapshot.child("vaccines").getChildren()) {
                    Map<String, String> vaccineData = (Map<String, String>) vaccineSnapshot.getValue();
                    allVaccinesData.add(vaccineData);
                }

                String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).into(selectedDogImageView);
                } else {
                    selectedDogImageView.setImageResource(R.drawable.dog_placeholder);
                }
            } else {
                Toast.makeText(this, "No data found for this dog.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load dog data.", Toast.LENGTH_SHORT).show());
    }


    private void loadVetInfo(Map<String, String> vetData) {
        // If vet data exists, expand and fill in the data
        if (vetData != null && !vetData.isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            vetView = inflater.inflate(R.layout.vet_info_fields, vetInfoContainer, false);

            EditText vetName = vetView.findViewById(R.id.vetName);
            EditText vetLocation = vetView.findViewById(R.id.vetLocation);
            EditText vetLastVisitDate = vetView.findViewById(R.id.vetLastVisitDate);
            EditText vetNextVisitDate = vetView.findViewById(R.id.vetNextVisitDate);

            vetName.setText(vetData.get("name"));
            vetLocation.setText(vetData.get("location"));
            vetLastVisitDate.setText(vetData.get("lastVisitDate"));
            vetNextVisitDate.setText(vetData.get("nextVisitDate"));

            vetInfoContainer.addView(vetView);
            vetInfoContainer.setVisibility(View.VISIBLE);
        }
    }


    private void addVaccineInfoFields() {
        View vaccineView = LayoutInflater.from(this).inflate(R.layout.vaccine_info_fields, vaccineInfoContainer, false);

        EditText kindEditText = vaccineView.findViewById(R.id.vaccineKind);
        EditText dateTookEditText = vaccineView.findViewById(R.id.vaccineDateTook);
        EditText nextDateEditText = vaccineView.findViewById(R.id.vaccineNextDate);
        Button saveVaccineInfoButton = vaccineView.findViewById(R.id.saveVaccineInfoButton);

        TextWatcher vaccineTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean enableButton = !kindEditText.getText().toString().trim().isEmpty() &&
                        isValidDate(dateTookEditText.getText().toString().trim()) &&
                        isValidDate(nextDateEditText.getText().toString().trim());

                saveVaccineInfoButton.setEnabled(enableButton);
            }
        };

        kindEditText.addTextChangedListener(vaccineTextWatcher);
        dateTookEditText.addTextChangedListener(vaccineTextWatcher);
        nextDateEditText.addTextChangedListener(vaccineTextWatcher);

        saveVaccineInfoButton.setEnabled(false); // Initially disabled
        saveVaccineInfoButton.setOnClickListener(view -> {
            Map<String, String> vaccineData = new HashMap<>();
            vaccineData.put("kind", kindEditText.getText().toString().trim());
            vaccineData.put("dateTook", dateTookEditText.getText().toString().trim());
            vaccineData.put("nextDate", nextDateEditText.getText().toString().trim());
            // Before continuing, checking if last date is before current time and next date is after current time
            if(!isValidPastDate(dateTookEditText.getText().toString().trim())) {
                Log.d("AddVaccineInfo", "dateEdittext: " + dateTookEditText.getText().toString().trim());
                Log.e("AddVaccineInfo", "Vaccine date in past is invalid.");
                Toast.makeText(UpdateDogInfoActivity.this, "Vaccine took date is invalid\ntry again..", Toast.LENGTH_LONG).show();
                return;
            }
            if(!isValidFutureDate(nextDateEditText.getText().toString().trim())) {
                Log.d("AddVaccineInfo", "nextDateEdittext: " + nextDateEditText.getText().toString().trim());
                Log.e("AddVaccineInfo", "Vaccine date in future is invalid.");
                Toast.makeText(UpdateDogInfoActivity.this, "Vaccine future date is invalid\ntry again..", Toast.LENGTH_LONG).show();
                return;
            }

            allVaccinesData.add(vaccineData); // Store the current vaccine data

            vaccineInfoContainer.removeView(vaccineView);
            saveVaccineInfoButton.setEnabled(true);

        });

        vaccineInfoContainer.addView(vaccineView);
        vaccineInfoContainer.setVisibility(View.VISIBLE);
        saveVaccineInfoButton.setEnabled(false);
    }

    private void saveUpdatedDogInfo() {
        if (!validateInputs()) {
            Toast.makeText(this, "Please check your inputs. Some fields are invalid.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("dogData/name", editTextDogName.getText().toString().trim());
        updateData.put("dogData/dateOfBirth", editTextDogDOB.getText().toString().trim());
        updateData.put("dogData/breed", editTextBreed.getText().toString().trim());
        updateData.put("dogData/favoriteFood", editTextFavoriteFood.getText().toString().trim());
        updateData.put("dogData/chipNumber", editTextChipNumber.getText().toString().trim());

        if (vetInfoContainer.getChildCount() > 0) {
            View vetView = vetInfoContainer.getChildAt(0);
            updateData.put("vetData/name", ((EditText) vetView.findViewById(R.id.vetName)).getText().toString().trim());
            updateData.put("vetData/location", ((EditText) vetView.findViewById(R.id.vetLocation)).getText().toString().trim());
            updateData.put("vetData/lastVisitDate", ((EditText) vetView.findViewById(R.id.vetLastVisitDate)).getText().toString().trim());
            updateData.put("vetData/nextVisitDate", ((EditText) vetView.findViewById(R.id.vetNextVisitDate)).getText().toString().trim());
        }

        if (!allVaccinesData.isEmpty()) {
            updateData.put("vaccines", allVaccinesData);
        }

        if (imageUri != null) {
            String imagePath = "dog_images/" + chipNumber + "_" + System.currentTimeMillis();
            StorageReference imageRef = FirebaseStorage.getInstance().getReference(imagePath);

            imageRef.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return imageRef.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String imageUrl = task.getResult().toString();
                            updateData.put("imageUrl", imageUrl);
                            updateFirebaseData(updateData);
                        } else {
                            Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            updateFirebaseData(updateData);
        }
    }



    private boolean validateInputs() {
        boolean isValid = editTextDogName.getText().length() > 0 &&
                isValidDate(editTextDogDOB.getText().toString()) &&
                editTextBreed.getText().length() > 0 &&
                editTextFavoriteFood.getText().length() > 0 &&
                editTextChipNumber.getText().length() > 0 &&
                isValidPastDate(editTextDogDOB.getText().toString());

        // Check if vet info views are added and validate them if they are
        if (vetInfoContainer.getChildCount() > 0) {
            View vetView = vetInfoContainer.getChildAt(0);
            EditText vetNextVisitDate = vetView.findViewById(R.id.vetNextVisitDate);
            isValid &= isValidFutureDate(vetNextVisitDate.getText().toString());
        }

        return isValid;
    }


    private void updateFirebaseData(Map<String, Object> updateData) {
        dogRef.updateChildren(updateData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Dog information updated successfully.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UpdateDogInfoActivity.this, HomePageActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("userName", userName);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to update dog information\ntry again...", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void deleteDog() {
        // Confirm with the user before deletion
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this dog?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dogRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpdateDogInfoActivity.this, "Dog deleted successfully.", Toast.LENGTH_SHORT).show();
                            // Redirect back to the home page
                            Intent intent = new Intent(UpdateDogInfoActivity.this, HomePageActivity.class);
                            intent.putExtra("uid", uid);
                            intent.putExtra("userName", userName);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(UpdateDogInfoActivity.this, "Failed to delete dog.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
