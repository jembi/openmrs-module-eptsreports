package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/** Converter to return the state of stay for ART patients */
public class StateOfStayArtPatientConverter implements DataConverter {

  final String activoPrograma = "Activo no Programa";

  final String transferredOut = "Transferido para";

  final String transferredIn = "Transferido de";

  final String suspended = "Suspensão";

  final String abandoned = "Abandono";

  final String died = "Óbito";

  final String autoTransfer = "Auto Transferência";

  final String restart = "Re-inicio";

  final String negativeDiagnosis = "HIV Negativo";

  final String getTransferidoParaSectorTb = "Transferido Para Sector Tb";

  final String getTransferidoParaConsultasIntegradas = "Transferido Para Consultas Integradas";

  final String getTransferidoParaConsultaDeCriancaSadia =
      "Transferido Para Consulta De Crianca Sadia";

  final String ccrCured = "CCR Curado";

  @Override
  public Object convert(Object obj) {
    if (obj == null) {
      return "";
    }

    switch (obj.toString()) {
      case "7":
        return transferredOut;
      case "8":
        return suspended;
      case "9":
        return abandoned;
      case "10":
        return died;
      case "11":
        return activoPrograma;
      case "14":
        return abandoned;
      case "15":
        return died;
      case "23":
        return ccrCured;
      case "31":
        return transferredIn;
      case "32":
        return transferredOut;
      case "83":
        return getTransferidoParaSectorTb;
      case "84":
        return getTransferidoParaConsultasIntegradas;
      case "85":
        return getTransferidoParaConsultaDeCriancaSadia;
      case "1706":
        return transferredOut;
      case "1709":
        return suspended;
      case "1707":
        return abandoned;
      case "1366":
        return died;
      case "23863":
        return autoTransfer;
      case "23903":
        return negativeDiagnosis;
      case "1369":
        return transferredIn;
      case "6269":
        return activoPrograma;
      case "6":
        return activoPrograma;
      case "29":
        return transferredIn;
      case "1705":
        return restart;
      case "165483":
        return getTransferidoParaSectorTb;
      case "165484":
        return getTransferidoParaConsultasIntegradas;
      case "165485":
        return getTransferidoParaConsultaDeCriancaSadia;
      default:
        return "";
    }
  }

  @Override
  public Class<?> getInputDataType() {
    return String.class;
  }

  @Override
  public Class<?> getDataType() {
    return String.class;
  }
}
