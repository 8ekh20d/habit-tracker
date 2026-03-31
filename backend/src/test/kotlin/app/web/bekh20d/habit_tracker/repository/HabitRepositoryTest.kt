package app.web.bekh20d.habit_tracker.repository

import app.web.bekh20d.habit_tracker.model.FrequencyType
import app.web.bekh20d.habit_tracker.model.Habit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class HabitRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var habitRepository: HabitRepository

    @Test
    fun `should save and retrieve habit by id and userId`() {
        // Given
        val habit = Habit(
            userId = 1L,
            name = "Morning Exercise",
            frequencyType = FrequencyType.DAILY
        )
        entityManager.persist(habit)
        entityManager.flush()

        // When
        val found = habitRepository.findByIdAndUserId(habit.id, 1L)

        // Then
        assertThat(found).isNotNull
        assertThat(found?.name).isEqualTo("Morning Exercise")
        assertThat(found?.userId).isEqualTo(1L)
        assertThat(found?.frequencyType).isEqualTo(FrequencyType.DAILY)
    }

    @Test
    fun `should return null when habit not found for user`() {
        // Given
        val habit = Habit(
            userId = 1L,
            name = "Morning Exercise",
            frequencyType = FrequencyType.DAILY
        )
        entityManager.persist(habit)
        entityManager.flush()

        // When
        val found = habitRepository.findByIdAndUserId(habit.id, 2L)

        // Then
        assertThat(found).isNull()
    }

    @Test
    fun `should find all habits by userId`() {
        // Given
        val habit1 = Habit(userId = 1L, name = "Morning Exercise", frequencyType = FrequencyType.DAILY)
        val habit2 = Habit(userId = 1L, name = "Read Book", frequencyType = FrequencyType.DAILY)
        val habit3 = Habit(userId = 2L, name = "Meditation", frequencyType = FrequencyType.DAILY)
        
        entityManager.persist(habit1)
        entityManager.persist(habit2)
        entityManager.persist(habit3)
        entityManager.flush()

        // When
        val user1Habits = habitRepository.findByUserId(1L)
        val user2Habits = habitRepository.findByUserId(2L)

        // Then
        assertThat(user1Habits).hasSize(2)
        assertThat(user1Habits.map { it.name }).containsExactlyInAnyOrder("Morning Exercise", "Read Book")
        
        assertThat(user2Habits).hasSize(1)
        assertThat(user2Habits[0].name).isEqualTo("Meditation")
    }

    @Test
    fun `should use DAILY as default frequency type`() {
        // Given
        val habit = Habit(
            userId = 1L,
            name = "Test Habit"
        )
        
        // Then
        assertThat(habit.frequencyType).isEqualTo(FrequencyType.DAILY)
    }
}
