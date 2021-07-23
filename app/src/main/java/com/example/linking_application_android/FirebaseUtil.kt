package com.example.linking_application_android

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class FirebaseUtils {
    val fireStoreDatabase = FirebaseFirestore.getInstance()
}

fun readFBData() {
    val hashMap = hashMapOf<String, Any>(
        "name" to "John doe",
        "city" to "Nairobi",
        "age" to 24 )

    val db = FirebaseUtils().fireStoreDatabase
    val docRef = db.collection("Beacons").document("NA20")
    docRef.get()
        .addOnSuccessListener { document ->
            if (document != null) {
                Log.d("FIREBASE", "DocumentSnapshot data: ${document.data}")
            } else {
                Log.d("FIREBASE", "No such document")
            }
        }
        .addOnFailureListener { exception ->
            Log.d("FIREBASE", "get failed with ", exception)
        }
}