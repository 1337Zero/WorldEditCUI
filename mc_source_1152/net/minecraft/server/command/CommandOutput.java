package net.minecraft.server.command;

import net.minecraft.text.Text;

public interface CommandOutput {
   CommandOutput DUMMY = new CommandOutput() {
      public void sendMessage(Text message) {
      }

      public boolean sendCommandFeedback() {
         return false;
      }

      public boolean shouldTrackOutput() {
         return false;
      }

      public boolean shouldBroadcastConsoleToOps() {
         return false;
      }
   };

   void sendMessage(Text message);

   boolean sendCommandFeedback();

   boolean shouldTrackOutput();

   boolean shouldBroadcastConsoleToOps();
}
