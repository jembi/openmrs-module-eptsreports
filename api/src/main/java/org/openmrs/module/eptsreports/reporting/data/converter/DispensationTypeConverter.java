package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

public class DispensationTypeConverter implements DataConverter {

  @Override
  public Object convert(Object obj) {
    if (obj == null) {
      return "";
    }

    switch (obj.toString()) {
      case "1098":
        return "Mensal";
      case "23720":
        return "Trimestral";
      case "23888":
        return "Semestral";

      case "165175":
        return "Horario Normal De Expedinte";

      case "165176":
        return "Fora Do Horário";

      case "165177":
        return "Farmac/Farmácia Privada";

      case "165178":
        return "Dispensa Comunitária Via Provedor";

      case "165179":
        return "Dispensa Comunitária Via Ape";

      case "165180":
        return "Brigadas Móveis Diurnas";

      case "165181":
        return "Brigadas Moveis Noturnas(Hotspots)";

      case "165182":
        return "Clinicas Moveis Diurnas ";

      case "165183":
        return "Clinicas Moveis Noturnas(Hotspots)";

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
