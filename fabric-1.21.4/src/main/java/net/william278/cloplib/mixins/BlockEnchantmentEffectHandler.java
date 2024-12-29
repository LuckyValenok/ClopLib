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

package net.william278.cloplib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.william278.cloplib.events.EnchantmentEffectEvents;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface BlockEnchantmentEffectHandler {

    @Unique
    default void handleEnchantmentBlockEffect(ServerWorld world, Entity user, BlockPos blockPos, CallbackInfo ci) {
        final ActionResult result = EnchantmentEffectEvents.BEFORE_BLOCK_UPDATE.invoker().blockUpdate(user, world, blockPos);
        if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }

}
