data class ImageSet(val name: String, val images: () -> List<String>)

val imageSets = arrayOf(
    ImageSet("Kotlin logos") {
        listOf(
            "kotlin0",
            "kotlin1",
            "kotlin2",
            "kotlin3",
            "kotlin4",
        )
    },
    ImageSet("Kodee") {
        listOf(
            "kodee-greeting",
            "kodee-inlove",
            "kodee-jumping",
            "kodee-naughty",
            "kodee-sharing",
            "kodee-sitting",
            "kodee-waving",
        ).shuffled()
    },
)
