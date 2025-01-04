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
import lombok.Getter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.william278.cloplib.events.*;
import net.william278.cloplib.handler.Handler;
import net.william278.cloplib.handler.SpecialTypeChecker;
import net.william278.cloplib.handler.TypeChecker;
import net.william278.cloplib.operation.OperationPosition;
import net.william278.cloplib.operation.OperationType;
import net.william278.cloplib.operation.OperationUser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A listener for Fabric callbacks that can be used to cancel operations
 */
@Getter
public abstract class FabricOperationListener implements OperationListener, FabricWorldListener, FabricBreakListener,
        FabricUseItemListener, FabricUseBlockListener, FabricUseEntityListener, FabricEntityDamageListener,
        FabricEntityListener, FabricBlockMoveListener, FabricFireListener, FabricMoveListener {

    private final Handler handler;
    private final TypeChecker checker;
    private final Map<InspectorCallbackProvider.InspectionTool, BiConsumer<OperationUser, OperationPosition>> inspectionToolHandlers;

    // Maps of registry blocks to operation types, precalculated on data (re)load for perf
    private final Map<Item, OperationType> precalculatedItemMap = Maps.newHashMap();
    private final Map<Block, OperationType> precalculatedBlockMap = Maps.newHashMap();

    @SuppressWarnings("unused")
    public FabricOperationListener(@NotNull Handler handler, @NotNull ModContainer modContainer) {
        this(
                handler,
                SpecialTypeChecker.load(Objects.requireNonNull(
                        getSpecialTypes(modContainer),
                        "Failed to load special types file")
                ),
                Maps.newHashMap()
        );
    }

    @ApiStatus.Internal
    public FabricOperationListener(@NotNull Handler handler, @NotNull SpecialTypeChecker checker,
                                   @NotNull HashMap<InspectorCallbackProvider.InspectionTool, BiConsumer<OperationUser, OperationPosition>> map) {
        this.handler = handler;
        this.checker = checker;
        this.inspectionToolHandlers = map;
        this.registerCallbacks();
    }

    private void registerCallbacks() {
        // Register implemented callback event handlers
        PlayerBlockBreakEvents.BEFORE.register(this::onPlayerBreakBlock);
        AttackEntityCallback.EVENT.register(this::onPlayerAttackEntity);
        UseItemCallback.EVENT.register(this::onPlayerUseItem);
        UseBlockCallback.EVENT.register(this::onPlayerUseBlock);
        UseEntityCallback.EVENT.register(this::onPlayerUseEntity);
        LecternEvents.BEFORE_BOOK_TAKEN.register(this::onPlayerTakeLecternBook);
        PressureBlockEvents.BEFORE_COLLISION.register(this::onEntityPhysicallyInteract);
        ProjectileEvents.BEFORE_BLOCK_HIT.register(this::onProjectileHitBlock);
        ProjectileEvents.BEFORE_ENTITY_HIT.register(this::onProjectileHitEntity);
        DispenserEvents.BEFORE_PLACE.register(this::onDispenserPlace);
        FluidEvents.BEFORE_FLOW.register(this::onBlockFromTo);
        PistonEvents.BEFORE_ACTUATION.register(this::onPistonActuate);
        RaidEvents.BEFORE_START.register(this::onRaidTriggered);
        FireTickEvents.BEFORE_BURN.register(this::onBlockBurn);
        FireTickEvents.BEFORE_SPREAD.register(this::onFireSpread);
        EnchantmentEffectEvents.BEFORE_BLOCK_UPDATE.register(this::onPlayerFrostWalker);
        SpawnEvents.BEFORE_MOB_SPAWN.register(this::onCreatureSpawn);
        ExplosionEvents.BEFORE_BLOCKS_BROKEN.register(this::onExplosionBreakBlocks);
        ExplosionEvents.BEFORE_DAMAGE_ENTITY.register(this::onExplosionDamageEntity);
        PlayerMovementEvents.BEFORE_MOVE.register(this::onPlayerMove);

        // Register handlers for precalculating data
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(this::onDataReloaded);
    }

    // Recalculate block operation map when the server starts
    private void onServerStarted(MinecraftServer server) {
        this.precalculateMaps();
    }

    // Recalculate block operation map when the data pack is reloaded
    private void onDataReloaded(MinecraftServer server, LifecycledResourceManager manager, boolean b) {
        this.precalculateMaps();
    }

    private void precalculateMaps() {
        precalculatedItemMap.clear();
        this.precalculateItems(precalculatedItemMap);
        precalculatedBlockMap.clear();
        this.precalculateBlocks(precalculatedBlockMap);
    }

    @Nullable
    private static InputStream getSpecialTypes(@NotNull ModContainer modContainer) {
        return modContainer.findPath(SPECIAL_TYPES_FILE)
                .map(path -> {
                    try {
                        return Files.newInputStream(path);
                    } catch (IOException ignored) {
                        return null;
                    }
                })
                .orElse(FabricOperationListener.class.getClassLoader().getResourceAsStream(SPECIAL_TYPES_FILE));
    }

    /**
     * Returns the {@link OperationPosition} of a pos and world
     *
     * @param pos   the location
     * @param world the world
     * @param yaw   the yaw
     * @param pitch the pitch
     * @return the OperationPosition of the location
     * @since 1.1
     */
    @NotNull
    public abstract OperationPosition getPosition(@NotNull Vec3d pos, @NotNull net.minecraft.world.World world,
                                                  float yaw, float pitch);

    /**
     * Returns the {@link OperationUser} of a {@link PlayerEntity}
     *
     * @param player the player
     * @return the OperationUser of the player
     * @since 1.1
     */
    @NotNull
    public abstract OperationUser getUser(@NotNull PlayerEntity player);

    /**
     * Set the callback for when a player inspects a block while holding something
     *
     * @param tool     the tool the user must be holding to trigger the callback
     * @param callback the callback to set
     * @since 1.1
     */
    @Override
    public void setInspectorCallback(@NotNull InspectorCallbackProvider.InspectionTool tool,
                                     @NotNull BiConsumer<OperationUser, OperationPosition> callback) {
        inspectionToolHandlers.put(tool, callback);
    }

}