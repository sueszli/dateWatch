package at.ac.tuwien.sepm.groupphase.backend.domain.participation.controller;

import at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto.sse.ArrivalStatisticsDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.service.EventArrivalService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import java.security.Principal;


@Slf4j
@PermitAll
@RestController
@RequestMapping("${routes.rest.v1}/event/{token}/arrival")
@RequiredArgsConstructor
public class EventArrivalController {

    private final EventArrivalService eventArrivalService;


    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    @Operation(summary = "Indicate arrival")
    @RolesAllowed("PARTICIPANT")
    public void arrivedAtEvent(@PathVariable("token") String accessToken, @RequestBody String entranceCode, Principal user){
        eventArrivalService.participantArrivedAtEvent(accessToken, entranceCode, user.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    @Operation(summary = "Get all arrivals.")
    @RolesAllowed({"ORGANIZER"})
    public ArrivalStatisticsDto getArrivalStatistics(@PathVariable final String token, Principal user) {
        return eventArrivalService.getEventArrivalStatistics(token, user.getName());
    }
}
