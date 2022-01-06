package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - //
    // embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        android.Manifest.permission.ACCESS_NETWORK_STATE
    )

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    // DONE: Add End to End testing to the app
    @Test
    fun emptyRemainderList_showNoData() = runBlocking {
        // When enter in the Reminder Activity without reminders
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Then No Data must appears
        onView(withText(R.string.no_data))
            .check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun addReminder_noTitle_shouldReturnError() = runBlocking {
        // Given enter in the Reminder Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // When try to save a Reminder without Location
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        // Then an SnackBar with error must appears
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))
        activityScenario.close()
    }

    @Test
    fun addReminder_noLocation_shouldReturnError() = runBlocking {
        // Given enter in the Reminder Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // When try to save a Reminder without Location
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle))
            .perform(replaceText("Reminder title"))
        onView(withId(R.id.reminderDescription))
            .perform(replaceText("Reminder description"))
        onView(withId(R.id.saveReminder)).perform(click())

        // Then an SnackBar with error must appears
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))
        activityScenario.close()
    }

    @Test
    fun addLocation_noLocationSelected_shouldReturnError() = runBlocking {
        // Given enter in the Reminder Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // When try to save a Location without select a location
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())

        // Then a Toast with the error should appears
        val activity = getActivity(activityScenario)
        onView(withText(R.string.select_poi))
            .inRoot(withDecorView(not(`is`(activity!!.window.decorView))))
            .check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun addLocation_validLocation_showLocationData() = runBlocking {
        // Given enter in the Reminder Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // When try to add a location to a reminder
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).perform(click())
        // Then a map appears
        onView(withId(R.id.map)).check(matches(isDisplayed()))

        // And when a location is selected and saved
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.saveButton)).perform(click())

        // Then a reminder location name must appears
        onView(allOf(withId(R.id.selectedLocation), not(withText(""))))
        activityScenario.close()
    }


    @Test
    fun addReminder_showReminderList() = runBlocking {
        // Given enter in the Reminder Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // When save a valid reminder
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.saveButton)).perform(click())

        onView(withId(R.id.reminderTitle))
            .perform(replaceText("Remainder title"))
        onView(withId(R.id.reminderDescription))
            .perform(replaceText("Remainder description"))
        onView(withId(R.id.saveReminder)).perform(click())

        val activity = getActivity(activityScenario)
        onView(withText(R.string.reminder_saved))
            .inRoot(withDecorView(not(`is`(activity!!.window.decorView))))
            .check(matches(isDisplayed()))
        onView(withId(R.id.remindersRecyclerView))
            .check(matches(hasDescendant(withText("Remainder title"))))
        onView(withId(R.id.remindersRecyclerView))
            .check(matches(hasDescendant(withText("Remainder description"))))

        activityScenario.close()
    }

    /*
     * Auxiliary function to get activity context
     */
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }
}
