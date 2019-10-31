package co.smartobjects.ui.javafx

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.MultipleSelectionModel

internal class ModeloDeSeleccionVacioListView<T> : MultipleSelectionModel<T>()
{
    override fun clearSelection(index: Int)
    {
    }

    override fun clearSelection()
    {
    }

    override fun selectLast()
    {
    }

    override fun isSelected(index: Int): Boolean
    {
        return false
    }

    override fun getSelectedIndices(): ObservableList<Int>
    {
        return FXCollections.emptyObservableList()
    }

    override fun selectAll()
    {
    }

    override fun getSelectedItems(): ObservableList<T>
    {
        return FXCollections.emptyObservableList()
    }

    override fun select(index: Int)
    {
    }

    override fun select(obj: T)
    {
    }

    override fun isEmpty(): Boolean
    {
        return true
    }

    override fun selectNext()
    {
    }

    override fun selectPrevious()
    {
    }

    override fun selectIndices(index: Int, vararg indices: Int)
    {
    }

    override fun selectFirst()
    {
    }

    override fun clearAndSelect(index: Int)
    {
    }
}