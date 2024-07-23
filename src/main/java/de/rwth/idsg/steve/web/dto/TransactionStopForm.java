package de.rwth.idsg.steve.web.dto;

import io.swagger.annotations.ApiModelProperty;
import ocpp.cs._2015._10.StopTransactionRequest;

public class TransactionStopForm extends StopTransactionRequest {
    @ApiModelProperty(value = "The identifier of the chargebox (i.e. charging station)")
    public String chargeBoxId;

    @ApiModelProperty(value = "The identifier of the connector in the chargebox (i.e. charging station)")
    public int connectorId;
}
