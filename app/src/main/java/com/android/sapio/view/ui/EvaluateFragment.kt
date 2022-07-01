package com.android.sapio.view.ui

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.sapio.R
import com.android.sapio.databinding.FragmentEvaluateBinding
import com.android.sapio.model.Application
import com.android.sapio.model.PhoneApplicationRepository
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class EvaluateFragment : Fragment() {

    companion object {
        const val HAS_MICRO_G = 1;
        const val NO_MICRO_G = 2;
        const val MICRO_G_APP_LABEL = "microG Services Core"
    }

    @Inject lateinit var mPhoneApplicationRepository: PhoneApplicationRepository
    private lateinit var mBinding: FragmentEvaluateBinding
    private lateinit var mPackageName: String
    private var mIsMicroGInstalled by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentEvaluateBinding.inflate(layoutInflater)

        mIsMicroGInstalled = isMicroGInstalled()

        mBinding.emoji.text = "\u26A0\uFE0F"
        if (!isRooted()) {
            mBinding.rootBeerWarning.visibility = View.INVISIBLE
            mBinding.emoji.visibility = View.INVISIBLE
        }

        mPackageName = arguments?.getString("package")!!

        mBinding.validateButton.setOnClickListener { onValidateClicked() }
        mBinding.backButton.setOnClickListener { findNavController().navigate(R.id.action_evaluateFragment_to_chooseAppFragment) }
        return mBinding.root
    }

    private fun onValidateClicked() {
        runBlocking {

            if (mBinding.note.checkedRadioButtonId == -1) {
                Toast.makeText(context, "Please select an evaluation.", Toast.LENGTH_SHORT).show()
                return@runBlocking
            }

            val app = mPhoneApplicationRepository.getApplicationFromPackageName(requireContext(), mPackageName)
            evaluateApp(app!!, requireView())
            findNavController().navigate(R.id.action_evaluateFragment_to_successFragment)
        }
    }

    private suspend fun evaluateApp(app: Application, view: View) {
        val parseApp = ParseObject("LibreApps")
        val existingEvaluation = fetchExistingEvaluation(app)
        if (existingEvaluation != null) {
            parseApp.objectId = existingEvaluation.objectId
        }

        val parseFile = ParseFile("icon.png", app.icon?.let { fromDrawableToByArray(it) })
        parseFile.saveInBackground()

        parseApp.put("name", app.name)
        parseApp.put("package", app.packageName)
        parseApp.put("icon", parseFile)

        val rate = getRateFromId(mBinding.note.checkedRadioButtonId, view)
        parseApp.put("rating", rate)

        parseApp.put("microg", mIsMicroGInstalled)

        if (existingEvaluation == null) {
            parseApp.saveInBackground()
        } else if (existingEvaluation.getInt("rating") != rate) {
            parseApp.saveInBackground()
        }
    }

    private fun isMicroGInstalled() : Int {
        val packageManager = requireContext().packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in apps) {
            if (app.packageName == "com.google.android.gms" &&
                packageManager.getApplicationLabel(app).toString() == MICRO_G_APP_LABEL
            ) {
                return HAS_MICRO_G
            }
        }

        return NO_MICRO_G
    }

    private suspend fun fetchExistingEvaluation(app: Application) : ParseObject? {
        return withContext(Dispatchers.IO) {
            val query = ParseQuery.getQuery<ParseObject>("LibreApps")
            query.whereEqualTo("package", app.packageName)
            query.whereEqualTo("microg", mIsMicroGInstalled)
            val answers = query.find()
            if (answers.size == 1) {
                return@withContext answers[0]
            } else {
                return@withContext null
            }
        }
    }

    private fun fromDrawableToByArray(drawable: Drawable) : ByteArray {
        val bitmap = drawable.toBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun getRateFromId(id: Int, view: View) : Int {
        val radioButton: RadioButton = view.findViewById(id)
        return when(radioButton.text) {
            getString(R.string.works_perfectly) -> 1
            getString(R.string.works_partially) -> 2
            getString(R.string.dont_work) -> 3
            else -> 0
        }
    }

    private fun isRooted() = RootBeer(context).isRooted
}
