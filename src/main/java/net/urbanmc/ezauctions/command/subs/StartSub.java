package net.urbanmc.ezauctions.command.subs;

import net.urbanmc.ezauctions.object.Auction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StartSub extends SubCommand {

	public StartSub() {
		super("start", "start", true, "s");
	}

	public void run(CommandSender sender, String[] args) {

		Player p = (Player) sender;

		if (args.length < 3 || args.length > 5) {
			sendPropMessage(p,"command.auc.");
			return;
		}

		parseAuction(p, args[1], args[2], args.length < 4 ? "1" : args[3], args.length < 5 ? "0" : args[4]);

	}

	private Auction parseAuction(Player p, String amount, String price, String bidInc, String buyout) {

		int actualAmt = findAmtItems(p);

		int amt = isPosInt(amount);

		if(amount.equalsIgnoreCase("hand") || amount.equalsIgnoreCase("all"))
			amt = actualAmt;

		if(amt <= 0) {
			sendPropMessage(p, "command.auc.start.invalid_amt");
			return null;
		}

		double start = isPosDouble(price);

		if(start <= 0) {
			sendPropMessage(p, "command.auc.start.invalid_start_price");
			return null;
		}


		return null;
	}


	private int findAmtItems(Player p) {
		int amt = 0;

		ItemStack it = p.getInventory().getItemInMainHand();

		for(ItemStack item : p.getInventory().getContents()) {
			if(!item.getType().equals(it.getType()))
				continue;
			if(item.getItemMeta().equals(it.getItemMeta()))
				continue;
			amt += item.getAmount();
		}

		return amt;
	}


	private int isPosInt(String num) {
		try {
			return Integer.valueOf(num);
		}
		catch (NumberFormatException e) {
			return -1;
		}
	}

	private double isPosDouble(String doub) {
		try {
			return Double.valueOf(doub);
		}
		catch (NumberFormatException e) {
			return -1;
		}
	}
}