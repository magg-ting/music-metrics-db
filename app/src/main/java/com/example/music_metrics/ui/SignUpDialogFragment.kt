package com.example.music_metrics.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.music_metrics.R
import com.example.music_metrics.databinding.FragmentSignupBinding
import com.example.music_metrics.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpDialogFragment : DialogFragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSignup = binding.btnSignup
        val edtxtUsername = binding.username
        val edtxtEmail = binding.email
        val edtxtPassword = binding.password
        val edtxtConfirmPassword = binding.confirmPassword
        val btnLogin = binding.loginPrompt

        btnSignup.isEnabled = false

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val username = edtxtUsername.text.toString().trim()
                val email = edtxtEmail.text.toString().trim()
                val password = edtxtPassword.text.toString().trim()
                val confirmPassword = edtxtConfirmPassword.text.toString().trim()
                btnSignup.isEnabled =
                    username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        edtxtEmail.addTextChangedListener(textWatcher)
        edtxtPassword.addTextChangedListener(textWatcher)
        edtxtConfirmPassword.addTextChangedListener(textWatcher)

        btnSignup.setOnClickListener {
            val username = edtxtUsername.text.toString().trim()
            val email = edtxtEmail.text.toString().trim()
            val password = edtxtPassword.text.toString().trim()
            val confirmPassword = edtxtConfirmPassword.text.toString().trim()

            when {
                username.contains(Regex("\\s")) -> {
                    Toast.makeText(context, R.string.invalid_username, Toast.LENGTH_SHORT).show()
                }

                confirmPassword != password -> {
                    Toast.makeText(context, R.string.unmatched_passwords, Toast.LENGTH_SHORT).show()
                }

                isPasswordValid(password) -> {
                    loginViewModel.signup(username, email, password)
                }

                else -> {
                    Toast.makeText(context, R.string.invalid_password, Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnLogin.setOnClickListener {
            dismiss() //dismiss the signup modal
            val loginDialogFragment = LoginDialogFragment()
            loginDialogFragment.show(parentFragmentManager, "loginDialogFragment")
        }

        // Observe sign-up status
        loginViewModel.signUpStatus.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {isSuccess ->
                    if (isSuccess) {
                        Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show()
                        dismiss()  //dismiss the signup dialog upon successful sign in
                    } else {
                        // Handle unexpected false result (if used)
                    }
                },
                onFailure = {
                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        // Set the width of the dialog to 80% of the screen width
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isPasswordValid(password: String): Boolean {
        val passwordPattern =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,12}$"
        return password.matches(passwordPattern.toRegex())
    }

}