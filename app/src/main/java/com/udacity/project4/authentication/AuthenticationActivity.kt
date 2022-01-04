package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private val viewModel by viewModels<AuthenticationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                // DONE: If the user was authenticated, send him to RemindersActivity
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    startRemindersActivity()
                }
                else -> {
                    buttonLogin.setOnClickListener {
                        launchSignInFlow()
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val toastText = "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT).show()
                startRemindersActivity()

            } else {
                val toastText = "Sign in unsuccessful"
                Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // DONE: Implement the create account and sign in using FirebaseUI,
    // use sign in using email and sign in using Google
    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(providers)
                // DONE: A bonus is to customize the sign in flow to look nice using :
                // https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
                .setLogo(R.drawable.map)
                .build(),
            SIGN_IN_RESULT_CODE)
    }

    private fun startRemindersActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
    }
}
