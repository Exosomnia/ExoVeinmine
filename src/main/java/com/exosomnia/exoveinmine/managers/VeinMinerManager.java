package com.exosomnia.exoveinmine.managers;

import com.exosomnia.exoveinmine.controllers.VeinMinerController;

import java.util.*;

public class VeinMinerManager {

    Map<UUID, Boolean> players = new HashMap<>(); //Tracks players and whether their vein mining is active or not
    Map<UUID, List<VeinMinerController>> actions = new HashMap<>(); //Tracks vein mining actions and the owners

    public void setPlayerActive(UUID uuid, Boolean active) {
        players.put(uuid, active);
    }

    public Boolean getPlayerActive(UUID uuid) {
        return players.get(uuid);
    }

    public void addPlayer(UUID uuid) {
        players.put(uuid, false);
        actions.put(uuid, new ArrayList<>());
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
        actions.remove(uuid);
    }

    public void createController(UUID uuid, VeinMinerController controller) {
        actions.get(uuid).add(controller);
    }

    //Returns true is the manager processed controllers for the player, false otherwise.
    public boolean processControllers(UUID uuid) {
        List<VeinMinerController> playerControllers = actions.get(uuid);
        if (playerControllers == null || playerControllers.isEmpty()) return false;

        playerControllers.removeIf(VeinMinerController::iterate);
        return true;
    }
}
