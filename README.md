# MCBMods

## Setting Up

1. Create a copy of the .env.template file and rename it to .env, fill in the parameters
2. Create a libs folder in the root of the repository and add the following jar dependencies:
   1. CyanosLootableBodies
   2. journeysmap
   3. FlansMod
   4. MinecraftBreakdownClient
3. In run/Flan add all content pack dependencies
4. Create a run Application with the following parameters
   1. Main class is "net.fabricmc.devlaunchinjector.Main"
   2. Add the following VM Options: "-Dfabric.dli.config=C:*path to project*\.gradle\loom-cache\launch.cfg" "-Dfabric.dli.env=client" "-Dfabric.dli.main=net.minecraft.launchwrapper.Launch"

## Open Source Software
MCBMods would not be possible without the following open source software:

| Software                                                                       | License                                                                                                           |
|--------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| [Apache Commons Lang](https://github.com/apache/commons-lang)                  | [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)                                                     |
| [Minecraft Forge](https://github.com/MinecraftForge/MinecraftForge/tree/1.8.9) | [Minecraft Forge License](https://github.com/MinecraftForge/MinecraftForge/blob/1.8.9/MinecraftForge-License.txt) |
| [Vigilance](https://github.com/EssentialGG/Vigilance)                          | [LGPL 3.0](https://github.com/EssentialGG/Vigilance/blob/master/LICENSE)                                          |
| [Skytils](https://github.com/Skytils/SkytilsMod/tree/dev)                      | [AGPL 3.0](https://github.com/Skytils/SkytilsMod/blob/dev/LICENSE.md)                                             |
| [Not Enough Updates](https://github.com/NotEnoughUpdates/NotEnoughUpdates)     | [GPL 3.0](https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/master/COPYING)                               |
| [Flan's Mod](https://github.com/FlansMods/FlansMod/tree/1.8)                   | [CCPL](https://github.com/FlansMods/FlansMod/blob/1.8/LICENSE.txt)                                                |
| [Inventory Tweaks](https://github.com/Inventory-Tweaks/inventory-tweaks)       | [MIT](https://github.com/Inventory-Tweaks/inventory-tweaks/blob/develop/LICENSE.md)                               |