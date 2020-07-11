package com.example.util.simpletimetracker

import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class ChangeRunningRecordTest : BaseUiTest() {

    @Test
    fun test() {
        val name = "Test1"
        val newName = "Test2"
        val firstColor = ColorMapper.availableColors.first()
        val lastColor = ColorMapper.availableColors.last()
        val firstIcon = iconMapper.availableIconsNames.values.first()
        val lastIcon = iconMapper.availableIconsNames.values.last()

        // Add activities
        NavUtils.addActivity(name, firstColor, firstIcon)
        NavUtils.addActivity(newName, lastColor, lastIcon)

        val currentTime = System.currentTimeMillis()
        var timeStartedTimestamp = currentTime
        var timeStarted = timeMapper.formatDateTime(timeStartedTimestamp)
        var timeStartedPreview = timeStartedTimestamp
            .let(timeMapper::formatTime)

        // Start timer
        clickOnViewWithText(name)
        checkRunningRecordDisplayed(name, firstColor, firstIcon, timeStartedPreview)

        // Open edit view
        longClickOnView(allOf(isDescendantOfA(withId(R.id.layoutRunningRecordItem)), withText(name)))

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeRunningRecordDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRunningRecordType))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRunningRecordTimeStarted), withText(timeStarted)))

        // Preview is updated
        checkPreviewUpdated(withText(name))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(withTag(firstIcon))
        checkPreviewUpdated(withText(timeStartedPreview))

        // Change item
        clickOnViewWithText(R.string.change_running_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRunningRecordType, withText(newName))

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }
        val hourStarted = 0
        val minutesStarted = 0
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        clickOnViewWithId(R.id.tvChangeRunningRecordTimeStarted)
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(hourStarted, minutesStarted))
        clickOnViewWithText(R.string.date_time_dialog_date)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(year, month + 1, day))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        timeStartedTimestamp = Calendar.getInstance().run {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hourStarted)
            set(Calendar.MINUTE, minutesStarted)
            timeInMillis
        }
        timeStarted = timeStartedTimestamp.let(timeMapper::formatDateTime)
        timeStartedPreview = timeStartedTimestamp
            .let(timeMapper::formatTime)

        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRunningRecordTimeStarted), withText(timeStarted)))

        // Preview is updated
        checkPreviewUpdated(withText(newName))
        checkPreviewUpdated(withCardColor(lastColor))
        checkPreviewUpdated(withTag(lastIcon))
        checkPreviewUpdated(withText(timeStartedPreview))

        // Save
        clickOnViewWithText(R.string.change_running_record_save)

        // Record updated
        checkViewDoesNotExist(allOf(isDescendantOfA(withId(R.id.layoutRunningRecordItem)), withText(name)))
        checkRunningRecordDisplayed(newName, lastColor, lastIcon, timeStartedPreview)
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(
            allOf(isDescendantOfA(withId(R.id.previewChangeRunningRecord)), matcher)
        )

    private fun checkRunningRecordDisplayed(name: String, color: Int, icon: Int, timeStarted: String) {
        checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name)))
        checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withCardColor(color)))
        checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withTag(icon)))
        checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(timeStarted)))
    }
}
