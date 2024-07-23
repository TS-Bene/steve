package de.rwth.idsg.steve.web.dto;

import io.swagger.annotations.ApiModelProperty;
import ocpp.cs._2015._10.StartTransactionRequest;
import org.joda.time.DateTime;

public class TransactionStartForm extends StartTransactionRequest {
    @ApiModelProperty(value = "The identifier of the chargebox (i.e. charging station)")
    public String chargeBoxId;
}
