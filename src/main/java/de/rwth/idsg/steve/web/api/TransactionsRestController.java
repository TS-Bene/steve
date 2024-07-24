/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2024 SteVe Community Team
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.rwth.idsg.steve.web.api;

import de.rwth.idsg.steve.ocpp.OcppTransport;
import de.rwth.idsg.steve.repository.TransactionRepository;
import de.rwth.idsg.steve.repository.dto.ChargePointSelect;
import de.rwth.idsg.steve.repository.dto.Transaction;
import de.rwth.idsg.steve.service.CentralSystemService16_Service;
import de.rwth.idsg.steve.service.ChargePointService16_Client;
import de.rwth.idsg.steve.web.api.ApiControllerAdvice.ApiErrorResponse;
import de.rwth.idsg.steve.web.api.exception.BadRequestException;
import de.rwth.idsg.steve.web.dto.TransactionQueryForm;
import de.rwth.idsg.steve.web.dto.TransactionStartForm;
import de.rwth.idsg.steve.web.dto.TransactionStopForm;
import de.rwth.idsg.steve.web.dto.ocpp.RemoteStartTransactionParams;
import de.rwth.idsg.steve.web.dto.ocpp.RemoteStopTransactionParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 13.09.2022
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TransactionsRestController {

    private final TransactionRepository transactionRepository;

    //private final OcppServerRepository ocppServerRepository;

    private final ChargePointService16_Client chargePoint16Service;

    //private final CentralSystemService16_Service centralService16;

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request", response = ApiErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiErrorResponse.class)}
    )
    @GetMapping(value = "")
    @ResponseBody
    public List<Transaction> get(@Valid TransactionQueryForm.ForApi params) {
        log.debug("Read request for query: {}", params);

        if (params.isReturnCSV()) {
            throw new BadRequestException("returnCSV=true is not supported for API calls");
        }

        var response = transactionRepository.getTransactions(params);
        log.debug("Read response for query: {}", response);
        return response;
    }

    @PostMapping(value = "/start")
    @ResponseBody
    public void startTransaction(@RequestBody @Valid TransactionStartForm params) {
//TODO: timestamp optional?
        //Queues the transaction to start on wallbox
        var chargePoint = new ChargePointSelect(OcppTransport.JSON, params.chargeBoxId);
        var transParams = new RemoteStartTransactionParams();
        transParams.setChargePointSelectList(Arrays.asList(chargePoint));
        transParams.setConnectorId(params.getConnectorId());
        transParams.setIdTag(params.getIdTag());
        //TODO: Wait until task is completed before trying to get the active transactions
        chargePoint16Service.remoteStartTransaction(transParams);//response should be TaskId

        //Starts the transaction in the Steve Backend
        //return centralService16.startTransaction(params, params.chargeBoxId);
    }

    @PostMapping(value = "/stop")
    public void stopTransaction(@RequestBody @Valid TransactionStopForm params) {
        //TODO: way reliable way to get the transaction id of open transaction(s)
        // for chargePoint + connector + TagId (TagId can not be in more than one active transaction)
        Integer activeTransactionId = transactionRepository.getActiveTransactionIdOnConnector(params.chargeBoxId, params.getIdTag(), params.connectorId);
        if (activeTransactionId !=null) {
            log.info("Trying to stop Transaction with id " + activeTransactionId + " on chargebox " + params.chargeBoxId);
            //needs to contain transactionId, maybe more
            var chargePoint = new ChargePointSelect(OcppTransport.JSON, params.chargeBoxId);
            var transParams = new RemoteStopTransactionParams();
            transParams.setTransactionId(activeTransactionId);
            transParams.setChargePointSelectList(Arrays.asList(chargePoint));
            chargePoint16Service.remoteStopTransaction(transParams);
            //return centralService16.stopTransaction(params, params.chargeBoxId);
        } else {
            String message="No active transactions found";
            log.warn(message);
            //Throwing Exception to cause non 200 Response code
            throw new BadRequestException(message);
        }
    }
}
