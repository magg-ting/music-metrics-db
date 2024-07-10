package com.example.melody_meter_local.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

import com.example.melody_meter_local.databinding.FragmentLoginBinding
import com.example.melody_meter_local.ui.signup.SignUpDialogFragment
import com.google.firebase.auth.FirebaseAuth

class LoginDialogFragment : DialogFragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

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

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) {
                    if (it.isSuccessful) {
                        loginViewModel.login()
                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                        dismiss()  //dismiss the login modal upon successful sign in
                    }
                }
                .addOnFailureListener{
                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        }

        btnSignup.setOnClickListener {
            dismiss()  //dismiss the login modal
            val signUpDialogFragment = SignUpDialogFragment()
            signUpDialogFragment.show(parentFragmentManager, "signUpDialogFragment")
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
}