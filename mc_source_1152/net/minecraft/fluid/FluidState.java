package net.minecraft.fluid;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public interface FluidState extends State<FluidState> {
   Fluid getFluid();

   default boolean isStill() {
      return this.getFluid().isStill(this);
   }

   default boolean isEmpty() {
      return this.getFluid().isEmpty();
   }

   default float getHeight(BlockView blockView, BlockPos blockPos) {
      return this.getFluid().getHeight(this, blockView, blockPos);
   }

   default float getHeight() {
      return this.getFluid().getHeight(this);
   }

   default int getLevel() {
      return this.getFluid().getLevel(this);
   }

   @Environment(EnvType.CLIENT)
   default boolean method_15756(BlockView view, BlockPos pos) {
      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            BlockPos blockPos = pos.add(i, 0, j);
            FluidState fluidState = view.getFluidState(blockPos);
            if (!fluidState.getFluid().matchesType(this.getFluid()) && !view.getBlockState(blockPos).isFullOpaque(view, blockPos)) {
               return true;
            }
         }
      }

      return false;
   }

   default void onScheduledTick(World world, BlockPos pos) {
      this.getFluid().onScheduledTick(world, pos, this);
   }

   @Environment(EnvType.CLIENT)
   default void randomDisplayTick(World world, BlockPos pos, Random random) {
      this.getFluid().randomDisplayTick(world, pos, this, random);
   }

   default boolean hasRandomTicks() {
      return this.getFluid().hasRandomTicks();
   }

   default void onRandomTick(World world, BlockPos pos, Random random) {
      this.getFluid().onRandomTick(world, pos, this, random);
   }

   default Vec3d getVelocity(BlockView world, BlockPos pos) {
      return this.getFluid().getVelocity(world, pos, this);
   }

   default BlockState getBlockState() {
      return this.getFluid().toBlockState(this);
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   default ParticleEffect getParticle() {
      return this.getFluid().getParticle();
   }

   default boolean matches(Tag<Fluid> tag) {
      return this.getFluid().matches(tag);
   }

   default float getBlastResistance() {
      return this.getFluid().getBlastResistance();
   }

   default boolean method_15764(BlockView blockView, BlockPos blockPos, Fluid fluid, Direction direction) {
      return this.getFluid().method_15777(this, blockView, blockPos, fluid, direction);
   }

   static <T> Dynamic<T> serialize(DynamicOps<T> ops, FluidState state) {
      ImmutableMap<Property<?>, Comparable<?>> immutableMap = state.getEntries();
      Object object2;
      if (immutableMap.isEmpty()) {
         object2 = ops.createMap(ImmutableMap.of(ops.createString("Name"), ops.createString(Registry.FLUID.getId(state.getFluid()).toString())));
      } else {
         object2 = ops.createMap(ImmutableMap.of(ops.createString("Name"), ops.createString(Registry.FLUID.getId(state.getFluid()).toString()), ops.createString("Properties"), ops.createMap((Map)immutableMap.entrySet().stream().map((entry) -> {
            return Pair.of(ops.createString(((Property)entry.getKey()).getName()), ops.createString(State.nameValue((Property)entry.getKey(), (Comparable)entry.getValue())));
         }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))));
      }

      return new Dynamic(ops, object2);
   }

   static <T> FluidState deserialize(Dynamic<T> dynamic) {
      DefaultedRegistry var10000 = Registry.FLUID;
      Optional var10003 = dynamic.getElement("Name");
      DynamicOps var10004 = dynamic.getOps();
      var10004.getClass();
      Fluid fluid = (Fluid)var10000.get(new Identifier((String)var10003.flatMap(var10004::getStringValue).orElse("minecraft:empty")));
      Map<String, String> map = dynamic.get("Properties").asMap((dynamicx) -> {
         return dynamicx.asString("");
      }, (dynamicx) -> {
         return dynamicx.asString("");
      });
      FluidState fluidState = fluid.getDefaultState();
      StateManager<Fluid, FluidState> stateManager = fluid.getStateManager();
      Iterator var5 = map.entrySet().iterator();

      while(var5.hasNext()) {
         Entry<String, String> entry = (Entry)var5.next();
         String string = (String)entry.getKey();
         Property<?> property = stateManager.getProperty(string);
         if (property != null) {
            fluidState = (FluidState)State.tryRead(fluidState, property, string, dynamic.toString(), (String)entry.getValue());
         }
      }

      return fluidState;
   }

   default VoxelShape getShape(BlockView blockView, BlockPos blockPos) {
      return this.getFluid().getShape(this, blockView, blockPos);
   }
}
