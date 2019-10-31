package co.smartobjects.ui.javafx.controladores.menuprincipal

import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosMenuPrincipalView
import com.jfoenix.controls.JFXButton
import javafx.beans.NamedArg
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay

internal class BotonMenuPrincipal
(
        @NamedArg("nombreIcono") _nombreIcono: String
) : JFXButton()
{
    @Suppress("unused")
    private val nombreIcono: String = _nombreIcono

    init
    {
        styleClass.setAll("boton-icono-menu-principal-defecto")
        alignment = Pos.BOTTOM_CENTER
        contentDisplay = ContentDisplay.TOP
        graphic = PrompterIconosMenuPrincipalView().apply {
            glyphName = _nombreIcono
        }
    }
}