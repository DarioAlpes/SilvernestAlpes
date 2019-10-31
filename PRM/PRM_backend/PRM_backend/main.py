# -*- coding: utf-8 -*
import os
import re
from datetime import timedelta

from flask import Flask
from flask_limiter import Limiter

from InitializeGlobalScope import initialize_global_variables
from commons.excepciones.apiexceptions import APIException
from commons.excepciones.apiexceptions import NotLoggedInError
from commons.excepciones.apiexceptionshandlers import handle_api_exception as api_exception_handler
from commons.services.clienteView import enable_auth
from config_loader import load_secret, is_prod, is_pre_prod
from flask_cors.extension import CORS
from flask_limiter.util import get_remote_address
from flask_login.login_manager import LoginManager
from passlib.context import CryptContext
import requests_toolbelt.adapters.appengine

# Para poderlo correr localmente toca forzar URLFetch. Remoto tiene que usar sockets o falla
if not os.environ.get('SERVER_SOFTWARE', '').startswith('Google App Engine'):
    if os.name is None and not os.environ.get('SERVER_SOFTWARE', '') == "":
        requests_toolbelt.adapters.appengine.monkeypatch(True)

pwd_context = CryptContext(
    schemes=["pbkdf2_sha256"],
    deprecated="auto"
)


class HTTPMethodOverrideMiddleware(object):
    allowed_methods = frozenset([
        'GET',
        'HEAD',
        'POST',
        'DELETE',
        'PUT',
        'PATCH',
        'OPTIONS'
    ])
    bodyless_methods = frozenset(['GET', 'HEAD', 'OPTIONS', 'DELETE'])

    def __init__(self, current_app):
        self.app = current_app

    def __call__(self, environ, start_response):
        method = environ.get('HTTP_X_HTTP_METHOD_OVERRIDE', '').upper()
        if method in self.allowed_methods:
            method = method.encode('ascii', 'replace')
            environ['REQUEST_METHOD'] = method
        if method in self.bodyless_methods:
            environ['CONTENT_LENGTH'] = '0'
        return self.app(environ, start_response)


def login_rate_limit_from_config():
    return app.config.get("LOGIN_LIMIT_BY_IP", "20/minute")


app = Flask(__name__)
app.config['PERMANENT_SESSION_LIFETIME'] = timedelta(hours=2)
app.config['REMEMBER_COOKIE_DURATION'] = timedelta(days=365)
app.config['REMEMBER_COOKIE_SECURE'] = True
app.config['SESSION_COOKIE_SECURE'] = True
if is_prod():
    origins = ["https://silvernest-front.appspot.com"]
elif is_pre_prod():
    origins = ["https://silvernest-front-pruebas.appspot.com"]
else:
    origins = [re.compile(r"^https?://localhost(:\d{1,5})?$", re.IGNORECASE), "https://smart-objects-front.appspot.com"]


def matches_allowed_origin(origin):
    for possible_origin in origins:
        try:
            # noinspection PyUnresolvedReferences
            if possible_origin.match(origin):
                return True
        except AttributeError:
            if origin == possible_origin:
                return True


CORS(app, supports_credentials=True, origins=origins)

app.secret_key = load_secret()

app.wsgi_app = HTTPMethodOverrideMiddleware(app.wsgi_app)
login_manager = LoginManager()
login_manager.init_app(app)
login_manager.session_protection = "strong"
limiter = Limiter(app, key_func=get_remote_address)

login_limit = limiter.shared_limit(login_rate_limit_from_config, scope="login")


@login_manager.unauthorized_handler
def unauthorized():
    raise NotLoggedInError()


def init_blueprints():
    from CJM.services.beacons.clientBeaconsView import app as client_beacons_view
    from CJM.services.beacons.globalBeaconsView import app as global_beacons_view
    from CJM.services.currencyView import app as currency_view
    from CJM.services.eventos.accionesView import app as actions_view
    from CJM.services.eventos.comprasView import app as purchases_view
    from CJM.services.eventos.devolucionesView import app as refunds_view
    from CJM.services.eventos.eventsView import app as events_view
    from CJM.services.eventos.feedbacksView import app as feedbacks_view
    from CJM.services.eventos.pedidosView import app as timeline_orders_view
    from CJM.services.eventos.visitasView import app as visits_view
    from CJM.services.lecturas.readingsView import app as readings_view
    from CJM.services.measures.measuresView import app as measures_view
    from CJM.services.paquetes.accesoPaqueteView import app as access_view
    from CJM.services.paquetes.consumoCantidadPaqueteView import app as amount_consumption_view
    from CJM.services.paquetes.consumoDineroPaqueteView import app as money_consumption_view
    from CJM.services.reservas.eventoSocialView import app as social_event_view
    from CJM.services.paquetes.packagePriceRuleView import app as package_price_rules_view
    from CJM.services.paquetes.paqueteView import app as packages_view
    from CJM.services.reservas.orders.personAccessView import app as person_access_view
    from CJM.services.reservas.accessTopoffsView import app as access_topoffs_view
    from CJM.services.reservas.amountTopoffsView import app as amount_topoffs_view
    from CJM.services.reservas.moneyTopoffsView import app as money_topoffs_view
    from CJM.services.reservas.reservaPersonaView import app as person_reservation_view
    from CJM.services.reservas.reservaView import app as reservation_view
    from CJM.services.reservas.orders.orderView import app as orders_view
    from CJM.services.reservas.orders.moneyConsumptionView import app as money_consumptions_view
    from CJM.services.reservas.orders.amountConsumptionView import app as amount_consumptions_view
    from CJM.services.persons.imagePersonView import app as person_images_view
    from CJM.services.persons.personaView import app as persona_view
    from CJM.services.persons.personsRelationshipsView import app as persons_relationships_view
    from CJM.services.persons.personFieldOptionalityView import app as person_optionalities_view
    from CJM.services.sensores.clientSensorsView import app as client_sensors_view
    from CJM.services.sensores.globalSensorsView import app as global_sensors_view
    from CJM.services.skus.categoriaSKUView import app as sku_category_view
    from CJM.services.skus.imageSKUView import app as sku_images_view
    from CJM.services.skus.skuView import app as sku_view
    from CJM.services.skus.ubicacionVentaSKUView import app as sku_sales_locations_view
    from CJM.services.tags.supportedTagsView import app as supported_tags_view
    from CJM.services.tags.clientTagsView import app as client_tags_view
    from CJM.services.reports.transactionsPerUserView import app as transactions_per_user_view
    from CJM.services.reports.entititesByKindPerUserView import app as entitites_per_user_view
    from CJM.services.reports.consumptionsPerSkuView import app as consumptions_per_sku_view
    from commons.services.clienteView import app as client_view
    from commons.services.logsView import app as logs_view
    from commons.services.locations.imageLocationView import app as location_images_view
    from commons.services.locations.locationFieldOptionalityView import app as location_fields_optionality_view
    from commons.services.locations.locationTagView import app as location_tags_view
    from commons.services.locations.ubicacionView import app as locations_view
    from commons.services.users.usuarioView import app as users_view, load_user_from_user_token

    app.register_blueprint(client_view)
    app.register_blueprint(logs_view)
    app.register_blueprint(locations_view)
    app.register_blueprint(location_tags_view)
    app.register_blueprint(location_images_view)
    app.register_blueprint(location_fields_optionality_view)
    app.register_blueprint(users_view)
    app.register_blueprint(supported_tags_view)
    app.register_blueprint(client_tags_view)

    app.register_blueprint(readings_view)
    app.register_blueprint(global_beacons_view)
    app.register_blueprint(client_beacons_view)
    app.register_blueprint(global_sensors_view)
    app.register_blueprint(client_sensors_view)
    app.register_blueprint(persona_view)
    app.register_blueprint(persons_relationships_view)
    app.register_blueprint(person_optionalities_view)
    app.register_blueprint(currency_view)
    app.register_blueprint(sku_view)
    app.register_blueprint(sku_category_view)
    app.register_blueprint(sku_sales_locations_view)
    app.register_blueprint(sku_images_view)
    app.register_blueprint(person_images_view)
    app.register_blueprint(packages_view)
    app.register_blueprint(package_price_rules_view)
    app.register_blueprint(social_event_view)
    app.register_blueprint(amount_consumption_view)
    app.register_blueprint(money_consumption_view)
    app.register_blueprint(access_view)
    app.register_blueprint(reservation_view)
    app.register_blueprint(person_reservation_view)
    app.register_blueprint(person_access_view)
    app.register_blueprint(money_topoffs_view)
    app.register_blueprint(amount_topoffs_view)
    app.register_blueprint(access_topoffs_view)
    app.register_blueprint(measures_view)
    app.register_blueprint(visits_view)
    app.register_blueprint(timeline_orders_view)
    app.register_blueprint(events_view)
    app.register_blueprint(feedbacks_view)
    app.register_blueprint(actions_view)
    app.register_blueprint(purchases_view)
    app.register_blueprint(refunds_view)

    app.register_blueprint(orders_view)
    app.register_blueprint(money_consumptions_view)
    app.register_blueprint(amount_consumptions_view)

    app.register_blueprint(transactions_per_user_view)
    app.register_blueprint(entitites_per_user_view)
    app.register_blueprint(consumptions_per_sku_view)

    @login_manager.user_loader
    def load_user(username):
        return load_user_from_user_token(username)


init_blueprints()
initialize_global_variables()
if is_prod() or is_pre_prod():
    enable_auth()


def prepare_for_testing(num_default_locations):
    global pwd_context
    pwd_context = CryptContext(
        schemes=["pbkdf2_sha256"],
        deprecated="auto",
        pbkdf2_sha256__rounds=1
    )
    app.config["LOGIN_LIMIT_BY_IP"] = "10000/second"
    enable_auth()
    initialize_global_variables()
    from commons.entidades.locations.Ubicacion import Ubicacion
    Ubicacion.trim_default_locations(num_default_locations)


@app.errorhandler(APIException)
@app.errorhandler(NotLoggedInError)
def handle_api_exceptions(error):
    return api_exception_handler(error)


if __name__ == '__main__':
    app.run()
