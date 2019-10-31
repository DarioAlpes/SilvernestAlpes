package co.smartobjects.silvernestandroid.conteodeubicaciones

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.silvernestandroid.R

class AdaptadorUbicacionesContabilizables
(
        context: Context, @LayoutRes resource: Int
) : ArrayAdapter<Ubicacion>(context, resource)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val texto =
                if (convertView == null)
                {
                    View.inflate(context, R.layout.item_de_spinner_sencillo, null) as TextView
                }
                else
                {
                    convertView as TextView
                }

        return texto.apply {
            text = super.getItem(position)!!.nombre
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val texto =
                if (convertView == null)
                {
                    View.inflate(context, R.layout.item_de_spinner_sencillo, null) as TextView
                }
                else
                {
                    convertView as TextView
                }

        return texto.apply {
            text = super.getItem(position)!!.nombre
        }
    }
}
