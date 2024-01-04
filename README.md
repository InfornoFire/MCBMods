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