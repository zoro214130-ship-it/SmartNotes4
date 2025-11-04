package com.smartnotes.ui.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.smartnotes.R
import com.smartnotes.databinding.ActivityRegisterBinding
import com.smartnotes.firebase.FirebaseManager
import com.smartnotes.utils.smoothTransition
import com.smartnotes.utils.showSnack

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.root.startAnimation(fadeIn)

        binding.progressBar.visibility = View.GONE

        binding.btnRegister.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()
            val confirm = binding.editPasswordConfirm.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                showSnack(binding.root, "Please fill in all fields")
                return@setOnClickListener
            }
            if (password != confirm) {
                showSnack(binding.root, "Passwords do not match")
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            FirebaseManager.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    binding.progressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        val user = FirebaseManager.auth.currentUser
                        user?.let {
                            FirebaseManager.saveUserData(it.uid, name, email)
                        }
                        showSnack(binding.root, "Account created successfully!")
                        startActivity(Intent(this, LoginActivity::class.java))
                        smoothTransition()
                        finish()
                    } else {
                        showSnack(binding.root, "Error: ${task.exception?.message}")
                    }
                }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            smoothTransition()
        }
    }
}
