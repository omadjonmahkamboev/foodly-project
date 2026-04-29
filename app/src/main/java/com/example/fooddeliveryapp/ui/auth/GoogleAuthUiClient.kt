package com.example.fooddeliveryapp.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.fooddeliveryapp.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.SecureRandom
import org.json.JSONObject

data class GoogleAccountProfile(
    val name: String,
    val email: String,
    val subject: String?,
    val photoUrl: String?,
)

sealed interface GoogleAuthUiResult {
    data class Success(val profile: GoogleAccountProfile) : GoogleAuthUiResult
    data class Error(val message: String) : GoogleAuthUiResult
}

class GoogleAuthUiClient(
    private val context: Context,
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(): GoogleAuthUiResult {
        val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (clientId.isBlank()) {
            return GoogleAuthUiResult.Error(
                "Add GOOGLE_WEB_CLIENT_ID to local.properties to enable Google sign-in.",
            )
        }

        val googleOption = GetSignInWithGoogleOption.Builder(clientId)
            .setNonce(generateSecureRandomNonce())
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        return try {
            val response = credentialManager.getCredential(
                context = context.findActivity() ?: context,
                request = request,
            )
            parseGoogleCredential(response)
        } catch (_: NoCredentialException) {
            GoogleAuthUiResult.Error("No Google account was selected.")
        } catch (exception: GetCredentialException) {
            GoogleAuthUiResult.Error(exception.message ?: "Google sign-in failed.")
        } catch (exception: Exception) {
            GoogleAuthUiResult.Error(exception.message ?: "Google sign-in failed.")
        }
    }

    private fun parseGoogleCredential(response: GetCredentialResponse): GoogleAuthUiResult {
        val credential = response.credential
        if (
            credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleAuthUiResult.Error("Unexpected Google credential response.")
        }

        return try {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val email = jwtClaim(googleCredential.idToken, "email") ?: googleCredential.id
            val subject = jwtClaim(googleCredential.idToken, "sub")
            GoogleAuthUiResult.Success(
                GoogleAccountProfile(
                    name = googleCredential.displayName.orEmpty(),
                    email = email,
                    subject = subject,
                    photoUrl = googleCredential.profilePictureUri?.toString(),
                ),
            )
        } catch (_: GoogleIdTokenParsingException) {
            GoogleAuthUiResult.Error("Could not parse Google account data.")
        }
    }
}

private fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom().nextBytes(randomBytes)
    return Base64.encodeToString(
        randomBytes,
        Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING,
    )
}

private fun jwtClaim(idToken: String, claimName: String): String? =
    runCatching {
        val payload = idToken.split(".").getOrNull(1).orEmpty()
        if (payload.isBlank()) return@runCatching null
        val bytes = Base64.decode(
            payload,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
        )
        JSONObject(String(bytes, Charsets.UTF_8))
            .optString(claimName)
            .takeIf { it.isNotBlank() }
    }.getOrNull()

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
