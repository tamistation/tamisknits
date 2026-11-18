const {onCall, HttpsError} = require("firebase-functions/v2/https");
const {setGlobalOptions} = require("firebase-functions");
const admin = require("firebase-admin");
const crypto = require("crypto");

admin.initializeApp();

setGlobalOptions({
  maxInstances: 10,
});

const db = admin.firestore();
const auth = admin.auth();

const ADMIN_USER_TYPE_ID = "1dJUrO951d1Ly8QzMnJQ";
const DELIVERY_USER_TYPE_ID = "Bjf9UbBIfFPcsMCpDbvq";

/**
 * Generates a cryptographically secure temporary password.
 * @param {number} length The length of the password.
 * @return {string} The generated temporary password.
 */
function generateTemporaryPassword(length = 12) {
  const characters =
    "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";

  const randomBytes = crypto.randomBytes(length);
  let password = "";

  for (let i = 0; i < length; i++) {
    password += characters.charAt(randomBytes[i] % characters.length);
  }

  return password;
}

/**
 * Creates a Firebase Authentication and Firestore account for a delivery user.
 */
exports.createDeliveryAccount = onCall(async (request) => {
  try {
    // 1. Make sure the caller is logged in
    if (!request.auth) {
      throw new HttpsError(
          "unauthenticated",
          "You must be logged in to create a delivery account.",
      );
    }

    // 2. Check that the caller is an admin
    const adminDoc = await db
        .collection("User")
        .doc(request.auth.uid)
        .get();

    if (!adminDoc.exists) {
      throw new HttpsError(
          "permission-denied",
          "Only admins can create delivery accounts.",
      );
    }

    const adminData = adminDoc.data();

    if (adminData.usertypeId !== ADMIN_USER_TYPE_ID) {
      throw new HttpsError(
          "permission-denied",
          "Only admins can create delivery accounts.",
      );
    }

    // 3. Validate the delivery information
    const {name, email, phone} = request.data;

    if (!name || !email || !phone) {
      throw new HttpsError(
          "invalid-argument",
          "Name, email, and phone are required.",
      );
    }

    // 4. Generate a temporary password on the server
    const temporaryPassword = generateTemporaryPassword();

    // 5. Create Firebase Authentication account
    const userRecord = await auth.createUser({
      email: email,
      password: temporaryPassword,
      displayName: name,
    });

    // 6. Create Firestore User document
    await db.collection("User").doc(userRecord.uid).set({
      uid: userRecord.uid,
      name: name,
      email: email,
      phone: phone,
      usertypeId: DELIVERY_USER_TYPE_ID,
      status: "active",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // 7. Return the temporary password to the admin
    // It is NOT stored in Firestore.
    return {
      success: true,
      uid: userRecord.uid,
      temporaryPassword: temporaryPassword,
    };
  } catch (error) {
    console.error("Error creating delivery account:", error);

    if (error instanceof HttpsError) {
      throw error;
    }

    throw new HttpsError(
        "internal",
        error.message || "Failed to create delivery account.",
    );
  }
});
