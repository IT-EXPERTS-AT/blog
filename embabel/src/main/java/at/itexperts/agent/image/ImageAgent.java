package at.itexperts.agent.image;


import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.itexperts.agent.NoResult;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.AgentImage;
import com.embabel.agent.api.common.MultimodalContent;
import com.embabel.agent.api.common.OperationContext;

@Agent(description = "Image")
public class ImageAgent {

   private static final Logger log = LoggerFactory.getLogger(ImageAgent.class);

   @Action
   public ParsedBill parseImage(ImageData imageData, OperationContext context) {
      if (imageData.imageBytes() != null && imageData.imageBytes().length > 0) {
         AgentImage agentImage = AgentImage.create("image/png", imageData.imageBytes());

         return context.ai().withDefaultLlm().withImage(agentImage).createObject(
               MultimodalContent.withImage(
                     "The uploaded image might contain a german or english bill. If so extract the names of the bill items and the name of the shop",
                     agentImage),
               ParsedBill.class);
      }
      throw new IllegalStateException();
   }

   @Action
   public ProcessedBill processBillItems(ParsedBill parsedBill, OperationContext context) {
      var billItems = String.join(",", parsedBill.rawBillItems());
      var shop = parsedBill.shop();

      return context.ai().withDefaultLlm()
            .createObject(
                  "The following items: %s were bought at %s. Categorize them! The following category are available: Food, books, other".formatted(
                        billItems, shop),
                  ProcessedBill.class);
   }

   @Action
   @AchievesGoal(description = "stores processed bills")
   public NoResult storeBills(ProcessedBill bill) {
      log.info("Bought: {} at {}", bill.items(), bill.shop());

      return new NoResult();
   }

   public record ImageData(byte[] imageBytes) {
   }


   public record ParsedBill(List<String> rawBillItems, String shop) {

   }


   public record ProcessedBill(List<BillItem> items, String shop) {

   }


   public record BillItem(String text, double price, String category) {

   }

}
