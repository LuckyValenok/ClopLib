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

package net.william278.cloplib.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public final class FireUpdates {

    @NotNull
    public static final Event<BeforeFireSpreads> BEFORE_FIRE_SPREAD = EventFactory.createArrayBacked(
            BeforeFireSpreads.class,
            (callbacks) -> (world, pos) -> {
                for (BeforeFireSpreads listener : callbacks) {
                    final ActionResult result = listener.spreads(world, pos);
                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            }
    );

    @NotNull
    public static final Event<BeforeFireBurns> BEFORE_FIRE_BURNS = EventFactory.createArrayBacked(
            BeforeFireBurns.class,
            (callbacks) -> (world, pos) -> {
                for (BeforeFireBurns listener : callbacks) {
                    final ActionResult result = listener.burns(world, pos);
                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            }
    );

    @FunctionalInterface
    public interface BeforeFireSpreads {

        @NotNull
        ActionResult spreads(World world, BlockPos pos);

    }

    @FunctionalInterface
    public interface BeforeFireBurns {

        @NotNull
        ActionResult burns(World world, BlockPos pos);

    }

}
