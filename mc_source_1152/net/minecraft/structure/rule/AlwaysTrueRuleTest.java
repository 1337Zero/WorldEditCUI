package net.minecraft.structure.rule;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.block.BlockState;

public class AlwaysTrueRuleTest extends AbstractRuleTest {
   public static final AlwaysTrueRuleTest INSTANCE = new AlwaysTrueRuleTest();

   private AlwaysTrueRuleTest() {
   }

   public boolean test(BlockState blockState, Random random) {
      return true;
   }

   protected RuleTest getRuleTest() {
      return RuleTest.ALWAYS_TRUE;
   }

   protected <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
      return new Dynamic(dynamicOps, dynamicOps.emptyMap());
   }
}
