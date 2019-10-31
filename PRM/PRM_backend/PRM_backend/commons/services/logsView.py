# -*- coding: utf-8 -*
import smtplib
import traceback
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

from commons.excepciones.apiexceptions import ValidationError
from config_loader import get_pass_logs_mail
from flask import request, Blueprint

from commons.utils import with_json_body
from commons.validations import validate_string_not_empty, validate_id_client

LOGS_VIEW_NAME = "logs"
app = Blueprint(LOGS_VIEW_NAME, __name__)
_LOG_MESSAGE_NAME = "message"
_LOG_FILE_NAME = "filename"
_LOG_SUBJECT_NAME = "subject"
_LOGS_MAIL = "logs@smartobjects.co"


@app.route('/clients/<int:id_client>/logs/', methods=['POST'], strict_slashes=False)
@with_json_body
def send_log(id_client):
    validate_id_client(id_client)
    message = request.json.get(_LOG_MESSAGE_NAME)
    filename = request.json.get(_LOG_FILE_NAME)
    subject = request.json.get(_LOG_SUBJECT_NAME)
    validate_string_not_empty(message, _LOG_MESSAGE_NAME)
    validate_string_not_empty(filename, _LOG_FILE_NAME)
    validate_string_not_empty(subject, _LOG_SUBJECT_NAME)

    full_message = MIMEMultipart()
    full_message['Subject'] = subject
    full_message['From'] = _LOGS_MAIL
    full_message['To'] = _LOGS_MAIL
    full_message.preamble = ""
    attachment = MIMEText(message, _charset="utf-8")
    attachment.add_header('Content-Disposition', 'attachment', filename=filename)
    full_message.attach(attachment)
    password = get_pass_logs_mail()
    try:
        server_ssl = smtplib.SMTP_SSL("smtp.gmail.com", 465)
        server_ssl.ehlo()
        server_ssl.login(_LOGS_MAIL, password)
        server_ssl.sendmail(_LOGS_MAIL, [_LOGS_MAIL], full_message.as_string())
        server_ssl.close()
        return "{}"
    except Exception as e:
        traceback.print_exc()
        raise ValidationError(unicode(e.message))

