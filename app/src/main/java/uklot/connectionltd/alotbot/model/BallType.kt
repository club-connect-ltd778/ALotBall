package uklot.connectionltd.alotbot.model

import uklot.connectionltd.alotbot.R

enum class BallType(
    val titleRes: Int,
    val iconRes: Int,
    val promptTemplate: String
) {
    FOOTBALL(
        titleRes = R.string.football,
        iconRes = R.drawable.ios_ball_football,
        promptTemplate = """
            Create a unique football (soccer) ball design inspired by: "{USER_TEXT}".

            Style instructions:

            Keep realistic 3D football structure with pentagon/hexagon panels

            Customize panel patterns based on theme

            Apply colors, symbols, typography from user text

            Add creative national or club-style elements without copying real brands

            High-detail texture, light reflections, stadium lighting feel

            White or stadium background
        """.trimIndent()
    ),
    BASKETBALL(
        titleRes = R.string.basketball,
        iconRes = R.drawable.ios_ball_basketball,
        promptTemplate = """
            Design a unique basketball based on: "{USER_TEXT}".

            Requirements:

            Correct panel layout and ball proportions

            Custom surface material (wood, metal, leather, marble, futuristic)

            Colors and patterns based on user text

            Add optional emblem or icon

            Realistic texture and lighting

            Court or studio background
        """.trimIndent()
    ),
    TENNIS(
        titleRes = R.string.tennis,
        iconRes = R.drawable.ios_ball_tennis,
        promptTemplate = """
            Generate a unique tennis ball inspired by: "{USER_TEXT}".

            Features:

            Realistic fuzzy texture

            Preserve tennis ball curvature and seam

            Apply thematic colors and patterns (neon, gradient, metallic, nature-themed, etc.)

            Optional glowing or motion effect based on user text

            Clean studio background
        """.trimIndent()
    );
}
