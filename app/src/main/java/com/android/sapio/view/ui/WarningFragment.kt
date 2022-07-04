package com.android.sapio.view.ui
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.sapio.R
import com.android.sapio.databinding.FragmentWarningBinding
import com.android.sapio.model.PhoneApplicationRepository
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNet
import com.nimbusds.jose.JWSObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class WarningFragment : Fragment() {

    private lateinit var mBinding: FragmentWarningBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentWarningBinding.inflate(layoutInflater)

        mBinding.proceedButton.isEnabled = false
        mBinding.proceedButton.setOnClickListener {
            findNavController().navigate(R.id.action_warningFragment_to_chooseAppFragment)
        }

        checkSafetyNet()


        return mBinding.root
    }

    private fun checkSafetyNet() {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext()) == ConnectionResult.SUCCESS) {
            val nonce = System.currentTimeMillis().toString() + "Sapio"
            SafetyNet.getClient(requireContext()).attest(nonce.toByteArray(), "AIzaSyAGXF8P1HbejJ2zthzsi2jd6nv6U86wA4g")
                .addOnSuccessListener {
                    val jws = JWSObject.parse(it.jwsResult)
                    val json = jws.payload.toJSONObject()
                    val ctsProfileMatch = json["ctsProfileMatch"].toString().toBoolean()
                    val basicIntegrity = json["basicIntegrity"].toString().toBoolean()
                    mBinding.proceedButton.isEnabled = !(ctsProfileMatch && basicIntegrity)
                }
                .addOnFailureListener {
                    mBinding.proceedButton.isEnabled = true
                }
        } else {
            mBinding.proceedButton.isEnabled = true
        }
    }
}