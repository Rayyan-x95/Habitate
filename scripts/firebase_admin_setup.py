import firebase_admin
from firebase_admin import credentials, firestore
import os

# Path to serviceAccountKey.json (assuming script is run from project root or scripts folder)
KEY_PATH = "serviceAccountKey.json"
if not os.path.exists(KEY_PATH):
    # Try looking in parent directory if run from scripts/
    KEY_PATH = "../serviceAccountKey.json"

if not os.path.exists(KEY_PATH):
    print(f"Error: Could not find {KEY_PATH}")
    exit(1)

# Initialize Firebase Admin SDK
try:
    cred = credentials.Certificate(KEY_PATH)
    firebase_admin.initialize_app(cred)
    print("✅ Firebase Admin SDK initialized successfully.")
    
    # Example: Access Firestore
    db = firestore.client()
    print("Connected to Firestore.")
    
except Exception as e:
    print(f"❌ Error initializing Firebase Admin SDK: {e}")
