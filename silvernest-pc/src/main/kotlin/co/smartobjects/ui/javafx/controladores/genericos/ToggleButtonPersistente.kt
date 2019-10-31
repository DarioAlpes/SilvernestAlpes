package co.smartobjects.ui.javafx.controladores.genericos

import javafx.scene.control.ToggleButton


internal class ToggleButtonPersistente : ToggleButton()
{
    override fun fire()
    {
        if (!isDisable)
        {
            if (toggleGroup == null || !isSelected)
            {
                super.fire()
            }
        }
    }
}