package com.example.melody_meter_local.ui

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
import com.example.melody_meter_local.model.User
import com.example.melody_meter_local.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpDialogFragment : DialogFragment() {
    private var _binding: FragmentSignupBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
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

            // TODO: add password validation
            if (confirmPassword == password && username.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) {
                        if (it.isSuccessful) {
                            // get the user id and add user to database
                            val uid = auth.currentUser?.uid
                            val user = User(username = username)
                            databaseReference = FirebaseDatabase.getInstance().getReference("Users")

                            if (uid != null) {
                                databaseReference.child(uid).setValue(user)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Account created successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            loginViewModel.login()
                                            dismiss()  //dismiss the signup modal upon successful registration
                                            //findNavController().navigate(R.id.navigation_edit_profile)
                                        }
                                    }
                            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}