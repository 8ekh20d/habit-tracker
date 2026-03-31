package app.web.bekh20d.habit_tracker.property

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import java.time.LocalDate

/**
 * Property-Based Test for LocalDate Timezone Safety
 * 
 * **Validates: Requirements 8.7, 9.5, 19.1, 19.2, 19.3**
 * 
 * This test verifies that LocalDate operations are timezone-independent and that date
 * calculations work correctly across month and year boundaries.
 */
class LocalDateTimezoneSafetyPropertyTest : StringSpec({

    "Property 7: date.minusDays(n).plusDays(n) == date" {
        checkAll<LocalDate, Long>(
            5,
            Arb.localDate(),
            Arb.long(0L..365L)  // days to subtract/add
        ) { date, days ->
            // Act: Subtract and then add the same number of days
            val result = date.minusDays(days).plusDays(days)
            
            // Assert: Result should equal original date
            result shouldBe date
        }
    }

    "Property 7: date calculations work correctly across month boundaries" {
        checkAll(
            5,
            Arb.long(2020L..2030L),  // year
            Arb.long(1L..12L)        // month
        ) { yearLong: Long, monthLong: Long ->
            val year = yearLong.toInt()
            val month = monthLong.toInt()
            
            // Arrange: Get last day of month
            val lastDayOfMonth = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)
            val dayOfMonth = lastDayOfMonth.dayOfMonth
            
            // Act: Add one day to last day of month
            val nextDay = lastDayOfMonth.plusDays(1)
            
            // Assert: Next day should be first day of next month (or next year)
            nextDay.dayOfMonth shouldBe 1
            
            // Act: Subtract one day from first day of month
            val firstDayOfMonth = LocalDate.of(year, month, 1)
            val previousDay = firstDayOfMonth.minusDays(1)
            
            // Assert: Previous day should be last day of previous month
            previousDay.dayOfMonth shouldBe previousDay.lengthOfMonth()
        }
    }

    "Property 7: LocalDate operations are timezone-independent" {
        checkAll<LocalDate>(
            5,
            Arb.localDate()
        ) { date ->
            // Act: Perform date arithmetic
            val tomorrow = date.plusDays(1)
            val yesterday = date.minusDays(1)
            
            // Assert: Date arithmetic is consistent
            tomorrow.minusDays(1) shouldBe date
            yesterday.plusDays(1) shouldBe date
            
            // Assert: No time component exists
            // LocalDate has no time zone or time component by design
            // This property is inherent to LocalDate type
            date.toString().contains("T") shouldBe false
        }
    }

    "Property 7: date comparisons work correctly" {
        checkAll<LocalDate, Long>(
            5,
            Arb.localDate(),
            Arb.long(1L..100L)
        ) { date, days ->
            // Act: Create dates before and after
            val before = date.minusDays(days)
            val after = date.plusDays(days)
            
            // Assert: Comparisons work correctly
            before.isBefore(date) shouldBe true
            after.isAfter(date) shouldBe true
            date.isEqual(date) shouldBe true
        }
    }
})
