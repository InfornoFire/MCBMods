package inforno.mcbmods.gui

import com.flansmod.common.guns.BulletType
import com.flansmod.common.guns.ItemGun
import com.flansmod.common.guns.raytracing.PlayerHitbox
import com.flansmod.common.teams.ItemTeamArmour
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.events.UIClickEvent
import gg.essential.elementa.state.BasicState
import gg.essential.universal.GuiScale
import gg.essential.universal.UKeyboard
import gg.essential.universal.USound
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.utils.onLeftClick
import inforno.mcbmods.MCBMods
import inforno.mcbmods.gui.components.MCBModsDropDown
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.client.resources.I18n
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.item.Item
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.text.DecimalFormat
import kotlin.experimental.or
import kotlin.math.atan


class DamageCalcGui : WindowScreen(
    ElementaVersion.V1,
    newGuiScale = GuiScale.scaleForScreenSize().ordinal,
    restoreCurrentGuiOnClose = true
) {

    private val df = DecimalFormat("#.##")
    private val dfp = DecimalFormat("#.##%")

    companion object {
        private var selectedGun: Int = 0
        private var selectedArmor: Array<Int> = Array(4) { -1 }

        private val guns: MutableList<ItemGun> = ArrayList()
        private val armors: Array<ArrayList<ItemArmor>> = Array(4) { ArrayList() }
        private lateinit var armorOptions: Array<MutableList<String>>
        private lateinit var gunsOptions: List<String>

        private var target = 0
        private var dropDownSelect = 4
    }

    private var gunIcon: UIImage? = null
    private var armorIcon: Array<UIImage?> = arrayOfNulls(4)
    private var damageResult: Double = 0.0

    // WIP
    private val wip = UIText("Work In Progress").constrain {
        x = 2.pixels()
        y = 2.pixels()
        textScale = 2.pixels()
    } childOf window

    // Item Slots
    private val itemSlotsContainer by UIContainer().constrain {
        x = 25.pixels()
        y = 25.pixels()
        width = 40.pixels()
        height = 350.pixels()
    } childOf window

    private val itemSlots: Array<UIBlock> = Array(5) {
        UIBlock(Color(0, 0, 0, 40)).constrain {
            x = 0.pixels()
            y = 25.pixels() + 50.pixels() * it
            width = 42.pixels()
            height = 42.pixels()
        } childOf itemSlotsContainer effect OutlineEffect(
            VigilancePalette.getDivider(),
            1f
        ).bindColor(BasicState(VigilancePalette.getDivider()))
    }

    private var dropDownSelectEffect: OutlineEffect = OutlineEffect(Color(0, 255, 0), 1.0f)
    private var targetSelectEffect: OutlineEffect = OutlineEffect(Color(255, 0, 0), 2.5f)

    private var dropDowns: Array<MCBModsDropDown>

    // Player
    private val playerContainer by UIBlock(Color(0, 0, 0, 30)).constrain {
        x = 85.pixels()
        y = 60.pixels()
        width = 200.pixels()
        height = 300.pixels()
    } childOf window

    private var player: EntityOtherPlayerMP

    /*
    private val player by EssentialAPI.getEssentialComponentFactory().buildEmulatedPlayer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = RelativeConstraint(0.8f)
        height = RelativeConstraint(0.8f)
    } childOf playerContainer */


    // Stats
    private var gunDamage = 0.0
    private var indirectProtection = 0.0
    private var stopPower = 0.0
    private var penetratingPower = 0.0
    private var speedLoss = 0.0
    private var speedChange = 0.0
    private var hasPen = false
    private var ballisticReduced = 0.0
    private var flansDefense = 0.0
    private var flansReduced = 0.0
    private var vanillaDefense = 0.0
    private var vanillaReduced = 0.0
    private var mult = 0.0

    private val statContainer by UIBlock(Color(0, 0, 0, 40)).constrain {
        x = 25.pixels(alignOpposite = true)
        y = 25.pixels()
        width = 400.pixels()
        height = 430.pixels()
    } childOf window

    private val damageText = UIText().constrain {
        x = CenterConstraint()
        y = 5.pixels()
        textScale = 1.5.pixels()
    } childOf statContainer

    private val gunDmgText = UIText().constrain {
        x = CenterConstraint()
        y = 30.pixels()
        textScale = 1.5.pixels()
    } childOf statContainer

    private val multText = UIText().constrain {
        x = CenterConstraint()
        y = 45.pixels()
        textScale = 1.5.pixels()
    } childOf statContainer

    private val indirProtText = UIText().constrain {
        x = CenterConstraint()
        y = 70.pixels()
        textScale = 1.5.pixels()
    } childOf statContainer

    private val speedChangeText = UIText().constrain {
        x = CenterConstraint()
        y = 70.pixels()
        textScale = 1.5.pixels()
    } childOf statContainer

    private val flansDefText = UIText().constrain {
        x = CenterConstraint()
        y = 95.pixels()
        textScale = 1.5.pixels()
    } childOf statContainer

    private val vanillaDefText = UIText().constrain {
        x = CenterConstraint()
        y = 110.pixels()
        textScale = 1.5.pixels()
    } childOf statContainer

    init {
        var playerLocationSkin: ResourceLocation? = null
        var playerLocationCape: ResourceLocation? = null
        var skinType: String? = null

        try {
            Minecraft.getMinecraft().skinManager.loadProfileTextures(
                MCBMods.player.get().gameProfile,
                { type, location, profileTexture ->
                    when (type) {
                        MinecraftProfileTexture.Type.SKIN -> {
                            playerLocationSkin = location
                            skinType = profileTexture.getMetadata("model")

                            if (skinType == null) {
                                skinType = "default"
                            }
                        }

                        else -> {
                            playerLocationCape = location
                        }
                    }
                },
                false
            )
        } catch (e: Exception) {
        }

        player = object : EntityOtherPlayerMP(Minecraft.getMinecraft().theWorld, MCBMods.player.get().gameProfile) {
            override fun getLocationSkin(): ResourceLocation? {
                return playerLocationSkin ?: DefaultPlayerSkin.getDefaultSkin(this.uniqueID)
            }

            override fun getLocationCape(): ResourceLocation? {
                return playerLocationCape
            }

            override fun getSkinType(): String? {
                return skinType ?: DefaultPlayerSkin.getSkinType(this.uniqueID)
            }
        }

        player.alwaysRenderNameTag = false
        player.customNameTag = ""

        if (guns.isEmpty()) {
            for (item in Item.itemRegistry) {
                if (item is ItemGun && (item.type.damage * 2) > 3) guns += item
                else if (item is ItemArmor) {
                    armors[item.armorType] += item
                }
            }

            guns.sortBy {
                I18n.format(it.unlocalizedName + ".name").lowercase()
            }
            gunsOptions = guns.map { I18n.format(it.unlocalizedName + ".name") }

            for (armor in armors) {
                armor.sortBy {
                    I18n.format(it.unlocalizedName + ".name").lowercase()
                }
            }
            armorOptions = Array(4) { index ->
                armors[index].map { I18n.format(it.unlocalizedName + ".name") }.toMutableList()
            }
            armorOptions.forEach { it += "None" }
        }

        // Init the companion objects
        if (selectedArmor[0] == -1) {
            for (index in 0..3) {
                selectedArmor[index] = armors[index].size
            }
        }

        dropDowns = Array(5) { index ->
            if (index < 4) {
                MCBModsDropDown(0, armorOptions[index]).constrain {
                    x = 85.pixels()
                    y = 25.pixels()
                } childOf window
            } else {
                MCBModsDropDown(0, gunsOptions).constrain {
                    x = 85.pixels()
                    y = 25.pixels()
                } childOf window
            }
        }

        dropDowns[4].onValueChange {
            selectedGun = it
            gunIcon = generateImage(gunIcon, guns[selectedGun], 4)
            player.inventory.mainInventory[player.inventory.currentItem] = ItemStack(guns[selectedGun])
            updateStats()
        }
        dropDowns[4].hide(instantly = true)
        dropDowns[4].select(selectedGun)
        for (index in 0..3) {
            dropDowns[index].onValueChange {
                if (it < armors[index].size) {
                    selectedArmor[index] = it
                } else {
                    selectedArmor[index] = armors[index].size
                }
                armorIcon[index] = generateImage(armorIcon[index], armors[index].getOrNull(selectedArmor[index]), index)
                player.inventory.armorInventory[3 - index] = ItemStack(armors[index].getOrNull(selectedArmor[index]))
                updateStats()
            }
            dropDowns[index].hide(instantly = true)
            dropDowns[index].select(selectedArmor[index])
        }
        itemSlots[target] effect targetSelectEffect
        itemSlots[dropDownSelect] effect dropDownSelectEffect
        dropDowns[dropDownSelect].unhide()

        window.onKeyType { _, _ ->
            if (UKeyboard.isKeyDown(UKeyboard.KEY_MINUS)) {
                Inspector(window) childOf window
                return@onKeyType
            }
        }
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun onDrawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        player.onEntityUpdate()

        var b: Byte = 0
        for (part in EnumPlayerModelParts.values()) {
            b = b.or(part.partMask.toByte())
        }
        player.dataWatcher.updateObject(10, b)

        var playerSP = Minecraft.getMinecraft().thePlayer
        player.posX = playerSP.posX
        player.posY = -100.0
        player.posZ = playerSP.posZ

        drawEntityOnScreen(
            180,
            320,
            120,
            180.0f - mouseX,
            160.0f - mouseY,
            player
        )
        super.onDrawScreen(mouseX, mouseY, partialTicks)
    }

    private fun generateImage(icon: UIImage?, item: Item?, index: Int): UIImage {
        if (icon != null) {
            itemSlots[index].removeChild(icon)
        }
        val loc = if (item != null) {
            "/assets/" + Minecraft.getMinecraft().renderItem.itemModelMesher.getItemModel(ItemStack(item)).particleTexture.iconName.replace(
                ":",
                "/textures/"
            ) + ".png"
        } else {
            "/assets/minecraft/textures/items/empty_armor_slot_" + when (index) {
                0 -> "helmet"
                1 -> "chestplate"
                2 -> "leggings"
                3 -> "boots"
                else -> "error"
            } + ".png"
        }
        val image by UIImage.ofResourceCached(loc).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 40.pixels()
            height = 40.pixels()
        } childOf itemSlots[index]
        image.onLeftClick { // dropDown selection
            USound.playButtonPress()
            dropDownSelect = index
            for (x in 0..4) {
                dropDowns[x].collapse(unHover = true, instantly = true)
                dropDowns[x].hide(instantly = true)
                itemSlots[x].removeEffect(dropDownSelectEffect)
            }
            dropDowns[index].unhide()
            dropDownSelectEffect = OutlineEffect(Color(0, 255, 0), 1.0f)
            itemSlots[index] effect dropDownSelectEffect
        }
        image.onRightClick { // target selection
            if (index < 4) {
                USound.playButtonPress()
                target = index
                for (x in 0..4) {
                    itemSlots[x].removeEffect(targetSelectEffect)
                }
                targetSelectEffect = OutlineEffect(Color(255, 0, 0), 2.5f)
                itemSlots[index] effect targetSelectEffect
                itemSlots[index].effects.remove(dropDownSelectEffect)
                itemSlots[index].effects.add(dropDownSelectEffect)
                updateStats()
            }
        }
        return image
    }

    private fun updateStats() {
        calculateDamage()
        damageText.setText("Damage: ${df.format(damageResult)} | ${df.format(damageResult / 2.0)} §c❤")
        gunDmgText.setText("Original Gun Damage ${df.format(guns[selectedGun].type.damage * 2.0)} | ${df.format(guns[selectedGun].type.damage)} §c❤")
        multText.setText("Hit BodyPart Multiplier: ${df.format(mult)}x - New Damage: ${df.format(guns[selectedGun].type.damage * 2.0 * mult)}")
        if (hasPen) {
            speedChangeText.setText(
                "Ballistic Damage Reduction: ${dfp.format(1.0 - speedChange)} | ${
                    df.format(
                        ballisticReduced
                    )
                }"
            )
            speedChangeText.unhide()
            indirProtText.hide(instantly = true)
        } else {
            indirProtText.setText(
                "Ballistic Damage Reduction: ${dfp.format(1.0 - indirectProtection)} | ${
                    df.format(
                        ballisticReduced
                    )
                }"
            )
            indirProtText.unhide()
            speedChangeText.hide(instantly = true)
        }
        flansDefText.setText("Flans Defense Damage Reduction: ${dfp.format(flansDefense)} | ${df.format(flansReduced)}")
        vanillaDefText.setText(
            "Vanilla Defense Damage Reduction: ${dfp.format(vanillaDefense)} | ${
                df.format(
                    vanillaReduced
                )
            }"
        )
    }

    /**
     * BulletType stats shown in [BulletType.AmmoType]
     * plateLevel found in [com.flansmod.common.teams.ArmourType] and [ItemTeamArmour]
     *
     * PenetrationPower calculated at [com.flansmod.common.network.PacketClientBulletUpdate.handleServerSide]
     * Which calls [com.flansmod.common.guns.EntityBullet.doHit]
     * Which calls [PlayerHitbox.hitByBullet] and also [PlayerHitbox.hitArmorByBullet]
     */
    private fun calculateDamage() {
        val bulletType = guns[selectedGun].type.ammo[0] as BulletType
        val ammoType = bulletType.ammoType

        gunDamage = guns[selectedGun].type.damage * guns[selectedGun].type.numBullets * bulletType.damageVsLiving * bulletType.numBullets * 2.0

        if (selectedArmor[target] == armors[target].size) {
            indirectProtection = 1.0
            stopPower = 0.0
        } else if (armors[target][selectedArmor[target]] is ItemTeamArmour) { // Flans Armor Calculations
            // val armorHit = armors[target][selectedArmor[target]] as ItemTeamArmour

            // indirectProtection = 0.25 * (1.0 - PlayerHitbox.getArmorPlateIndirectProtection(armorHit.type.plateLevel))
            // stopPower = armorHit.type.defence * 10.0 + PlayerHitbox.getArmorPlateStopPower(armorHit.type.plateLevel)
        } else { // Vanilla Armor Calculations
            indirectProtection = 1.0
            stopPower = armors[target][selectedArmor[target]].damageReduceAmount * 0.5
        }

        penetratingPower = 1.0 * ammoType.speed * ammoType.penetration / BulletType.BULLET_SPEED_MODIFIER
        speedLoss = stopPower * ammoType.penetrationLoss
        speedChange = 1 - speedLoss / penetratingPower

        mult = if (target == 0) 1.6 else if (target == 1) 1.0 else 0.6
        gunDamage *= mult

        val base = gunDamage * indirectProtection * ammoType.nonPenetrationDamageModifier
        damageResult = if (speedChange > 0.0) {
            kotlin.math.max(base, gunDamage * speedChange)
        } else {
            kotlin.math.min(gunDamage, base)
        }
        hasPen = damageResult > base
        ballisticReduced = gunDamage - damageResult

        flansDefense = 0.0
        vanillaDefense = 0.0
        for (index in 0..3) {
            if (selectedArmor[index] == armors[index].size) continue
            val armor = armors[index][selectedArmor[index]]
            if (armor is ItemTeamArmour) {
                flansDefense += armor.type.defence * 0.7
            } else {
                vanillaDefense += armor.damageReduceAmount / 25.0
            }
        }
        flansReduced = damageResult * flansDefense
        damageResult -= flansReduced

        vanillaReduced = damageResult * vanillaDefense
        damageResult -= vanillaReduced
    }

    private inline fun UIComponent.onRightClick(crossinline method: UIComponent.(event: UIClickEvent) -> Unit) =
        onMouseClick {
            if (it.mouseButton == 1) {
                this.method(it)
            }
        }

    override fun updateGuiScale() {
        newGuiScale = GuiScale.scaleForScreenSize().ordinal
        super.updateGuiScale()
    }

    /**
     * From NEU: https://github.com/Moulberry/NotEnoughUpdates/blob/master/src/main/java/io/github/moulberry/notenoughupdates/profileviewer/BasicPage.java
     */
    private fun drawEntityOnScreen(
        posX: Int,
        posY: Int,
        scale: Int,
        mouseX: Float,
        mouseY: Float,
        ent: EntityLivingBase
    ) {
        GlStateManager.enableColorMaterial()
        GlStateManager.pushMatrix()
        GlStateManager.translate(posX.toFloat(), posY.toFloat(), 50.0f)
        GlStateManager.scale((-scale).toFloat(), scale.toFloat(), scale.toFloat())
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f)
        //val f = ent.renderYawOffset
        //val g = ent.rotationYaw
        //val h = ent.rotationPitch
        //val i = ent.prevRotationYawHead
        //val j = ent.rotationYawHead
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(25f, 1.0f, 0.0f, 0.0f)
        ent.renderYawOffset = atan(mouseX / 40.0f) * 20.0f
        ent.rotationYaw = atan(mouseX / 40.0f) * 40.0f
        ent.rotationPitch = -atan(mouseY / 40.0f) * 20.0f
        ent.rotationYawHead = atan(mouseX / 40.0f) * 40.0f
        ent.prevRotationYawHead = atan(mouseX / 40.0f) * 40.0f
        val rendermanager = Minecraft.getMinecraft().renderManager
        rendermanager.setPlayerViewY(180.0f)
        rendermanager.isRenderShadow = false
        rendermanager.renderEntityWithPosYaw(ent, 0.0, 0.0, 0.0, 0.0f, 1.0f)
        //ent.renderYawOffset = f
        //ent.rotationYaw = g
        //ent.rotationPitch = h
        //ent.prevRotationYawHead = i
        //ent.rotationYawHead = j
        GlStateManager.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
    }
}