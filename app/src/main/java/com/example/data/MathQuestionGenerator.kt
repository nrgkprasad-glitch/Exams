package com.example.data

import kotlin.random.Random

object MathQuestionGenerator {

    data class MathQuestionTemplate(
        val category: String,
        val text: String,
        val answer: String
    )

    fun generate60Questions(): List<MathQuestionTemplate> {
        val list = mutableListOf<MathQuestionTemplate>()
        val random = Random(System.currentTimeMillis())

        // 1. Square of numbers ending with 0 (possibilities: 10, 20, ..., 90)
        val endIn0 = (1..9).map { it * 10 }.shuffled(random).take(4)
        for (num in endIn0) {
            list.add(MathQuestionTemplate(
                category = "Square of numbers ending with 0",
                text = "What is the square of $num? (i.e. $num × $num)",
                answer = (num * num).toString()
            ))
        }

        // 2. Square of numbers ending with 5 (possibilities: 15, 25, ..., 95)
        val endIn5 = (1..9).map { it * 10 + 5 }.shuffled(random).take(4)
        for (num in endIn5) {
            list.add(MathQuestionTemplate(
                category = "Square of numbers ending with 5",
                text = "What is the square of $num? (i.e. $num × $num)",
                answer = (num * num).toString()
            ))
        }

        // 3. Square of any two-digit number
        val twoDigitSquares = (10..99).filter { it % 10 != 0 && it % 10 != 5 }.shuffled(random).take(4)
        for (num in twoDigitSquares) {
            list.add(MathQuestionTemplate(
                category = "Square of any two-digit number",
                text = "What is the square of $num? (i.e. $num × $num)",
                answer = (num * num).toString()
            ))
        }

        // 4. Multiply by 11
        val mult11List = (11..99).shuffled(random).take(4)
        for (num in mult11List) {
            list.add(MathQuestionTemplate(
                category = "Multiply by 11",
                text = "Calculate: $num × 11",
                answer = (num * 11).toString()
            ))
        }

        // 5. 12–19 × Single Digit
        val teensSinglePairs = mutableListOf<Pair<Int, Int>>()
        while (teensSinglePairs.size < 4) {
            val teen = random.nextInt(12, 20)
            val single = random.nextInt(2, 10)
            val pair = Pair(teen, single)
            if (pair !in teensSinglePairs) {
                teensSinglePairs.add(pair)
            }
        }
        for (pair in teensSinglePairs) {
            list.add(MathQuestionTemplate(
                category = "12–19 × Single Digit",
                text = "Calculate: ${pair.first} × ${pair.second}",
                answer = (pair.first * pair.second).toString()
            ))
        }

        // 6. Two-digit × Single Digit
        val twoDigitSinglePairs = mutableListOf<Pair<Int, Int>>()
        while (twoDigitSinglePairs.size < 4) {
            val twoDigit = random.nextInt(21, 99)
            val single = random.nextInt(2, 10)
            val pair = Pair(twoDigit, single)
            if (pair !in twoDigitSinglePairs) {
                twoDigitSinglePairs.add(pair)
            }
        }
        for (pair in twoDigitSinglePairs) {
            list.add(MathQuestionTemplate(
                category = "Two-digit × Single Digit",
                text = "Calculate: ${pair.first} × ${pair.second}",
                answer = (pair.first * pair.second).toString()
            ))
        }

        // 7. Same tens digit, units add to 10
        val sameTensList = mutableListOf<Pair<Int, Int>>()
        while (sameTensList.size < 4) {
            val tens = random.nextInt(1, 10)
            val u1 = random.nextInt(1, 9) // 1 to 8, so u2 is 2 to 9
            val u2 = 10 - u1
            val n1 = tens * 10 + u1
            val n2 = tens * 10 + u2
            val pair = Pair(n1, n2)
            if (pair !in sameTensList && n1 != n2) {
                sameTensList.add(pair)
            }
        }
        for (pair in sameTensList) {
            list.add(MathQuestionTemplate(
                category = "Same tens digit, units add to 10",
                text = "Calculate: ${pair.first} × ${pair.second}",
                answer = (pair.first * pair.second).toString()
            ))
        }

        // 8. Same units digit, tens add to 10
        val sameUnitsList = mutableListOf<Pair<Int, Int>>()
        while (sameUnitsList.size < 4) {
            val units = random.nextInt(1, 10)
            val t1 = random.nextInt(1, 9)
            val t2 = 10 - t1
            val n1 = t1 * 10 + units
            val n2 = t2 * 10 + units
            val pair = Pair(n1, n2)
            if (pair !in sameUnitsList && n1 != n2) {
                sameUnitsList.add(pair)
            }
        }
        for (pair in sameUnitsList) {
            list.add(MathQuestionTemplate(
                category = "Same units digit, tens add to 10",
                text = "Calculate: ${pair.first} × ${pair.second}",
                answer = (pair.first * pair.second).toString()
            ))
        }

        // 9. Numbers ending with 1
        val endingWith1List = mutableListOf<Pair<Int, Int>>()
        val end1Choices = (1..9).map { it * 10 + 1 }
        while (endingWith1List.size < 4) {
            val n1 = end1Choices.random(random)
            val n2 = end1Choices.random(random)
            val pair = Pair(n1, n2)
            if (pair !in endingWith1List && n1 != n2) {
                endingWith1List.add(pair)
            }
        }
        for (pair in endingWith1List) {
            list.add(MathQuestionTemplate(
                category = "Numbers ending with 1",
                text = "Calculate: ${pair.first} × ${pair.second}",
                answer = (pair.first * pair.second).toString()
            ))
        }

        // 10. Teens × Teens
        val teensPairs = mutableListOf<Pair<Int, Int>>()
        while (teensPairs.size < 4) {
            val n1 = random.nextInt(11, 20)
            val n2 = random.nextInt(11, 20)
            val pair = Pair(n1, n2)
            if (pair !in teensPairs) {
                teensPairs.add(pair)
            }
        }
        for (pair in teensPairs) {
            list.add(MathQuestionTemplate(
                category = "Teens × Teens",
                text = "Calculate: ${pair.first} × ${pair.second}",
                answer = (pair.first * pair.second).toString()
            ))
        }

        // 11. Three-digit Addition
        val addPairs = mutableListOf<Pair<Int, Int>>()
        while (addPairs.size < 4) {
            val n1 = random.nextInt(100, 1000)
            val n2 = random.nextInt(100, 1000)
            val pair = Pair(n1, n2)
            if (pair !in addPairs) {
                addPairs.add(pair)
            }
        }
        for (pair in addPairs) {
            list.add(MathQuestionTemplate(
                category = "Three-digit Addition",
                text = "Calculate: ${pair.first} + ${pair.second}",
                answer = (pair.first + pair.second).toString()
            ))
        }

        // 12. Three-digit Subtraction
        val subPairs = mutableListOf<Pair<Int, Int>>()
        while (subPairs.size < 4) {
            val n1 = random.nextInt(100, 1000)
            val n2 = random.nextInt(100, 1000)
            val pair = if (n1 >= n2) Pair(n1, n2) else Pair(n2, n1)
            if (pair !in subPairs) {
                subPairs.add(pair)
            }
        }
        for (pair in subPairs) {
            list.add(MathQuestionTemplate(
                category = "Three-digit Subtraction",
                text = "Calculate: ${pair.first} − ${pair.second}",
                answer = (pair.first - pair.second).toString()
            ))
        }

        // 13. Three-digit Multiplication (middle digit zero)
        val middleZeroPairs = mutableListOf<Pair<Int, Int>>()
        while (middleZeroPairs.size < 4) {
            val h1 = random.nextInt(1, 10)
            val u1 = random.nextInt(1, 10)
            val n1 = h1 * 100 + u1

            val h2 = random.nextInt(1, 10)
            val u2 = random.nextInt(1, 10)
            val n2 = h2 * 100 + u2

            val pair = Pair(n1, n2)
            if (pair !in middleZeroPairs) {
                middleZeroPairs.add(pair)
            }
        }
        for (pair in middleZeroPairs) {
            list.add(MathQuestionTemplate(
                category = "Three-digit Multiplication (middle digit zero)",
                text = "Calculate: ${pair.first} × ${pair.second}",
                answer = (pair.first * pair.second).toString()
            ))
        }

        // 14. Three-digit Division
        val divPairs = mutableListOf<Pair<Int, Int>>()
        while (divPairs.size < 4) {
            val divisor = random.nextInt(2, 10)
            val quotient = random.nextInt(20, 125) // ensuring product is 3 digit
            val dividend = divisor * quotient
            if (dividend in 100..999) {
                val pair = Pair(dividend, divisor)
                if (pair !in divPairs) {
                    divPairs.add(pair)
                }
            }
        }
        for (pair in divPairs) {
            list.add(MathQuestionTemplate(
                category = "Three-digit Division",
                text = "Calculate: ${pair.first} ÷ ${pair.second}",
                answer = (pair.first / pair.second).toString()
            ))
        }

        // 15. Any Two-digit Multiplication
        val anyTwoDigitPairs = mutableListOf<Pair<Int, Int>>()
        while (anyTwoDigitPairs.size < 4) {
            val n1 = random.nextInt(10, 100)
            val n2 = random.nextInt(10, 100)
            // exclude matching previous categories to keep it interesting
            if (n1 % 10 != 0 && n2 % 10 != 0 && n1 % 10 != 5 && n2 % 10 != 5 && n1 != 11 && n2 != 11) {
                val pair = Pair(n1, n2)
                if (pair !in anyTwoDigitPairs) {
                    anyTwoDigitPairs.add(pair)
                }
            }
        }
        for (pair in anyTwoDigitPairs) {
            list.add(MathQuestionTemplate(
                category = "Any Two-digit Multiplication",
                text = "Calculate: ${pair.first} × ${pair.second}",
                answer = (pair.first * pair.second).toString()
            ))
        }

        return list.shuffled(random) // Shuffle across categories to randomize
    }
}
