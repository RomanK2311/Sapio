package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.klee.sapio.databinding.FragmentMainBinding
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.ui.viewmodel.FeedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private lateinit var mBinding: FragmentMainBinding
    private lateinit var mFeedAppAdapter: FeedAppAdapter
    private val mViewModel by viewModels<FeedViewModel>()

    @Inject
    lateinit var mEvaluationRepository: EvaluationRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMainBinding.inflate(layoutInflater)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(context)

        val coroutineScope = viewLifecycleOwner.lifecycleScope
        fetchFeed(coroutineScope)

        mBinding.refreshView.setOnRefreshListener {
            fetchFeed(coroutineScope)
        }

        return mBinding.root
    }

    private fun fetchFeed(coroutineScope: CoroutineScope) {
        mBinding.refreshView.isRefreshing
        coroutineScope.launch {
            collectFeed()
        }
    }

    private suspend fun collectFeed() {
        mViewModel.evaluations.collect { list ->
            if (list.isEmpty()) {
                ToastMessage.showNetworkIssue(requireContext())
            }

            mFeedAppAdapter = FeedAppAdapter(
                requireContext(),
                list,
                mEvaluationRepository
            )

            mBinding.recyclerView.adapter = mFeedAppAdapter
            mBinding.refreshView.isRefreshing = false
        }
    }
}
