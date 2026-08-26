package com.example.vicecheatoverlay

data class Cheat(val code: String, val description: String)

object CheatRepository {
    val cheats = listOf(
        Cheat("ASPIRINE", "Full Health"),
        Cheat("PRECIOUSPROTECTION", "Full Armor"),
        Cheat("LEAVEMEALONE", "Remove Wanted Level"),
        Cheat("YOUWONTTAKEMEALIVE", "Increase Wanted Level"),
        Cheat("PANZER", "Spawn Tank"),
        Cheat("BIGBANG", "Destroy Cars"),
        Cheat("ALOVELYDAY", "Sunny Weather"),
        Cheat("ONSPEED", "Faster Gameplay"),
        Cheat("BOOOOOORING", "Slower Gameplay"),
        Cheat("THUGSTOOLS", "Weapon Set 1"),
        Cheat("PROFESSIONALTOOLS", "Weapon Set 2"),
        Cheat("NUTTERTOOLS", "Weapon Set 3")
    )
}
