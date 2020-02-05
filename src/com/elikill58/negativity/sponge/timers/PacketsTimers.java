package com.elikill58.negativity.sponge.timers;

import java.util.function.Consumer;

import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;

import com.elikill58.negativity.sponge.SpongeNegativity;
import com.elikill58.negativity.sponge.SpongeNegativityPlayer;
import com.elikill58.negativity.sponge.utils.Utils;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.FlyingReason;
import com.elikill58.negativity.universal.ReportType;

public class PacketsTimers implements Consumer<Task> {

	@Override
	public void accept(Task task) {
		for (Player p : Utils.getOnlinePlayers()) {
			SpongeNegativityPlayer np = SpongeNegativityPlayer.getNegativityPlayer(p);
			if (np.BETTER_CLICK < np.ACTUAL_CLICK)
				np.BETTER_CLICK = np.ACTUAL_CLICK;
			np.LAST_CLICK = np.ACTUAL_CLICK;
			np.ACTUAL_CLICK = 0;
			if (np.SEC_ACTIVE < 2) {
				np.SEC_ACTIVE++;
				return;
			}

			if (!SpongeNegativity.hasPacketGate)
				return;
			int ping = Utils.getPing(p);
			if (ping == 0)
				ping = 1;
			int flying = np.FLYING - (ping / 6);
			if (flying > 28) {
				if (np.hasDetectionActive(np.flyingReason.getCheat())) {
					if (np.getItemTypeInHand().getType().equals(ItemTypes.BOW))
						np.flyingReason = FlyingReason.BOW;
					ReportType type = flying > 30 ? ReportType.WARNING : ReportType.VIOLATION;
					if (np.flyingReason.getCheat().isSetBack() && SpongeNegativity.alertMod(type, p, np.flyingReason.getCheat(), Utils.parseInPorcent(flying - (ping / 9)),
							"Flying in one second: " + np.FLYING + ", ping: " + ping + ", max_flying: " + np.MAX_FLYING,
							"Too many packet: " + flying + "\n(Valid packets with low ping: 20)")) {
						switch (np.flyingReason) {
							case EAT:
								p.getInventory().offer(ItemStack.of(np.eatMaterial, 1));
								break;
							case POTION:
								for (PotionEffect pe : np.POTION_EFFECTS)
									if (!np.hasPotionEffect(pe.getType())) {
										PotionEffectData effects = p.getOrCreate(PotionEffectData.class).get();
										effects.addElement(pe);
										p.offer(effects);
										np.POTION_EFFECTS.remove(pe);
									}
								break;
							case REGEN:
								for (int i = 20; i < flying; i++) {
									p.damage(0.5, DamageSources.MAGIC);
								}
								break;
							default:
								break;
						}
					}
				}
			}
			/*Cheat FLY = Cheat.fromString("FLY").get();
			if (np.hasDetectionActive(FLY)) {
				if (np.FLYING > 4 && (np.POSITION + np.POSITION_LOOK + np.FLYING) < 9) {
					np.NO_PACKET++;
					if (np.NO_PACKET > 4) {
						int reliability = 0;
						ReportType type = ReportType.WARNING;
						if (np.ONLY_KEEP_ALIVE > 10)
							type = ReportType.VIOLATION;
						SpongeNegativity.alertMod(type, p, FLY, reliability,
								np.ONLY_KEEP_ALIVE + " second of only KeepAlive. Last other: "
										+ np.LAST_OTHER_KEEP_ALIVE + "(" + new Timestamp(np.TIME_OTHER_KEEP_ALIVE)
										+ ", there is: " + (System.currentTimeMillis() - np.TIME_OTHER_KEEP_ALIVE)
										+ "ms)");
					}
				}
			}*/
			Cheat FORCEFIELD = Cheat.fromString("FORCEFIELD").get();
			if (np.hasDetectionActive(FORCEFIELD)) {
				if (np.ARM > 16 && np.USE_ENTITY > 20) {
					ReportType type = ReportType.WARNING;
					if (np.getWarn(FORCEFIELD) > 5)
						type = ReportType.VIOLATION;
					SpongeNegativity.alertMod(type, p, FORCEFIELD,
							Utils.parseInPorcent(np.ARM + np.USE_ENTITY + np.getWarn(FORCEFIELD)),
							"ArmAnimation (Attack in one second): " + np.ARM
									+ ", UseEntity (interaction with other entity): " + np.USE_ENTITY + " And warn: "
									+ np.getWarn(FORCEFIELD) + ". Ping: " + ping);
				}
			}
			Cheat BLINK = Cheat.fromString("BLINK").get();
			if (np.hasDetectionActive(BLINK) && !np.bypassBlink && ping < 140) {
				int total = np.ALL - np.KEEP_ALIVE;
				if (total == 0) {
					int reliability = Utils.parseInPorcent(150 - ping);
					if (reliability >= BLINK.getReliabilityAlert()) {
						boolean last = np.IS_LAST_SEC_BLINK == 2;
						np.IS_LAST_SEC_BLINK++;
						long time_last = System.currentTimeMillis() - np.TIME_OTHER_KEEP_ALIVE;
						if (last) {
							SpongeNegativity.alertMod(ReportType.WARNING, p, BLINK, reliability,
									"No packet. Last other than KeepAlive: " + np.LAST_OTHER_KEEP_ALIVE + " there is: "
											+ time_last + "ms . Ping: " + ping + ". Warn: " + np.getWarn(BLINK));
						}
					}
				} else {
					np.IS_LAST_SEC_BLINK = 0;
				}
				
				if(ping < BLINK.getMaxAlertPing()){
					int allPos = np.POSITION_LOOK + np.POSITION;
					if(allPos > 60) {
						SpongeNegativity.alertMod(allPos > 70 ? ReportType.VIOLATION : ReportType.WARNING, p, BLINK, Utils.parseInPorcent(20 + allPos), "PositionLook packet: " + np.POSITION_LOOK + " Position Packet: " + np.POSITION +  " (=" + allPos + ") Ping: " + ping + " Warn for Timer: " + np.getWarn(BLINK));
					}
				}
			}
			Cheat SNEAK = Cheat.fromString("SNEAK").get();
			if (np.hasDetectionActive(SNEAK) && ping < 140) {
				if (np.ENTITY_ACTION > 35) {
					if (np.IS_LAST_SEC_SNEAK)
						SpongeNegativity.alertMod(ReportType.WARNING, p, SNEAK, Utils.parseInPorcent(55 + np.ENTITY_ACTION), "EntityAction packet: " + np.ENTITY_ACTION + " Ping: " + ping + " Warn for Sneak: " + np.getWarn(SNEAK));
					np.IS_LAST_SEC_SNEAK = true;
				} else {
					np.IS_LAST_SEC_SNEAK = false;
				}
			}
			Cheat FASTPLACE = Cheat.fromString("FASTPLACE").get();
			if (np.hasDetectionActive(FASTPLACE) && ping < 200 && np.BLOCK_PLACE > 10) {
				SpongeNegativity.alertMod(ReportType.WARNING, p, FASTPLACE, Utils.parseInPorcent(np.BLOCK_PLACE * 5), "BLockPlace: " + np.BLOCK_PLACE + " Ping: " + ping + " Warn for BlockPlace: " + np.getWarn(FASTPLACE));
			}
			np.clearPackets();
		}
	}
}
