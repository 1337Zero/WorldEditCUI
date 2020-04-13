package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.HotbarStorage;
import net.minecraft.client.options.HotbarStorageEntry;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchableContainer;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class CreativeInventoryScreen extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeContainer> {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");
   private static final BasicInventory inventory = new BasicInventory(45);
   private static int selectedTab;
   private float scrollPosition;
   private boolean scrolling;
   private TextFieldWidget searchBox;
   @Nullable
   private List<Slot> slots;
   @Nullable
   private Slot deleteItemSlot;
   private CreativeInventoryListener listener;
   private boolean ignoreTypedCharacter;
   private boolean lastClickOutsideBounds;
   private final Map<Identifier, Tag<Item>> searchResultTags = Maps.newTreeMap();

   public CreativeInventoryScreen(PlayerEntity player) {
      super(new CreativeInventoryScreen.CreativeContainer(player), player.inventory, new LiteralText(""));
      player.container = this.container;
      this.passEvents = true;
      this.containerHeight = 136;
      this.containerWidth = 195;
   }

   public void tick() {
      if (!this.minecraft.interactionManager.hasCreativeInventory()) {
         this.minecraft.openScreen(new InventoryScreen(this.minecraft.player));
      } else if (this.searchBox != null) {
         this.searchBox.tick();
      }

   }

   protected void onMouseClick(@Nullable Slot slot, int invSlot, int button, SlotActionType slotActionType) {
      if (this.isCreativeInventorySlot(slot)) {
         this.searchBox.setCursorToEnd();
         this.searchBox.setSelectionEnd(0);
      }

      boolean bl = slotActionType == SlotActionType.QUICK_MOVE;
      slotActionType = invSlot == -999 && slotActionType == SlotActionType.PICKUP ? SlotActionType.THROW : slotActionType;
      ItemStack itemStack3;
      PlayerInventory playerInventory;
      if (slot == null && selectedTab != ItemGroup.INVENTORY.getIndex() && slotActionType != SlotActionType.QUICK_CRAFT) {
         playerInventory = this.minecraft.player.inventory;
         if (!playerInventory.getCursorStack().isEmpty() && this.lastClickOutsideBounds) {
            if (button == 0) {
               this.minecraft.player.dropItem(playerInventory.getCursorStack(), true);
               this.minecraft.interactionManager.dropCreativeStack(playerInventory.getCursorStack());
               playerInventory.setCursorStack(ItemStack.EMPTY);
            }

            if (button == 1) {
               itemStack3 = playerInventory.getCursorStack().split(1);
               this.minecraft.player.dropItem(itemStack3, true);
               this.minecraft.interactionManager.dropCreativeStack(itemStack3);
            }
         }
      } else {
         if (slot != null && !slot.canTakeItems(this.minecraft.player)) {
            return;
         }

         if (slot == this.deleteItemSlot && bl) {
            for(int i = 0; i < this.minecraft.player.playerContainer.getStacks().size(); ++i) {
               this.minecraft.interactionManager.clickCreativeStack(ItemStack.EMPTY, i);
            }
         } else {
            ItemStack itemStack8;
            if (selectedTab == ItemGroup.INVENTORY.getIndex()) {
               if (slot == this.deleteItemSlot) {
                  this.minecraft.player.inventory.setCursorStack(ItemStack.EMPTY);
               } else if (slotActionType == SlotActionType.THROW && slot != null && slot.hasStack()) {
                  itemStack8 = slot.takeStack(button == 0 ? 1 : slot.getStack().getMaxCount());
                  itemStack3 = slot.getStack();
                  this.minecraft.player.dropItem(itemStack8, true);
                  this.minecraft.interactionManager.dropCreativeStack(itemStack8);
                  this.minecraft.interactionManager.clickCreativeStack(itemStack3, ((CreativeInventoryScreen.CreativeSlot)slot).slot.id);
               } else if (slotActionType == SlotActionType.THROW && !this.minecraft.player.inventory.getCursorStack().isEmpty()) {
                  this.minecraft.player.dropItem(this.minecraft.player.inventory.getCursorStack(), true);
                  this.minecraft.interactionManager.dropCreativeStack(this.minecraft.player.inventory.getCursorStack());
                  this.minecraft.player.inventory.setCursorStack(ItemStack.EMPTY);
               } else {
                  this.minecraft.player.playerContainer.onSlotClick(slot == null ? invSlot : ((CreativeInventoryScreen.CreativeSlot)slot).slot.id, button, slotActionType, this.minecraft.player);
                  this.minecraft.player.playerContainer.sendContentUpdates();
               }
            } else {
               ItemStack itemStack10;
               if (slotActionType != SlotActionType.QUICK_CRAFT && slot.inventory == inventory) {
                  playerInventory = this.minecraft.player.inventory;
                  itemStack3 = playerInventory.getCursorStack();
                  ItemStack itemStack4 = slot.getStack();
                  if (slotActionType == SlotActionType.SWAP) {
                     if (!itemStack4.isEmpty() && button >= 0 && button < 9) {
                        itemStack10 = itemStack4.copy();
                        itemStack10.setCount(itemStack10.getMaxCount());
                        this.minecraft.player.inventory.setInvStack(button, itemStack10);
                        this.minecraft.player.playerContainer.sendContentUpdates();
                     }

                     return;
                  }

                  if (slotActionType == SlotActionType.CLONE) {
                     if (playerInventory.getCursorStack().isEmpty() && slot.hasStack()) {
                        itemStack10 = slot.getStack().copy();
                        itemStack10.setCount(itemStack10.getMaxCount());
                        playerInventory.setCursorStack(itemStack10);
                     }

                     return;
                  }

                  if (slotActionType == SlotActionType.THROW) {
                     if (!itemStack4.isEmpty()) {
                        itemStack10 = itemStack4.copy();
                        itemStack10.setCount(button == 0 ? 1 : itemStack10.getMaxCount());
                        this.minecraft.player.dropItem(itemStack10, true);
                        this.minecraft.interactionManager.dropCreativeStack(itemStack10);
                     }

                     return;
                  }

                  if (!itemStack3.isEmpty() && !itemStack4.isEmpty() && itemStack3.isItemEqualIgnoreDamage(itemStack4) && ItemStack.areTagsEqual(itemStack3, itemStack4)) {
                     if (button == 0) {
                        if (bl) {
                           itemStack3.setCount(itemStack3.getMaxCount());
                        } else if (itemStack3.getCount() < itemStack3.getMaxCount()) {
                           itemStack3.increment(1);
                        }
                     } else {
                        itemStack3.decrement(1);
                     }
                  } else if (!itemStack4.isEmpty() && itemStack3.isEmpty()) {
                     playerInventory.setCursorStack(itemStack4.copy());
                     itemStack3 = playerInventory.getCursorStack();
                     if (bl) {
                        itemStack3.setCount(itemStack3.getMaxCount());
                     }
                  } else if (button == 0) {
                     playerInventory.setCursorStack(ItemStack.EMPTY);
                  } else {
                     playerInventory.getCursorStack().decrement(1);
                  }
               } else if (this.container != null) {
                  itemStack8 = slot == null ? ItemStack.EMPTY : ((CreativeInventoryScreen.CreativeContainer)this.container).getSlot(slot.id).getStack();
                  ((CreativeInventoryScreen.CreativeContainer)this.container).onSlotClick(slot == null ? invSlot : slot.id, button, slotActionType, this.minecraft.player);
                  if (Container.unpackButtonId(button) == 2) {
                     for(int j = 0; j < 9; ++j) {
                        this.minecraft.interactionManager.clickCreativeStack(((CreativeInventoryScreen.CreativeContainer)this.container).getSlot(45 + j).getStack(), 36 + j);
                     }
                  } else if (slot != null) {
                     itemStack3 = ((CreativeInventoryScreen.CreativeContainer)this.container).getSlot(slot.id).getStack();
                     this.minecraft.interactionManager.clickCreativeStack(itemStack3, slot.id - ((CreativeInventoryScreen.CreativeContainer)this.container).slotList.size() + 9 + 36);
                     int k = 45 + button;
                     if (slotActionType == SlotActionType.SWAP) {
                        this.minecraft.interactionManager.clickCreativeStack(itemStack8, k - ((CreativeInventoryScreen.CreativeContainer)this.container).slotList.size() + 9 + 36);
                     } else if (slotActionType == SlotActionType.THROW && !itemStack8.isEmpty()) {
                        itemStack10 = itemStack8.copy();
                        itemStack10.setCount(button == 0 ? 1 : itemStack10.getMaxCount());
                        this.minecraft.player.dropItem(itemStack10, true);
                        this.minecraft.interactionManager.dropCreativeStack(itemStack10);
                     }

                     this.minecraft.player.playerContainer.sendContentUpdates();
                  }
               }
            }
         }
      }

   }

   private boolean isCreativeInventorySlot(@Nullable Slot slot) {
      return slot != null && slot.inventory == inventory;
   }

   protected void applyStatusEffectOffset() {
      int i = this.x;
      super.applyStatusEffectOffset();
      if (this.searchBox != null && this.x != i) {
         this.searchBox.setX(this.x + 82);
      }

   }

   protected void init() {
      if (this.minecraft.interactionManager.hasCreativeInventory()) {
         super.init();
         this.minecraft.keyboard.enableRepeatEvents(true);
         TextRenderer var10003 = this.font;
         int var10004 = this.x + 82;
         int var10005 = this.y + 6;
         this.font.getClass();
         this.searchBox = new TextFieldWidget(var10003, var10004, var10005, 80, 9, I18n.translate("itemGroup.search"));
         this.searchBox.setMaxLength(50);
         this.searchBox.setHasBorder(false);
         this.searchBox.setVisible(false);
         this.searchBox.setEditableColor(16777215);
         this.children.add(this.searchBox);
         int i = selectedTab;
         selectedTab = -1;
         this.setSelectedTab(ItemGroup.GROUPS[i]);
         this.minecraft.player.playerContainer.removeListener(this.listener);
         this.listener = new CreativeInventoryListener(this.minecraft);
         this.minecraft.player.playerContainer.addListener(this.listener);
      } else {
         this.minecraft.openScreen(new InventoryScreen(this.minecraft.player));
      }

   }

   public void resize(MinecraftClient client, int width, int height) {
      String string = this.searchBox.getText();
      this.init(client, width, height);
      this.searchBox.setText(string);
      if (!this.searchBox.getText().isEmpty()) {
         this.search();
      }

   }

   public void removed() {
      super.removed();
      if (this.minecraft.player != null && this.minecraft.player.inventory != null) {
         this.minecraft.player.playerContainer.removeListener(this.listener);
      }

      this.minecraft.keyboard.enableRepeatEvents(false);
   }

   public boolean charTyped(char chr, int keyCode) {
      if (this.ignoreTypedCharacter) {
         return false;
      } else if (selectedTab != ItemGroup.SEARCH.getIndex()) {
         return false;
      } else {
         String string = this.searchBox.getText();
         if (this.searchBox.charTyped(chr, keyCode)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
               this.search();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.ignoreTypedCharacter = false;
      if (selectedTab != ItemGroup.SEARCH.getIndex()) {
         if (this.minecraft.options.keyChat.matchesKey(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            this.setSelectedTab(ItemGroup.SEARCH);
            return true;
         } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
         }
      } else {
         boolean bl = !this.isCreativeInventorySlot(this.focusedSlot) || this.focusedSlot != null && this.focusedSlot.hasStack();
         if (bl && this.handleHotbarKeyPressed(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            return true;
         } else {
            String string = this.searchBox.getText();
            if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
               if (!Objects.equals(string, this.searchBox.getText())) {
                  this.search();
               }

               return true;
            } else {
               return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256 ? true : super.keyPressed(keyCode, scanCode, modifiers);
            }
         }
      }
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      this.ignoreTypedCharacter = false;
      return super.keyReleased(keyCode, scanCode, modifiers);
   }

   private void search() {
      ((CreativeInventoryScreen.CreativeContainer)this.container).itemList.clear();
      this.searchResultTags.clear();
      String string = this.searchBox.getText();
      if (string.isEmpty()) {
         Iterator var2 = Registry.ITEM.iterator();

         while(var2.hasNext()) {
            Item item = (Item)var2.next();
            item.appendStacks(ItemGroup.SEARCH, ((CreativeInventoryScreen.CreativeContainer)this.container).itemList);
         }
      } else {
         SearchableContainer searchable2;
         if (string.startsWith("#")) {
            string = string.substring(1);
            searchable2 = this.minecraft.getSearchableContainer(SearchManager.ITEM_TAG);
            this.searchForTags(string);
         } else {
            searchable2 = this.minecraft.getSearchableContainer(SearchManager.ITEM_TOOLTIP);
         }

         ((CreativeInventoryScreen.CreativeContainer)this.container).itemList.addAll(searchable2.findAll(string.toLowerCase(Locale.ROOT)));
      }

      this.scrollPosition = 0.0F;
      ((CreativeInventoryScreen.CreativeContainer)this.container).scrollItems(0.0F);
   }

   private void searchForTags(String string) {
      int i = string.indexOf(58);
      Predicate predicate2;
      if (i == -1) {
         predicate2 = (identifier) -> {
            return identifier.getPath().contains(string);
         };
      } else {
         String string2 = string.substring(0, i).trim();
         String string3 = string.substring(i + 1).trim();
         predicate2 = (identifier) -> {
            return identifier.getNamespace().contains(string2) && identifier.getPath().contains(string3);
         };
      }

      TagContainer<Item> tagContainer = ItemTags.getContainer();
      tagContainer.getKeys().stream().filter(predicate2).forEach((identifier) -> {
         Tag var10000 = (Tag)this.searchResultTags.put(identifier, tagContainer.get(identifier));
      });
   }

   protected void drawForeground(int mouseX, int mouseY) {
      ItemGroup itemGroup = ItemGroup.GROUPS[selectedTab];
      if (itemGroup.hasTooltip()) {
         RenderSystem.disableBlend();
         this.font.draw(I18n.translate(itemGroup.getTranslationKey()), 8.0F, 6.0F, 4210752);
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         double d = mouseX - (double)this.x;
         double e = mouseY - (double)this.y;
         ItemGroup[] var10 = ItemGroup.GROUPS;
         int var11 = var10.length;

         for(int var12 = 0; var12 < var11; ++var12) {
            ItemGroup itemGroup = var10[var12];
            if (this.isClickInTab(itemGroup, d, e)) {
               return true;
            }
         }

         if (selectedTab != ItemGroup.INVENTORY.getIndex() && this.isClickInScrollbar(mouseX, mouseY)) {
            this.scrolling = this.hasScrollbar();
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0) {
         double d = mouseX - (double)this.x;
         double e = mouseY - (double)this.y;
         this.scrolling = false;
         ItemGroup[] var10 = ItemGroup.GROUPS;
         int var11 = var10.length;

         for(int var12 = 0; var12 < var11; ++var12) {
            ItemGroup itemGroup = var10[var12];
            if (this.isClickInTab(itemGroup, d, e)) {
               this.setSelectedTab(itemGroup);
               return true;
            }
         }
      }

      return super.mouseReleased(mouseX, mouseY, button);
   }

   private boolean hasScrollbar() {
      return selectedTab != ItemGroup.INVENTORY.getIndex() && ItemGroup.GROUPS[selectedTab].hasScrollbar() && ((CreativeInventoryScreen.CreativeContainer)this.container).shouldShowScrollbar();
   }

   private void setSelectedTab(ItemGroup group) {
      int i = selectedTab;
      selectedTab = group.getIndex();
      this.cursorDragSlots.clear();
      ((CreativeInventoryScreen.CreativeContainer)this.container).itemList.clear();
      int l;
      int aa;
      if (group == ItemGroup.HOTBAR) {
         HotbarStorage hotbarStorage = this.minecraft.getCreativeHotbarStorage();

         for(l = 0; l < 9; ++l) {
            HotbarStorageEntry hotbarStorageEntry = hotbarStorage.getSavedHotbar(l);
            if (hotbarStorageEntry.isEmpty()) {
               for(aa = 0; aa < 9; ++aa) {
                  if (aa == l) {
                     ItemStack itemStack = new ItemStack(Items.PAPER);
                     itemStack.getOrCreateSubTag("CustomCreativeLock");
                     String string = this.minecraft.options.keysHotbar[l].getLocalizedName();
                     String string2 = this.minecraft.options.keySaveToolbarActivator.getLocalizedName();
                     itemStack.setCustomName(new TranslatableText("inventory.hotbarInfo", new Object[]{string2, string}));
                     ((CreativeInventoryScreen.CreativeContainer)this.container).itemList.add(itemStack);
                  } else {
                     ((CreativeInventoryScreen.CreativeContainer)this.container).itemList.add(ItemStack.EMPTY);
                  }
               }
            } else {
               ((CreativeInventoryScreen.CreativeContainer)this.container).itemList.addAll(hotbarStorageEntry);
            }
         }
      } else if (group != ItemGroup.SEARCH) {
         group.appendStacks(((CreativeInventoryScreen.CreativeContainer)this.container).itemList);
      }

      if (group == ItemGroup.INVENTORY) {
         Container container = this.minecraft.player.playerContainer;
         if (this.slots == null) {
            this.slots = ImmutableList.copyOf(((CreativeInventoryScreen.CreativeContainer)this.container).slotList);
         }

         ((CreativeInventoryScreen.CreativeContainer)this.container).slotList.clear();

         for(l = 0; l < container.slotList.size(); ++l) {
            int t;
            int v;
            int w;
            int x;
            if (l >= 5 && l < 9) {
               v = l - 5;
               w = v / 2;
               x = v % 2;
               t = 54 + w * 54;
               aa = 6 + x * 27;
            } else if (l >= 0 && l < 5) {
               t = -2000;
               aa = -2000;
            } else if (l == 45) {
               t = 35;
               aa = 20;
            } else {
               v = l - 9;
               w = v % 9;
               x = v / 9;
               t = 9 + w * 18;
               if (l >= 36) {
                  aa = 112;
               } else {
                  aa = 54 + x * 18;
               }
            }

            Slot slot = new CreativeInventoryScreen.CreativeSlot((Slot)container.slotList.get(l), l, t, aa);
            ((CreativeInventoryScreen.CreativeContainer)this.container).slotList.add(slot);
         }

         this.deleteItemSlot = new Slot(inventory, 0, 173, 112);
         ((CreativeInventoryScreen.CreativeContainer)this.container).slotList.add(this.deleteItemSlot);
      } else if (i == ItemGroup.INVENTORY.getIndex()) {
         ((CreativeInventoryScreen.CreativeContainer)this.container).slotList.clear();
         ((CreativeInventoryScreen.CreativeContainer)this.container).slotList.addAll(this.slots);
         this.slots = null;
      }

      if (this.searchBox != null) {
         if (group == ItemGroup.SEARCH) {
            this.searchBox.setVisible(true);
            this.searchBox.setFocusUnlocked(false);
            this.searchBox.setSelected(true);
            if (i != group.getIndex()) {
               this.searchBox.setText("");
            }

            this.search();
         } else {
            this.searchBox.setVisible(false);
            this.searchBox.setFocusUnlocked(true);
            this.searchBox.setSelected(false);
            this.searchBox.setText("");
         }
      }

      this.scrollPosition = 0.0F;
      ((CreativeInventoryScreen.CreativeContainer)this.container).scrollItems(0.0F);
   }

   public boolean mouseScrolled(double d, double e, double amount) {
      if (!this.hasScrollbar()) {
         return false;
      } else {
         int i = (((CreativeInventoryScreen.CreativeContainer)this.container).itemList.size() + 9 - 1) / 9 - 5;
         this.scrollPosition = (float)((double)this.scrollPosition - amount / (double)i);
         this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
         ((CreativeInventoryScreen.CreativeContainer)this.container).scrollItems(this.scrollPosition);
         return true;
      }
   }

   protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
      boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.containerWidth) || mouseY >= (double)(top + this.containerHeight);
      this.lastClickOutsideBounds = bl && !this.isClickInTab(ItemGroup.GROUPS[selectedTab], mouseX, mouseY);
      return this.lastClickOutsideBounds;
   }

   protected boolean isClickInScrollbar(double mouseX, double mouseY) {
      int i = this.x;
      int j = this.y;
      int k = i + 175;
      int l = j + 18;
      int m = k + 14;
      int n = l + 112;
      return mouseX >= (double)k && mouseY >= (double)l && mouseX < (double)m && mouseY < (double)n;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.scrolling) {
         int i = this.y + 18;
         int j = i + 112;
         this.scrollPosition = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
         this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
         ((CreativeInventoryScreen.CreativeContainer)this.container).scrollItems(this.scrollPosition);
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   public void render(int mouseX, int mouseY, float delta) {
      this.renderBackground();
      super.render(mouseX, mouseY, delta);
      ItemGroup[] var4 = ItemGroup.GROUPS;
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         ItemGroup itemGroup = var4[var6];
         if (this.renderTabTooltipIfHovered(itemGroup, mouseX, mouseY)) {
            break;
         }
      }

      if (this.deleteItemSlot != null && selectedTab == ItemGroup.INVENTORY.getIndex() && this.isPointWithinBounds(this.deleteItemSlot.xPosition, this.deleteItemSlot.yPosition, 16, 16, (double)mouseX, (double)mouseY)) {
         this.renderTooltip(I18n.translate("inventory.binSlot"), mouseX, mouseY);
      }

      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.drawMouseoverTooltip(mouseX, mouseY);
   }

   protected void renderTooltip(ItemStack stack, int x, int y) {
      if (selectedTab == ItemGroup.SEARCH.getIndex()) {
         List<Text> list = stack.getTooltip(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL);
         List<String> list2 = Lists.newArrayListWithCapacity(list.size());
         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            Text text = (Text)var6.next();
            list2.add(text.asFormattedString());
         }

         Item item = stack.getItem();
         ItemGroup itemGroup = item.getGroup();
         if (itemGroup == null && item == Items.ENCHANTED_BOOK) {
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
            if (map.size() == 1) {
               Enchantment enchantment = (Enchantment)map.keySet().iterator().next();
               ItemGroup[] var10 = ItemGroup.GROUPS;
               int var11 = var10.length;

               for(int var12 = 0; var12 < var11; ++var12) {
                  ItemGroup itemGroup2 = var10[var12];
                  if (itemGroup2.containsEnchantments(enchantment.type)) {
                     itemGroup = itemGroup2;
                     break;
                  }
               }
            }
         }

         this.searchResultTags.forEach((identifier, tag) -> {
            if (tag.contains(item)) {
               list2.add(1, "" + Formatting.BOLD + Formatting.DARK_PURPLE + "#" + identifier);
            }

         });
         if (itemGroup != null) {
            list2.add(1, "" + Formatting.BOLD + Formatting.BLUE + I18n.translate(itemGroup.getTranslationKey()));
         }

         for(int i = 0; i < list2.size(); ++i) {
            if (i == 0) {
               list2.set(i, stack.getRarity().formatting + (String)list2.get(i));
            } else {
               list2.set(i, Formatting.GRAY + (String)list2.get(i));
            }
         }

         this.renderTooltip(list2, x, y);
      } else {
         super.renderTooltip(stack, x, y);
      }

   }

   protected void drawBackground(float delta, int mouseX, int mouseY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      ItemGroup itemGroup = ItemGroup.GROUPS[selectedTab];
      ItemGroup[] var5 = ItemGroup.GROUPS;
      int j = var5.length;

      int k;
      for(k = 0; k < j; ++k) {
         ItemGroup itemGroup2 = var5[k];
         this.minecraft.getTextureManager().bindTexture(TEXTURE);
         if (itemGroup2.getIndex() != selectedTab) {
            this.renderTabIcon(itemGroup2);
         }
      }

      this.minecraft.getTextureManager().bindTexture(new Identifier("textures/gui/container/creative_inventory/tab_" + itemGroup.getTexture()));
      this.blit(this.x, this.y, 0, 0, this.containerWidth, this.containerHeight);
      this.searchBox.render(mouseX, mouseY, delta);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      int i = this.x + 175;
      j = this.y + 18;
      k = j + 112;
      this.minecraft.getTextureManager().bindTexture(TEXTURE);
      if (itemGroup.hasScrollbar()) {
         this.blit(i, j + (int)((float)(k - j - 17) * this.scrollPosition), 232 + (this.hasScrollbar() ? 0 : 12), 0, 12, 15);
      }

      this.renderTabIcon(itemGroup);
      if (itemGroup == ItemGroup.INVENTORY) {
         InventoryScreen.drawEntity(this.x + 88, this.y + 45, 20, (float)(this.x + 88 - mouseX), (float)(this.y + 45 - 30 - mouseY), this.minecraft.player);
      }

   }

   protected boolean isClickInTab(ItemGroup group, double mouseX, double mouseY) {
      int i = group.getColumn();
      int j = 28 * i;
      int k = 0;
      if (group.isSpecial()) {
         j = this.containerWidth - 28 * (6 - i) + 2;
      } else if (i > 0) {
         j += i;
      }

      int k;
      if (group.isTopRow()) {
         k = k - 32;
      } else {
         k = k + this.containerHeight;
      }

      return mouseX >= (double)j && mouseX <= (double)(j + 28) && mouseY >= (double)k && mouseY <= (double)(k + 32);
   }

   protected boolean renderTabTooltipIfHovered(ItemGroup group, int mouseX, int mouseY) {
      int i = group.getColumn();
      int j = 28 * i;
      int k = 0;
      if (group.isSpecial()) {
         j = this.containerWidth - 28 * (6 - i) + 2;
      } else if (i > 0) {
         j += i;
      }

      int k;
      if (group.isTopRow()) {
         k = k - 32;
      } else {
         k = k + this.containerHeight;
      }

      if (this.isPointWithinBounds(j + 3, k + 3, 23, 27, (double)mouseX, (double)mouseY)) {
         this.renderTooltip(I18n.translate(group.getTranslationKey()), mouseX, mouseY);
         return true;
      } else {
         return false;
      }
   }

   protected void renderTabIcon(ItemGroup group) {
      boolean bl = group.getIndex() == selectedTab;
      boolean bl2 = group.isTopRow();
      int i = group.getColumn();
      int j = i * 28;
      int k = 0;
      int l = this.x + 28 * i;
      int m = this.y;
      int n = true;
      if (bl) {
         k += 32;
      }

      if (group.isSpecial()) {
         l = this.x + this.containerWidth - 28 * (6 - i);
      } else if (i > 0) {
         l += i;
      }

      if (bl2) {
         m -= 28;
      } else {
         k += 64;
         m += this.containerHeight - 4;
      }

      this.blit(l, m, j, k, 28, 32);
      this.setBlitOffset(100);
      this.itemRenderer.zOffset = 100.0F;
      l += 6;
      m += 8 + (bl2 ? 1 : -1);
      RenderSystem.enableRescaleNormal();
      ItemStack itemStack = group.getIcon();
      this.itemRenderer.renderGuiItem(itemStack, l, m);
      this.itemRenderer.renderGuiItemOverlay(this.font, itemStack, l, m);
      this.itemRenderer.zOffset = 0.0F;
      this.setBlitOffset(0);
   }

   public int getSelectedTab() {
      return selectedTab;
   }

   public static void onHotbarKeyPress(MinecraftClient client, int index, boolean restore, boolean save) {
      ClientPlayerEntity clientPlayerEntity = client.player;
      HotbarStorage hotbarStorage = client.getCreativeHotbarStorage();
      HotbarStorageEntry hotbarStorageEntry = hotbarStorage.getSavedHotbar(index);
      int j;
      if (restore) {
         for(j = 0; j < PlayerInventory.getHotbarSize(); ++j) {
            ItemStack itemStack = ((ItemStack)hotbarStorageEntry.get(j)).copy();
            clientPlayerEntity.inventory.setInvStack(j, itemStack);
            client.interactionManager.clickCreativeStack(itemStack, 36 + j);
         }

         clientPlayerEntity.playerContainer.sendContentUpdates();
      } else if (save) {
         for(j = 0; j < PlayerInventory.getHotbarSize(); ++j) {
            hotbarStorageEntry.set(j, clientPlayerEntity.inventory.getInvStack(j).copy());
         }

         String string = client.options.keysHotbar[index].getLocalizedName();
         String string2 = client.options.keyLoadToolbarActivator.getLocalizedName();
         client.inGameHud.setOverlayMessage((Text)(new TranslatableText("inventory.hotbarSaved", new Object[]{string2, string})), false);
         hotbarStorage.save();
      }

   }

   static {
      selectedTab = ItemGroup.BUILDING_BLOCKS.getIndex();
   }

   @Environment(EnvType.CLIENT)
   static class LockableSlot extends Slot {
      public LockableSlot(Inventory invSlot, int xPosition, int yPosition, int i) {
         super(invSlot, xPosition, yPosition, i);
      }

      public boolean canTakeItems(PlayerEntity playerEntity) {
         if (super.canTakeItems(playerEntity) && this.hasStack()) {
            return this.getStack().getSubTag("CustomCreativeLock") == null;
         } else {
            return !this.hasStack();
         }
      }
   }

   @Environment(EnvType.CLIENT)
   static class CreativeSlot extends Slot {
      private final Slot slot;

      public CreativeSlot(Slot slot, int invSlot, int x, int y) {
         super(slot.inventory, invSlot, x, y);
         this.slot = slot;
      }

      public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
         return this.slot.onTakeItem(player, stack);
      }

      public boolean canInsert(ItemStack stack) {
         return this.slot.canInsert(stack);
      }

      public ItemStack getStack() {
         return this.slot.getStack();
      }

      public boolean hasStack() {
         return this.slot.hasStack();
      }

      public void setStack(ItemStack itemStack) {
         this.slot.setStack(itemStack);
      }

      public void markDirty() {
         this.slot.markDirty();
      }

      public int getMaxStackAmount() {
         return this.slot.getMaxStackAmount();
      }

      public int getMaxStackAmount(ItemStack itemStack) {
         return this.slot.getMaxStackAmount(itemStack);
      }

      @Nullable
      public Pair<Identifier, Identifier> getBackgroundSprite() {
         return this.slot.getBackgroundSprite();
      }

      public ItemStack takeStack(int amount) {
         return this.slot.takeStack(amount);
      }

      public boolean doDrawHoveringEffect() {
         return this.slot.doDrawHoveringEffect();
      }

      public boolean canTakeItems(PlayerEntity playerEntity) {
         return this.slot.canTakeItems(playerEntity);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class CreativeContainer extends Container {
      public final DefaultedList<ItemStack> itemList = DefaultedList.of();

      public CreativeContainer(PlayerEntity playerEntity) {
         super((ContainerType)null, 0);
         PlayerInventory playerInventory = playerEntity.inventory;

         int k;
         for(k = 0; k < 5; ++k) {
            for(int j = 0; j < 9; ++j) {
               this.addSlot(new CreativeInventoryScreen.LockableSlot(CreativeInventoryScreen.inventory, k * 9 + j, 9 + j * 18, 18 + k * 18));
            }
         }

         for(k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 9 + k * 18, 112));
         }

         this.scrollItems(0.0F);
      }

      public boolean canUse(PlayerEntity player) {
         return true;
      }

      public void scrollItems(float position) {
         int i = (this.itemList.size() + 9 - 1) / 9 - 5;
         int j = (int)((double)(position * (float)i) + 0.5D);
         if (j < 0) {
            j = 0;
         }

         for(int k = 0; k < 5; ++k) {
            for(int l = 0; l < 9; ++l) {
               int m = l + (k + j) * 9;
               if (m >= 0 && m < this.itemList.size()) {
                  CreativeInventoryScreen.inventory.setInvStack(l + k * 9, (ItemStack)this.itemList.get(m));
               } else {
                  CreativeInventoryScreen.inventory.setInvStack(l + k * 9, ItemStack.EMPTY);
               }
            }
         }

      }

      public boolean shouldShowScrollbar() {
         return this.itemList.size() > 45;
      }

      public ItemStack transferSlot(PlayerEntity player, int invSlot) {
         if (invSlot >= this.slotList.size() - 9 && invSlot < this.slotList.size()) {
            Slot slot = (Slot)this.slotList.get(invSlot);
            if (slot != null && slot.hasStack()) {
               slot.setStack(ItemStack.EMPTY);
            }
         }

         return ItemStack.EMPTY;
      }

      public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
         return slot.inventory != CreativeInventoryScreen.inventory;
      }

      public boolean canInsertIntoSlot(Slot slot) {
         return slot.inventory != CreativeInventoryScreen.inventory;
      }
   }
}
