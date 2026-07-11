package org.hedgetech.waxeverything.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.hedgetech.waxeverything.Constants;

public final class WaxOverlayRenderer {
    private static final Identifier KEY_CATEGORY_IDENTIFIER = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "key.categories.waxeverything");
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(KEY_CATEGORY_IDENTIFIER);
    private static final String KEY_NAME = "key.waxeverything.show_waxed";

    public static final KeyMapping KEY_SHOW_WAXED = new KeyMapping(
            KEY_NAME,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KEY_CATEGORY
    );
}
