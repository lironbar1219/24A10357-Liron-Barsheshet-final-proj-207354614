package com.example.Dog_Manager.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.Dog_Manager.Interfaces.DogItemClickListener;
import com.example.Dog_Manager.Objects.Dog;
import com.example.Dog_Manager.R;

import java.util.ArrayList;

public class DogAdapter extends RecyclerView.Adapter<DogAdapter.DogViewHolder> {
    private ArrayList<Dog> dogList;
    private ViewGroup parent;
    private int viewType;
    private DogItemClickListener listener;


    public DogAdapter(ArrayList<Dog> dogList, DogItemClickListener listener) {
        this.dogList = dogList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        this.viewType = viewType;
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dog_item, parent, false);
        return new DogViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull DogViewHolder holder, int position) {
        Dog dog = dogList.get(position);
        holder.itemView.setOnClickListener(v -> listener.onDogClick(dog));
        holder.bind(dog);
    }

    @Override
    public int getItemCount() {
        return dogList.size();
    }

    public void updateDogsList(ArrayList<Dog> newDogsList) {
        dogList.clear();
        dogList.addAll(newDogsList);
        notifyDataSetChanged();
    }

    static class DogViewHolder extends RecyclerView.ViewHolder {
        TextView dogDataTextView;
        TextView vetDataTextView;
        ImageView dogImageView;
        DogItemClickListener listener;

        public DogViewHolder(@NonNull View itemView, DogItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            dogDataTextView = itemView.findViewById(R.id.dogData);
            vetDataTextView = itemView.findViewById(R.id.vetData);
            dogImageView = itemView.findViewById(R.id.dogShapeableImageView);
        }

        void bind(Dog dog) {
            // Convert dogData into displayable String
            String dogInfo = "Name: " + dog.getDogData().get("name") + "\nBreed: " + dog.getDogData().get("breed");
            dogDataTextView.setText(dogInfo);

            // Check if vet data is available before accessing it
            if (dog.getVetData() != null && dog.getVetData().get("name") != null) {
                String vetInfo = "Vet: " + dog.getVetData().get("name") + "\nLocation: " + dog.getVetData().get("location");
                vetDataTextView.setText(vetInfo);
            } else {
                // Handle case where vet data is not available
                Log.d("DogAdapter", "No vet data available");
                vetDataTextView.setText("No vet data available");
            }

            // Load the dog image
            if (dog.getImageUrl() != null && !dog.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(dog.getImageUrl())
                        .centerCrop()
                        .into(dogImageView);
            } else {
                dogImageView.setImageResource(R.drawable.dog_placeholder); // Default placeholder
            }

            dogImageView.setOnClickListener(v -> {
                listener.onDogClick(dog);
            });
            dogImageView.setOnLongClickListener((v) -> {
                int daysToBirthday = dog.getDaysToBirthday();
                if(daysToBirthday == -1) {
                    Log.e("DogAdapter", "Failed to calculate days to birthday in dog class");
                } else if(daysToBirthday == 0) {
                    showPopupWindow(dogImageView, "🎉 It's my birthday today 🎉");
                    Toast.makeText(itemView.getContext(), "🎉 " + dog.getDogData().get("name") + " has a birthday today! 🎉", Toast.LENGTH_SHORT).show();
                } else {
                    showPopupWindow(dogImageView, "My birthday is in " + dog.getDaysToBirthday() + " days! 🎉");
                }
                return true;
            });
        }


        void showPopupWindow(View anchorView, String text) {
            LayoutInflater inflater = LayoutInflater.from(anchorView.getContext());
            View popupView = inflater.inflate(R.layout.layout_tooltip, null);

            TextView textView = popupView.findViewById(R.id.tooltip_text);
            textView.setText(text);

            final PopupWindow popupWindow = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true); // true for focusable

            popupWindow.showAsDropDown(anchorView, 0, -anchorView.getHeight()); // Show above the anchor view
            anchorView.postDelayed(popupWindow::dismiss, 2000); // Dismiss after 2 seconds
        }

    }

}