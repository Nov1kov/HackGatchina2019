package ru.tudimsudim.hackgatchina

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_launcher.*


class Launcher : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var mGoogleSignInClient: GoogleSignInClient? = null

    private fun signIn() {
        loading.visibility = View.VISIBLE
        val signInIntent = mGoogleSignInClient?.getSignInIntent()
        startActivityForResult(signInIntent, 201)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 201) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("Google sign in failed", e)
                // ...
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        if (auth.currentUser != null)
            updateUiWithUser(auth.currentUser)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    updateUiWithUser(auth.currentUser)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        GoogleSignIn.getLastSignedInAccount(this)
        sign_in_button.setOnClickListener {
            signIn()
        }
    }


    private fun updateUiWithUser(model: FirebaseUser?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        with (sharedPref.edit()) {
            putString("email", model?.email)
            apply()
        }
        with (sharedPref.edit()) {
            putString("name", model?.displayName)
            apply()
        }
        with (sharedPref.edit()) {
            putString("uid", model?.uid)
            apply()
        }

        Toast.makeText(this, "Добро пожаловать",Toast.LENGTH_LONG).show()
        startActivity(
            Intent(this, NearestIssuesActivity::class.java)
        )
        finish()
    }

}
