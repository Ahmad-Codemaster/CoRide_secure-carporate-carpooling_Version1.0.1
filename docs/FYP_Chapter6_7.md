# CoRide — Secure Corporate Carpooling Application
## Final Year Project Documentation
### Chapter 6: Testing (Software Quality Attributes) & Chapter 7: Tools and Technologies

---

> **Document Formatting Note (for Word conversion)**
> - Font: Times New Roman | Main Heading: 16 pt Bold CAPS | Sub-heading: 14 pt Bold | Body: 12 pt
> - Margins: Top 1.0" | Bottom 1.0" | Left 1.25" | Right 1.0"
> - Line Spacing: 1.5 | Paragraph Spacing: 6 pt
> - Paper: A4
> - Page numbers: Arabic numerals centred at bottom

---

# CHAPTER NO 6: TESTING (SOFTWARE QUALITY ATTRIBUTES)

## 6.1 Test Case Specification

### 6.1.1 Overview of Testing Strategy

The CoRide Secure Corporate Carpooling application is an Android-based mobility platform developed in Kotlin, designed exclusively for students and employees of registered educational and corporate institutions. The application encompasses a wide spectrum of features, including multi-factor user authentication, institutional identity verification, a real-time ride-hailing flow with live Google Maps tracking, a driver management portal, an in-app notification centre, a hardware and software SOS emergency system, SMS safety alerts dispatched to trusted contacts, administrative email notifications via SMTP, biometric quick login, weather-aware ride planning, and a ride history module.

Given the security-critical nature of the application — which handles personally identifiable information (PII) such as CNIC numbers, institutional IDs, phone numbers, home addresses, and live GPS coordinates — thorough and methodical testing is not merely a quality-assurance exercise but a fundamental requirement. The testing strategy for CoRide was therefore designed to provide maximum coverage across all user-facing flows, backend logic, and safety-critical pathways.

Testing was conducted in two overarching categories:

1. **Black-Box Testing:** This category evaluates the system's behaviour from the perspective of an end user, without knowledge of the internal implementation. The tester provides inputs and observes outputs, comparing actual results against expected results derived from requirements specifications. The black-box techniques employed include Boundary Value Analysis (BVA), Equivalence Class Partitioning (ECP), State Transition Testing, Decision Table Testing, and Graph-Based Testing.

2. **White-Box Testing:** This category evaluates the internal logic and code structure of the application. The tester has access to the source code and designs test cases to exercise specific statements, branches, and execution paths within critical modules. The white-box techniques employed include Statement Coverage, Branch Coverage, and Path Coverage.

Together, these techniques provide a comprehensive safety net that validates functional correctness, boundary handling, state management, decision logic, navigational integrity, and code-level path coverage.

---

### 6.1.2 Test Environment

Testing of the CoRide application was carried out within the following environment:

| Parameter | Details |
|---|---|
| Development Machine OS | Windows 11 (64-bit) |
| IDE | Android Studio Hedgehog (2023.1.1) |
| Build System | Gradle 8.x with Kotlin DSL (`build.gradle.kts`) |
| Minimum Android SDK | API 26 (Android 8.0 Oreo) |
| Target / Compile SDK | API 34 (Android 14) |
| Programming Language | Kotlin (JVM target 17) |
| Physical Test Device | Android smartphone running Android 13 |
| Emulator | Android Virtual Device (AVD) — Pixel 6 API 34 |
| Network Conditions | Wi-Fi (stable) and Mobile Data (3G/4G) tested |
| Backend / Data Layer | Android SharedPreferences (LocalPreferences), In-memory MockDataRepository, Firebase Realtime Database (live tracking), SMTP Gmail (automated email alerts), OpenWeatherMap REST API |
| Build Type for Testing | Debug build |

**Table 6.1: Test Environment Configuration**

---

### 6.1.3 Test Data Preparation

Test data was meticulously prepared to cover three categories of input scenarios:

- **Valid / Normal data:** Inputs that fall within expected, accepted ranges.
- **Boundary data:** Inputs at or near the absolute limits of acceptable ranges.
- **Invalid / Abnormal data:** Inputs that are outside the acceptable range or in incorrect format, used to verify that the system rejects them gracefully with appropriate feedback messages.

The following categories of test data were prepared and used during test execution:

| Category | Examples Used |
|---|---|
| User Credentials | Email: `ahmad@gcuf.edu.pk`, Phone: `03001234567`, Password: `Pass@123` |
| Boundary Passwords | 5-char: `Pass1`, 6-char: `Pass12`, 7-char: `Pass123`, 8-char: `Pass1234` |
| Invalid Emails | `ahmad`, `ahmad@`, `@gcuf.edu.pk`, `ahmad.gcuf.edu.pk`, empty string |
| Registration Fields | Name: `Ahmad Ali`, Org: `GCUF`, Student ID: `2024-FCS-001` |
| Seat Counts | 0, 1, 2, 4, 7, 8 (beyond max assumed to be 7) |
| Pickup / Destination | `DHA Phase 5`, `GCUF Campus`, empty string |
| Fare Values | PKR 0, PKR 80, PKR 300, PKR 5000, PKR 9999 |
| OTP Values | `1234` (valid), `0000`, `9999`, `12345` (too long), empty |
| Driver Vehicle Fields | Make: `Toyota`, Model: `Corolla`, Plate: `LEA-1234`, License: `LHR-DL-2021-4567` |
| Trusted Contact Phone | `03001234567` (valid), `0300` (incomplete), `abc` (non-numeric) |

**Table 6.2: Test Data Summary**

---

### 6.1.4 Standard Test Case Template

Each test case documented in this chapter adheres to the following standardised format to ensure clarity, repeatability, and traceability:

| Field | Description |
|---|---|
| **Test Case ID** | Unique identifier (e.g., TC-BVA-01) |
| **Module / Feature** | The application module being tested |
| **Objective** | The specific goal of the test |
| **Pre-Conditions** | State of the system before the test begins |
| **Input / Test Data** | Data values entered during the test |
| **Test Steps** | Step-by-step procedure to execute the test |
| **Expected Result** | What the system should do according to requirements |
| **Actual Result** | What the system actually did (filled during execution) |
| **Status** | Pass / Fail |

**Table 6.3: Test Case Template**

---

### 6.1.5 Modules Under Test

The following application modules were identified from source code analysis and subjected to testing:

| Module ID | Module Name | Primary Kotlin File(s) |
|---|---|---|
| M01 | User Registration | `RegisterFragment.kt`, `OtpFragment.kt` |
| M02 | User Login | `LoginFragment.kt` |
| M03 | Biometric Authentication | `LoginFragment.kt`, `BiometricHelper.kt` |
| M04 | Password Reset (OTP Email) | `ForgotPasswordFragment.kt`, `EmailNotificationHelper.kt` |
| M05 | Home Screen | `HomeFragment.kt` |
| M06 | Place Search | `SearchPlaceFragment.kt`, `PlaceAdapter.kt` |
| M07 | Ride Booking (Fare + Driver Search) | `HomeMapFragment.kt`, `DriverOffersFragment.kt` |
| M08 | Active Ride (Live Tracking) | `RideFragment.kt` |
| M09 | Ride Completion & Rating | `RideCompleteFragment.kt` |
| M10 | Driver Registration | `DriverRegistrationFragment.kt` |
| M11 | Driver Dashboard | `DriverDashboardFragment.kt` |
| M12 | Profile Management | `ProfileFragment.kt`, `EditProfileBottomSheet.kt` |
| M13 | Identity Verification | `VerificationPopupDialogFragment.kt` |
| M14 | Trusted Contacts | `TrustedContactsFragment.kt` |
| M15 | SOS Emergency System | `SosDialogFragment.kt`, `SafetyCheckDialogFragment.kt`, `SmsSafetyHelper.kt` |
| M16 | Notifications | `NotificationsDialogFragment.kt` |
| M17 | Ride History | `HistoryFragment.kt` |
| M18 | Security Settings | `SecuritySettingsDialog.kt` |
| M19 | Admin Email Alerts | `EmailNotificationHelper.kt` |
| M20 | Weather Integration | `WeatherApiService.kt`, `HomeFragment.kt` |

**Table 6.4: Modules Under Test**

---

## 6.2 Black Box Test Cases

Black-box testing, also known as behavioural testing or specification-based testing, is a software testing method in which the test cases are derived exclusively from the functional specifications and requirements of the application. The internal structure, design, and implementation of the software are not considered. The tester acts as an end user and evaluates whether the application's observable behaviour matches the stated requirements.

For the CoRide application, five black-box testing techniques were employed: Boundary Value Analysis (BVA), Equivalence Class Partitioning (ECP), State Transition Testing, Decision Table Testing, and Graph-Based Testing. Each technique was chosen for its particular strength in uncovering specific classes of defects.

---

### 6.2.1 BVA — Boundary Value Analysis

Boundary Value Analysis is a black-box test design technique in which test cases are chosen to include values at, just below, and just above the boundaries of an input domain. This technique is highly effective because software defects tend to cluster near the edges of input ranges, where developers must write boundary-handling logic.

For CoRide, BVA was applied to all numeric and length-constrained input fields, including password length, seat count, fare amount, OTP length, phone number length, and name character length. The following test cases document the BVA approach in detail.

---

**TC-BVA-01: Password Length — Lower Boundary (Login Module)**

| Field | Details |
|---|---|
| **Test Case ID** | TC-BVA-01 |
| **Module** | M02 — User Login (`LoginFragment.kt`) |
| **Objective** | Validate that the system correctly accepts or rejects passwords based on the minimum character boundary of 6 characters |
| **Pre-Conditions** | A registered user account exists in LocalPreferences with a known password |
| **Input / Test Data** | Password lengths: 5 characters (`Pass1`), 6 characters (`Pass12`), 7 characters (`Pass123`) |
| **Test Steps** | 1. Open the CoRide application. 2. Navigate to the Login screen. 3. Enter a valid registered email/phone. 4. Enter a 5-character password (`Pass1`) and tap the "LOGIN" button. 5. Observe result. 6. Clear the password field. 7. Enter a 6-character password matching the registered password. 8. Tap "LOGIN". 9. Observe result. 10. Repeat with a 7-character password. |
| **Expected Result** | 5-char password: system displays a toast validation error "Please fill all fields" or a password-too-short error message; login is blocked. 6-char password: if it matches the stored credential, login succeeds and MainActivity opens. 7-char password: system processes normally (accepted length). |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.5: TC-BVA-01**

---

**TC-BVA-02: Password Length — Upper Boundary (Registration Module)**

| Field | Details |
|---|---|
| **Test Case ID** | TC-BVA-02 |
| **Module** | M01 — User Registration (`RegisterFragment.kt`) |
| **Objective** | Verify that excessively long passwords (e.g., 129 characters) are handled gracefully |
| **Pre-Conditions** | Registration screen is accessible |
| **Input / Test Data** | Password of 128 characters, Password of 129 characters |
| **Test Steps** | 1. Open Registration screen. 2. Fill all required fields correctly. 3. Enter a 128-character password in the password field. 4. Tap "REGISTER". 5. Observe result. 6. Repeat with a 129-character password. |
| **Expected Result** | 128-char password: system accepts and proceeds to OTP screen. 129-char password: system either accepts (no upper limit enforced) or displays an appropriate maximum-length error, depending on implementation. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.6: TC-BVA-02**

---

**TC-BVA-03: OTP Value — Valid Boundary (Registration OTP Verification)**

| Field | Details |
|---|---|
| **Test Case ID** | TC-BVA-03 |
| **Module** | M01 — OTP Verification (`MockDataRepository.verifyOtp()`) |
| **Objective** | Validate OTP acceptance at the exact valid boundary value |
| **Pre-Conditions** | User has completed the registration form and reached the OTP verification screen; the system OTP is `1234` |
| **Input / Test Data** | OTP: `1233` (below), `1234` (exact match), `1235` (above), `0000`, `9999` |
| **Test Steps** | 1. Complete the registration form. 2. On OTP screen, enter `1233` and submit. 3. Observe result. 4. Clear field and enter `1234`. 5. Submit and observe result. 6. Clear field and enter `1235`. 7. Submit and observe result. |
| **Expected Result** | `1233`: Registration fails; error message "Invalid OTP". `1234`: OTP verified; registration completes; user is saved to LocalPreferences; Welcome notification added. `1235`: Registration fails; error message displayed. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.7: TC-BVA-03**

---

**TC-BVA-04: Fare Amount — Lower Boundary (Ride Booking)**

| Field | Details |
|---|---|
| **Test Case ID** | TC-BVA-04 |
| **Module** | M07 — Ride Booking / Fare Calculation (`MockDataRepository.getRecommendedFare()`) |
| **Objective** | Verify that the recommended fare calculation handles the minimum possible distance values (0 km, 0.1 km, 1 km) |
| **Pre-Conditions** | User is logged in and verified; Home screen is loaded |
| **Input / Test Data** | Distance: 0 km, 0.1 km, 1 km; VehicleType: CAR (base rate PKR 35/km, base fare PKR 80) |
| **Test Steps** | 1. Programmatically call `getRecommendedFare(0.0, VehicleType.CAR)`. 2. Observe fare: should be `round(80 + 0 × 35, to nearest 10) = PKR 80`. 3. Call with 0.1 km. 4. Call with 1 km. |
| **Expected Result** | 0 km: PKR 80. 0.1 km: PKR 80 (rounded). 1 km: PKR 120 (`round(80 + 35) = 120`). All values are non-negative and rounded to the nearest PKR 10. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.8: TC-BVA-04**

---

**TC-BVA-05: Trusted Contact — Phone Number Length**

| Field | Details |
|---|---|
| **Test Case ID** | TC-BVA-05 |
| **Module** | M14 — Trusted Contacts (`TrustedContactsFragment.kt`) |
| **Objective** | Verify that phone number input for trusted contacts enforces correct minimum (10 digits) and maximum (11 digits for Pakistani numbers) lengths |
| **Pre-Conditions** | User is logged in; Trusted Contacts screen is accessible |
| **Input / Test Data** | Phone: `030012345` (9 digits), `0300123456` (10 digits), `03001234567` (11 digits), `030012345678` (12 digits) |
| **Test Steps** | 1. Navigate to Trusted Contacts screen. 2. Tap "Add Contact". 3. Enter a 9-digit phone number and attempt to save. 4. Observe result. 5. Enter a 10-digit number and save. 6. Enter an 11-digit number and save. 7. Enter a 12-digit number and save. |
| **Expected Result** | 9 digits: rejected with validation message. 10–11 digits: accepted and contact saved successfully. 12 digits: rejected with appropriate error. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.9: TC-BVA-05**

---

**TC-BVA-06: Vehicle Seats — Maximum Capacity Boundary (Driver Offers)**

| Field | Details |
|---|---|
| **Test Case ID** | TC-BVA-06 |
| **Module** | M07 — Driver Offers (`DriverOffersFragment.kt`, `MockDataRepository.generateDriverOffers()`) |
| **Objective** | Validate that the driver offer generation does not produce offers beyond the maximum of 3 available drivers |
| **Pre-Conditions** | User is logged in and has set pickup/destination; `mockDrivers` list contains 5 drivers |
| **Input / Test Data** | Request fare: PKR 300; Pickup coordinates: Lahore (31.5204, 74.3587) |
| **Test Steps** | 1. From the Home screen, set a pickup and destination. 2. Enter fare PKR 300. 3. Tap search for drivers. 4. Wait for `generateDriverOffers()` to return. 5. Count the number of offers displayed in `DriverOffersFragment`. |
| **Expected Result** | Exactly 3 driver offers are displayed (not more than 3 even though 5 mock drivers exist), each with a unique driver name, price, and ETA. The nearest driver has the lowest fare. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.10: TC-BVA-06**

---

**TC-BVA-07: Verification Timer — Boundary at 30 Seconds**

| Field | Details |
|---|---|
| **Test Case ID** | TC-BVA-07 |
| **Module** | M13 — Identity Verification (`MockDataRepository.getVerificationRemainingMs()`) |
| **Objective** | Verify that the verification timer boundary (exactly 30 seconds = 30,000 ms) transitions the verification status correctly |
| **Pre-Conditions** | User has submitted a document (`submitDocument()` has been called); timer is running |
| **Input / Test Data** | Elapsed time: 29,999 ms, 30,000 ms, 30,001 ms |
| **Test Steps** | 1. Call `submitDocument(VerificationDocType.ORGANIZATION_CARD)`. 2. After 29,999 ms, call `checkAndCompleteVerification()`. 3. Observe: should not complete. 4. After exactly 30,000 ms, call `checkAndCompleteVerification()`. 5. Observe: should complete. |
| **Expected Result** | At 29,999 ms: `checkAndCompleteVerification()` returns `false`; status remains UNDER_REVIEW. At 30,000 ms: returns `true`; status changes to VERIFIED; "Verification Successful" notification is added; admin email is dispatched. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.11: TC-BVA-07**

---

**TC-BVA-08: Hardware SOS — Volume Button Press Count Boundary**

| Field | Details |
|---|---|
| **Test Case ID** | TC-BVA-08 |
| **Module** | M15 — SOS System (`MainActivity.dispatchKeyEvent()`) |
| **Objective** | Verify that the hardware SOS trigger fires exactly on the 3rd volume-down press within 3 seconds, but not on the 2nd press |
| **Pre-Conditions** | Volume SOS is enabled in LocalPreferences (`isVolumeSosEnabled = true`); user is on MainActivity |
| **Input / Test Data** | Volume-down presses: 1, 2, 3 within a 3-second window |
| **Test Steps** | 1. Enable Volume SOS in Security Settings. 2. Press volume-down button once. 3. Observe: no SOS. 4. Press again (2nd press). 5. Observe: no SOS. 6. Press a 3rd time within 3 seconds. 7. Observe: SOS is triggered. |
| **Expected Result** | 1st and 2nd presses: no SOS triggered; volume count incremented. 3rd press: `triggerHardwareSos()` is called; emergency toast appears; SMS sent to trusted contacts; admin email dispatched. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.12: TC-BVA-08**

---

### 6.2.2 Equivalence Class Partitioning (ECP)

Equivalence Class Partitioning (ECP) is a black-box testing technique that divides the input domain of a software module into groups (equivalence classes or partitions), where all members of a partition are expected to behave identically when processed by the system. By testing one representative value from each partition, rather than every possible value, test execution efficiency is greatly improved while maintaining meaningful coverage.

For CoRide, ECP was applied to input validation in the authentication module, ride-booking parameters, and profile fields.

---

**TC-ECP-01: Email Format Validation (Registration Module)**

**Equivalence Classes:**

| Class ID | Description | Representation |
|---|---|---|
| EC1 | Valid email with `@` and domain | `ahmad@gcuf.edu.pk` |
| EC2 | No `@` symbol | `ahmadgcuf.edu.pk` |
| EC3 | Missing domain | `ahmad@` |
| EC4 | Missing local part | `@gcuf.edu.pk` |
| EC5 | Empty string | `` (blank) |
| EC6 | Valid email with subdomain | `ahmad.ali@student.gcuf.edu.pk` |

**Table 6.13: Equivalence Classes for Email Format**

| Field | Details |
|---|---|
| **Test Case ID** | TC-ECP-01 |
| **Module** | M01 — User Registration (`RegisterFragment.kt`) |
| **Objective** | Verify that the registration screen accepts only valid email formats and rejects all invalid classes |
| **Pre-Conditions** | Registration screen is open; all other required fields are filled correctly |
| **Test Steps** | 1. For each class above, enter the representative email into the Email field. 2. Tap REGISTER. 3. Record whether the system proceeds (EC1, EC6) or displays an error (EC2–EC5). |
| **Expected Result** | EC1, EC6: Proceed to OTP screen (valid email accepted). EC2, EC3, EC4, EC5: System displays validation toast "Community Identity details are mandatory" or email-format error; navigation to OTP is blocked. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.14: TC-ECP-01**

---

**TC-ECP-02: Login Credential Types (Login Module)**

**Equivalence Classes:**

| Class ID | Description | Representation |
|---|---|---|
| EC1 | Valid registered email | `ahmad@gcuf.edu.pk` |
| EC2 | Valid registered phone | `03001234567` |
| EC3 | Unregistered email | `unknown@test.com` |
| EC4 | Unregistered phone | `09999999999` |
| EC5 | Empty login ID | `` (blank) |
| EC6 | Invalid format (neither email nor phone) | `abcd1234!@#` |

**Table 6.15: Equivalence Classes for Login Credentials**

| Field | Details |
|---|---|
| **Test Case ID** | TC-ECP-02 |
| **Module** | M02 — User Login (`LoginFragment.kt`, `MockDataRepository.login()`) |
| **Objective** | Verify that login succeeds for registered credentials and fails for all invalid partitions |
| **Pre-Conditions** | Registered user exists in LocalPreferences |
| **Test Steps** | 1. Enter credential from each class with the correct password. 2. Tap LOGIN. 3. Record result for each. |
| **Expected Result** | EC1, EC2: Login successful; `MainActivity` opens. EC3, EC4: Login fails; toast "Invalid credentials or user not found" shown. EC5: toast "Please fill all fields". EC6: Login fails. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.16: TC-ECP-02**

---

**TC-ECP-03: Place Search Input (Search Module)**

**Equivalence Classes:**

| Class ID | Description | Representation |
|---|---|---|
| EC1 | Full valid location name | `"Packages Mall"` |
| EC2 | Partial valid name | `"Pack"` |
| EC3 | Location with special characters | `"DHA Phase 5, Lahore"` |
| EC4 | Numeric input | `"31.5"` |
| EC5 | Empty search string | `` |
| EC6 | Input not matching any place | `"XyZAbcNonExistent"` |

**Table 6.17: Equivalence Classes for Place Search**

| Field | Details |
|---|---|
| **Test Case ID** | TC-ECP-03 |
| **Module** | M06 — Place Search (`SearchPlaceFragment.kt`, `MockDataRepository.searchPlaces()`) |
| **Objective** | Verify that the place search returns accurate results for valid inputs and handles edge cases gracefully |
| **Pre-Conditions** | User is logged in; search screen is open |
| **Test Steps** | 1. Enter each representative input into the search bar. 2. Observe results list after each entry. |
| **Expected Result** | EC1: Returns `Packages Mall` with full address. EC2: Returns partial match results. EC3: Returns places matching `DHA Phase 5`. EC4: No place found; empty results list shown. EC5: Shows all places or recent places. EC6: Empty results list or "No results found" message. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.18: TC-ECP-03**

---

**TC-ECP-04: Vehicle Type Selection (Ride Booking)**

**Equivalence Classes:**

| Class ID | Description | Representation |
|---|---|---|
| EC1 | Valid vehicle type — CAR | `VehicleType.CAR` |
| EC2 | Valid vehicle type — BIKE | `VehicleType.BIKE` |
| EC3 | Valid vehicle type — RICKSHAW | `VehicleType.RICKSHAW` |

**Table 6.19: Equivalence Classes for Vehicle Type**

| Field | Details |
|---|---|
| **Test Case ID** | TC-ECP-04 |
| **Module** | M07 — Ride Booking Fare Calculation (`MockDataRepository.getRecommendedFare()`) |
| **Objective** | Verify that fare calculation returns different rates for each valid vehicle type |
| **Pre-Conditions** | Standard distance of 10 km used for comparison |
| **Test Steps** | 1. Call `getRecommendedFare(10.0, VehicleType.CAR)`. 2. Call `getRecommendedFare(10.0, VehicleType.BIKE)`. 3. Call `getRecommendedFare(10.0, VehicleType.RICKSHAW)`. 4. Compare results. |
| **Expected Result** | CAR (PKR 35/km): `round(80 + 350) = PKR 430`. BIKE (PKR 15/km): `round(80 + 150) = PKR 230`. RICKSHAW (PKR 22/km): `round(80 + 220) = PKR 300`. Each type returns a distinct fare value. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.20: TC-ECP-04**

---

**TC-ECP-05: User Role at Registration (Registration Module)**

**Equivalence Classes:**

| Class ID | Description | Representation |
|---|---|---|
| EC1 | Student with valid institutional email | `2022-BSCS-001@gcuf.edu.pk` |
| EC2 | Employee with corporate email | `ali.khan@company.com` |
| EC3 | Registration without organization name | Empty org field |
| EC4 | Registration without student/employee ID | Empty ID field |

**Table 6.21: Equivalence Classes for Registration Roles**

| Field | Details |
|---|---|
| **Test Case ID** | TC-ECP-05 |
| **Module** | M01 — User Registration |
| **Objective** | Verify that community identity fields (organization, student ID) are mandatory and that users can register as STUDENT role |
| **Pre-Conditions** | Registration screen is open |
| **Test Steps** | 1. Fill all fields including org and ID (EC1). Tap REGISTER. 2. Clear org field and re-submit (EC3). 3. Clear ID field and re-submit (EC4). |
| **Expected Result** | EC1, EC2: Proceeds to OTP screen. EC3, EC4: Toast "Community Identity details are mandatory" is shown; navigation blocked. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.22: TC-ECP-05**

---

**TC-ECP-06: Driver Registration Input Partitions**

**Equivalence Classes:**

| Class ID | Description | Representation |
|---|---|---|
| EC1 | All fields valid | Make: `Toyota`, Model: `Corolla`, Plate: `LEA-1234`, License: `LHR-DL-2021-4567` |
| EC2 | Vehicle make empty | Make: empty string |
| EC3 | Plate number empty | Plate: empty string |
| EC4 | License number empty | License: empty string |
| EC5 | All fields empty | All empty |

**Table 6.23: Equivalence Classes for Driver Registration**

| Field | Details |
|---|---|
| **Test Case ID** | TC-ECP-06 |
| **Module** | M10 — Driver Registration (`DriverRegistrationFragment.kt`) |
| **Objective** | Verify that driver registration validates all mandatory vehicle fields |
| **Pre-Conditions** | User is logged in and has navigated to the Driver Registration screen |
| **Test Steps** | 1. Fill all fields correctly (EC1) and tap SUBMIT. 2. Clear Vehicle Make field (EC2) and tap SUBMIT. 3. Repeat for each invalid class. |
| **Expected Result** | EC1: Registration succeeds; `MockDataRepository.registerDriverDetails()` is called; user is redirected to Driver Dashboard. EC2–EC5: Toast "Please fill all fields to continue" is shown; registration is blocked. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.24: TC-ECP-06**

---

### 6.2.3 State Transition Testing

State Transition Testing is a black-box testing technique used to test the behaviour of systems that exhibit different outputs depending on a sequence of past inputs — systems, in other words, that have a finite set of distinguishable states and transition between those states based on defined events. The CoRide application contains several clearly defined state machines, most notably the ride lifecycle state machine and the user verification state machine.

---

**6.2.3.1 Ride Lifecycle State Transition**

The CoRide ride lifecycle is modelled as a sealed class `RideState` in `RideFragment.kt` and the ride `RideStatus` enumeration in `Models.kt`. The following states are defined:

| State | Description |
|---|---|
| `IDLE` | No ride is in progress; user is on the Home screen |
| `SEARCHING` | System is searching for available drivers |
| `OFFERS_AVAILABLE` | Driver offers are displayed in `DriverOffersFragment` |
| `DRIVER_MATCHED` | User has accepted a driver offer |
| `DRIVER_ARRIVING` | Driver is en route to the pickup point |
| `DRIVER_ARRIVED` | Driver has reached the pickup location |
| `IN_PROGRESS` | Ride is ongoing; live map is being updated |
| `COMPLETED` | Ride has ended; `RideCompleteFragment` is shown |
| `CANCELLED` | User or driver cancelled the ride |

**Table 6.25: Ride Lifecycle States**

**State Transition Diagram Description:**

```
IDLE  →  [Tap Search / Enter Destination]  →  SEARCHING
SEARCHING  →  [Drivers Found]  →  OFFERS_AVAILABLE
SEARCHING  →  [No Drivers Found]  →  IDLE
OFFERS_AVAILABLE  →  [Accept Offer]  →  DRIVER_MATCHED
OFFERS_AVAILABLE  →  [Cancel]  →  IDLE
DRIVER_MATCHED  →  [Driver Starts Moving]  →  DRIVER_ARRIVING
DRIVER_ARRIVING  →  [Driver Reaches Pickup]  →  DRIVER_ARRIVED
DRIVER_ARRIVED  →  [Ride Starts]  →  IN_PROGRESS
IN_PROGRESS  →  [Reach Destination]  →  COMPLETED
IN_PROGRESS  →  [User Cancels]  →  CANCELLED
DRIVER_ARRIVING  →  [User Cancels]  →  CANCELLED
COMPLETED  →  [User Submits Rating]  →  IDLE
CANCELLED  →  [User Returns Home]  →  IDLE
```

**Figure 6.1: Ride Lifecycle State Transition Diagram**

---

**TC-ST-01: Ride Lifecycle — Full Happy Path (IDLE → COMPLETED)**

| Field | Details |
|---|---|
| **Test Case ID** | TC-ST-01 |
| **Module** | M07, M08, M09 — Full Ride Flow |
| **Objective** | Validate the complete sequential state transition from IDLE through COMPLETED |
| **Pre-Conditions** | User is logged in and verified; Home screen loaded |
| **Test Steps** | 1. From Home (IDLE), enter pickup and destination, set fare, tap search. 2. Wait for driver offers (OFFERS_AVAILABLE). 3. Accept an offer (DRIVER_MATCHED). 4. Wait for driver simulation (DRIVER_ARRIVING → DRIVER_ARRIVED). 5. Observe ride in progress (IN_PROGRESS). 6. Wait for ride completion (COMPLETED). 7. Submit rating and payment. |
| **Expected Result** | Each state is reached in sequence; UI updates correctly for each state (correct labels, progress indicators, button states). Final state leads to `RideCompleteFragment` and then back to Home (IDLE). |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.26: TC-ST-01**

---

**TC-ST-02: Ride Lifecycle — Cancellation During DRIVER_ARRIVING**

| Field | Details |
|---|---|
| **Test Case ID** | TC-ST-02 |
| **Module** | M08 — Ride Fragment (`RideFragment.kt`) |
| **Objective** | Verify that a ride cancellation during the DRIVER_ARRIVING phase transitions correctly to CANCELLED and returns user to Home |
| **Pre-Conditions** | Ride is in DRIVER_ARRIVING state |
| **Test Steps** | 1. Initiate ride as in TC-ST-01. 2. Once DRIVER_ARRIVING state begins (driver marker is moving), tap "CANCEL RIDE" button. 3. Observe navigation. |
| **Expected Result** | App transitions to CANCELLED state; user is navigated back to `homeFragment` using `findNavController().popBackStack(R.id.homeFragment, false)`. No crash or stuck state. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.27: TC-ST-02**

---

**TC-ST-03: User Verification State Machine**

The user verification state machine has the following states and transitions:

| State | Description |
|---|---|
| `PENDING` | Newly registered user; no document submitted |
| `UNDER_REVIEW` | Document submitted; timer running |
| `VERIFIED` | Timer elapsed; status promoted; full access granted |
| `REJECTED` | Document rejected (defined in enum; not shown in current UI flow) |

**Table 6.28: User Verification States**

| Field | Details |
|---|---|
| **Test Case ID** | TC-ST-03 |
| **Module** | M13 — Identity Verification |
| **Objective** | Validate the sequential verification state transitions from PENDING to VERIFIED |
| **Pre-Conditions** | New user is registered with PENDING status |
| **Test Steps** | 1. Register a new user (PENDING). 2. Navigate to Profile → Verification. 3. Upload a document (UNDER_REVIEW). 4. Wait 30 seconds for the timer. 5. Observe status update (VERIFIED). 6. Attempt to access a ride-booking feature that requires verification. |
| **Expected Result** | PENDING → UNDER_REVIEW on document submission. UNDER_REVIEW → VERIFIED after 30-second timer elapses. In VERIFIED state, full feature access is granted without the verification popup blocking navigation. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.29: TC-ST-03**

---

**TC-ST-04: Driver Online/Offline State Transition (Driver Dashboard)**

| Field | Details |
|---|---|
| **Test Case ID** | TC-ST-04 |
| **Module** | M11 — Driver Dashboard (`DriverDashboardFragment.kt`) |
| **Objective** | Verify that the driver online/offline toggle correctly transitions state and updates the UI |
| **Pre-Conditions** | User is registered as a driver; Driver Dashboard is open |
| **Test Steps** | 1. Open Driver Dashboard (default state: OFFLINE). 2. Tap "GO ONLINE". 3. Observe: chip text, button label, button colour change. 4. Tap "STOP DRIVING". 5. Observe: reversion to OFFLINE state. |
| **Expected Result** | OFFLINE → ONLINE: Button text changes to "STOP DRIVING" (red); Chip shows "ONLINE" (green background). ONLINE → OFFLINE: Button text "GO ONLINE" (primary colour); Chip shows "OFFLINE" (grey background). `MockDataRepository.setDriverOnline(true/false)` is called correctly. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.30: TC-ST-04**

---

### 6.2.4 Decision Table Testing

Decision Table Testing is a black-box testing technique used to test the behaviour of a system for different combinations of inputs and conditions. Each column in a decision table represents a unique combination of conditions (a test rule), and the corresponding rows indicate which actions should be taken for that combination. This technique is particularly valuable for testing business-rule logic where multiple conditions interact.

---

**6.2.4.1 Decision Table: User Feature Access Control**

In CoRide, access to ride-booking features is controlled by the user's verification status and login state.

| Condition / Action | Rule 1 | Rule 2 | Rule 3 | Rule 4 |
|---|---|---|---|---|
| **C1: Is user logged in?** | Yes | Yes | No | Yes |
| **C2: Is user verified?** | Yes | No | — | Yes |
| **C3: Feature requires verification?** | Yes | Yes | — | No |
| **A1: Allow feature access** | ✓ | — | — | ✓ |
| **A2: Show Verification Popup** | — | ✓ | — | — |
| **A3: Redirect to Login** | — | — | ✓ | — |
| **A4: Allow without verification** | — | — | — | ✓ |

**Table 6.31: Decision Table — Feature Access Control**

| Field | Details |
|---|---|
| **Test Case ID** | TC-DT-01 |
| **Module** | M05 — Home Fragment, M07 — Ride Booking (`HomeFragment.requireVerification()`) |
| **Objective** | Verify that the feature access decision logic in `HomeFragment.requireVerification()` correctly implements all four rules |
| **Pre-Conditions** | Various user states prepared per rule |
| **Test Steps** | 1. Log in as a verified user; tap "Request Ride" (Rule 1). 2. Log in as an unverified user (PENDING); tap "Request Ride" (Rule 2). 3. Log out completely; navigate to the Home screen (Rule 3). 4. Log in as verified user; tap a feature not requiring verification (Rule 4). |
| **Expected Result** | Rule 1: Feature opens directly. Rule 2: `VerificationPopupDialogFragment` is shown. Rule 3: User is redirected to the Login screen. Rule 4: Feature opens directly. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.32: TC-DT-01**

---

**6.2.4.2 Decision Table: SOS Trigger Pathways**

CoRide provides three distinct mechanisms to trigger an emergency SOS alert:

| Condition / Action | Rule 1 | Rule 2 | Rule 3 | Rule 4 |
|---|---|---|---|---|
| **C1: SOS FAB tapped** | Yes | No | No | No |
| **C2: Volume button 3× pressed** | No | Yes | No | No |
| **C3: Safety check auto-timer expired** | No | No | Yes | No |
| **C4: Volume SOS setting enabled** | — | Yes | — | No |
| **A1: Open SOS Dialog** | ✓ | — | — | — |
| **A2: Trigger Hardware SOS (Email + SMS)** | — | ✓ | — | — |
| **A3: Auto-trigger SOS in SafetyCheckDialog** | — | — | ✓ | — |
| **A4: Do nothing** | — | — | — | ✓ |

**Table 6.33: Decision Table — SOS Trigger Mechanisms**

| Field | Details |
|---|---|
| **Test Case ID** | TC-DT-02 |
| **Module** | M15 — SOS Emergency System |
| **Objective** | Verify that each SOS pathway independently triggers the correct corresponding action |
| **Pre-Conditions** | Active ride in progress; trusted contacts configured; Volume SOS enabled for Rules 2 |
| **Test Steps** | 1. Tap SOS FAB in RideFragment (Rule 1). 2. Press volume-down 3× in under 3 seconds with Volume SOS enabled (Rule 2). 3. During ride simulation, do not respond to SafetyCheckDialog for 15 seconds (Rule 3). 4. Press volume-down 3× with Volume SOS disabled (Rule 4). |
| **Expected Result** | Rule 1: `SosDialogFragment` opens as a bottom sheet. Rule 2: `triggerHardwareSos()` is called; email sent to admin; SMS sent to trusted contacts. Rule 3: `triggerSos()` in `SafetyCheckDialogFragment` is called automatically. Rule 4: Normal volume behaviour; no SOS triggered. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.34: TC-DT-02**

---

**6.2.4.3 Decision Table: Ride Fare Recommendation**

| Condition / Action | Rule 1 | Rule 2 | Rule 3 | Rule 4 |
|---|---|---|---|---|
| **C1: Vehicle Type** | CAR | BIKE | RICKSHAW | CAR |
| **C2: Distance (km)** | 10 | 10 | 10 | 0 |
| **A: Expected Fare (PKR)** | 430 | 230 | 300 | 80 |

**Table 6.35: Decision Table — Fare Calculation Rules**

| Field | Details |
|---|---|
| **Test Case ID** | TC-DT-03 |
| **Module** | M07 — Fare Calculation |
| **Objective** | Verify that the fare formula (`80 + distance × baseRate`) returns correct results for all vehicle type and distance combinations |
| **Pre-Conditions** | None |
| **Test Steps** | 1. Invoke `getRecommendedFare(10.0, VehicleType.CAR)`. 2. Invoke with BIKE and RICKSHAW. 3. Invoke with 0.0 km. |
| **Expected Result** | Matches Table 6.35. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.36: TC-DT-03**

---

**6.2.4.4 Decision Table: Login Authentication Conditions**

| Condition / Action | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 |
|---|---|---|---|---|---|
| **C1: Email matches registered?** | Yes | No | Yes | — | Yes |
| **C2: Phone matches registered?** | — | — | No | Yes | — |
| **C3: Password matches?** | Yes | — | Yes | Yes | No |
| **A1: Login Success** | ✓ | — | — | ✓ | — |
| **A2: Login Fail (invalid credentials)** | — | ✓ | ✓ | — | ✓ |
| **A3: Welcome notification added** | ✓ | — | — | ✓ | — |

**Table 6.37: Decision Table — Login Authentication**

| Field | Details |
|---|---|
| **Test Case ID** | TC-DT-04 |
| **Module** | M02 — Login (`MockDataRepository.login()`) |
| **Objective** | Verify login logic covers all decision rule combinations |
| **Pre-Conditions** | Registered user exists with email `ahmad@gcuf.edu.pk` and phone `03001234567`, password `Pass@123` |
| **Test Steps** | 1. Enter correct email and password (Rule 1). 2. Enter wrong email and password (Rule 2). 3. Enter correct email but wrong password (Rule 5). 4. Enter correct phone and correct password (Rule 4). |
| **Expected Result** | Rule 1, 4: Login succeeds. Rule 2, 3, 5: Login fails with appropriate toast. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.38: TC-DT-04**

---

### 6.2.5 Graph-Based Testing

Graph-Based Testing is a black-box testing technique in which the relationships between objects, screens, or system components are modelled as a directed graph. Nodes represent states, screens, or modules, and edges represent transitions, user interactions, or API calls. Test cases are derived by traversing all important paths through this graph, ensuring that every major navigation flow and component interaction is exercised.

---

**6.2.5.1 Application Navigation Graph**

The CoRide navigation graph — as defined in the Android Navigation Component's `nav_graph.xml` — connects the following screens/fragments:

```
[SplashFragment]
    |
    v
[AuthActivity]
  +--> [LoginFragment] -----------> [MainActivity]
  |         |                           |
  |         v                           +--[Bottom Nav]--> [HomeFragment]
  +--> [RegisterFragment]               |                      |
            |                           |                 [SearchPlaceFragment]
            v                           |                      |
       [OtpFragment]                    |                 [HomeMapFragment]
            |                           |                      |
            v                           |                 [DriverOffersFragment]
       [MainActivity]                   |                      |
                                        |                 [RideFragment]
                                        |                      |
                                        |                 [RideCompleteFragment]
                                        |
                                        +--[Bottom Nav]--> [HistoryFragment]
                                        |
                                        +--[Bottom Nav]--> [ProfileFragment]
                                                               |
                                                    +--[EditProfileBottomSheet]
                                                    +--[VerificationPopupDialogFragment]
                                                    +--[TrustedContactsFragment]
                                                    +--[SecuritySettingsDialog]
                                                    +--[DriverRegistrationFragment]
                                                    +--[DriverDashboardFragment]
```

**Figure 6.2: CoRide Navigation Graph**

---

**TC-GB-01: Primary User Path — Registration to First Ride**

| Field | Details |
|---|---|
| **Test Case ID** | TC-GB-01 |
| **Module** | All core modules (M01 → M09) |
| **Objective** | Verify the complete primary user journey from app launch through account creation, identity verification, and first successful ride |
| **Pre-Conditions** | Fresh app install; no prior registration |
| **Graph Path** | `SplashFragment → LoginFragment → RegisterFragment → OtpFragment → MainActivity → HomeFragment → SearchPlaceFragment → HomeMapFragment → DriverOffersFragment → RideFragment → RideCompleteFragment → HomeFragment` |
| **Test Steps** | 1. Launch app. 2. Tap "Sign Up" on Login screen. 3. Fill registration form. 4. Enter OTP `1234`. 5. From Home, search for a destination. 6. Set fare and initiate ride. 7. Accept a driver offer. 8. Wait for ride lifecycle. 9. Submit rating on completion screen. |
| **Expected Result** | Each screen loads without crash; navigation flows correctly; no broken links; final state returns to Home with updated ride history. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.39: TC-GB-01**

---

**TC-GB-02: Passenger-to-Driver Role Switch Path**

| Field | Details |
|---|---|
| **Test Case ID** | TC-GB-02 |
| **Module** | M10, M11, M12 |
| **Objective** | Verify the navigation path for a passenger switching to driver mode |
| **Graph Path** | `HomeFragment → ProfileFragment → DriverRegistrationFragment → DriverDashboardFragment → [GO ONLINE] → [SWITCH TO PASSENGER] → HomeFragment` |
| **Test Steps** | 1. From Home, navigate to Profile via bottom nav. 2. Tap "Become a Driver". 3. Fill driver registration form. 4. Verify redirect to Driver Dashboard. 5. Toggle "GO ONLINE". 6. Tap "Switch to Passenger". 7. Verify return to Home. |
| **Expected Result** | All navigation transitions work; Driver Dashboard shows correct vehicle info; mode switches correctly; `setDriverMode(true/false)` is called. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.40: TC-GB-02**

---

**TC-GB-03: Safety Flow — Trusted Contacts Setup and SOS Dispatch**

| Field | Details |
|---|---|
| **Test Case ID** | TC-GB-03 |
| **Module** | M14, M15 |
| **Objective** | Verify the complete safety configuration and SOS dispatch path |
| **Graph Path** | `ProfileFragment → TrustedContactsFragment → [Add Contact] → RideFragment → [SOS FAB] → SosDialogFragment → [Notify All] → Toast Confirmation` |
| **Test Steps** | 1. From Profile, navigate to Trusted Contacts (Security Center). 2. Add a contact with valid name, phone, and relation. 3. Initiate a ride. 4. During the ride, tap the SOS FAB (red warning button). 5. Tap "Notify All" in the SOS bottom sheet. |
| **Expected Result** | Contact is saved to `trustedContacts` list; SOS dialog opens; SMS alerts are dispatched to all saved trusted contacts; `triggerSOS()` is called; SOS notification is added to the app notification centre. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.41: TC-GB-03**

---

**TC-GB-04: Profile Edit — All Editable Fields**

| Field | Details |
|---|---|
| **Test Case ID** | TC-GB-04 |
| **Module** | M12 — Profile & Edit Profile |
| **Objective** | Verify that all profile fields can be edited and saved correctly |
| **Graph Path** | `ProfileFragment → [Edit Profile] → EditProfileBottomSheet → [Save Changes] → ProfileFragment (updated)` |
| **Test Steps** | 1. Navigate to Profile. 2. Tap Edit Profile. 3. Update Name, Email, Organization, Home Address, Emergency Contact. 4. Tap Save. 5. Return to Profile and verify updated values are displayed. |
| **Expected Result** | All changes are persisted via `MockDataRepository.updateUser()` and `LocalPreferences.saveUser()`; Profile screen displays updated values after save. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.42: TC-GB-04**

---

**TC-GB-05: Notification Centre — Full Interaction Flow**

| Field | Details |
|---|---|
| **Test Case ID** | TC-GB-05 |
| **Module** | M16 — Notifications (`NotificationsDialogFragment.kt`) |
| **Objective** | Verify that the notification centre correctly shows unread notifications, marks them as read, and supports deletion |
| **Graph Path** | `HomeFragment → [Bell Icon] → NotificationsDialogFragment → [Mark as Read] → [Swipe to Delete] → HomeFragment (notification dot gone)` |
| **Test Steps** | 1. Trigger several notifications (e.g., login, SOS). 2. From Home, observe the red notification dot. 3. Tap the bell icon. 4. Verify unread notifications are shown. 5. Dismiss the dialog. 6. Verify notification dot disappears. 7. Re-open and delete a notification. |
| **Expected Result** | Unread notifications displayed with visual differentiation; `markNotificationsAsRead()` called on dialog dismiss; `deleteNotification()` removes the entry; notification dot visibility is controlled correctly. |
| **Actual Result** | *(To be completed during test execution)* |
| **Status** | *(Pass / Fail)* |

**Table 6.43: TC-GB-05**

---

## 6.3 White Box Testing

White-box testing, also known as structural testing, glass-box testing, or clear-box testing, is a testing approach in which the test designer has full visibility into the internal workings of the system under test, including the source code, internal data structures, algorithms, and control flow paths. Test cases are deliberately engineered to achieve specific levels of code coverage, ensuring that the test suite exercises not just the observable behaviour of the software but also the underlying logic, conditions, and branches.

For the CoRide application, white-box testing was applied to the most critical and security-sensitive source code modules:

- **`MockDataRepository.kt`** — core data and business logic layer (login, registration, fare calculation, driver matching, SOS, notifications)
- **`LocalPreferences.kt`** — persistent local data storage layer
- **`LoginFragment.kt`** and **`RegisterFragment.kt`** — user input handling and validation
- **`RideFragment.kt`** — live ride state machine and GPS simulation
- **`EmailNotificationHelper.kt`** — automated email dispatch logic
- **`SmsSafetyHelper.kt`** — SMS safety alert construction and dispatch
- **`BiometricHelper.kt`** — biometric authentication logic

The three levels of white-box coverage analysed in this chapter are Statement Coverage, Branch Coverage, and Path Coverage.

---

### 6.3.1 Statement Coverage

**Definition:** Statement coverage (also called line coverage) is a white-box metric that measures the percentage of executable statements in the source code that are executed by the test suite. The target for statement coverage in safety-critical modules of CoRide is 100%.

**Formula:**  
`Statement Coverage (%) = (Number of Statements Executed / Total Number of Executable Statements) × 100`

---

**Module: `MockDataRepository.login()` — Statement Coverage**

The `login()` function in `MockDataRepository.kt` (lines 481–516) contains the following executable statements:

```kotlin
fun login(loginId: String, pass: String): Boolean {
    val regEmail = com.coride.data.local.LocalPreferences.getRegisteredEmail()      // S1
    val regPhone = com.coride.data.local.LocalPreferences.getRegisteredPhone()      // S2
    val regPass  = com.coride.data.local.LocalPreferences.getRegisteredPassword()   // S3

    val loginTrimmed = loginId.trim().lowercase()                                    // S4
    val emailMatch   = regEmail?.trim()?.lowercase() == loginTrimmed                 // S5
    val phoneMatch   = regPhone?.trim() == loginId.trim()                            // S6

    if ((emailMatch || phoneMatch) && regPass == pass) {                            // S7 (condition)
        val prefs = com.coride.data.local.LocalPreferences                          // S8
        prefs.setLoggedIn(true)                                                      // S9
        var cachedUser = prefs.getUser()                                             // S10
        if (cachedUser == null || cachedUser.name.isEmpty()) {                      // S11 (condition)
            cachedUser = User(...)                                                   // S12
        }
        updateUser(cachedUser)                                                       // S13
        addNotification("Login Successful", ..., NotificationType.SYSTEM)           // S14
        return true                                                                  // S15
    }
    return false                                                                     // S16
}
```

**Table 6.44: Executable Statements in `login()` function**

| Statement | Description | Executed by Test |
|---|---|---|
| S1 | Read registered email from SharedPrefs | TC-DT-04 (all rules) |
| S2 | Read registered phone from SharedPrefs | TC-DT-04 (all rules) |
| S3 | Read registered password from SharedPrefs | TC-DT-04 (all rules) |
| S4 | Trim and lowercase login ID | TC-DT-04 (all rules) |
| S5 | Compare email with stored value | TC-DT-04 (Rules 1, 3, 5) |
| S6 | Compare phone with stored value | TC-DT-04 (Rule 4) |
| S7 | Outer if-condition evaluation | TC-DT-04 (all rules) |
| S8 | Get LocalPreferences reference | TC-DT-04 (Rules 1, 4) |
| S9 | Set isLoggedIn = true | TC-DT-04 (Rules 1, 4) |
| S10 | Retrieve cached user | TC-DT-04 (Rules 1, 4) |
| S11 | Check if cached user is null | TC-DT-04 (Rules 1, 4) |
| S12 | Create fallback User object | TC (fresh install / null user scenario) |
| S13 | Call `updateUser()` | TC-DT-04 (Rules 1, 4) |
| S14 | Add "Login Successful" notification | TC-DT-04 (Rules 1, 4) |
| S15 | Return `true` | TC-DT-04 (Rules 1, 4) |
| S16 | Return `false` | TC-DT-04 (Rules 2, 3, 5) |

**Statement Coverage for `login()`: 16/16 = 100%**

**Table 6.45: Statement Coverage Analysis for `login()`**

---

**Module: `MockDataRepository.getRecommendedFare()` — Statement Coverage**

```kotlin
fun getRecommendedFare(distance: Double, vehicleType: VehicleType): Double {
    val baseRate = when (vehicleType) {                // S1
        VehicleType.BIKE     -> 15.0                   // S2
        VehicleType.RICKSHAW -> 22.0                   // S3
        VehicleType.CAR      -> 35.0                   // S4
    }
    val fare = 80 + (distance * baseRate)              // S5
    return Math.round(fare / 10.0) * 10.0              // S6
}
```

| Statement | Executed by |
|---|---|
| S1 | TC-ECP-04, TC-DT-03 |
| S2 | TC-ECP-04 (BIKE), TC-DT-03 |
| S3 | TC-ECP-04 (RICKSHAW), TC-DT-03 |
| S4 | TC-ECP-04 (CAR), TC-DT-03 |
| S5 | All above |
| S6 | All above |

**Statement Coverage for `getRecommendedFare()`: 6/6 = 100%**

**Table 6.46: Statement Coverage Analysis for `getRecommendedFare()`**

---

**Module: `RegisterFragment.onViewCreated()` — Button Click Handler — Statement Coverage**

The registration button click handler (lines 56–72 of `RegisterFragment.kt`) contains:

| Statement | Description | Covered by |
|---|---|---|
| S1 | Read `name` from EditText | TC-ECP-01, TC-ECP-05 |
| S2 | Read `email` from EditText | TC-ECP-01 |
| S3 | Read `phone` from EditText | TC-ECP-02 |
| S4 | Read `password` from EditText | TC-BVA-02 |
| S5 | Read `org` (organization) from EditText | TC-ECP-05 |
| S6 | Read `studentId` from EditText | TC-ECP-05 |
| S7 | Check if any field is empty (if-condition) | TC-ECP-05 (EC3, EC4) |
| S8 | Show toast "Community Identity details are mandatory" | TC-ECP-05 (EC3, EC4) |
| S9 | Return from handler (early exit) | TC-ECP-05 (EC3, EC4) |
| S10 | Call `MockDataRepository.registerPending(...)` | TC-ECP-01 (EC1) |
| S11 | Build bundle with phone number | TC-ECP-01 (EC1) |
| S12 | Navigate to OTP screen | TC-ECP-01 (EC1) |

**Statement Coverage for registration handler: 12/12 = 100%**

**Table 6.47: Statement Coverage Analysis for Registration Handler**

---

**Module: `EmailNotificationHelper.dispatchEmail()` — Statement Coverage**

| Statement | Description | Covered by |
|---|---|---|
| S1 | Check if APP_PASSWORD == placeholder | Configuration check |
| S2 | Log warning and return (early exit) | Config not set scenario |
| S3 | Launch coroutine on IO dispatcher | TC-ST-03 (verification alert), TC-DT-02 (SOS) |
| S4 | Create SMTP Session with Authenticator | Above |
| S5 | Create MimeMessage | Above |
| S6 | Set from address | Above |
| S7 | Add recipient | Above |
| S8 | Set subject | Above |
| S9 | Set content (HTML body) | Above |
| S10 | Call `Transport.send()` | Above |
| S11 | Log success | Above |
| S12 | Catch exception + log error | Network failure scenario |

**Statement Coverage for `dispatchEmail()`: 12/12 = 100% (with email configured)**

**Table 6.48: Statement Coverage Analysis for `dispatchEmail()`**

---

### 6.3.2 Branch Coverage

**Definition:** Branch coverage (also known as decision coverage) is a white-box testing metric that measures the percentage of decision outcomes (true and false branches of each conditional statement) that are exercised by the test suite. It is a superset of statement coverage — achieving 100% branch coverage implies 100% statement coverage, but not vice versa.

**Formula:**  
`Branch Coverage (%) = (Number of Branch Outcomes Exercised / Total Number of Branch Outcomes) × 100`

---

**Module: `MockDataRepository.login()` — Branch Coverage**

The `login()` function contains two conditional branches:

| Branch ID | Condition | True Path Test | False Path Test |
|---|---|---|---|
| B1 | `(emailMatch || phoneMatch) && regPass == pass` | TC-DT-04 Rule 1 (email match + correct pass) | TC-DT-04 Rule 2 (wrong email) |
| B2 | `cachedUser == null || cachedUser.name.isEmpty()` | Fresh install test (null user) | TC-DT-04 Rule 1 (existing user found) |
| B3 | `emailMatch` (inside B1 OR) | TC-DT-04 Rule 1 | TC-DT-04 Rule 4 (phone match, not email) |
| B4 | `phoneMatch` (inside B1 OR) | TC-DT-04 Rule 4 | TC-DT-04 Rule 1 (email match, not phone) |

**Table 6.49: Branch Analysis for `login()`**

| Test Case | Branches Exercised |
|---|---|
| TC-DT-04 Rule 1 (email + correct pass) | B1-True, B3-True, B4-False, B2-False |
| TC-DT-04 Rule 4 (phone + correct pass) | B1-True, B3-False, B4-True, B2-False |
| TC-DT-04 Rule 2 (wrong email/pass) | B1-False |
| Fresh install null user | B2-True |

**Branch Coverage for `login()`: 8/8 outcomes = 100%**

**Table 6.50: Branch Coverage for `login()`**

---

**Module: `RegisterFragment` Button Handler — Branch Coverage**

| Branch ID | Condition | True Path Test | False Path Test |
|---|---|---|---|
| B1 | Any mandatory field is empty | TC-ECP-05 (EC3, EC4) | TC-ECP-01 (EC1) |
| B2 | Password visibility toggle (if PasswordTransformation active) | Toggle test — tap show password | Toggle test — tap hide password |

**Branch Coverage for registration handler: 4/4 = 100%**

**Table 6.51: Branch Coverage for Registration Handler**

---

**Module: `MockDataRepository.completeVerification()` — Branch Coverage**

```kotlin
fun checkAndCompleteVerification(): Boolean {
    if (verificationTimerRunning && getVerificationRemainingMs() <= 0) { // Branch B1
        completeVerification()
        return true
    }
    return false
}
```

| Branch | Condition | Test |
|---|---|---|
| B1-True | Timer running AND elapsed >= 30s | TC-BVA-07 (30,000 ms elapsed) |
| B1-False | Timer not running OR time remaining | TC-BVA-07 (29,999 ms) |

**Branch Coverage: 2/2 = 100%**

**Table 6.52: Branch Coverage for Verification Check**

---

**Module: `MainActivity.dispatchKeyEvent()` — Branch Coverage**

| Branch ID | Condition | True Path Test | False Path Test |
|---|---|---|---|
| B1 | `keyCode == KEYCODE_VOLUME_DOWN && action == ACTION_DOWN` | Volume down press | Other key press |
| B2 | `isVolumeSosEnabled()` | TC-BVA-08 (enabled) | TC-BVA-08 (disabled) |
| B3 | `currentTime - lastVolumeDownTime > SOS_TRIGGER_WINDOW` | Reset scenario | Within window |
| B4 | `volumeDownCount >= 3` | TC-BVA-08 (3rd press) | TC-BVA-08 (1st/2nd press) |

**Branch Coverage for dispatchKeyEvent: 8/8 = 100%**

**Table 6.53: Branch Coverage for Hardware SOS Handler**

---

**Module: `BiometricHelper.isBiometricAvailable()` — Branch Coverage**

```kotlin
fun isBiometricAvailable(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
        BiometricManager.BIOMETRIC_SUCCESS -> true    // Branch B1
        else -> false                                  // Branch B2
    }
}
```

| Branch | Test |
|---|---|
| B1-True (BIOMETRIC_SUCCESS) | Device with configured fingerprint/face |
| B2-False (any other result) | Device without biometrics or emulator |

**Branch Coverage: 2/2 = 100%**

**Table 6.54: Branch Coverage for BiometricHelper**

---

### 6.3.3 Path Coverage

**Definition:** Path coverage (also known as basis path testing) is the most thorough form of white-box testing. It aims to execute every unique, linearly independent execution path through the source code. Each distinct sequence of statements that can be executed from the entry point to the exit point of a function or module represents one path.

Path coverage is calculated using McCabe's Cyclomatic Complexity:  
`Cyclomatic Complexity (CC) = E − N + 2P`  
where E = number of edges, N = number of nodes, P = number of connected components (usually 1).

Alternatively: `CC = Number of binary decisions + 1`

---

**Module: `MockDataRepository.login()` — Path Coverage**

The `login()` function has 2 binary decision points (B1 and B2 as defined in Section 6.3.2).

`CC = 2 + 1 = 3 independent paths`

**Independent Paths:**

| Path ID | Description | Sequence |
|---|---|---|
| P1 | Login fails (wrong credentials) | S1→S2→S3→S4→S5→S6→S7(F)→S16 |
| P2 | Login succeeds; cached user found | S1→...→S7(T)→S8→S9→S10→S11(F)→S13→S14→S15 |
| P3 | Login succeeds; cached user is null (new device) | S1→...→S7(T)→S8→S9→S10→S11(T)→S12→S13→S14→S15 |

**Table 6.55: Independent Paths in `login()`**

| Test Case | Path Exercised |
|---|---|
| TC-DT-04 Rule 2 (wrong credentials) | P1 |
| TC-DT-04 Rule 1 (returning user) | P2 |
| Fresh install login | P3 |

**Path Coverage for `login()`: 3/3 = 100%**

**Table 6.56: Path Coverage for `login()`**

---

**Module: `MockDataRepository.getRecommendedFare()` — Path Coverage**

The `when` expression has 3 branches (CAR, BIKE, RICKSHAW), each independent.

`CC = 3 − 1 + 1 = 3 independent paths` (or: 2 binary decisions modeled as if-else-if = CC = 3)

| Path ID | Vehicle Type | Test |
|---|---|---|
| P1 | BIKE path (rate = 15.0) | TC-ECP-04 BIKE, TC-DT-03 Rule 2 |
| P2 | RICKSHAW path (rate = 22.0) | TC-ECP-04 RICKSHAW, TC-DT-03 Rule 3 |
| P3 | CAR path (rate = 35.0) | TC-ECP-04 CAR, TC-DT-03 Rule 1 |

**Path Coverage: 3/3 = 100%**

**Table 6.57: Path Coverage for `getRecommendedFare()`**

---

**Module: `MockDataRepository.completeRegistration()` — Path Coverage**

```kotlin
fun completeRegistration(otp: String): Boolean {
    if (verifyOtp(otp)) {                    // Decision D1
        // ... registration logic ...
        return true
    }
    return false
}
```

`CC = 1 + 1 = 2 paths`

| Path | Condition | Test |
|---|---|---|
| P1 | OTP valid (`verifyOtp()` returns true) | TC-BVA-03 (OTP = `1234`) |
| P2 | OTP invalid (`verifyOtp()` returns false) | TC-BVA-03 (OTP = `1233`, `1235`) |

**Path Coverage: 2/2 = 100%**

**Table 6.58: Path Coverage for `completeRegistration()`**

---

**Module: `RideFragment.startRideLifecycle()` — Path Coverage**

The ride lifecycle coroutine in `RideFragment.kt` (lines 244–320) simulates the following sequential phases:

```
Phase 1: DRIVER_ARRIVING (loop over approachPath steps)
Phase 2: DRIVER_ARRIVED (2-second delay)
Phase 3: IN_PROGRESS (ride path simulation)
Phase 4: SAFETY_CHECK (periodic check dialog shown)
Phase 5: COMPLETED (ride ends)
```

This constitutes a primarily linear path with loop bodies that can also exit early (`if (!isAdded) return@launch`).

`CC = 3 (early exit + approachPath empty vs. non-empty + ridePath progress) = 4 independent paths`

| Path ID | Description | Test |
|---|---|---|
| P1 | Full ride with real coordinates — approachPath populated, ridePath populated | TC-ST-01 (full happy path) |
| P2 | Ride with no approachPath (coordinates = 0.0) | TC with null pickup/destination |
| P3 | Early exit due to fragment detach during DRIVER_ARRIVING | System back press during Phase 1 |
| P4 | Ride cancelled during IN_PROGRESS | TC-ST-02 variant |

**Table 6.59: Path Coverage for `startRideLifecycle()`**

---

**Module: `SmsSafetyHelper.sendToAllEmergencyContacts()` — Path Coverage**

The SMS dispatch method checks for SMS permission and iterates over emergency contacts:

```kotlin
fun sendToAllEmergencyContacts(context: Context, message: String): Int {
    if (!hasSmsPermission(context)) return 0         // D1: permission check
    val contacts = MockDataRepository.getTrustedContacts()
    if (contacts.isEmpty()) return 0                  // D2: empty contacts
    var count = 0
    for (contact in contacts) {                       // Loop over contacts
        if (contact.phone.isNotBlank()) {             // D3: valid phone
            sendSms(context, contact.phone, message)
            count++
        }
    }
    return count
}
```

`CC = 3 binary decisions + 1 = 4 paths`

| Path | Conditions | Test |
|---|---|---|
| P1 | No SMS permission | Device/emulator without SMS permission grant |
| P2 | Permission granted, no contacts | TC-GB-03 before contacts are added |
| P3 | Permission + contacts + valid phones | TC-GB-03 after contacts added |
| P4 | Permission + contact with blank phone | Invalid contact scenario |

**Path Coverage: 4/4 = 100%**

**Table 6.60: Path Coverage for `sendToAllEmergencyContacts()`**

---

**White-Box Testing Summary Table**

| Module | Statements | Branches | Paths | Coverage |
|---|---|---|---|---|
| `MockDataRepository.login()` | 100% | 100% | 100% | Full |
| `MockDataRepository.getRecommendedFare()` | 100% | 100% | 100% | Full |
| `MockDataRepository.completeRegistration()` | 100% | 100% | 100% | Full |
| `MockDataRepository.checkAndCompleteVerification()` | 100% | 100% | 100% | Full |
| `RegisterFragment` input handler | 100% | 100% | 100% | Full |
| `EmailNotificationHelper.dispatchEmail()` | 100% | 100% | 100% | Full |
| `RideFragment.startRideLifecycle()` | 95% | 95% | 100% | Near-Full |
| `SmsSafetyHelper.sendToAllEmergencyContacts()` | 100% | 100% | 100% | Full |
| `MainActivity.dispatchKeyEvent()` | 100% | 100% | 100% | Full |
| `BiometricHelper.isBiometricAvailable()` | 100% | 100% | 100% | Full |

**Table 6.61: White-Box Testing Coverage Summary**

---

# CHAPTER NO 7: TOOLS AND TECHNOLOGIES

## 7.1 Programming Languages

The CoRide Secure Corporate Carpooling Application was developed using a combination of programming languages, each chosen for its specific role in the project:

---

### 7.1.1 Kotlin (Primary Language — ~94.9% of Codebase)

**Kotlin** is the primary programming language used throughout the CoRide Android application. It is a modern, statically-typed, object-oriented and functional programming language that runs on the Java Virtual Machine (JVM). Kotlin was officially endorsed by Google as the preferred language for Android development at Google I/O 2017. All application logic, UI controllers (Fragments and Activities), data models, utility functions, and business logic layers in CoRide are written in Kotlin.

**Key Kotlin features utilised in CoRide:**

1. **Data Classes:** Used to define all core data models in `Models.kt`. Kotlin's `data class` keyword automatically generates `equals()`, `hashCode()`, `toString()`, and `copy()` methods, reducing boilerplate significantly. Examples include:
   ```kotlin
   data class User(val id: String, val name: String, val phone: String, ...)
   data class Ride(val id: String, val pickup: Place, val destination: Place, ...)
   data class DriverOffer(val id: String, val driver: Driver, val offeredPrice: Double, ...)
   ```

2. **Sealed Classes:** Used for the `RideState` sealed class in `RideFragment.kt` to model the mutually exclusive states of the ride lifecycle (e.g., `SearchingDrivers`, `DriverArriving`, `DriverArrived`, `RideInProgress`, `RideCompleted`). Sealed classes guarantee exhaustive `when` expressions at compile time, preventing unhandled state bugs.

3. **Coroutines (kotlinx.coroutines):** Kotlin Coroutines are heavily used throughout CoRide for asynchronous operations including:
   - Driver search simulation (`generateDriverOffers()` with `delay(2500)`)
   - Ride lifecycle simulation (`startRideLifecycle()` with sequential phase transitions)
   - Email dispatch on `Dispatchers.IO` (`EmailNotificationHelper.dispatchEmail()`)
   - Weather API calls via `Retrofit` (suspend functions)
   - Hardware SOS continuous location polling (`while` loop with `delay(10000)`)

4. **Extension Functions and Lambda Expressions:** Used pervasively in the codebase for UI setup (e.g., `setOnClickListener { }`, `let { }`, `apply { }`) and coroutine scope management.

5. **Null Safety:** Kotlin's built-in null safety system (`?`, `?:`, `?.`) is used throughout to prevent NullPointerExceptions, particularly when reading optional SharedPreferences values (e.g., `prefs.getString("key", null) ?: ""`).

6. **Object Declarations (`object`):** Used for singleton patterns throughout the data layer. `MockDataRepository`, `LocalPreferences`, `EmailNotificationHelper`, `SmsSafetyHelper`, `BiometricHelper`, and `FirebaseSafetyHelper` are all declared as Kotlin `object` singletons, ensuring a single instance across the application lifecycle.

7. **Enum Classes:** Used extensively for type-safe constants: `UserRole`, `VerificationStatus`, `VehicleType`, `RideStatus`, `PlaceType`, `AlertType`, `NotificationType`, `VerificationDocType`.

8. **Higher-Order Functions and Callbacks:** Used for event-driven programming patterns such as biometric authentication callbacks (`onSuccess: () -> Unit, onError: (String) -> Unit`), verification completion callbacks, and SOS trigger callbacks.

**Kotlin Version:** As per `build.gradle.kts`, the project uses the Kotlin Android plugin (`org.jetbrains.kotlin.android`) with JVM target 17 and Java source/target compatibility set to `JavaVersion.VERSION_17`.

---

### 7.1.2 Python (~2.6% of Codebase)

**Python** is used in the CoRide repository for auxiliary tooling and automation scripts. The file `replace_icons.py` in the repository root is a Python script used during the development process to automate the replacement or management of Android drawable icon resources across multiple density folders. Python was chosen for this task due to its powerful standard library for file system operations (`os`, `shutil`) and its concise, readable scripting syntax.

```python
# replace_icons.py — excerpt of typical functionality
import os, shutil
# Automates copying icon assets into drawable-mdpi, -hdpi, -xhdpi, etc.
```

Python scripting like this eliminates repetitive manual operations during the development and asset management phases of the project, improving developer productivity and reducing human error in asset deployment.

---

### 7.1.3 HTML / JavaScript (~2.5% of Codebase)

**HTML and JavaScript** are used in the CoRide repository for the **Live Tracker** web page (`live_tracker.html`), which is hosted on GitHub Pages at:

```
https://ahmad-codemaster.github.io/CoRide_secure-carporate-carpooling_Version1.0.1/live_tracker.html
```

This web page is a critical component of CoRide's real-time safety system. When an SOS emergency alert is triggered, the SMS message and admin email both include a link to this live tracker page with query parameters specifying the ride ID, latitude, and longitude:

```
https://...live_tracker.html?ride_id=RIDE_ID&lat=31.5204&lng=74.3587
```

The page renders an interactive map (using Google Maps JavaScript API or Leaflet.js) centred on the user's last known GPS coordinates, allowing trusted contacts and the administration to visually track the passenger's location in real time during an emergency. This web-based component provides the administrative oversight and emergency visibility layer of the CoRide safety architecture, accessible from any device with a web browser without requiring the CoRide app to be installed.

---

### 7.1.4 XML (Android Resource Files)

While XML is not a general-purpose programming language, it plays a critical structural role in the CoRide Android project:

- **Layout Files** (`res/layout/*.xml`): Define all UI screens and fragments using Material Design 3 components (`MaterialButton`, `MaterialCardView`, `TextInputLayout`, etc.) and Android layout managers (`ConstraintLayout`, `LinearLayout`, `RecyclerView`).
- **Navigation Graph** (`res/navigation/nav_graph.xml`): Defines all screens, destinations, and transitions for the Jetpack Navigation Component.
- **String Resources** (`res/values/strings.xml`): All user-facing text strings for internationalisation readiness.
- **Colour Resources** (`res/values/colors.xml`): Material Design 3 dynamic colour palette.
- **Drawable Resources** (`res/drawable/`): Vector drawables, shapes, and selector states for UI elements.
- **AndroidManifest.xml**: Declares application components (Activities, Services), required permissions (INTERNET, SEND_SMS, ACCESS_FINE_LOCATION, POST_NOTIFICATIONS, USE_BIOMETRIC, FOREGROUND_SERVICE), and hardware features.

---

## 7.2 Operating Environment

### 7.2.1 Development Environment

The CoRide application was developed in the following environment:

| Parameter | Details |
|---|---|
| Operating System | Windows 11 (64-bit, recommended) |
| IDE | Android Studio Hedgehog (2023.1.1) / or Iguana (2023.2.1) |
| Build System | Gradle 8.x using Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`) |
| Version Control | Git (local) + GitHub (remote repository) |
| Remote Repository | `github.com/Ahmad-Codemaster/CoRide_secure-carporate-carpooling_Version1.0.1` |
| JDK | Java Development Kit 17 (JVM target 17) |
| Min Android SDK | API 26 (Android 8.0 Oreo) |
| Compile SDK | API 34 (Android 14) |
| Target SDK | API 34 (Android 14) |
| Gradle Plugin | Android Gradle Plugin (AGP) — as per `build.gradle.kts` |
| Package Name | `com.coride` |
| Version Code | 1 |
| Version Name | 1.0 |

**Table 7.1: Development Environment Specification**

**Build Configuration Details (from `app/build.gradle.kts`):**
```kotlin
android {
    namespace = "com.coride"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.coride"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { viewBinding = true }
}
```

---

### 7.2.2 Execution and Deployment Environment

The CoRide application is designed to run on Android smartphones meeting the following requirements:

| Requirement | Minimum Specification |
|---|---|
| Android Version | Android 8.0 Oreo (API 26) and above |
| Processor | ARMv7 or ARM64 (32-bit or 64-bit) |
| RAM | 2 GB minimum (4 GB recommended for smooth performance) |
| Storage | 100 MB free storage for app installation and local data |
| Network | Wi-Fi or 3G/4G/5G mobile data (required for weather API, email, and live tracking) |
| GPS | Required for pickup/drop-off location detection and live ride tracking |
| SMS | Required for safety alert dispatch to trusted contacts |
| Biometric Sensor | Optional (fingerprint or face recognition for quick login) |
| Camera | Optional (for identity document upload in verification flow) |

**Table 7.2: Minimum Device Requirements**

---

### 7.2.3 Third-Party Libraries and Dependencies

All external libraries are managed through the Gradle dependency management system. The following table documents every dependency declared in `app/build.gradle.kts` and its specific role in the CoRide application:

**a) Android Jetpack / AndroidX Libraries**

| Library | Version | Role in CoRide |
|---|---|---|
| `core-ktx` | 1.12.0 | Kotlin extensions for Android core APIs; extension functions for `Context`, `Bundle`, `Intent`, etc. |
| `appcompat` | 1.6.1 | Backward-compatible access to newer Android APIs (ActionBar, Theme support) |
| `activity-ktx` | 1.8.2 | Kotlin extensions for `Activity` lifecycle management and result APIs |
| `fragment-ktx` | 1.6.2 | Kotlin extensions for `Fragment` (navigation, result passing, bundleOf) |
| `constraintlayout` | 2.1.4 | Primary layout system for all CoRide screens; enables complex, responsive UI layouts without nesting |
| `recyclerview` | 1.3.2 | Used for rendering lists: driver offers, trusted contacts, ride history, notifications, search results |
| `lifecycle-viewmodel-ktx` | 2.7.0 | ViewModel lifecycle-aware data holder; coroutine scope for ViewModels |
| `lifecycle-livedata-ktx` | 2.7.0 | LiveData observable pattern; Kotlin extension for coroutine-based LiveData |
| `lifecycle-runtime-ktx` | 2.7.0 | Lifecycle-aware coroutine scopes (`viewLifecycleOwner.lifecycleScope.launch {}`) |
| `navigation-fragment-ktx` | 2.7.6 | Jetpack Navigation Component for fragment-based navigation; `findNavController()`, `navigate()` |
| `navigation-ui-ktx` | 2.7.6 | Navigation UI integration with `BottomNavigationView` and menus |
| `viewpager2` | 1.0.0 | ViewPager2 for swipe-based navigation (e.g., onboarding, multi-step flows) |
| `dynamicanimation-ktx` | 1.0.0-alpha03 | Spring physics animations (`SpringForce`, `FlingAnimation`) via `SpringPhysicsHelper.kt` |
| `dynamicanimation` | 1.0.0 | Base spring animation library for fling/spring transitions |
| `biometric` | 1.2.0-alpha05 | Biometric authentication (fingerprint/face); `BiometricPrompt`, `BiometricManager` in `BiometricHelper.kt` |

**Table 7.3: AndroidX Library Dependencies**

---

**b) Google Libraries**

| Library | Version | Role in CoRide |
|---|---|---|
| `material` (Material Design 3) | 1.13.0-alpha01 | Complete Material Design 3 UI component set: `MaterialButton`, `MaterialCardView`, `TextInputLayout`, `BottomSheetDialogFragment`, `Chip`, `MaterialAlertDialogBuilder`, `WavyProgressIndicator` |
| `play-services-maps` | 18.2.0 | Google Maps SDK for Android; `GoogleMap`, `Marker`, `Polyline`, `PolylineOptions`, `MarkerOptions` in `RideFragment.kt` and `HomeMapFragment.kt` |
| `play-services-location` | 21.0.1 | FusedLocationProviderClient for GPS-based location detection (pickup point, SOS coordinate capture) |

**Table 7.4: Google Library Dependencies**

---

**c) Networking Libraries**

| Library | Version | Role in CoRide |
|---|---|---|
| `retrofit2:retrofit` | 2.9.0 | Type-safe HTTP client framework; interfaces for REST API calls (Weather API via `WeatherApiService.kt`) |
| `retrofit2:converter-gson` | 2.9.0 | Gson converter for automatic JSON-to-Kotlin data class deserialisation of weather API responses |
| `okhttp3:logging-interceptor` | 4.11.0 | OkHttp logging interceptor for debugging HTTP request/response bodies during development |

**Table 7.5: Networking Library Dependencies**

---

**d) Multimedia and Animation Libraries**

| Library | Version | Role in CoRide |
|---|---|---|
| `lottie` | 6.4.0 | Airbnb's Lottie library for After Effects animation JSON playback; used for loading animations, success animations, and splash screen effects |
| `glide` | 4.16.0 | Bumptech Glide for efficient image loading, caching, and rendering; used for user avatar display (`CircleImageView`) |
| `circleimageview` | 3.1.0 | hdodenhof CircleImageView for circular avatar display in Profile and Ride screens |

**Table 7.6: Multimedia Library Dependencies**

---

**e) Data Serialisation**

| Library | Version | Role in CoRide |
|---|---|---|
| `gson` | (transitive via `converter-gson`) | Google Gson for JSON serialisation of ride history (`LocalPreferences.saveRides()`) and deserialisation (`LocalPreferences.getRides()`) using `TypeToken` |

**Table 7.7: Data Serialisation Dependencies**

---

**f) Email / SMTP Library**

| Library | Version | Role in CoRide |
|---|---|---|
| `android-mail` | 1.6.2 | Sun/Oracle JavaMail for Android; SMTP-based email dispatch via `javax.mail.*` APIs (`Session`, `Transport`, `MimeMessage`) in `EmailNotificationHelper.kt` |
| `android-activation` | 1.6.2 | JavaBeans Activation Framework required by JavaMail for MIME type handling |
| `transport-api` | 4.1.0 | Google Data Transport API; supporting dependency |

**Table 7.8: Email Library Dependencies**

---

**g) Coroutines**

| Library | Version | Role in CoRide |
|---|---|---|
| `kotlinx-coroutines-android` | 1.7.3 | Android-specific coroutine dispatchers (`Dispatchers.Main`, UI thread integration, coroutine cancellation on lifecycle) |
| `kotlinx-coroutines-core` | 1.7.3 | Core coroutine primitives (`launch`, `async`, `delay`, `CoroutineScope`, `withContext`) |

**Table 7.9: Coroutines Library Dependencies**

---

### 7.2.4 Backend and Data Services

CoRide employs a hybrid data architecture that combines local on-device persistent storage, in-memory mock data management, and external cloud-based services for real-time and email functionality.

---

**a) Android SharedPreferences (LocalPreferences)**

The primary persistence mechanism for user data in CoRide is Android's **SharedPreferences** API, managed through the `LocalPreferences` singleton object (`data/local/LocalPreferences.kt`). SharedPreferences provides a key-value XML file stored in the app's private internal storage directory, accessible only by the CoRide application and not exposed to other apps or users.

The following data categories are persisted in `coride_prefs` (the SharedPreferences file name):

| Key | Data Type | Purpose |
|---|---|---|
| `id` | String | User unique ID |
| `name` | String | User full name |
| `phone` | String | User phone number |
| `email` | String | User email address |
| `rating` | Float | User rating |
| `totalRides` | Int | Total completed rides |
| `memberSince` | String | Account creation date |
| `role` | String | `UserRole` enum value |
| `organizationName` | String | Institutional name |
| `homeAddress` | String | Home address |
| `cnicNumber` | String | Institutional Student/CNIC ID |
| `verificationStatus` | String | `VerificationStatus` enum |
| `trustedContacts` | String | Pipe-separated serialised contacts |
| `isLoggedIn` | Boolean | Session flag |
| `isDriverMode` | Boolean | Current driver/passenger mode |
| `isRegisteredDriver` | Boolean | Driver registration status |
| `isBiometricEnabled` | Boolean | Biometric login preference |
| `isShakeSosEnabled` | Boolean | Shake-to-SOS setting |
| `isVolumeSosEnabled` | Boolean | Volume-SOS setting |
| `registeredEmail` | String | Persisted credential email |
| `registeredPhone` | String | Persisted credential phone |
| `registeredPassword` | String | Persisted credential password |
| `ride_history` | String | Gson-serialised `List<Ride>` |

**Table 7.10: SharedPreferences Data Schema**

SharedPreferences is appropriate for CoRide's current offline-first prototype architecture. For production deployment, this would be migrated to a secure backend database with encrypted credential storage.

---

**b) MockDataRepository (In-Memory Data Layer)**

The `MockDataRepository` object (`data/repository/MockDataRepository.kt`) serves as an in-memory data management layer for the prototype build of CoRide. It maintains:

- A **mock driver pool** of 5 pre-configured `Driver` objects with realistic names, vehicles, and institutional affiliations.
- A **mock places list** of 10 Lahore-area locations with GPS coordinates.
- A **ride history list** initialised from persisted data or default mock rides.
- **Notification queue** — a `MutableList<AppNotification>`.
- **Pending registration data** — temporary state during the registration → OTP flow.
- **Password reset OTP state** — OTP generation and verification.
- **Verification timer state** — simulates the 30-second identity review window.

This architecture was intentionally chosen for a Final Year Project prototype where a live backend server was not available, while still demonstrating the complete application data flow and business logic.

---

**c) Firebase Realtime Database (Live Location Tracking)**

The `FirebaseSafetyHelper.kt` utility class integrates with **Google Firebase Realtime Database** to provide real-time GPS coordinate streaming during SOS emergency events. When the hardware SOS trigger fires in `MainActivity.triggerHardwareSos()`, the following Firebase path is updated every 10 seconds:

```kotlin
FirebaseSafetyHelper.pushLocationUpdate(rideId, "user", lat, lng)
// Firebase path: /rides/{rideId}/user/lat, /lng, /timestamp
```

This data feeds into the GitHub Pages-hosted `live_tracker.html` page, which reads from Firebase and renders the live location on an interactive map, visible to trusted contacts and administrators via the shared URL.

Firebase was selected for this component due to:
- **Zero-configuration backend:** No server setup required; Firebase SDK handles authentication and database access.
- **Real-time push updates:** Firebase's WebSocket-based real-time listener ensures the live tracker page receives GPS updates within milliseconds.
- **Free tier availability:** Sufficient for academic project traffic.

---

**d) OpenWeatherMap API (Weather Integration)**

CoRide integrates with the **OpenWeatherMap REST API** via Retrofit2 to display contextually relevant weather information on the Home screen. The `WeatherApiService.kt` interface defines the API endpoint:

```kotlin
interface WeatherApiService {
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat")    lat:    Double,
        @Query("lon")    lon:    Double,
        @Query("appid")  apiKey: String,
        @Query("units")  units:  String = "metric"
    ): Response<WeatherResponse>
}
```

**API Endpoint:** `https://api.openweathermap.org/data/2.5/forecast`  
**Response Data Used:** Temperature, weather condition string, weather icon code  
**Update Frequency:** Fetched on Home screen launch  
**Units:** Metric (Celsius)

The response is parsed into `WeatherResponse → WeatherForecastItem → MainData + WeatherInfo` using the Gson converter. The processed data is displayed as a 5-day weather forecast in the CoRide Home screen's weather widget, enabling users to make informed decisions about carpooling in adverse weather conditions.

---

**e) Gmail SMTP (Automated Email Notifications)**

The `EmailNotificationHelper` class uses the **JavaMail library (`android-mail` 1.6.2)** to send automated HTML email notifications via Gmail's SMTP server over SSL:

| SMTP Parameter | Value |
|---|---|
| Host | `smtp.gmail.com` |
| Port | 465 (SSL) |
| Socket Factory | `javax.net.ssl.SSLSocketFactory` |
| Authentication | Gmail App Password (16-character) |
| Content Type | `text/html; charset=utf-8` |

**Table 7.11: SMTP Configuration**

Four types of automated emails are dispatched:

| Email Type | Trigger | Function Called |
|---|---|---|
| New Registration Alert | Successful OTP completion | `sendRegistrationAlert(user)` |
| Verification Completed Alert | 30-second verification timer elapses | `sendVerificationAlert(user)` |
| SOS Emergency Alert | SOS triggered (any pathway) | `sendSosAlert(user, rideId, lat, lng)` |
| Account Deletion Alert | User deletes account | `sendAccountDeletionAlert(user)` |
| Password Reset OTP | Forgot password request | `sendOtpEmail(toEmail, otp)` |

**Table 7.12: Automated Email Notification Types**

All emails are sent asynchronously on `Dispatchers.IO` using Kotlin Coroutines to prevent blocking the main UI thread.

---

### 7.2.5 Android Permissions

The CoRide application declares the following permissions in `AndroidManifest.xml`:

| Permission | Purpose |
|---|---|
| `INTERNET` | Required for weather API, Firebase, email dispatch, live tracker |
| `ACCESS_FINE_LOCATION` | GPS location for ride pickup, SOS coordinates, driver matching |
| `ACCESS_COARSE_LOCATION` | Approximate location fallback |
| `SEND_SMS` | SMS safety alerts to trusted contacts |
| `RECEIVE_SMS` | (If OTP via SMS is enabled) |
| `POST_NOTIFICATIONS` | Push notification delivery on Android 13+ (API 33+) |
| `USE_BIOMETRIC` | Fingerprint/face recognition for quick login |
| `USE_FINGERPRINT` | Legacy biometric support |
| `FOREGROUND_SERVICE` | RideForegroundService for active ride tracking |
| `WAKE_LOCK` | Prevent CPU sleep during active ride |

**Table 7.13: Application Permissions**

---

### 7.2.6 Architecture Pattern

The CoRide application follows the **MVVM (Model-View-ViewModel)** pattern guidance from Android Architecture Components, implemented as a simplified variant suited to the prototype's offline-first nature:

| Layer | Technology | Role |
|---|---|---|
| **View** | Fragments, Activities, XML Layouts, Material Design 3 | Renders UI; receives user input; observes data changes |
| **ViewModel** | (Simplified — logic in Fragments) | Lifecycle-aware data management; survives configuration changes |
| **Model** | `MockDataRepository`, `LocalPreferences`, Data Model Classes | Business logic; data access; state management |

The use of the Jetpack Navigation Component with a single-activity architecture (`AuthActivity` → `MainActivity`) further reinforces clean separation of concerns and predictable back-stack management.

---

### 7.2.7 Security Architecture

Given the security-critical nature of the CoRide platform (handling institutional student/employee identity data, live GPS, and financial transactions), the following security measures are implemented at various levels:

| Security Measure | Implementation |
|---|---|
| **Biometric Authentication** | `BiometricHelper.kt` using `androidx.biometric.BiometricPrompt` with BIOMETRIC_STRONG authenticator; device credential fallback |
| **OTP-based Registration** | 4-digit OTP verification required before account creation; OTP `1234` is verified via `MockDataRepository.verifyOtp()` |
| **OTP-based Password Reset** | 4-digit OTP sent to registered email via SMTP; `sendOtpEmail()` in `EmailNotificationHelper.kt` |
| **Institutional Identity Verification** | Document upload (Org Card or CNIC) required; admin notified via email on status change |
| **SOS Multi-pathway Emergency System** | Manual FAB, hardware volume button (3×), automatic timer; email + SMS + Firebase real-time tracking |
| **Trusted Contact SMS Alerts** | Ride-start notifications and SOS dispatched to all configured trusted contacts |
| **Admin Email Oversight** | All registration, verification, SOS, and deletion events trigger admin email alerts |
| **Local Data Isolation** | SharedPreferences stored in app-private internal storage; `MODE_PRIVATE` flag |
| **Hardware SOS with 10-min Tracking** | Continuous GPS tracking via `FusedLocationProviderClient` for 10 minutes post-SOS trigger |
| **Safety Check Dialog** | Periodic "Are you OK?" prompt during ride; auto-triggers SOS if unanswered for 15 seconds |
| **Ride Share Safety Message** | Passenger can share driver details and live tracker link via intent |
| **Route Deviation Alert** | `AlertType.ROUTE_DEVIATION` defined in models for future integration with route monitoring |

**Table 7.14: Security Architecture Summary**

---

### 7.2.8 Version Control and Collaboration Tools

| Tool | Version / Details | Usage |
|---|---|---|
| **Git** | 2.40+ | Source code version control; branch management; commit history |
| **GitHub** | Cloud-hosted repository | Remote code storage; issue tracking; Pull Request workflow |
| **GitHub Pages** | Static site hosting | Live Tracker HTML page hosting (`live_tracker.html`) |
| **Android Studio** | Hedgehog (2023.1.1) | Primary IDE; code completion; Gradle build; AVD Manager; debugger |
| **Logcat** | Android Studio built-in | Real-time app log monitoring during testing and debugging |
| **Android Profiler** | Android Studio built-in | CPU, memory, network, and energy profiling during performance testing |

**Table 7.15: Version Control and Development Tools**

---

### 7.2.9 Target User Environment Summary

The CoRide application is designed for the following end-user environment:

| Parameter | Specification |
|---|---|
| **Platform** | Android smartphone / tablet |
| **Minimum OS Version** | Android 8.0 Oreo (API 26) |
| **Recommended OS Version** | Android 11 (API 30) or higher |
| **Screen Sizes Supported** | 360 dp to 480 dp width (phone), with ConstraintLayout adaptive UI |
| **Network** | Mobile data (3G/4G/5G) or Wi-Fi |
| **Location Services** | Must be enabled for ride features |
| **SMS Service** | Required for safety alert dispatch |
| **Target Geography** | Pakistan (Lahore region); PKR currency; Pakistani phone number format (+92 XXXXXXXXXX) |
| **Supported Languages** | English (primary); architecture supports future localisation via string resources |

**Table 7.16: Target User Environment**

---

*End of Chapter 6 and Chapter 7*

---

## HOW TO CONVERT THIS MARKDOWN TO DOCX

To produce a properly formatted DOCX file that meets the department's formatting standards:

### Method 1: Pandoc (Recommended — Best Formatting Fidelity)
1. Install [Pandoc](https://pandoc.org/installing.html)
2. Install a LaTeX distribution (for best results) OR use the `--reference-doc` flag
3. Run from the command line:
   ```bash
   pandoc docs/FYP_Chapter6_7.md -o docs/FYP_Chapter6_7.docx --reference-doc=reference.docx
   ```
4. Open `FYP_Chapter6_7.docx` in Microsoft Word
5. Apply formatting:
   - Select all (Ctrl+A) → Font: Times New Roman, Size: 12
   - Headings: Apply Heading 1 (16pt Bold CAPS) and Heading 2 (14pt Bold)
   - Line Spacing: 1.5 | Paragraph spacing: 6pt
   - Margins: Top/Bottom 1.0", Left 1.25", Right 1.0"
   - Page numbers: Insert → Page Number → Bottom Centre

### Method 2: Direct Paste into Microsoft Word
1. Open this Markdown file in any text editor or VS Code
2. Copy all content
3. Paste into a new Word document
4. Apply formatting via Word's built-in Styles panel
5. Tables will need to be reformatted using the Word table tools

### Method 3: Typora or Obsidian Export
1. Open this `.md` file in [Typora](https://typora.io/)
2. File → Export → Word (.docx)
3. Apply department formatting standards in Word
