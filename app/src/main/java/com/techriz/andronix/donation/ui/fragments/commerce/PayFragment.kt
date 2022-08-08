package com.techriz.andronix.donation.ui.fragments.commerce

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.SkuDetails
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment
import com.techriz.andronix.donation.R
import com.techriz.andronix.donation.databinding.LoginFirstSheetBinding
import com.techriz.andronix.donation.databinding.PayFragmentBinding
import com.techriz.andronix.donation.databinding.PurchaseFailedDialogBinding
import com.techriz.andronix.donation.databinding.PurchaseSuccessDialogBinding
import com.techriz.andronix.donation.repository.BillingRepository
import com.techriz.andronix.donation.repository.PurchaseStateClass
import com.techriz.andronix.donation.repository.billingLog
import com.techriz.andronix.donation.ui.fragments.Loader
import com.techriz.andronix.donation.utils.*
import com.techriz.andronix.donation.utils.ActionUtils.showSnackbar
import com.techriz.andronix.donation.utils.Constants.ANDRONIX_SUPPORT_EMAIL
import com.techriz.andronix.donation.utils.NavigationAnimations.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion

import javax.inject.Inject

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PayFragment : Fragment(R.layout.pay_fragment) {
    private lateinit var binding: PayFragmentBinding

    private val viewModel: PayViewModel by viewModels()
    private lateinit var billingJob: Job


    @Inject
    lateinit var billingRepository: BillingRepository

    @Inject
    lateinit var loader: Loader


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val viewModel = viewModel
        binding = PayFragmentBinding.bind(view)

        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val sku = arguments?.getString("sku")
        if (sku != null) {
            when (sku) {
                SkuInfo.PRIMUS().sku_id -> {

                    SkuInfo.PRIMUS().apply {
                        binding.buyTitle.text = title
                        binding.buyDesc.text = desc
                    }
                }
                SkuInfo.BLAZE().sku_id -> {
                    SkuInfo.BLAZE().apply {
                        binding.buyTitle.text = title
                        binding.buyDesc.text = desc
                    }
                }
                SkuInfo.WARRIOR().sku_id -> {
                    SkuInfo.WARRIOR().apply {
                        binding.buyTitle.text = title
                        binding.buyDesc.text = desc
                    }
                }
                SkuInfo.SAVIOR().sku_id -> {
                    SkuInfo.SAVIOR().apply {
                        binding.buyTitle.text = title
                        binding.buyDesc.text = desc
                    }
                }
            }

            viewModel.selectedSku = sku


            billingJob = lifecycleScope.launch(Dispatchers.IO) {
                initBilling()
            }

            binding.buyButton.setOnClickListener {
                if (viewModel.isSkuDetailInit()) {
                    viewModel.getFlowDetails().apply {
                        if (skuDetails != null) {
                            loader.startLoader()
                            billingRepository.launchBillingFlow(
                                requireActivity(),
                                skuDetails!!,
                                uid,
                                email
                            )
                        }
                    }
                }
            }
        }

    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private suspend fun initBilling() {
        viewModel.initBilling()
            .onCompletion { viewModel.resetBillingState() }
            .collect {
                when (it) {
                    is PurchaseStateClass.Ready -> {
                        "Billing client ready".billingLog()
                        billingRepository.getSkuDetails()
                    }
                    is PurchaseStateClass.ServerStatusOK -> {
                        "Andronix Server Status OK".billingLog()
                        withContext(Dispatchers.Main) {
                            binding.payProgress.makeGone()
                            binding.buyButton.makeVisible()
                            binding.warningServerErrorText.makeGone()
                        }
                    }
                    is PurchaseStateClass.ServerStatusFailed -> {
                        "Andronix Server Status Failed".billingLog()
                        withContext(Dispatchers.Main) {
                            binding.payProgress.makeGone()
                            binding.buyButton.makeGone()
                            binding.warningServerErrorText.makeVisible()
                        }
                    }
                    is PurchaseStateClass.SkuDetails -> {
                        "SKU Details OK".billingLog()
                        val details = billingRepository.getSingleSkuDetails(
                            it.skuDetailsList,
                            viewModel.selectedSku
                        )
                        println(details)
                        if (details != null) {
                            withContext(Dispatchers.Main) {
                                viewModel.skuDetails = details
                                binding.buyButtonTextview.text = "Donate ${details.price}"
                                binding.buyButton.isClickable = true
                            }
                        } else {
                            "Details null".billingLog()
                        }
                    }
                    is PurchaseStateClass.Consumed -> {
                        "Purchase consumed".billingLog()
                    }
                    is PurchaseStateClass.Completed -> {
                        "Purchase completed".billingLog()
                        withContext(Dispatchers.Main) {
                            loader.stopLoading()
                            showPostPurchaseDialog(true)
                        }
                    }
                    is PurchaseStateClass.Delayed -> {
                        withContext(Dispatchers.Main) {
                            loader.setDespText("We are having a delayed response while charging you. This might take a few minutes; please don't close or leave the app.")
                        }
                    }
                    is PurchaseStateClass.Disconnected -> {
                        "Billing client disconnected".billingLog()
                    }
                    is PurchaseStateClass.Cancelled -> {
                        loader.stopLoading()
                        "Purchase was cancelled".showSnackbar(binding.root, false)
                    }
                    is PurchaseStateClass.Error -> {
                        withContext(Dispatchers.Main) {
                            loader.stopLoading()
                            println(
                                "Billing Error ${it.message}"
                            )
                            "Billing error ${it.message}".billingLog(true)
                            showPostPurchaseDialog(false, it.message)
                        }
                    }
                    else -> Unit
                }
            }
    }

    private fun showPostPurchaseDialog(isSuccessful: Boolean, error: String = "") {
        if (isSuccessful) {
            val binding = PurchaseSuccessDialogBinding.inflate(layoutInflater)
            val dialog = RoundedBottomSheetDialog(requireContext())
            dialog.setContentView(binding.root)
            binding.close.setOnClickListener {
                dialog.dismiss()
            }
            binding.successSupportText.text =
                viewModel.getSupportSpannableTextSuccess(requireContext().getString(R.string.purchase_successful)) {
                    ActionUtils.launchSendEmailIntent(
                        "Purchase Support",
                        "",
                        requireContext(),
                        arrayOf(ANDRONIX_SUPPORT_EMAIL)
                    )
                }
            binding.close.setOnClickListener { dialog.dismiss() }
            dialog.show()
        } else {
            val binding = PurchaseFailedDialogBinding.inflate(layoutInflater)
            val dialog = RoundedBottomSheetDialog(requireContext())
            dialog.setContentView(binding.root)
            binding.close.setOnClickListener {
                dialog.dismiss()
            }
            binding.failedSupportText.text =
                viewModel.getSupportSpannableTextFailed(requireContext().getString(R.string.payment_failed)) {
                    ActionUtils.launchSendEmailIntent(
                        "Purchase Support",
                        "Error - $error \n\n Please add your message below this. \n\n",
                        requireContext(),
                        arrayOf(ANDRONIX_SUPPORT_EMAIL)
                    )
                }
            binding.close.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }


    override fun onStop() {
        super.onStop()
        if (this::billingJob.isInitialized) {
            billingJob.cancel()
        }
    }
}