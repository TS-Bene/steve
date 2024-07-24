package de.rwth.idsg.steve.web.dto;

import io.swagger.annotations.ApiModelProperty;

public class UpdateChargeBoxPkForm {
    @ApiModelProperty(value="The ChargeBox to update")
    public String chargeBoxId;
    @ApiModelProperty(value="The public key to set/update")
    public String publicKey;
}
