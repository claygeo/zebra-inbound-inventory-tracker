# Inbound Inventory Tracker

An Android app built with Kotlin and Jetpack Compose for Zebra scanners, designed to track inbound inventory for dispensaries or warehouses. Features include user login, batch management, barcode scanning, and audit logging.

## Features
- Login screen for user authentication
- Main menu for navigation
- Prepped Batches screen to create and view batches
- Inbound Shipment screen for scanning and recording SKUs
- Audit logs for tracking user actions

## Prerequisites
- Android Studio
- Zebra scanner device (or emulator for testing)
- Kotlin and Jetpack Compose dependencies (see `build.gradle`)

## Setup
1. Clone the repository: `git clone [your-repo-url]`
2. Open in Android Studio
3. Sync project with Gradle
4. Run on a Zebra scanner or emulator

## Visuals

Main Logins: 
![image](https://github.com/user-attachments/assets/aa117639-ea79-4b5f-a696-8ae66bce3347)

Main Interface:
![image](https://github.com/user-attachments/assets/30dbec14-55a0-4d7b-9c00-04c35d34b681)

Prepped Batches: 
![image](https://github.com/user-attachments/assets/ae0a7aea-2cd5-4f97-97af-0f8fe32cb1e9)

Add New Batch:
![image](https://github.com/user-attachments/assets/9dc23bb5-7627-4385-9d7f-e1de0ba7e4b6)

Inbound Shipment:
![image](https://github.com/user-attachments/assets/15e5265e-a2e0-4872-a3a3-fb8b12217a41)

Inbound Shipment User Interface:
![image](https://github.com/user-attachments/assets/f92fc294-a82e-4048-b1ed-0dce4b9a915b)

![image](https://github.com/user-attachments/assets/16c979c1-0a92-4a96-b7cd-cef8ec2d4bea)



## Notes
- Zebra scanner SDK integration is required for barcode scanning (see `InventoryRepository.kt`).

