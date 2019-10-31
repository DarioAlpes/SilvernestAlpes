from CJM.entidades.eventos.ReporteVisita import ReporteVisita

from commons.entidades.locations.Ubicacion import Ubicacion


class ReporteVisitaPorTipo:
    INITIAL_TIME_NAME = ReporteVisita.INITIAL_TIME_NAME
    FINAL_TIME_NAME = ReporteVisita.FINAL_TIME_NAME
    UBICACION_ID_NAME = Ubicacion.ID_UBICACION_NAME
    UBICACION_TYPE_NAME = Ubicacion.TYPE_NAME
    UBICACION_NAME = "location-name"
    REPORT_NAME = "report"

    def __init__(self):
        self._visits_per_location = []

    def add_report_for_location(self, id_location, location_name, report):
        self._visits_per_location.append(_ReporteByIdLocation(id_location, location_name, report))

    def to_list(self):
        return self._visits_per_location


class _ReporteByIdLocation:
    def __init__(self, id_location, location_name, report):
        self.id_location = id_location
        self.location_name = location_name
        self.report = report

    def to_dict(self):
        fields_dict = dict()
        fields_dict[ReporteVisitaPorTipo.UBICACION_ID_NAME] = self.id_location
        fields_dict[ReporteVisitaPorTipo.UBICACION_NAME] = self.location_name
        fields_dict[ReporteVisitaPorTipo.REPORT_NAME] = self.report.to_dict()
        return fields_dict
