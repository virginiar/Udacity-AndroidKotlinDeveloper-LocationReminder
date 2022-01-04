package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // DONE: Add testing implementation to the RemindersLocalRepository.kt
    private lateinit var database: RemindersDatabase
    private lateinit var remindersRepository: RemindersLocalRepository

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDatabaseAndRepository() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        remindersRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Unconfined
        )
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetReminders() = runBlocking {
        // Given a valid reminder
        val reminderDTO = ReminderDTO(
            "Reminder title",
            "Reminder description",
            "Googleplex",
            37.422160,
            -122.084270
        )

        // When save the reminder in the repository
       remindersRepository.saveReminder(reminderDTO)

        // Then there is one element in the database
        val reminders = remindersRepository.getReminders() as Result.Success
        assertThat(reminders, notNullValue())
        assertThat(reminders.data.size, `is`(1))
    }

    @Test
    fun saveReminderAndGetReminderById() = runBlocking {
        // Given a valid reminder
        val reminderDTO = ReminderDTO(
            "Reminder title",
            "Reminder description",
            "Googleplex",
            37.422160,
            -122.084270
        )

        // When save the reminder in the repository
        remindersRepository.saveReminder(reminderDTO)

        // Then the loaded data contains the expected values
        val loaded = remindersRepository.getReminder(reminderDTO.id) as Result.Success
        assertThat(loaded.data, notNullValue())
        assertThat(loaded.data.id, `is`(reminderDTO.id))
        assertThat(loaded.data.title, `is`(reminderDTO.title))
        assertThat(loaded.data.description, `is`(reminderDTO.description))
        assertThat(loaded.data.location, `is`(reminderDTO.location))
        assertThat(loaded.data.latitude, `is`(reminderDTO.latitude))
        assertThat(loaded.data.longitude, `is`(reminderDTO.longitude))
    }

    @Test
    fun deleteAllRemindersAndGetReminders() = runBlocking {
        // Given a valid reminder saved in the repository
        val reminderDTO = ReminderDTO(
            "Reminder title",
            "Reminder description",
            "Googleplex",
            37.422160,
            -122.084270
        )
        remindersRepository.saveReminder(reminderDTO)

        // When delete all the reminders in the repository
        remindersRepository.deleteAllReminders()

        // Then there is not reminders in the repository
        val reminders = remindersRepository.getReminders() as Result.Success
        assertThat(reminders, notNullValue())
        assertThat(reminders.data.size, `is`(0))
    }

    @Test
    fun getReminderByIdWhitInvalidId() = runBlocking {
        // When try to retrieve a reminder in an empty repository
        val reminder = remindersRepository.getReminder("1234") as Result.Error

        // Then a error is returned
        assertThat(reminder, notNullValue())
        assertThat(reminder.message, `is`("Reminder not found!"))
    }
}