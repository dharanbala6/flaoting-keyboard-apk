package com.example.vicecheatoverlay

data class Cheat(val code: String, val description: String)

object CheatRepository {
    val cheats = listOf(
        Cheat("HESOYAM", "Health, Armor, \$250k"),
        Cheat("BAGUVIX", "High Health Resistance"),
        Cheat("CVWKXAM", "Infinite Oxygen"),
        Cheat("LXGIWYL", "Weapon Set 1"),
        Cheat("PROFESSIONALSKIT", "Weapon Set 2"),
        Cheat("UZUMYMW", "Weapon Set 3"),
        Cheat("AEZAKMI", "Lock Wanted Level"),
        Cheat("ASNAEB", "Clear Wanted Level"),
        Cheat("OSRBLHH", "Increase Wanted Level"),
        Cheat("AIWPRTON", "Spawn Rhino Tank"),
        Cheat("ROCKETMAN", "Spawn Jetpack"),
        Cheat("JUMPJET", "Spawn Hydra"),
        Cheat("OHDUDE", "Spawn Hunter"),
        Cheat("KGGGDKP", "Spawn Vortex"),
        Cheat("CPKTNWT", "Destroy Vehicles"),
        Cheat("SPEEDFREAK", "All Cars Have Nitro"),
        Cheat("YLTEICZ", "Aggressive Drivers"),
        Cheat("PLEASANTLYWARM", "Sunny Weather"),
        Cheat("TOODAMNHOT", "Very Sunny Weather"),
        Cheat("AUIFRVQS", "Rainy Weather"),
        Cheat("CFVFGMJ", "Foggy Weather"),
        Cheat("NIGHTPROWLER", "Midnight Clock"),
        Cheat("PPGWJHT", "Faster Gameplay"),
        Cheat("LIYOAAY", "Slower Gameplay"),
        Cheat("BUFFMEUP", "Maximum Muscle"),
        Cheat("KVGYZQK", "Skinny Player")
    )
}
