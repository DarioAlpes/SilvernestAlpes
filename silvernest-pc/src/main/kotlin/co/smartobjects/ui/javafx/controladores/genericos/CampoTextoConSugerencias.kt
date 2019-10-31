package co.smartobjects.ui.javafx.controladores.genericos

import com.jfoenix.controls.JFXTextField
import com.sun.javafx.scene.control.skin.ContextMenuContent
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Side
import javafx.scene.control.ContextMenu
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyEvent
import java.util.*


typealias CallbackClick = () -> Unit

internal class CampoTextoConSugerencias : JFXTextField()
{
    private val sugerencias: SortedSet<String>
    private val entriesPopup: ContextMenu
    var callbackClickEnSugerencia: CallbackClick? = null

    init
    {
        sugerencias = TreeSet<String>()

        entriesPopup = ContextMenu()
        textProperty().addListener { _, _, _ ->
            if (text.isEmpty())
            {
                entriesPopup.hide()
            }
            else
            {
                val sugerenciasEncontradas = sugerencias.filter {
                    it.toUpperCase().startsWith(text.toUpperCase())
                }

                if (sugerencias.size > 0)
                {
                    rellenarPopup(sugerenciasEncontradas)
                    if (!entriesPopup.isShowing)
                    {
                        entriesPopup.show(this@CampoTextoConSugerencias, Side.BOTTOM, 0.0, 0.0)
                    }
                }
                else
                {
                    entriesPopup.hide()
                }
            }
        }

        focusedProperty().addListener { _, _, _ -> entriesPopup.hide() }

        if (entriesPopup.skin != null)
        {
            val cmc = entriesPopup.skin.node as ContextMenuContent
            val menu_item_handler_cmc = MenuItemTabHandler(cmc)
            cmc.addEventHandler(KeyEvent.KEY_PRESSED, menu_item_handler_cmc)
        }
    }

    /**
     * Populate the entry set with the given search results.  Display is limited to 10 sugerencias, for performance.
     * @param searchResult The set of matching strings.
     */
    private fun rellenarPopup(searchResult: List<String>)
    {
        val menuItems = LinkedList<CustomMenuItem>()
        // If you'd like more entries, modify this line.
        val maxEntries = 10
        val count = Math.min(searchResult.size, maxEntries)
        for (i in 0 until count)
        {
            val result = searchResult[i]
            val entryLabel = Label(result)
            val item = CustomMenuItem(entryLabel, true)
            item.onAction = EventHandler<ActionEvent> {
                text = result
                entriesPopup.hide()
                this@CampoTextoConSugerencias.selectEnd()
                this@CampoTextoConSugerencias.deselect()
                callbackClickEnSugerencia?.invoke()
            }
            menuItems.add(item)
        }
        entriesPopup.items.clear()
        entriesPopup.items.addAll(menuItems)
    }

    fun usarSugerencias(sugerenciasNuevas: List<String>)
    {
        sugerencias.clear()
        sugerencias += sugerenciasNuevas
    }

    private class MenuItemTabHandler(private val cmc: ContextMenuContent) : EventHandler<KeyEvent>
    {
        override fun handle(event: KeyEvent)
        {
            val ke = event
            if (ke.code == javafx.scene.input.KeyCode.TAB)
            {
                ke.consume()
                findFocusedMenuItem()?.fire()
            }
        }

        fun findFocusedMenuItem(): MenuItem?
        {
            val items_container = cmc.itemsContainer
            for (i in 0 until items_container.children.size)
            {
                val n = items_container.children[i]
                if (n.isFocused)
                {
                    val menu_item_container = n as ContextMenuContent.MenuItemContainer
                    val menu_item = menu_item_container.item
                    return menu_item
                }
            }
            return null
        }
    }
}