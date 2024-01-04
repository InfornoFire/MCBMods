package inforno.mcbmods.gui.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.universal.USound
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.gui.settings.SettingComponent
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color

/**
 * Based on Vigilance's DropDown
 */
class MCBModsDropDown(
    initialSelection: Int,
    private val options: List<String>,
    outlineEffect: OutlineEffect? = OutlineEffect(VigilancePalette.getDivider(), 1f),
    optionPadding: Float = 6f
) : UIBlock() {
    private var selected = initialSelection
    private var onValueChange: (Int) -> Unit = { }
    private var active = false

    private val currentSelectionText by UIText(options[selected]).constrain {
        x = 5.pixels()
        y = 6.pixels()
        color = VigilancePalette.getMidText().toConstraint()
        fontProvider = getFontProvider()
        textScale = 1.5.pixels()
    } childOf this

    private val downArrow by UIImage.ofResourceCached(SettingComponent.DOWN_ARROW_PNG).constrain {
        x = 5.pixels(true)
        y = 7.5.pixels()
        width = 18.pixels()
        height = 10.pixels()
    } childOf this

    private val upArrow by UIImage.ofResourceCached(SettingComponent.UP_ARROW_PNG).constrain {
        x = 5.pixels(true)
        y = 7.5.pixels()
        width = 18.pixels()
        height = 10.pixels()
    }

    private val scrollContainer by UIContainer().constrain {
        x = 5.pixels()
        y = SiblingConstraint(optionPadding) boundTo currentSelectionText
        width = ChildBasedMaxSizeConstraint()
        height = ChildBasedSizeConstraint() + optionPadding.pixels()
    } childOf this

    private val optionsHolder by ScrollComponent(customScissorBoundingBox = scrollContainer).constrain {
        x = 0.pixels()
        y = 0.pixels()
        height = (((options.size - 1) * (getFontProvider().getStringHeight(
            "Text",
            getTextScale()
        ) + optionPadding) - optionPadding).pixels()) coerceAtMost
                basicHeightConstraint {
                    Window.of(this@MCBModsDropDown).getBottom() - this@MCBModsDropDown.getTop() - 31
                }
    } childOf scrollContainer

    private val mappedOptions = options.mapIndexed { index, option ->
        UIText(option).constrain {
            y = SiblingConstraint(optionPadding)
            color = Color(0, 0, 0, 0).toConstraint()
            fontProvider = getFontProvider()
            textScale = 1.5.pixels()
        }.onMouseEnter {
            hoverText(this)
        }.onMouseLeave {
            unHoverText(this)
        }.onMouseClick { event ->
            event.stopPropagation()
            select(index)
        }
    }

    private val collapsedWidth = (22.pixels() + CopyConstraintFloat().to(currentSelectionText)) * 1.5

    private val expandedWidth = (22.pixels() + (ChildBasedMaxSizeConstraint().to(optionsHolder)) * 1.5 coerceAtLeast CopyConstraintFloat().to(
        currentSelectionText
    ))

    init {
        constrain {
            width = collapsedWidth
            height = 25.pixels()
            color = VigilancePalette.getDarkHighlight().toConstraint()
        }

        readOptionComponents()

        optionsHolder.hide(instantly = true)

        outlineEffect?.let(::enableEffect)

        val outlineContainer = UIContainer().constrain {
            x = (-1).pixels()
            y = (-1).pixels()
            width = RelativeConstraint(1f) + 2.pixels()
            height = RelativeConstraint(1f) + 3f.pixels()
        }
        outlineContainer.parent = this
        children.add(0, outlineContainer)
        enableEffect(ScissorEffect(outlineContainer))

        onMouseEnter {
            hoverText(currentSelectionText)
        }

        onMouseLeave {
            if (active) return@onMouseLeave

            unHoverText(currentSelectionText)
        }

        onLeftClick { event ->
            USound.playButtonPress()
            event.stopPropagation()

            if (active) {
                collapse()
            } else {
                expand()
            }
        }
    }

    fun select(index: Int) {
        if (index in options.indices) {
            selected = index
            onValueChange(index)
            currentSelectionText.setText(options[index])
            collapse()
            readOptionComponents()
        }
    }

    fun onValueChange(listener: (Int) -> Unit) {
        onValueChange = listener
    }

    fun getValue() = selected

    private fun expand() {
        active = true
        mappedOptions.forEach {
            it.setColor(VigilancePalette.getMidText().toConstraint())
        }

        animate {
            setHeightAnimation(
                Animations.IN_SIN,
                0.35f,
                25.pixels() + RelativeConstraint(1f).boundTo(scrollContainer)
            )
        }

        optionsHolder.scrollToTop(false)

        replaceChild(upArrow, downArrow)
        setFloating(true)
        optionsHolder.unhide(useLastPosition = true)
        setWidth(expandedWidth)
    }

    fun collapse(unHover: Boolean = false, instantly: Boolean = false) {
        if (active)
            replaceChild(downArrow, upArrow)
        active = false

        fun animationComplete() {
            mappedOptions.forEach {
                it.setColor(Color(0, 0, 0, 0).toConstraint())
            }
            setFloating(false)
            optionsHolder.hide(instantly = true)
        }

        if (instantly) {
            setHeight(25.pixels())
            animationComplete()
        } else {
            animate {
                setHeightAnimation(Animations.OUT_SIN, 0.35f, 25.pixels())

                onComplete(::animationComplete)
            }
        }

        if (unHover)
            unHoverText(currentSelectionText)

        setWidth(collapsedWidth)
    }

    private fun hoverText(text: UIComponent) {
        text.animate {
            setColorAnimation(Animations.OUT_EXP, 0.25f, VigilancePalette.getBrightText().toConstraint())
        }
    }

    private fun unHoverText(text: UIComponent) {
        text.animate {
            setColorAnimation(Animations.OUT_EXP, 0.25f, VigilancePalette.getMidText().toConstraint())
        }
    }

    private fun readOptionComponents() {
        optionsHolder.clearChildren()
        mappedOptions.forEachIndexed { index, component ->
            if (index != selected)
                component childOf optionsHolder
        }
    }
}
