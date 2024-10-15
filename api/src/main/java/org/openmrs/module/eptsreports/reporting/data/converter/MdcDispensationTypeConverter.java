package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

public class MdcDispensationTypeConverter implements DataConverter {

  @Override
  public Object convert(Object obj) {
    if (obj == null) {
      return "";
    }

    switch (obj.toString()) {
      case "23888":
        return "DS";

      case "165175":
        return "Horario Normal De Expedinte";

      case "165176":
        return "Fora Do Horário";

      case "165177":
        return "Farmac/Farmácia Privada";

      case "165178":
        return "DCP";

      case "165179":
        return "DCA";

      case "165180":
        return "Brigadas Móveis Diurnas";

      case "165181":
        return "Brigadas Moveis Noturnas(Hotspots)";

      case "165182":
        return "Clinicas Moveis Diurnas ";

      case "165183":
        return "Clinicas Moveis Noturnas(Hotspots)";

      case "23730":
        return "DT";

      case "165264":
        return "BM";

      case "165265":
        return "CM";

      case "23725":
        return "AF";

      case "23729":
        return "FR";

      case "23724":
        return "AG";

      case "165317":
        return "TB";

      case "165318":
        return "CT";

      case "165319":
        return "SAAJ";

      case "165320":
        return "SMI";

      case "165321":
        return "DAH";

      case "165314":
        return "DA";

      case "165315":
        return "DD";

      case "23726":
        return "CA";

      case "165316":
        return "EH";

      case "23727":
        return "PU";

      case "165340":
        return "DB";

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
