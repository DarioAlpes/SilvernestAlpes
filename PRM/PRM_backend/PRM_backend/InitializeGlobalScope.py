from commons.entidades.Generador import Generador
from commons.entidades.users.Usuario import Usuario


def initialize_global_variables():
    Generador.initialize_global_generators()
    from main import app
    with app.app_context():
        Usuario.create_global_admin_user()
