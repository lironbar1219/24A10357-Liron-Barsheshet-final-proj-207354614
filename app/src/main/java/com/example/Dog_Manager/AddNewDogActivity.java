package com.example.Dog_Manager;

import android.app.Activity;
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

// Firebase image upload
import com.example.Dog_Manager.Interfaces.FirebaseResultCallback;
import com.example.Dog_Manager.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class AddNewDogActivity extends AppCompatActivity {

    private LinearLayout vaccineInfoContainer, vetInfoContainer, dogDataContainer;
    private Button buttonAddVaccineInfo, buttonAddVetInfo, buttonReturnHome;
    private boolean isVetInfoAdded = false;
    private final List<Map<String, String>> allVaccinesData = new ArrayList<>();
    private Map<String, String> vetData = new HashMap<>();
    private Button saveDogInfo;
    private View vetView;
    private String uid, userName;

    private ImageView selectedDogImageView;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> imagePickerActivityResultLauncher;
    private EditText editTextDogName, editTextDogDOB, editTextBreed, editTextFavoriteFood, editTextChipNumber;
    private ArrayList<String> chipNumbers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewdog);

        Intent newDogIntent = getIntent();
        uid = newDogIntent.getStringExtra("uid");
        userName = newDogIntent.getStringExtra("userName");
        chipNumbers = newDogIntent.getStringArrayListExtra("chipNumbers");
        Log.d("AddNewDogActivity", "chipNumbers: " + chipNumbers.toString());

        if(uid != null)
            Log.d("uid", uid);
        else
            Log.e("uid", "UID is null");


        findViews();

        View dogView = LayoutInflater.from(this).inflate(R.layout.activity_addnewdog, dogDataContainer, false);

        TextWatcher dogInfoTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean enableButton = !editTextDogName.getText().toString().trim().isEmpty() &&
                        isValidDate(editTextDogDOB.getText().toString().trim()) &&
                        !editTextBreed.getText().toString().trim().isEmpty() &&
                        !editTextFavoriteFood.getText().toString().trim().isEmpty() &&
                        !editTextChipNumber.getText().toString().trim().isEmpty() ;

                saveDogInfo.setEnabled(enableButton);
            }
        };

        editTextDogName.addTextChangedListener(dogInfoTextWatcher);
        editTextDogDOB.addTextChangedListener(dogInfoTextWatcher);
        editTextBreed.addTextChangedListener(dogInfoTextWatcher);
        editTextFavoriteFood.addTextChangedListener(dogInfoTextWatcher);
        editTextChipNumber.addTextChangedListener(dogInfoTextWatcher);

        saveDogInfo.setEnabled(false); // Initially disabled
        saveDogInfo.setOnClickListener(v -> {
            saveInfoAndSendToDB(savedInDB -> {
                if (savedInDB) {
                    Toast.makeText(AddNewDogActivity.this, "Dog saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddNewDogActivity.this, HomePageActivity.class);
                    intent.putExtra("uid", uid);
                    intent.putExtra("userName", userName);
                    startActivity(intent);
                } else {
                    Toast.makeText(AddNewDogActivity.this, "Oops.. Dog saving failed.\nPlease try again.", Toast.LENGTH_SHORT).show();
                    Log.e("SaveDogInfo", "Failed to save dog data.");
                }
            });
        });

        buttonAddVaccineInfo.setOnClickListener(v -> addVaccineInfoFields());
        buttonAddVetInfo.setOnClickListener(v -> {
            if (!isVetInfoAdded) {
                addVetInfoFields();
                isVetInfoAdded = true;
                buttonAddVetInfo.setEnabled(false); // Disable after adding
            }
        });

        buttonReturnHome.setOnClickListener(v -> {
            Intent intent = new Intent(AddNewDogActivity.this, HomePageActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("userName", userName);
            startActivity(intent);
        });

        Button selectImageButton = findViewById(R.id.selectImageButton);

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

        // Set the click listener for image selection button
        selectImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerActivityResultLauncher.launch(intent);
        });
    }

    private void findViews() {
        vaccineInfoContainer = findViewById(R.id.vaccineInfoContainer);
        vetInfoContainer = findViewById(R.id.vetInfoContainer);
        dogDataContainer = findViewById(R.id.dogDataContainer);
        buttonAddVaccineInfo = findViewById(R.id.buttonAddVaccineInfo);
        buttonAddVetInfo = findViewById(R.id.buttonAddVetInfo);
        saveDogInfo = findViewById(R.id.buttonSaveDogInfo);
        selectedDogImageView = findViewById(R.id.selectedDogImageView);
        buttonReturnHome = findViewById(R.id.buttonReturnHome);
        editTextDogName = findViewById(R.id.editTextDogName);
        editTextDogDOB = findViewById(R.id.editTextDogDOB);
        editTextBreed = findViewById(R.id.editTextBreed);
        editTextFavoriteFood = findViewById(R.id.editTextFavoriteFood);
        editTextChipNumber = findViewById(R.id.editTextChipNumber);
    }

    private void addVetInfoFields() {
        vetView = LayoutInflater.from(this).inflate(R.layout.vet_info_fields, vetInfoContainer, false);
        vetInfoContainer.addView(vetView);
        vetInfoContainer.setVisibility(View.VISIBLE);
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
                Toast.makeText(AddNewDogActivity.this, "Vaccine took date is invalid\ntry again..", Toast.LENGTH_LONG).show();
                return;
            }
            if(!isValidFutureDate(nextDateEditText.getText().toString().trim())) {
                Log.d("AddVaccineInfo", "nextDateEdittext: " + nextDateEditText.getText().toString().trim());
                Log.e("AddVaccineInfo", "Vaccine date in future is invalid.");
                Toast.makeText(AddNewDogActivity.this, "Vaccine future date is invalid\ntry again..", Toast.LENGTH_LONG).show();
                return;

            }

            allVaccinesData.add(vaccineData); // Store the current vaccine data

            vaccineInfoContainer.removeView(vaccineView);
            buttonAddVaccineInfo.setEnabled(true);

        });

        vaccineInfoContainer.addView(vaccineView);
        vaccineInfoContainer.setVisibility(View.VISIBLE);
        buttonAddVaccineInfo.setEnabled(false);
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

    private void saveInfoAndSendToDB(FirebaseResultCallback callback) {
        // Before proceed, checking if DOB of dog is before the current date
        if(!isValidPastDate(editTextDogDOB.getText().toString().trim())) {
            Log.e("SaveAllData", "Dog/vet date is invalid.");
            Toast.makeText(AddNewDogActivity.this, "At least one of the dates entered is invalid\ntry again..", Toast.LENGTH_LONG).show();
            callback.onResult(false);
            return;
        }
        if (vetInfoContainer.getChildCount() > 0 && vetView != null) {
            if(!isValidPastDate(((EditText) vetView.findViewById(R.id.vetLastVisitDate)).getText().toString().trim()) || !isValidFutureDate(((EditText) vetView.findViewById(R.id.vetNextVisitDate)).getText().toString().trim())) {
                Log.e("SaveAllData", "Vet date is invalid.");
                Toast.makeText(AddNewDogActivity.this, "At least one of the dates entered is invalid\ntry again..", Toast.LENGTH_LONG).show();
                callback.onResult(false);
                return;
            }

            // Find and store vet data
            vetData.put("name", ((EditText) vetView.findViewById(R.id.vetName)).getText().toString().trim());
            vetData.put("location", ((EditText) vetView.findViewById(R.id.vetLocation)).getText().toString().trim());
            vetData.put("lastVisitDate", ((EditText) vetView.findViewById(R.id.vetLastVisitDate)).getText().toString().trim());
            vetData.put("nextVisitDate", ((EditText) vetView.findViewById(R.id.vetNextVisitDate)).getText().toString().trim());
        }



        // Collect Dog Data
        Map<String, String> dogData = new HashMap<>();
        dogData.put("name", editTextDogName.getText().toString().trim());
        dogData.put("dateOfBirth", editTextDogDOB.getText().toString().trim());
        dogData.put("breed", editTextBreed.getText().toString().trim());
        dogData.put("favoriteFood", editTextFavoriteFood.getText().toString().trim());
        dogData.put("chipNumber", editTextChipNumber.getText().toString().trim());

        if (!isValidDogData(dogData)) {
            Log.e("SaveAllData", "Dog data is incomplete or invalid.");
            callback.onResult(false);
            return;
        }

        if (imageUri != null) {
            try {
                // Image has been selected, proceed with upload
                String fileName = "dog_images/" + editTextChipNumber.getText().toString().trim() + "_" + System.currentTimeMillis();
                StorageReference imageRef = FirebaseStorage.getInstance().getReference(fileName);

                imageRef.putFile(imageUri)
                        .continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                Log.e("ImageToStorageException", task.getException().toString());
                            }
                            return imageRef.getDownloadUrl();
                        })
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                String imageUrl = downloadUri.toString();
                                saveDogData(dogData, vetData, allVaccinesData, imageUrl, isSuccess -> {
                                    if (isSuccess) {
                                        Log.d("SaveAllData", "Dog data saved successfully (with image).");
                                        callback.onResult(true);
                                    } else {
                                        Log.e("SaveAllData", "Failed to save dog data (with image).");
                                        callback.onResult(false);
                                    }
                                });
                            } else {
                                // Handle the failure
                                Log.e("Upload Image", "Failed to upload image.", task.getException());
                                callback.onResult(false);
                            }
                        });
            } catch (Exception e) {
                Log.e("SaveAllData", "Failed to save data (with image)", e);
                callback.onResult(false);
                return;
            }
        } else {
            saveDogData(dogData, vetData, allVaccinesData, null, isSuccess -> {
                if (isSuccess) {
                    Log.d("SaveAllData", "Dog data saved successfully (with placeholder).");
                    callback.onResult(true);
                } else {
                    Log.e("SaveAllData", "Failed to save dog data (with placeholder).");
                    callback.onResult(false);
                }
            });
            Log.d("SaveAllData", "No image selected, saving data without image.");
        }
    }

    private boolean isValidPastDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate nowDate = LocalDate.now();
            LocalDate dob = LocalDate.parse(date, formatter);
            if (dob.isAfter(nowDate)) {
                Log.e("SaveAllData", "The date " + date + " is after the current date.");
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e("SaveAllData", "Failed to validate DOB", e);
            return false;
        }
    }

    private boolean isValidFutureDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate nowDate = LocalDate.now();
            LocalDate dob = LocalDate.parse(date, formatter);
            if (dob.isBefore(nowDate)) {
                Log.e("SaveAllData", "The date " + date + " is before the current date.");
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e("SaveAllData", "Failed to validate DOB", e);
            return false;
        }
    }

    private void saveDogData(Map<String, String> dogData, Map<String, String> vetData, List<Map<String, String>> vaccinesData, String imageUrl, FirebaseResultCallback callback) {
        Boolean passedOk = false;
        Map<String, Object> allData = new HashMap<>();
        allData.put("dogData", dogData);
        allData.put("vetData", vetData);
        allData.put("vaccines", vaccinesData);
        if (imageUrl != null) {
            allData.put("imageUrl", imageUrl); // Include the image URL
        }

        // Before proceed, checking if the chip number is unique
        String chipNumber = dogData.get("chipNumber");
        if(chipNumbers.contains(chipNumber)) {
            Log.e("AddNewDogActivity - SaveDogData", "Chip number is not unique.");
            Toast.makeText(AddNewDogActivity.this, "Chip number is not unique\ntry again..", Toast.LENGTH_LONG).show();
            return;
        }


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dogsRef = database.getReference("users")
                .child(uid)
                .child("dogs")
                .child(dogData.get("chipNumber"));

        dogsRef.setValue(allData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseDB", "Data saved successfully for dog with chip number: " + dogData.get("chipNumber"));
                    callback.onResult(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDB", "Failed to save data", e);
                    callback.onResult(false);
                });
    }



    private boolean isValidDogData(Map<String, String> dogData) {
        for (Map.Entry<String, String> entry : dogData.entrySet()) {
            String value = entry.getValue();
            if (value.isEmpty() || (entry.getKey().contains("Date") && !isValidDate(value))) {
                return false;
            }
        }
        return true;
    }

}
