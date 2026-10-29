package com.dynasty.cfb

import com.dynasty.cfb.data.*
import com.dynasty.cfb.domain.MatchEngine
import com.dynasty.cfb.domain.SeasonEngine
import com.dynasty.cfb.editor.EditorValidator
import com.dynasty.cfb.util.DataGenerator
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    @Test
    fun testPositionOverallCalculationAndGenerator() {
        val confs = DataGenerator.generateDefaultConferences()
        val colleges = DataGenerator.generateColleges(confs)
        assertTrue("Must generate at least 100 fictional colleges", colleges.size >= 100)

        val qbOvr = Player.calculatePositionOverall(
            pos = Position.QB,
            spd = 85, str = 70, awr = 88, thp = 92, tha = 90,
            car = 75, cth = 50, blk = 45, tak = 40, cov = 40, kpw = 40, kac = 40
        )
        assertTrue("Elite QB attributes should produce 85+ overall", qbOvr in 85..99)
    }

    @Test
    fun testMatchSimulationAndScoring() {
        val confs = DataGenerator.generateDefaultConferences()
        val colleges = DataGenerator.generateColleges(confs)
        val home = colleges[0]
        val away = colleges[1]
        val rosters = DataGenerator.generateAllRosters(listOf(home, away), seed = 42)

        val scheduled = ScheduledGame(
            id = "test_g1",
            week = 1,
            homeCollegeId = home.id,
            awayCollegeId = away.id,
            isConferenceGame = true
        )

        val result = MatchEngine.simulateFullMatchInstant(
            game = scheduled,
            homeCollege = home,
            awayCollege = away,
            homeRoster = rosters.filter { it.collegeId == home.id },
            awayRoster = rosters.filter { it.collegeId == away.id },
            tactics = TacticsConfig(),
            difficulty = Difficulty.NORMAL,
            rng = Random(99)
        )

        assertTrue("Simulated match must be marked played", result.played)
        assertNotEquals("Finished college football game cannot end in a tie", result.homeScore, result.awayScore)
        assertTrue("Scores must be non-negative", result.homeScore >= 0 && result.awayScore >= 0)
    }

    @Test
    fun testScheduleGenerationAndStandingsSorting() {
        val confs = DataGenerator.generateDefaultConferences()
        val colleges = DataGenerator.generateColleges(confs)
        val schedule = SeasonEngine.generateRegularSeasonSchedule(colleges, 2026)

        assertTrue("Regular season schedule must span Weeks 1..12", schedule.all { it.week in 1..12 })
        val team1Games = schedule.count { it.homeCollegeId == colleges.first().id || it.awayCollegeId == colleges.first().id }
        assertEquals("Every college should have 12 regular season games", 12, team1Games)

        val modified = colleges.mapIndexed { idx, c ->
            if (idx == 0) c.copy(confWins = 8, confLosses = 0, wins = 11, losses = 1)
            else if (idx == 1) c.copy(confWins = 6, confLosses = 2, wins = 9, losses = 3)
            else c
        }
        val sorted = SeasonEngine.sortConferenceStandings(modified, modified[0].conferenceId)
        assertEquals("8-0 conference team must be first in standings", 8, sorted.first().confWins)
    }

    @Test
    fun testCareerSaveExportAndImportValidation() {
        val confs = DataGenerator.generateDefaultConferences()
        val colleges = DataGenerator.generateColleges(confs).take(4)
        val players = DataGenerator.generateAllRosters(colleges, seed = 10)
        val state = CareerState(
            saveSlotId = 1,
            userCollegeId = colleges.first().id,
            coach = CoachProfile(name = "Test Coach"),
            conferences = confs,
            colleges = colleges,
            players = players
        )

        val json = EditorValidator.exportCareerToJson(state)
        assertTrue("Exported JSON should not be blank", json.isNotBlank())

        val imported = EditorValidator.importCareerFromJson(json)
        assertTrue("Valid exported JSON must re-import cleanly", imported.isSuccess)
        assertEquals("Coach name must match after round-trip", "Test Coach", imported.getOrNull()?.coach?.name)

        val corrupt = EditorValidator.importCareerFromJson("{bad_json")
        assertTrue("Corrupt JSON must fail validation safely", corrupt.isFailure)
    }
}
