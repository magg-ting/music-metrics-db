package com.example.music_metrics.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.music_metrics.R
import com.example.music_metrics.databinding.FragmentChangePasswordBinding
import com.example.music_metrics.di.UserDatabaseReference
import com.example.music_metrics.viewmodel.LoginViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()

    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    @UserDatabaseReference private lateinit var userDbReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = auth.currentUser

        // disable the save button by default
        binding.btnConfirm.isEnabled = false

        // get inputs from user
        val editTextCurrentPassword = binding.currentPassword
        val editTextNewPassword = binding.newPassword
        val editTextConfirmNewPassword = binding.confirmNewPassword

        // Add TextWatchers to enable the button when all fields are filled
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // get inputs from user
                val currentPassword = editTextCurrentPassword.text.toString()
                val newPassword = editTextNewPassword.text.toString()
                val confirmNewPassword = editTextConfirmNewPassword.text.toString()
                binding.btnConfirm.isEnabled =
                    currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        editTextCurrentPassword.addTextChangedListener(textWatcher)
        editTextNewPassword.addTextChangedListener(textWatcher)
        editTextConfirmNewPassword.addTextChangedListener(textWatcher)

        binding.btnConfirm.setOnClickListener {
            changePassword()
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun changePassword() {
        val user = auth.currentUser
        val currentPassword = binding.currentPassword.text.toString()
        val newPassword = binding.newPassword.text.toString()
        val confirmNewPassword = binding.confirmNewPassword.text.toString()

        if (newPassword != confirmNewPassword) {
            Toast.makeText(requireContext(), R.string.unmatched_passwords, Toast.LENGTH_SHORT).show()
            return
        }
        else if(!isPasswordValid(newPassword)){
            Toast.makeText(requireContext(), R.string.invalid_password, Toast.LENGTH_SHORT).show()
            return
        }
        // re-authenticate user and update password, ref https://stackoverflow.com/a/40904516
        else if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.password_changed,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.currentPassword.setText("")
                                    binding.newPassword.setText("")
                                    binding.confirmNewPassword.setText("")
                                    // navigate back to the profile page after changing the password
                                    findNavController().navigateUp()
                                }
                                else {
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.password_unchanged,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.e(
                                        "Change Password Fragment",
                                        "Failed to update password: ${updateTask.exception?.message}"
                                    )
                                }
                            }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.authentication_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(
                            "Change Password Fragment",
                            "Authentication failed: ${it.exception?.message}"
                        )
                    }
                }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,12}$"
        return password.matches(passwordPattern.toRegex())
    }
}