package com.smartvn.user_service.enums;

public enum InteractionType {
    CLICK(1.0f),
    VIEW(1.0f),
    ADD_TO_CART(2.0f);

    private final float weight;

    InteractionType(float weight) {
        this.weight = weight;
    }

    public float getWeight() {
        return weight;
    }
}