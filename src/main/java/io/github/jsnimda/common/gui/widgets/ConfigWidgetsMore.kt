package io.github.jsnimda.common.gui.widgets

import io.github.jsnimda.common.config.options.ConfigHotkey
import io.github.jsnimda.common.gui.Tooltips
import io.github.jsnimda.common.gui.screen.ConfigOptionHotkeyDialog
import io.github.jsnimda.common.input.GlobalInputHandler
import io.github.jsnimda.common.input.IKeybind
import io.github.jsnimda.common.math2d.Rectangle
import io.github.jsnimda.common.vanilla.VanillaUtil
import io.github.jsnimda.common.vanilla.alias.I18n
import io.github.jsnimda.common.vanilla.alias.Identifier
import io.github.jsnimda.common.vanilla.render.Sprite
import io.github.jsnimda.common.vanilla.render.rDrawSprite
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT

private val WIDGETS_TEXTURE = Identifier("inventoryprofiles", "textures/gui/widgets.png")
private val baseSprite = Sprite(WIDGETS_TEXTURE, Rectangle(20, 160, 20, 20))
private val modifiedSprite = baseSprite.right()
private const val textPrefix = "inventoryprofiles.common.gui.config."
private fun translate(suffix: String): String {
  return I18n.translate(textPrefix + suffix)
}

class ConfigHotkeyWidget(configOption: ConfigHotkey) : ConfigWidgetBase<ConfigHotkey>(configOption) {
  private val setKeyButton = ButtonWidget { -> GlobalInputHandler.currentAssigningKeybind = configOption.mainKeybind }
  private val iconButton = object : ButtonWidget({ button ->
    if (button == GLFW_MOUSE_BUTTON_RIGHT) {
      targetKeybind.resetSettingsToDefault()
    } else if (button == GLFW_MOUSE_BUTTON_LEFT) {
      onClickKeybindSettingsIcon()
    }
  }) {
    override fun renderButton(hovered: Boolean) {
      val spriteX = if (targetKeybind.isSettingsModified || configOption.alternativeKeybinds.isNotEmpty())
        modifiedSprite else baseSprite
      val spriteY = spriteX.down(targetKeybind.settings.activateOn.ordinal)
      rDrawSprite(spriteY, screenX, screenY)
    }
  }

  var targetKeybind: IKeybind = configOption.mainKeybind
  val keybindDisplayText
    get() = targetKeybind.displayText.let {
      if (GlobalInputHandler.currentAssigningKeybind === targetKeybind) "> §e$it§r <" else it
    }

  override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
    setKeyButton.text = keybindDisplayText
    super.render(mouseX, mouseY, partialTicks)
    if (iconButton.contains(mouseX, mouseY)) { // show Advanced Keybind Settings
      Tooltips.addTooltip(keybindSettingsTooltipText, mouseX, mouseY)
    }
  }

  private val keybindSettingsTooltipText: String
    get() {
      val yes = translate("yes")
      val no = translate("no")
      return with(targetKeybind.settings) {
        """§n${translate("advanced_keybind_settings")}
          |${translate("activate_on")}: §9$activateOn
          |${translate("context")}: §9$context
          |${translate("allow_extra_keys")}: §6${if (allowExtraKeys) yes else no}
          |${translate("order_sensitive")}: §6${if (orderSensitive) yes else no}
          |${translate("modifier_key")}: §9$modifierKey
          |
          |${translate("keybind_settings_tips")}""".trimMargin()
      }
    }

  protected fun onClickKeybindSettingsIcon() {
    VanillaUtil.openScreen(ConfigOptionHotkeyDialog(configOption))
  }

  override fun reset() {
    targetKeybind.resetKeyCodesToDefault()
  }

  override fun resetButtonActive(): Boolean {
    return targetKeybind.isKeyCodesModified
  }

  init {
    flex.normal.add(iconButton, 20, false, 20)
    flex.normal.addSpace(2)
    flex.addAndFit(setKeyButton)
  }
}