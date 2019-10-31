package co.smartobjects.nfc.lectorestags.parametrosDeAutenticacion

class ParametrosAutenticacionUltralight(
        val paginaInicialAutenticacion: Byte = 0xF,
        val maximoNumeroIntentos: Byte = 0xF,
        val protegerLectura: Boolean = false
                                       )