package de.rwth.idsg.steve.web.api;

import de.rwth.idsg.steve.repository.OcppServerRepository;
import de.rwth.idsg.steve.web.api.exception.BadRequestException;
import de.rwth.idsg.steve.web.dto.UpdateChargeBoxPkForm;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/chargeBox", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ChargeBoxController {
    private final OcppServerRepository ocppServerRepository;
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ApiControllerAdvice.ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiControllerAdvice.ApiErrorResponse.class)}
    )
    @PostMapping(value="/updatePublicKey")
    public void updatePublicKey(@RequestBody @Valid UpdateChargeBoxPkForm params){
        try {
            ocppServerRepository.updateChargeBoxPrivateKey(params.chargeBoxId, params.publicKey);
        }catch(Exception ex){
            log.error(String.format("Could not update publicKey for ChargeBox {0}. Reason: {1}",params.chargeBoxId,ex.getMessage()));
            throw new BadRequestException(ex.getMessage());
        }
    }
}
