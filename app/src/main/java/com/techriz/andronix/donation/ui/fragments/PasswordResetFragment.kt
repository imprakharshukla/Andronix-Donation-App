package studio.com.techriz.andronix.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment
import com.techriz.andronix.donation.R
import com.techriz.andronix.donation.databinding.FragmentPasswordResetBinding
import com.techriz.andronix.donation.repository.AuthRepository
import com.techriz.andronix.donation.repository.SuccessFail
import com.techriz.andronix.donation.utils.AuthVerificationUtils
import com.techriz.andronix.donation.utils.makeGone
import com.techriz.andronix.donation.utils.makeVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PasswordResetFragment @Inject constructor(
    private val authRepository: AuthRepository
) : RoundedBottomSheetDialogFragment() {

    lateinit var binding: FragmentPasswordResetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_password_reset, container, false)

        binding = FragmentPasswordResetBinding.bind(view)

        /*  binding.forgotPasswordCloseButton.setOnClickListener {
              requireActivity().onBackPressed()
          }*/

        binding.forgotPasswordConfirmButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            if (AuthVerificationUtils.isEmailValid(email)) {
                binding.forgotPasswordInputLayout.makeGone()
                binding.forgotPasswordProgressBar.makeVisible()
                authRepository.sendForgotPasswordEmail(email)
                lifecycleScope.launch(Dispatchers.IO) {
                    authRepository.passwordResetState.collect {
                        when (it) {
                            is SuccessFail.Success -> {
                                withContext(Dispatchers.Main) {
                                    binding.forgotPasswordProgressBar.makeGone()
                                    binding.forgotPasswordSuccessLayout.makeVisible()
                                }
                            }
                            is SuccessFail.Error -> {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Oops! ${it.error}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    requireActivity().onBackPressed()
                                }
                            }
                            else -> {
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Invalid Email. Please input a valid email.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        return view
    }
}