package com.cs6310.Controller;

import com.cs6310.Services.SimulationService;
import com.cs6310.Services.SimulationState;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "api")
@CrossOrigin
public class ApiController {

    @Resource(name = "SimulationService")
    SimulationService srv;

    @GetMapping(value = "/state")
    public SimulationState state() {
        return srv.getState();
    }

    @GetMapping(value = "/next")
    public SimulationState nextMove() {
        return srv.nextStep(false);
    }

    @GetMapping(value = "/fastforward")
    public SimulationState fastForward() {
        return srv.nextStep(true);
    }

    @GetMapping(value = "/stop")
    @RestartScope
    public SimulationState stop() {
        srv.stop();
        return srv.getState();
    }
}
