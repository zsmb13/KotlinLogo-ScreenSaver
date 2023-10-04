data class ImageSet(val name: String, val images: () -> List<String>)

val imageSets = arrayOf(
    ImageSet("Kotlin logos") {
        listOf(
            "kotlin0_10x",
            "kotlin1_10x",
            "kotlin2_10x",
            "kotlin3_10x",
            "kotlin4_10x",
        )
    },
    ImageSet("Kodee") {
        listOf(
            "Kodee-greeting",
            "Kodee-inlove",
            "Kodee-jumping",
            "Kodee-naughty",
            "Kodee-sharing",
            "Kodee-sitting",
            "Kodee-waving",
        ).shuffled()
    },
)
