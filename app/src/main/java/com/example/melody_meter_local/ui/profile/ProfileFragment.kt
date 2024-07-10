package com.example.melody_meter_local.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.melody_meter_local.databinding.FragmentProfileBinding
import com.example.melody_meter_local.ui.login.LoginDialogFragment
import com.example.melody_meter_local.ui.login.LoginViewModel
import com.google.firebase.database.DatabaseReference

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()

    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginViewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                // Update the profile view to show logged-in state
                binding.loggedInGroup.visibility = View.VISIBLE
                binding.notLoggedInGroup.visibility = View.GONE
                binding.textViewLogout.setOnClickListener {
                    loginViewModel.logout()
                }
            } else {
                // Show login prompt
                binding.loggedInGroup.visibility = View.GONE
                binding.notLoggedInGroup.visibility = View.VISIBLE
                binding.btnLogin.setOnClickListener {
                    val loginDialogFragment = LoginDialogFragment()
                    loginDialogFragment.show(parentFragmentManager, "loginDialogFragment")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}