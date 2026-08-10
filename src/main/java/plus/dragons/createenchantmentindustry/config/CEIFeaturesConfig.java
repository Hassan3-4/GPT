package plus.dragons.createenchantmentindustry.config;

import com.zurrtum.create.catnip.config.ConfigBase;

/** Feature switches retained from CEI's former CDP configuration layer. */
public final class CEIFeaturesConfig extends ConfigBase {
    private final ConfigGroup processing = group(1, "processing", "Processing Feature Elements");
    public final ConfigBool classicBlazeEnchanter = b(
        false,
        "classicBlazeEnchanter",
        "If Classic Blaze Enchanter should be enabled"
    );

    @Override
    public String getName() {
        return "features";
    }
}
