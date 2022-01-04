package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.runner.RunWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // DONE: Add testing implementation to the RemindersDao.kt
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetReminders() = runBlockingTest {
        // Given a valid reminder
        val reminderDTO = ReminderDTO(
            "Reminder title",
            "Reminder description",
            "Googleplex",
            37.422160,
            -122.084270
        )

        // When save the reminder in the database
        database.reminderDao().saveReminder(reminderDTO)

        // Then there is one element in the database
        val reminders = database.reminderDao().getReminders()
        assertNotNull(reminders)
        assertEquals(1, reminders.size)
    }

    @Test
    fun saveReminderAndGetReminderById() = runBlockingTest {
        // Given a valid reminder
        val reminderDTO = ReminderDTO(
            "Reminder title",
            "Reminder description",
            "Googleplex",
            37.422160,
            -122.084270
        )

        // When save the reminder in the database
        database.reminderDao().saveReminder(reminderDTO)

        // Then the loaded data contains the expected values
        val loadedReminder = database.reminderDao().getReminderById(reminderDTO.id)
        assertNotNull(loadedReminder)
        assertEquals(reminderDTO.id, loadedReminder?.id)
        assertEquals(reminderDTO.title, loadedReminder?.title)
        assertEquals(reminderDTO.description, loadedReminder?.description)
        assertEquals(reminderDTO.location, loadedReminder?.location)
        assertEquals(reminderDTO.latitude, loadedReminder?.latitude)
        assertEquals(reminderDTO.longitude, loadedReminder?.longitude)
    }

    @Test
    fun deleteAllRemindersAndGetReminders() = runBlockingTest {
        // Given a valid reminder saved in the database
        val reminderDTO = ReminderDTO(
            "Reminder title",
            "Reminder description",
            "Googleplex",
            37.422160,
            -122.084270
        )
        database.reminderDao().saveReminder(reminderDTO)

        // When delete all the reminders in the database
        database.reminderDao().deleteAllReminders()

        // Then there is not elements in the database
        val reminders = database.reminderDao().getReminders()
        assertNotNull(reminders)
        assertEquals(0, reminders.size)
    }

    @Test
    fun getReminderByIdWhitInvalidId() = runBlocking {
        // When try to retrieve a reminder in an empty database
        val reminder = database.reminderDao().getReminderById("1234")

        // Then a error is returned
        MatcherAssert.assertThat(reminder, CoreMatchers.nullValue())
    }
}