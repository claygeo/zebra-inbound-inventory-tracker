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

## Notes
- Zebra scanner SDK integration is required for barcode scanning (see `InventoryRepository.kt`).
- Dispensary-related terms (e.g., PREROLL, ROSIN) are generic.

## License
Licensed under the MIT License. See [LICENSE](LICENSE) for details.
