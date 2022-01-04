package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    private var reminders: MutableList<ReminderDTO>? = mutableListOf()
) : ReminderDataSource {

    // DONE: Create a fake data source to act as a double to the real data source
    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        // DONE: "Return the reminders"
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        reminders?.let {
            return Result.Success(ArrayList(it))
        }
        return Result.Error("Reminder list not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        // DONE: "Save the reminder"
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        // DONE: "Return the reminder with the id"
        reminders?.firstOrNull{
            it.id == id
        }?.let {
            return Result.Success(it)
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        // DONE: "Delete all the reminders"
        reminders = mutableListOf()
    }

}