package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
class SaveReminderViewModelTest {

    //DONE: Provide testing to the SaveReminderView and its live data objects
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext() as Application,
            fakeDataSource
        )
    }

    @After
    fun cleanupDataSource() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.deleteAllReminders()
        stopKoin()
    }

    @Test
    fun clearData_nullAllVariables() = mainCoroutineRule.runBlockingTest {

        // Given that the view model has remainder data
        saveReminderViewModel.reminderTitle.value = "title"
        saveReminderViewModel.reminderDescription.value = "description"
        saveReminderViewModel.reminderSelectedLocationStr.value = "location"
        saveReminderViewModel.latitude.value = 10.0
        saveReminderViewModel.longitude.value = 10.0
        saveReminderViewModel.selectedPOI.value = PointOfInterest(LatLng(10.0, 10.0), "", "")

        // When the model is cleared
        saveReminderViewModel.onClear()

        // Then all the variables of the ViewModel are null
        assertEquals(null, saveReminderViewModel.reminderTitle.getOrAwaitValue())
        assertEquals(null, saveReminderViewModel.reminderDescription.getOrAwaitValue())
        assertEquals(null, saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue())
        assertEquals(null, saveReminderViewModel.selectedPOI.getOrAwaitValue())
        assertEquals(null, saveReminderViewModel.latitude.getOrAwaitValue())
        assertEquals(null, saveReminderViewModel.longitude.getOrAwaitValue())
    }

    @Test
    fun validateEnteredData_validReminderData_returnsTrue() {
        // Given a valid reminder
        val reminderDataItem = ReminderDataItem(
            "Reminder title",
            "Reminder description",
            "Googleplex",
            37.422160,
            -122.084270
        )

        // When the validate function is called
        val isValidReminder = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Then it returns true
        assertEquals(true, isValidReminder)
    }

    @Test
    fun validateEnteredData_noTitleReminder_returnsFalse() {
        // Given a reminder without title
        val reminderDataItem = ReminderDataItem(
            "",
            "Reminder description",
            "Googleplex",
            37.422160,
            -122.084270
        )

        // When the validate function is called
        val isValidReminder = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Then it returns true
        assertEquals(false, isValidReminder)
    }

    @Test
    fun validateEnteredData_noTitleReminder_shouldReturnError() {
        // Given a reminder without title
        val reminderDataItem = ReminderDataItem(
            "",
            "Reminder description",
            "Googleplex",
            37.422160,
            -122.084270
        )

        // When the validate function is called
        val isValidReminder = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Then a SnackBar with Error is shown
        assertEquals(isValidReminder, false)
        assertEquals(R.string.err_enter_title, saveReminderViewModel.showSnackBarInt.getOrAwaitValue())
    }

    @Test
    fun validateEnteredData_noLocationReminder_returnsFalse() {
        // Given a reminder without title
        val reminderDataItem = ReminderDataItem(
            "Reminder title",
            "Reminder description",
            "",
            37.422160,
            -122.084270
        )

        // When the validate function is called
        val isValidReminder = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Then it returns true
        assertEquals(false, isValidReminder)
    }

    @Test
    fun validateEnteredData_noLocationReminder_shouldReturnError() {
        // Given a reminder without title
        val reminderDataItem = ReminderDataItem(
            "Reminder title",
            "Reminder description",
            "",
            37.422160,
            -122.084270
        )

        // When the validate function is called
        val isValidReminder = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Then a SnackBar with Error is shown
        assertEquals(isValidReminder, false)
        assertEquals(R.string.err_select_location, saveReminderViewModel.showSnackBarInt.getOrAwaitValue())
    }

    @Test
    fun saveReminder_checkLoading() {
        // Given that the execution is paused
        mainCoroutineRule.pauseDispatcher()

        // And given a valid reminder
        val reminderDataItem = ReminderDataItem(
            "Reminder title",
            "Reminder description",
            "Googleplex",
            37.422160,
            -122.084270
        )

        // When the reminder are saved
        saveReminderViewModel.saveReminder(reminderDataItem)

        // Then the Show Loading value is True
        assertEquals(true, saveReminderViewModel.showLoading.getOrAwaitValue())

        // And when the execution is resumed
        mainCoroutineRule.resumeDispatcher()

        // Then the Show Loading value is False
        assertEquals(false, saveReminderViewModel.showLoading.getOrAwaitValue())
    }
}