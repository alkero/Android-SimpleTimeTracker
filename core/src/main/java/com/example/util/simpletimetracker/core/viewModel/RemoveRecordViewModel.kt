package com.example.util.simpletimetracker.core.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.model.SnackBarMessage
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Record
import kotlinx.coroutines.launch
import javax.inject.Inject


class RemoveRecordViewModel @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor
) : ViewModel() {

    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData()
    val deleteIconVisibility: LiveData<Boolean> = MutableLiveData()
    val message: LiveData<SnackBarMessage?> = MutableLiveData()
    val needUpdate: LiveData<Boolean> = MutableLiveData()

    private var recordId: Long = 0

    fun prepare(id: Long) {
        recordId = id
        (deleteButtonEnabled as MutableLiveData).value = true
        (deleteIconVisibility as MutableLiveData).value = id != 0L
    }

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            if (recordId != 0L) {
                val removedRecord = recordInteractor.get(recordId)
                val removedName = removedRecord?.typeId
                    ?.let { recordTypeInteractor.get(it) }
                    ?.name
                    .orEmpty()

                recordInteractor.remove(recordId)
                (needUpdate as MutableLiveData).value = true

                (message as MutableLiveData).value = SnackBarMessage(
                    message = resourceRepo.getString(R.string.record_removed, removedName),
                    actionText = R.string.record_removed_undo.let(resourceRepo::getString),
                    actionListener = { removedRecord?.let(::onAction) }
                )
            }
        }
    }

    fun onMessageShown() {
        (message as MutableLiveData).value = null
    }

    fun onUpdated() {
        (needUpdate as MutableLiveData).value = false
    }

    private fun onAction(removedRecord: Record) {
        viewModelScope.launch {
            recordInteractor.add(removedRecord)
            (needUpdate as MutableLiveData).value = true
        }
    }
}