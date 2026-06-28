package com.example.data

import kotlin.random.Random

object GKQuestionGenerator {

    data class CountryData(
        val name: String,
        val capital: String,
        val currency: String,
        val continent: String,
        val neighbors: List<String>,
        val landmark: String, // Famous landmark/fact representing the 'Countries' topic
        val difficulty: String // "Easy", "Medium", "Hard", "Olympiad"
    )

    data class GKQuestionTemplate(
        val topic: String,
        val text: String,
        val correctAnswer: String,
        val options: List<String>, // Exactly 4 options
        val difficulty: String
    )

    // A rich dataset of 40 countries across all continents and difficulties
    val countries = listOf(
        CountryData("India", "New Delhi", "Rupee", "Asia", listOf("Pakistan", "China", "Nepal", "Bangladesh", "Myanmar"), "the Taj Mahal", "Easy"),
        CountryData("United States", "Washington D.C.", "Dollar", "North America", listOf("Canada", "Mexico"), "the Statue of Liberty", "Easy"),
        CountryData("United Kingdom", "London", "Pound", "Europe", listOf("Ireland"), "Big Ben", "Easy"),
        CountryData("France", "Paris", "Euro", "Europe", listOf("Spain", "Italy", "Germany", "Belgium", "Switzerland"), "the Eiffel Tower", "Easy"),
        CountryData("Japan", "Tokyo", "Yen", "Asia", listOf("South Korea", "Russia", "China"), "Mount Fuji", "Easy"),
        CountryData("Australia", "Canberra", "Dollar", "Oceania", listOf("New Zealand", "Indonesia", "Papua New Guinea"), "the Sydney Opera House", "Easy"),
        CountryData("China", "Beijing", "Yuan", "Asia", listOf("India", "Russia", "Mongolia", "Pakistan", "Nepal"), "the Great Wall", "Easy"),
        CountryData("Egypt", "Cairo", "Pound", "Africa", listOf("Libya", "Sudan", "Israel"), "the Pyramids of Giza", "Easy"),
        CountryData("Italy", "Rome", "Euro", "Europe", listOf("France", "Switzerland", "Austria", "Slovenia"), "the Colosseum", "Easy"),
        CountryData("Brazil", "Brasilia", "Real", "South America", listOf("Argentina", "Uruguay", "Paraguay", "Bolivia", "Colombia"), "the Christ the Redeemer statue", "Easy"),

        CountryData("Canada", "Ottawa", "Dollar", "North America", listOf("United States"), "Niagara Falls", "Medium"),
        CountryData("Germany", "Berlin", "Euro", "Europe", listOf("Denmark", "Poland", "Austria", "Switzerland", "France", "Belgium", "Netherlands"), "the Brandenburg Gate", "Medium"),
        CountryData("South Africa", "Pretoria", "Rand", "Africa", listOf("Namibia", "Botswana", "Zimbabwe", "Mozambique", "Lesotho"), "Table Mountain", "Medium"),
        CountryData("Russia", "Moscow", "Ruble", "Europe", listOf("China", "Mongolia", "Kazakhstan", "Finland", "Belarus"), "the Kremlin", "Medium"),
        CountryData("Spain", "Madrid", "Euro", "Europe", listOf("Portugal", "France", "Andorra"), "the Sagrada Familia", "Medium"),
        CountryData("Mexico", "Mexico City", "Peso", "North America", listOf("United States", "Guatemala", "Belize"), "Chichen Itza Pyramids", "Medium"),
        CountryData("Saudi Arabia", "Riyadh", "Riyal", "Asia", listOf("Yemen", "Oman", "UAE", "Kuwait", "Iraq", "Jordan"), "the Kaaba in Mecca", "Medium"),
        CountryData("Turkey", "Ankara", "Lira", "Asia", listOf("Greece", "Bulgaria", "Georgia", "Syria", "Iraq", "Iran"), "the Hagia Sophia", "Medium"),
        CountryData("South Korea", "Seoul", "Won", "Asia", listOf("North Korea"), "Gyeongbokgung Palace", "Medium"),
        CountryData("Argentina", "Buenos Aires", "Peso", "South America", listOf("Chile", "Bolivia", "Paraguay", "Brazil", "Uruguay"), "the Iguazu Falls", "Medium"),

        CountryData("Switzerland", "Bern", "Franc", "Europe", listOf("France", "Germany", "Austria", "Italy"), "the Matterhorn mountain", "Hard"),
        CountryData("Singapore", "Singapore", "Dollar", "Asia", listOf("Malaysia", "Indonesia"), "the Marina Bay Sands", "Hard"),
        CountryData("New Zealand", "Wellington", "Dollar", "Oceania", listOf("Australia"), "Milford Sound", "Hard"),
        CountryData("Pakistan", "Islamabad", "Rupee", "Asia", listOf("India", "Afghanistan", "Iran", "China"), "the Badshahi Mosque", "Hard"),
        CountryData("Netherlands", "Amsterdam", "Euro", "Europe", listOf("Belgium", "Germany"), "historic Windmills of Kinderdijk", "Hard"),
        CountryData("Sweden", "Stockholm", "Krona", "Europe", listOf("Norway", "Finland"), "the Icehotel", "Hard"),
        CountryData("Greece", "Athens", "Euro", "Europe", listOf("Albania", "North Macedonia", "Bulgaria", "Turkey"), "the Parthenon temple", "Hard"),
        CountryData("Thailand", "Bangkok", "Baht", "Asia", listOf("Myanmar", "Laos", "Cambodia", "Malaysia"), "the Temple of the Emerald Buddha", "Hard"),
        CountryData("Norway", "Oslo", "Krone", "Europe", listOf("Sweden", "Finland", "Russia"), "the Geirangerfjord", "Hard"),
        CountryData("Kenya", "Nairobi", "Shilling", "Africa", listOf("Somalia", "Ethiopia", "South Sudan", "Uganda", "Tanzania"), "the Maasai Mara reserve", "Hard"),

        CountryData("Vietnam", "Hanoi", "Dong", "Asia", listOf("China", "Laos", "Cambodia"), "Ha Long Bay", "Olympiad"),
        CountryData("Indonesia", "Jakarta", "Rupiah", "Asia", listOf("Malaysia", "Papua New Guinea", "East Timor"), "Borobudur Temple", "Olympiad"),
        CountryData("Bangladesh", "Dhaka", "Taka", "Asia", listOf("India", "Myanmar"), "the Sundarbans mangrove forest", "Olympiad"),
        CountryData("Iran", "Tehran", "Rial", "Asia", listOf("Iraq", "Turkey", "Afghanistan", "Pakistan", "Armenia", "Azerbaijan"), "the ancient ruins of Persepolis", "Olympiad"),
        CountryData("Peru", "Lima", "Sol", "South America", listOf("Ecuador", "Colombia", "Brazil", "Bolivia", "Chile"), "Machu Picchu", "Olympiad"),
        CountryData("Malaysia", "Kuala Lumpur", "Ringgit", "Asia", listOf("Thailand", "Brunei", "Indonesia", "Singapore"), "the Petronas Twin Towers", "Olympiad"),
        CountryData("Chile", "Santiago", "Peso", "South America", listOf("Peru", "Bolivia", "Argentina"), "the Easter Island Moai statues", "Olympiad"),
        CountryData("Nigeria", "Abuja", "Naira", "Africa", listOf("Benin", "Niger", "Chad", "Cameroon"), "Zuma Rock", "Olympiad"),
        CountryData("Poland", "Warsaw", "Zloty", "Europe", listOf("Germany", "Czechia", "Slovakia", "Ukraine", "Belarus", "Lithuania", "Russia"), "the Wieliczka Salt Mine", "Olympiad"),
        CountryData("Austria", "Vienna", "Euro", "Europe", listOf("Germany", "Czechia", "Slovakia", "Hungary", "Slovenia", "Italy", "Switzerland", "Liechtenstein"), "Schönbrunn Palace", "Olympiad")
    )

    fun generate40Questions(): List<GKQuestionTemplate> {
        val list = mutableListOf<GKQuestionTemplate>()
        val random = Random(System.currentTimeMillis())

        val difficulties = listOf("Easy", "Medium", "Hard", "Olympiad")
        val topics = listOf("Countries", "Capitals", "Currency", "Continents", "Neighboring Countries")

        // We need exactly 40 questions total.
        // Let's generate exactly 8 questions per topic (2 Easy, 2 Medium, 2 Hard, 2 Olympiad).
        // This is perfectly balanced!

        for (topic in topics) {
            for (diff in difficulties) {
                // Generate 2 questions for this topic and difficulty
                val eligibleCountries = countries.filter { it.difficulty == diff }
                val chosenCountries = eligibleCountries.shuffled(random).take(2)

                for (country in chosenCountries) {
                    val q = when (topic) {
                        "Capitals" -> createCapitalQuestion(country, random)
                        "Currency" -> createCurrencyQuestion(country, random)
                        "Continents" -> createContinentQuestion(country, random)
                        "Neighboring Countries" -> createNeighborQuestion(country, random)
                        else -> createCountryQuestion(country, random) // "Countries" topic
                    }
                    list.add(q)
                }
            }
        }

        return list.shuffled(random)
    }

    private fun createCapitalQuestion(country: CountryData, random: Random): GKQuestionTemplate {
        val qText = "What is the capital city of ${country.name}?"
        val correct = country.capital

        // Distractors should be capitals from other countries
        val otherCapitals = countries.filter { it.name != country.name }.map { it.capital }.distinct()
        val distractors = otherCapitals.shuffled(random).take(3)
        val options = (distractors + correct).shuffled(random)

        return GKQuestionTemplate("Capitals", qText, correct, options, country.difficulty)
    }

    private fun createCurrencyQuestion(country: CountryData, random: Random): GKQuestionTemplate {
        val qText = "Which currency is used in ${country.name}?"
        val correct = country.currency

        // Distractors should be currencies of other countries
        val otherCurrencies = countries.filter { it.currency != correct }.map { it.currency }.distinct()
        val distractors = otherCurrencies.shuffled(random).take(3)
        val options = (distractors + correct).shuffled(random)

        return GKQuestionTemplate("Currency", qText, correct, options, country.difficulty)
    }

    private fun createContinentQuestion(country: CountryData, random: Random): GKQuestionTemplate {
        val qText = "In which continent is ${country.name} located?"
        val correct = country.continent

        // Continents distractors
        val allContinents = listOf("Asia", "Europe", "Africa", "North America", "South America", "Oceania")
        val otherContinents = allContinents.filter { it != correct }
        val distractors = otherContinents.shuffled(random).take(3)
        val options = (distractors + correct).shuffled(random)

        return GKQuestionTemplate("Continents", qText, correct, options, country.difficulty)
    }

    private fun createCountryQuestion(country: CountryData, random: Random): GKQuestionTemplate {
        val qText = "Which country is famous for ${country.landmark}?"
        val correct = country.name

        // Distractors are names of other countries
        val otherCountries = countries.filter { it.name != country.name }.map { it.name }.distinct()
        val distractors = otherCountries.shuffled(random).take(3)
        val options = (distractors + correct).shuffled(random)

        return GKQuestionTemplate("Countries", qText, correct, options, country.difficulty)
    }

    private fun createNeighborQuestion(country: CountryData, random: Random): GKQuestionTemplate {
        // We will generate a question about neighbors.
        // We can ask: "Which of these is a neighboring country of [Country]?"
        // Or "Which of these countries shares a land/maritime boundary with [Country]?"
        val correctNeighbor = country.neighbors.random(random)
        val qText = "Which of these countries is a neighbor of ${country.name}?"

        // Distractors must NOT be neighbors of this country!
        val nonNeighbors = countries
            .filter { it.name != country.name && it.name !in country.neighbors && it.name != correctNeighbor }
            .map { it.name }
            .distinct()

        val distractors = nonNeighbors.shuffled(random).take(3)
        val options = (distractors + correctNeighbor).shuffled(random)

        return GKQuestionTemplate("Neighboring Countries", qText, correctNeighbor, options, country.difficulty)
    }
}
