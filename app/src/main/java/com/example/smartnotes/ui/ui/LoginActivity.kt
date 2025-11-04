package com.smartnotes.ui.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.smartnotes.R
import com.smartnotes.databinding.ActivityLoginBinding
import com.smartnotes.firebase.FirebaseManager
import com.smartnotes.utils.smoothTransition
import com.smartnotes.utils.showSnack

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✨ Fade-in animation for card (if exists)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.root.startAnimation(fadeIn)

        binding.progressBar.visibility = View.GONE

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showSnack(binding.root, "Please enter both email and password")
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            FirebaseManager.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    binding.progressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        val uid = FirebaseManager.auth.currentUser?.uid
                        if (uid != null) {
                            FirebaseManager.db.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->
                                    val userName = document.getString("name") ?: "User"
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.putExtra("user_name", userName)
                                    startActivity(intent)
                                    smoothTransition()
                                    finish()
                                }
                        }
                    } else {
                        showSnack(binding.root, "Login failed: ${task.exception?.message}")
                    }
                }
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            smoothTransition()
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                showSnack(binding.root, "Enter your email to reset password")
            } else {
                FirebaseManager.auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        showSnack(binding.root, "Password reset email sent!")
                    }
                    .addOnFailureListener {
                        showSnack(binding.root, "Error: ${it.message}")
                    }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseManager.auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            smoothTransition()
            finish()
        }
    }
}
