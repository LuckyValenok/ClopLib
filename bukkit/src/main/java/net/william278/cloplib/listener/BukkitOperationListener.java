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

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.william278.cloplib.handler.Handler;
import net.william278.cloplib.handler.SpecialTypeChecker;
import net.william278.cloplib.handler.TypeChecker;
import net.william278.cloplib.operation.OperationPosition;
import net.william278.cloplib.operation.OperationUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A listener for Bukkit events that can be used to cancel operations
 */
@Getter
@AllArgsConstructor
public abstract class BukkitOperationListener implements OperationListener, BukkitInteractListener,
        BukkitEntityDamageListener, BukkitPlaceListener, BukkitBreakListener, BukkitBlockMoveListener,
        BukkitPortalListener, BukkitMoveListener, BukkitEntityListener, BukkitFireListener, BukkitWorldListener {

    private final Handler handler;
    private final TypeChecker checker;
    private final Map<InspectionTool, BiConsumer<OperationUser, OperationPosition>> inspectionToolHandlers;

    @SuppressWarnings("unused")
    public BukkitOperationListener(@NotNull Handler handler, @NotNull JavaPlugin plugin) {
        this(
                handler,
                SpecialTypeChecker.load(Objects.requireNonNull(
                        plugin.getResource(SPECIAL_TYPES_FILE),
                        "Failed to load special types file")
                ),
                Maps.newHashMap()
        );
    }

    /**
     * Returns the {@link OperationPosition} of a {@link Location}
     *
     * @param location the location
     * @return the OperationPosition of the location
     * @since 1.0
     */
    @NotNull
    public abstract OperationPosition getPosition(@NotNull Location location);

    /**
     * Returns the {@link OperationUser} of a {@link Player}
     *
     * @param player the player
     * @return the OperationUser of the player
     * @since 1.0
     */
    @NotNull
    public abstract OperationUser getUser(@NotNull Player player);

    /**
     * Set the callback for when a player inspects a block while holding something
     *
     * @param tool     the tool the user must be holding to trigger the callback
     * @param callback the callback to set
     * @since 1.0.5
     */
    @Override
    public void setInspectorCallback(@NotNull InspectionTool tool,
                                     @NotNull BiConsumer<OperationUser, OperationPosition> callback) {
        inspectionToolHandlers.put(tool, callback);
    }

}
