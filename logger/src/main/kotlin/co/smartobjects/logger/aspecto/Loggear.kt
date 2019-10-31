package co.smartobjects.logger.aspecto

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Loggear(val contexto: String = "Contexto por defecto")