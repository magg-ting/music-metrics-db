package com.example.melody_meter_local.ui.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.melody_meter_local.databinding.FragmentSignupBinding
import com.example.melody_meter_local.ui.login.LoginDialogFragment
import com.example.melody_meter_local.ui.login.LoginViewModel
import com.google.firebase.auth.FirebaseAuth

class SignUpDialogFragment : DialogFragment() {
    private var _binding: FragmentSignupBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
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

        auth = FirebaseAuth.getInstance()

        val btnSignup = binding.btnSignup
        val edtxtEmail = binding.email
        val edtxtPassword = binding.password
        val edtxtConfirmPassword = binding.confirmPassword
        val btnLogin = binding.loginPrompt

        btnSignup.isEnabled = false

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = edtxtEmail.text.toString().trim()
                val password = edtxtPassword.text.toString().trim()
                val confirmPassword = edtxtConfirmPassword.text.toString().trim()
                btnSignup.isEnabled =
                    email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        edtxtEmail.addTextChangedListener(textWatcher)
        edtxtPassword.addTextChangedListener(textWatcher)
        edtxtConfirmPassword.addTextChangedListener(textWatcher)

        btnSignup.setOnClickListener {
            val email = edtxtEmail.text.toString().trim()
            val password = edtxtPassword.text.toString().trim()
            val confirmPassword = edtxtConfirmPassword.text.toString().trim()

            if (confirmPassword == password) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) {
                        if (it.isSuccessful) {
                            loginViewModel.login()
                            Toast.makeText(
                                context,
                                "Account created successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            dismiss()  //dismiss the signup modal upon successful registration
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            }

        }

        btnLogin.setOnClickListener {
            dismiss() //dismiss the signup modal
            val loginDialogFragment = LoginDialogFragment()
            loginDialogFragment.show(parentFragmentManager, "loginDialogFragment")
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