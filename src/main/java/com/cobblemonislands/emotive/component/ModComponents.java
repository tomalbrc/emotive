package com.cobblemonislands.emotive.component;

import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ModComponents {
    public static final DataComponentType<EmotiveToken> EMOTIVE_TOKEN = new DataComponentType.Builder<EmotiveToken>().persistent(EmotiveToken.CODEC).build();

    public static void register() {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath("emotive", "token"), EMOTIVE_TOKEN);
        PolymerComponent.registerDataComponent(EMOTIVE_TOKEN);
    }
}
