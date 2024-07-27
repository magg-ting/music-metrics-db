package com.example.melody_meter_local.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

import com.example.melody_meter_local.databinding.FragmentLoginBinding
import com.example.melody_meter_local.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginDialogFragment : DialogFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogin = binding.btnLogin
        val edtxtEmail = binding.email
        val edtxtPassword = binding.password
        val btnSignup = binding.signUpPrompt

        btnLogin.isEnabled = false

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = edtxtEmail.text.toString().trim()
                val password = edtxtPassword.text.toString().trim()
                btnLogin.isEnabled = email.isNotEmpty() && password.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        edtxtEmail.addTextChangedListener(textWatcher)
        edtxtPassword.addTextChangedListener(textWatcher)

        btnLogin.setOnClickListener {
            val email = edtxtEmail.text.toString().trim()
            val password = edtxtPassword.text.toString().trim()
            Log.d("LoginDialog", email)
            loginViewModel.login(email, password)
        }

        btnSignup.setOnClickListener {
            dismiss()  //dismiss the login modal
            val signUpDialogFragment = SignUpDialogFragment()
            signUpDialogFragment.show(parentFragmentManager, "signUpDialogFragment")
        }

        // Observe login success status
        loginViewModel.loginStatus.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {isSuccess ->
                    if (isSuccess) {
                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        // Handle unexpected false result (if used)
                    }
                },
                onFailure = {
                    // Only show the failure toast if login was attempted and failed
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

}