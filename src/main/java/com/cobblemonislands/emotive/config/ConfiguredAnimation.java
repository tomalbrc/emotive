package com.cobblemonislands.emotive.config;

import com.cobblemon.mod.common.entity.PoseType;
import com.cobblemonislands.emotive.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ConfiguredAnimation(
        String title,
        ResourceLocation npcClass,

        ResourceLocation resourceIdentifier,
        Set<String> aspects,
        Integer level,
        Boolean hideNametag,
        List<String> expressions,
        Float hitboxScale,
        Float renderScale,
        Float width,
        Float height,
        Float eyeHeight,
        String npcName,
        PoseType poseType,

        String animationName,
        int duration,
        ResourceLocation item,
        Integer customModelData,
        boolean glint,
        String permission,
        Integer permissionLevel
) {
    public static Style EMPTY = Style.EMPTY.withColor(ChatFormatting.WHITE).withUnderlined(false).withItalic(false).withObfuscated(false).withStrikethrough(false);

    public ResourceLocation npcClass() {
        return npcClass == null ? ResourceLocation.fromNamespaceAndPath("cobblemon", "npc") : npcClass;
    }

    public ResourceLocation resourceIdentifier() {
        return resourceIdentifier == null ? ResourceLocation.fromNamespaceAndPath("cobblemon", "standard") : resourceIdentifier;
    }

    public Set<String> aspects() {
        return aspects == null ? Set.of() : aspects;
    }

    public Integer level() {
        return level == null ? 0 : level;
    }

    public Boolean hideNametag() {
        return hideNametag == null || hideNametag;
    }

    public List<String> expressions() {
        return expressions == null ? List.of() : expressions;
    }

    public Float hitboxScale() {
        return hitboxScale == null ? 0.9f : hitboxScale;
    }

    public Float renderScale() {
        return renderScale == null ? 0.9f : renderScale;
    }

    public Float width() {
        return width == null ? 0.9f : width;
    }

    public Float height() {
        return height == null ? 1.7f : height;
    }

    public Float eyeHeight() {
        return eyeHeight == null ? 1.62f : eyeHeight;
    }

    public PoseType poseType() {
        return poseType == null ? PoseType.STAND : poseType;
    }

    public ResourceLocation item() {
        return item == null ? ResourceLocation.withDefaultNamespace("diamond") : item;
    }

    public Integer customModelData() {
        return customModelData == null ? 0 : customModelData;
    }

    public Integer permissionLevel() {
        return permissionLevel == null ? 0 : permissionLevel;
    }

    public String npcName() {
        return npcName == null ? "" : npcName;
    }

    public ItemStack itemStack() {
        ItemStack itemStack;

        if (item == null)
            itemStack = Items.ROTTEN_FLESH.getDefaultInstance();
        else
            itemStack = BuiltInRegistries.ITEM.get(item).getDefaultInstance();

        if (title != null) itemStack.set(DataComponents.ITEM_NAME, Component.empty().append(Component.empty().withStyle(EMPTY).append(TextUtil.parse(title))));
        if (customModelData != null)
            itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));

        if (glint)
            itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        return itemStack;
    }

    public Map<String, String> placeholder() {
        return Map.of("<duration>", duration(duration));
    }

    public String duration(int duration) {
        String formatted;
        if (duration == -1) {
            formatted = ModConfig.getInstance().messages.untilStopped;
        } else {
            formatted = duration + "s";
        }

        return formatted;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String title;
        private ResourceLocation npcClass = ResourceLocation.fromNamespaceAndPath("cobblemon", "npc");
        private String animationName;
        private int duration = 6;
        private ResourceLocation item = ResourceLocation.withDefaultNamespace("diamond");
        private Integer customModelData = null;
        private boolean glint;
        private int permissionLevel;
        private String permission;

        private ResourceLocation resourceIdentifier = ResourceLocation.fromNamespaceAndPath("cobblemon", "standard");
        private Set<String> aspects = Set.of();
        private Integer level = 0;
        private Boolean hideNameTag = true;
        private List<String> expressions = List.of();
        private Float hitboxScale = 0.9f;
        private Float renderScale = 0.9f;
        private Float width = 0.9f;
        private Float height = 1.7f;
        private Float eyeHeight = 1.62f;
        private String npcName;
        private PoseType poseType = PoseType.STAND;

        private Builder() {}

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder npcClass(ResourceLocation npcClass) {
            this.npcClass = npcClass;
            return this;
        }

        public Builder animationName(String animationName) {
            this.animationName = animationName;
            return this;
        }

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder item(ResourceLocation item) {
            this.item = item;
            return this;
        }

        public Builder glint(boolean glint) {
            this.glint = glint;
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder permissionLevel(int level) {
            this.permissionLevel = level;
            return this;
        }

        public Builder customModelData(int customModelData) {
            this.customModelData = customModelData;
            return this;
        }

        public Builder resourceIdentifier(ResourceLocation resourceIdentifier) {
            this.resourceIdentifier = resourceIdentifier;
            return this;
        }

        public Builder aspects(Set<String> aspects) {
            this.aspects = aspects;
            return this;
        }

        public Builder level(Integer level) {
            this.level = level;
            return this;
        }

        public Builder hideNameTag(Boolean hideNameTag) {
            this.hideNameTag = hideNameTag;
            return this;
        }

        public Builder expressions(List<String> expressions) {
            this.expressions = expressions;
            return this;
        }

        public Builder hitboxScale(Float hitboxScale) {
            this.hitboxScale = hitboxScale;
            return this;
        }

        public Builder renderScale(Float renderScale) {
            this.renderScale = renderScale;
            return this;
        }

        public Builder width(Float width) {
            this.width = width;
            return this;
        }

        public Builder height(Float height) {
            this.height = height;
            return this;
        }

        public Builder eyeHeight(Float eyeHeight) {
            this.eyeHeight = eyeHeight;
            return this;
        }

        public Builder npcName(String npcName) {
            this.npcName = npcName;
            return this;
        }

        public Builder poseType(PoseType poseType) {
            this.poseType = poseType;
            return this;
        }

        public ConfiguredAnimation build() {
            return new ConfiguredAnimation(
                    title,
                    npcClass,
                    resourceIdentifier,
                    aspects,
                    level,
                    hideNameTag,
                    expressions,
                    hitboxScale,
                    renderScale,
                    width,
                    height,
                    eyeHeight,
                    npcName,
                    poseType,
                    animationName,
                    duration,
                    item,
                    customModelData,
                    glint,
                    permission,
                    permissionLevel
            );
        }
    }
}
