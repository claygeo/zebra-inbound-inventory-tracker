# Inbound Inventory Tracker

An Android app built with Kotlin and Jetpack Compose for Zebra scanners, designed to track inbound inventory for dispensaries or warehouses. Features include user login, batch management, barcode scanning, and audit logging.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Database Setup](#database-setup)
- [Visuals](#visuals)
- [Notes](#notes)
  
## Features
- Login screen for user authentication
- Main menu for navigation
- Prepped Batches screen to create and view batches
- Inbound Shipment screen for scanning and recording SKUs
- Audit logs for tracking user actions

## Prerequisites
- Android Studio
- Zebra scanner device (or emulator for testing)
- Kotlin and Jetpack Compose dependencies (see build.gradle)

## Setup
1. Clone the repository: git clone [your-repo-url]
2. Open in Android Studio
3. Sync project with Gradle
4. Run on a Zebra scanner or emulator

## Database Setup
- To configure the Supabase database for the Kotlin Android app, you need to create the necessary tables. Copy and paste the following SQL code into the Supabase SQL Editor (found in your Supabase dashboard under SQL Editor). This will set up the audit_logs, batches, and inbound_records tables required for the application:
  
-- Create audit_logs table
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY,
    action TEXT NOT NULL,
    created_by TEXT NOT NULL,
    details TEXT,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CHECK (id IS NOT NULL),
    CHECK (action IS NOT NULL),
    CHECK (created_by IS NOT NULL)
);

-- Create batches table
CREATE TABLE batches (
    id BIGINT PRIMARY KEY,
    created_by TEXT NOT NULL,
    date TEXT NOT NULL,
    skus TEXT[] NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CHECK (id IS NOT NULL),
    CHECK (created_by IS NOT NULL),
    CHECK (date IS NOT NULL),
    CHECK (skus IS NOT NULL)
);

-- Create inbound_records table
CREATE TABLE inbound_records (
    id BIGINT PRIMARY KEY,
    barcode TEXT NOT NULL,
    box_count INTEGER NOT NULL,
    box_size TEXT NOT NULL,
    CHECK (id IS NOT NULL),
    CHECK (barcode IS NOT NULL),
    CHECK (box_count IS NOT NULL),
    CHECK (box_size IS NOT NULL)
);

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

User Interface:
![image](https://github.com/user-attachments/assets/f92fc294-a82e-4048-b1ed-0dce4b9a915b)

![image](https://github.com/user-attachments/assets/16c979c1-0a92-4a96-b7cd-cef8ec2d4bea)

![image](https://github.com/user-attachments/assets/fa0ff1ff-1aba-4391-b77b-471e0834290d)

## Notes
- Zebra scanner SDK integration is required for barcode scanning (see InventoryRepository.kt).

