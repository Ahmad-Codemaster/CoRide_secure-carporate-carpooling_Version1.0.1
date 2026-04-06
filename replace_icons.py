import os
import re

DRAWABLES_DIR = r"app\src\main\res\drawable"
LAYOUTS_DIR = r"app\src\main\res\layout"

# SVG logic for the icons
ICONS = {
    "ic_location_pin": "M12,2C8.13,2 5,5.13 5,9c0,5.25 7,13 7,13s7,-7.75 7,-13c0,-3.87 -3.13,-7 -7,-7zM12,11.5c-1.38,0 -2.5,-1.12 -2.5,-2.5s1.12,-2.5 2.5,-2.5 2.5,1.12 2.5,2.5 -1.12,2.5 -2.5,2.5z",
    "ic_delete": "M16,9v10H8V9h8m-1.5,-6h-5l-1,1H5v2h14V4h-3.5l-1,-1zM18,7H6v12c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7z",
    "ic_car": "M18.92,6.01C18.72,5.42 18.16,5 17.5,5h-11C5.84,5 5.28,5.42 5.08,6.01L3,12v8c0,0.55 0.45,1 1,1h1c0.55,0 1,-0.45 1,-1v-1h12v1c0,0.55 0.45,1 1,1h1c0.55,0 1,-0.45 1,-1v-8l-2.08,-5.99zM6.85,7h10.29l1.08,3.11H5.77l1.08,-3.11zM19,17H5v-4h14v4zM7.5,16c-0.83,0 -1.5,-0.67 -1.5,-1.5S6.67,13 7.5,13 9,13.67 9,14.5 8.33,16 7.5,16zM16.5,16c-0.83,0 -1.5,-0.67 -1.5,-1.5s0.67,-1.5 1.5,-1.5 1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5z",
    "ic_history": "M13,3c-4.97,0 -9,4.03 -9,9H1l3.89,3.89 0.07,0.14L9,12H6c0,-3.87 3.13,-7 7,-7s7,3.13 7,7 -3.13,7 -7,7c-1.93,0 -3.68,-0.79 -4.94,-2.06l-1.42,1.42C8.27,19.99 10.51,21 13,21c4.97,0 9,-4.03 9,-9s-4.03,-9 -9,-9zM12,8v5l4.28,2.54 0.72,-1.21 -3.5,-2.08V8H12z",
    "ic_arrow_back": "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z",
    "ic_map": "M20.5,3l-0.16,0.03L15,5.1 9,3 3.36,4.9c-0.21,0.07 -0.36,0.25 -0.36,0.48V20.5c0,0.28 0.22,0.5 0.5,0.5l0.16,-0.03L9,18.9l6,2.1 5.64,-1.9c0.21,-0.07 0.36,-0.25 0.36,-0.48V3.5c0,-0.28 -0.22,-0.5 -0.5,-0.5zM15,19l-6,-2.11V5l6,2.11V19z",
    "ic_share": "M18,16.08c-0.76,0 -1.44,0.3 -1.96,0.77L8.91,12.7c0.05,-0.23 0.09,-0.46 0.09,-0.7s-0.04,-0.47 -0.09,-0.7l7.05,-4.11c0.54,0.5 1.25,0.81 2.04,0.81 1.66,0 3,-1.34 3,-3s-1.34,-3 -3,-3 -3,1.34 -3,3c0,0.24 0.04,0.47 0.09,0.7L8.04,9.81C7.5,9.31 6.79,9 6,9c-1.66,0 -3,1.34 -3,3s1.34,3 3,3c0.79,0 1.5,-0.31 2.04,-0.81l7.12,4.16c-0.05,0.21 -0.08,0.43 -0.08,0.65 0,1.61 1.31,2.92 2.92,2.92s2.92,-1.31 2.92,-2.92c0,-1.61 -1.31,-2.92 -2.92,-2.92z",
    "ic_warning": "M1,21h22L12,2L1,21zM13,18h-2v-2h2v2zM13,14h-2v-4h2v4z",
    "ic_call": "M20.01,15.38c-1.23,0 -2.42,-0.2 -3.53,-0.56 -0.35,-0.12 -0.74,-0.03 -1.01,0.24l-1.57,1.97c-2.83,-1.35 -5.48,-3.9 -6.89,-6.83l1.95,-1.66c0.27,-0.28 0.35,-0.67 0.24,-1.02 -0.37,-1.11 -0.56,-2.3 -0.56,-3.53 0,-0.55 -0.45,-1 -1,-1H4c-0.55,0 -1,0.45 -1,1 0,9.39 7.61,17 17,17 0.55,0 1,-0.45 1,-1v-3.49c0,-0.55 -0.45,-1 -0.99,-1z",
    "ic_email": "M20,4H4c-1.1,0 -1.99,0.9 -1.99,2L2,18c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V6c0,-1.1 -0.9,-2 -2,-2zM20,8l-8,5 -8,-5V6l8,5 8,-5v2z",
    "ic_edit": "M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z",
    "ic_chevron_right": "M10,6L8.59,7.41 13.17,12l-4.58,4.59L10,18l6,-6L10,6z",
    "ic_info": "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-6h2v6zM13,9h-2V7h2v2z",
    "ic_settings": "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94 0,-0.32 -0.02,-0.64 -0.06,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94l-0.36,-2.54c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41l-0.36,2.54c-0.59,0.24 -1.13,0.56 -1.62,0.94l-2.39,-0.96c-0.22,-0.08 -0.47,0 -0.59,0.22l-1.92,3.32c-0.12,0.22 -0.07,0.49 0.12,0.61l2.03,1.58c-0.05,0.3 -0.06,0.61 -0.06,0.94s0.02,0.64 0.06,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.43,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.49 -0.12,-0.61l-2.03,-1.58zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6 3.6,1.62 3.6,3.6 -1.62,3.6 -3.6,3.6z",
    "ic_help": "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,19h-2v-2h2v2zM15.07,11.25l-0.9,0.92C13.45,12.9 13,13.5 13,15h-2v-0.5c0,-1.1 0.45,-2.1 1.17,-2.83l1.24,-1.26c0.37,-0.36 0.59,-0.86 0.59,-1.41 0,-1.1 -0.9,-2 -2,-2s-2,0.9 -2,2H8c0,-2.21 1.79,-4 4,-4s4,1.79 4,4c0,0.88 -0.36,1.68 -0.93,2.25z",
    "ic_lock": "M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM9,6c0,-1.66 1.34,-3 3,-3s3,1.34 3,3v2H9V6zM18,20H6V10h12v10zm-6,-3c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2z",
    "ic_my_location": "M12,8c-2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4 -1.79,-4 -4,-4zM20.94,11c-0.46,-4.17 -3.77,-7.48 -7.94,-7.94V1h-2v2.06C6.83,3.52 3.52,6.83 3.06,11H1v2h2.06c0.46,4.17 3.77,7.48 7.94,7.94V23h2v-2.06c4.17,-0.46 7.48,-3.77 7.94,-7.94H23v-2h-2.06zM12,19c-3.87,0 -7,-3.13 -7,-7s3.13,-7 7,-7 7,3.13 7,7 -3.13,7 -7,7z",
    "ic_search": "M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z",
    "ic_add": "M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z",
    "ic_work": "M20,6h-4V4c0,-1.11 -0.89,-2 -2,-2h-4c-1.11,0 -2,0.89 -2,2v2H4c-1.11,0 -1.99,0.89 -1.99,2L2,19c0,1.11 0.89,2 2,2h16c1.11,0 2,-0.89 2,-2V8c0,-1.11 -0.89,-2 -2,-2zM10,4h4v2h-4V4zM20,19H4V8h16v11z",
    "ic_visibility": "M12,4.5C7,4.5 2.73,7.61 1,12c1.73,4.39 6,7.5 11,7.5s9.27,-3.11 11,-7.5c-1.73,-4.39 -6,-7.5 -11,-7.5zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5zm0,-8c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3 -1.34,-3 -3,-3z",
    "ic_notifications": "M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2zm6,-6v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2zm-2,1H8v-6c0,-2.48 1.51,-4.5 4,-4.5s4,2.02 4,4.5v6z"
}

XML_TEMPLATE = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FF000000"
        android:pathData="{path}" />
</vector>
"""

# Map android drawables to our new ones
MAPPINGS = {
    "@android:drawable/ic_menu_myplaces": "@drawable/ic_location_pin",
    "@android:drawable/ic_menu_delete": "@drawable/ic_delete",
    "@android:drawable/ic_menu_directions": "@drawable/ic_car",
    "@android:drawable/ic_menu_recent_history": "@drawable/ic_history",
    "@android:drawable/ic_menu_revert": "@drawable/ic_arrow_back",
    "@android:drawable/ic_dialog_map": "@drawable/ic_map",
    "@android:drawable/ic_menu_share": "@drawable/ic_share",
    "@android:drawable/ic_dialog_alert": "@drawable/ic_warning",
    "@android:drawable/ic_menu_call": "@drawable/ic_call",
    "@android:drawable/ic_dialog_email": "@drawable/ic_email",
    "@android:drawable/ic_menu_edit": "@drawable/ic_edit",
    "@android:drawable/ic_media_play": "@drawable/ic_chevron_right",
    "@android:drawable/ic_dialog_info": "@drawable/ic_info",
    "@android:drawable/ic_menu_preferences": "@drawable/ic_settings",
    "@android:drawable/ic_menu_help": "@drawable/ic_help",
    "@android:drawable/ic_menu_info_details": "@drawable/ic_info",
    "@android:drawable/ic_lock_idle_lock": "@drawable/ic_lock",
    "@android:drawable/ic_menu_mylocation": "@drawable/ic_my_location",
    "@android:drawable/ic_menu_search": "@drawable/ic_search",
    "@android:drawable/ic_input_add": "@drawable/ic_add",
    "@android:drawable/ic_menu_agenda": "@drawable/ic_work",
    "@android:drawable/ic_menu_view": "@drawable/ic_visibility",
    "@android:drawable/ic_popup_reminder": "@drawable/ic_notifications",
}

# 1. Create the files
if not os.path.exists(DRAWABLES_DIR):
    os.makedirs(DRAWABLES_DIR, exist_ok=True)
    
for name, d in ICONS.items():
    filePath = os.path.join(DRAWABLES_DIR, f"{name}.xml")
    with open(filePath, "w", encoding="utf-8") as file:
        file.write(XML_TEMPLATE.format(path=d))
        
print("Successfully generated all M3 Drawable XML files.")

# 2. Replace in layouts
for root, _, files in os.walk(LAYOUTS_DIR):
    for f in files:
        if f.endswith(".xml"):
            full = os.path.join(root, f)
            with open(full, "r", encoding="utf-8") as r:
                content = r.read()
            original = content
            for old, new in MAPPINGS.items():
                content = content.replace(old, new)
            if content != original:
                with open(full, "w", encoding="utf-8") as w:
                    w.write(content)
                print(f"Updated: {f}")

print("Replacement Complete!")
