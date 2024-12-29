package net.william278.cloplib.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.william278.cloplib.events.FireUpdates;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Carefully prevent fire from doing specific things.
@Mixin(FireBlock.class)
public abstract class FireBlockMixin {

    @Shadow @Final
    public static IntProperty AGE;

    // Prevent fire spread contextually
    @Inject(method = "trySpreadingFire", at = @At("HEAD"), cancellable = true)
    private void trySpreadingFireMixin(World world, BlockPos pos, int spreadFactor, Random random,
                                       int currentAge, CallbackInfo cir) {
        final ActionResult result = FireUpdates.BEFORE_FIRE_SPREAD.invoker().spreads(world, pos);
        if (result == ActionResult.FAIL) {
            cir.cancel();
        }
    }

    // Prevent fire spread contextually
    @Inject(method = "getBurnChance(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)I", at = @At("HEAD"), cancellable = true)
    private void getBurnChanceMixin(WorldView world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        final ActionResult result = FireUpdates.BEFORE_FIRE_BURNS.invoker().burns((World) world, pos);
        if (result == ActionResult.FAIL) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }

    // Return fire age
    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;"))
    private int scheduledTickMixin(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        final ActionResult result = FireUpdates.BEFORE_FIRE_BURNS.invoker().burns((World) world, pos);
        if (result == ActionResult.FAIL) {
            return 0;
        }
        return state.get(AGE);
    }

}

