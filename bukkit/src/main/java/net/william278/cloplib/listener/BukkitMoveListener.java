/*
 * This file is part of ClopLib, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.cloplib.listener;

import net.william278.cloplib.operation.Operation;
import net.william278.cloplib.operation.OperationType;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public interface BukkitMoveListener extends BukkitListener {

    @EventHandler(ignoreCancelled = true)
    default void onPlayerMove(@NotNull PlayerMoveEvent e) {
        final Location fromLocation = e.getFrom();
        final Location toLocation = e.getTo();
        if (toLocation == null || fromLocation.getBlock().equals(toLocation.getBlock())) {
            return;
        }

        // Handle cancelling moving between chunks, if needed
        if (getHandler().cancelMovement(getUser(e.getPlayer()), getPosition(fromLocation), getPosition(toLocation))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    default void onPlayerEnderPearl(@NotNull PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
                || e.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            if (getHandler().cancelOperation(Operation.of(
                    getUser(e.getPlayer()),
                    OperationType.ENDER_PEARL_TELEPORT,
                    getPosition(e.getFrom())
            ))) {
                e.setCancelled(true);
            }
        }
    }

}
