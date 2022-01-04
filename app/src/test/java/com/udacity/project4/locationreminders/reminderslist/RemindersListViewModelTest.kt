package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
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

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O])
class RemindersListViewModelTest {

    //DONE: Provide testing to the RemindersListViewModel and its live data objects
    private lateinit var remindersListViewModel: RemindersListViewModel
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
        stopKoin()
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext() as Application,
            fakeDataSource
        )
    }

    @After
    fun cleanupDataSource() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.deleteAllReminders()
    }

    @Test
    fun loadReminders_noReminders_showNoData() = mainCoroutineRule.runBlockingTest{
        // When load reminders and there is not any reminder
        remindersListViewModel.loadReminders()

        // Then the Show No Data value must be True
        assertEquals(true, remindersListViewModel.showNoData.getOrAwaitValue())
    }

    @Test
    fun loadReminders_noReminders_getsEmptyRemindersList() = mainCoroutineRule.runBlockingTest{
        // When load reminders and there is not any reminder
        remindersListViewModel.loadReminders()

        // Then the reminders list must be empty
        val reminders = remindersListViewModel.remindersList.getOrAwaitValue()
        assertEquals(0, reminders.size)
    }

    @Test
    fun loadReminders_oneReminder_noShowNoData() = mainCoroutineRule.runBlockingTest{
        // Given that a reminder is added
        val reminder = ReminderDTO(
            "Reminder title",
            "Reminder description",
            "Googleplex",
            37.422160,
            -122.084270
        )
        fakeDataSource.saveReminder(reminder)

        // When load reminders and there is any reminder
        remindersListViewModel.loadReminders()

        // Then the Show No Data value must be False
        assertEquals(false, remindersListViewModel.showNoData.getOrAwaitValue())
    }

    @Test
    fun loadReminders_oneReminder_getsOneListElement() = mainCoroutineRule.runBlockingTest {
        // Given that a reminder is added
        val reminder = ReminderDTO(
            "Reminder title",
            "Reminder description",
            "Googleplex",
            37.422160,
            -122.084270
        )
        fakeDataSource.saveReminder(reminder)

        // When load reminders
        remindersListViewModel.loadReminders()

        // Then the reminders list must have one element
        val reminders = remindersListViewModel.remindersList.getOrAwaitValue()
        assertEquals(1, reminders.size)
    }

    @Test
    fun loadReminders_checkLoading() = mainCoroutineRule.runBlockingTest {
        // Given that the execution is paused
        mainCoroutineRule.pauseDispatcher()

        // When the reminders are loaded
        remindersListViewModel.loadReminders()

        // Then the Show Loading value is True
        assertEquals(true, remindersListViewModel.showLoading.getOrAwaitValue())

        // And when the execution is resumed
        mainCoroutineRule.resumeDispatcher()

        // Then the Show Loading value is False
        assertEquals(false, remindersListViewModel.showLoading.getOrAwaitValue())
    }

    @Test
    fun loadReminders_shouldReturnError() = mainCoroutineRule.runBlockingTest {
        // Given that a ViewModel's Error
        fakeDataSource.setReturnError(true)

        // When the reminders are loaded
        remindersListViewModel.loadReminders()

        // Then a SnackBar with Error is displayed
        assertEquals("Test exception", remindersListViewModel.showSnackBar.getOrAwaitValue())
    }
}