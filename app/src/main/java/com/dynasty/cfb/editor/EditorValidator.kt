package com.dynasty.cfb.editor

import com.dynasty.cfb.data.CareerState
import com.dynasty.cfb.data.College
import com.dynasty.cfb.data.Conference
import com.dynasty.cfb.data.Player
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object EditorValidator {

    val jsonFormat = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun validateCollegeEdit(name: String, mascot: String, abbr: String, prestigeScore: Int): ValidationResult {
        if (name.isBlank() || name.length < 3) return ValidationResult.Invalid("College name must be at least 3 characters.")
        if (mascot.isBlank()) return ValidationResult.Invalid("Mascot cannot be empty.")
        if (abbr.length !in 2..4) return ValidationResult.Invalid("Abbreviation must be 2 to 4 letters.")
        if (prestigeScore !in 40..99) return ValidationResult.Invalid("Prestige score must be between 40 and 99.")
        return ValidationResult.Valid
    }

    fun validatePlayerEdit(firstName: String, lastName: String, gpa: Double, potential: Int): ValidationResult {
        if (firstName.isBlank() || lastName.isBlank()) return ValidationResult.Invalid("First and last name are required.")
        if (gpa !in 1.0..4.0) return ValidationResult.Invalid("GPA must be between 1.0 and 4.0.")
        if (potential !in 50..99) return ValidationResult.Invalid("Potential must be between 50 and 99.")
        return ValidationResult.Valid
    }

    fun validateConferenceEdit(name: String, shortName: String, prestige: Int): ValidationResult {
        if (name.isBlank()) return ValidationResult.Invalid("Conference name cannot be blank.")
        if (shortName.length !in 2..6) return ValidationResult.Invalid("Short name must be 2 to 6 characters.")
        if (prestige !in 1..5) return ValidationResult.Invalid("Conference prestige must be 1 to 5 stars.")
        return ValidationResult.Valid
    }

    fun exportCareerToJson(state: CareerState): String {
        return jsonFormat.encodeToString(state)
    }

    fun importCareerFromJson(rawJson: String): Result<CareerState> {
        return try {
            val decoded = jsonFormat.decodeFromString<CareerState>(rawJson.trim())
            if (decoded.colleges.isEmpty()) {
                Result.failure(IllegalArgumentException("Imported JSON contains 0 colleges."))
            } else if (decoded.players.isEmpty()) {
                Result.failure(IllegalArgumentException("Imported JSON contains 0 players."))
            } else if (decoded.colleges.none { it.id == decoded.userCollegeId }) {
                Result.failure(IllegalArgumentException("User college ID not found in imported colleges list."))
            } else {
                Result.success(decoded)
            }
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Invalid Dynasty JSON format: ${e.message}"))
        }
    }
}
