# DogManager

## Overview

**DogManager** is a comprehensive mobile application designed to help dog owners effectively manage their pets' health records, vaccination schedules, and other essential information. This app simplifies pet management by providing a centralized platform for storing and accessing your dog's data.

## Features

### User Authentication
- Users can sign up or sign in using their email or phone number, with all data securely handled through Firebase Authentication.

### Real-Time Data Management
- The app uses Firebase Real-Time Database to ensure that all data entered is instantly synced across all user devices.

### Image Handling
- Users can upload pictures of their dogs directly from their gallery. These images are stored in Firebase Storage, allowing easy access and management.

### Comprehensive Data Entry
- The app includes detailed forms for entering all necessary data about the pets. Text watchers are employed to validate the data entered and ensure all required fields are filled before submission.

### Robust Data Validation
- Dynamic form handling with text watchers ensures that buttons are enabled only when all fields are filled correctly, enhancing usability and data integrity.

## App Pages

### Sign In/Up Page
Users can create a new account or sign in to an existing account using their email or phone number.
![Sign In/Up Page](path/to/signin_screenshot.png)

### Homepage
The homepage lists all the dogs registered by the user. Each entry provides quick access to the dog's detailed profile.
![Homepage](path/to/homepage_screenshot.png)

### Add New Dog
This page allows users to add a new dog to their account, entering details such as the dog's name, breed, date of birth, and a profile photo.
![Add New Dog](path/to/addnewdog_screenshot.png)

### Specific Dog Information
Users can view detailed information about each dog, including veterinary visits, vaccination records, and other health-related data.
![Specific Dog Information](path/to/specificdoginfo_screenshot.png)

### Update/Delete Dog
From this page, users can update or delete information about their dogs. This includes managing appointments, updating vaccination dates, or removing a dog's profile.
![Update/Delete Dog](path/to/updatedeletedog_screenshot.png)

## Installation

1. Clone the repository:
   ```git clone https://github.com/yourusername/DogManager.git```
2. Open the project in Android Studio.
3. Build the project by navigating to Build > Make Project.
4. Run the app on a connected device or emulator by navigating to Run > Run 'app'.

