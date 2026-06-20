package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun productivityScore_isBoundedAndSensible() {
    // Verify our math formula: Score = 70 + (focusMins * 2) - (distractionCount * 3)
    // with constraints [10, 100]
    val focusMinsSuccess = 30
    val distractionsCount = 5
    
    var score = 70 + (focusMinsSuccess * 2) - (distractionsCount * 3)
    if (score < 10) score = 10
    if (score > 100) score = 100
    
    assertEquals(100, score) // 70 + 60 - 15 = 115, bounded at 100
    
    val focusMinsZero = 0
    val distractionsHeavy = 40
    var badScore = 70 + (focusMinsZero * 2) - (distractionsHeavy * 3) // 70 - 120 = -50
    if (badScore < 10) badScore = 10
    if (badScore > 100) badScore = 100
    
    assertEquals(10, badScore) // negative scores bound at 10
  }
}
